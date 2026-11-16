/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package hr.mikrotron.poslotron.pricelist

import java.math.RoundingMode
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Timestamp
import java.text.SimpleDateFormat

import org.apache.ofbiz.base.util.UtilDateTime
import org.apache.ofbiz.base.util.UtilMisc
import org.apache.ofbiz.base.util.UtilProperties
import org.apache.ofbiz.base.util.UtilValidate
import org.apache.ofbiz.entity.GenericValue
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.product.category.CategoryContentWrapper

/*
 * Pricelist publication services (Croatian Trade Act, Art. 12 - objava cjenika
 * na mreznim stranicama).
 *
 * Both services run unauthenticated from the nightly JobSandbox jobs (jobs have
 * no session, so no userLogin can be provided). They only read catalog data and
 * write to the plugin's own PoslotronPricelist entity plus the configured
 * output directory, so no explicit permission check is needed.
 */

Map generatePricelist() {
    String productStoreId = UtilProperties.getPropertyValue('poslotron', 'pricelist.productStoreId')
    if (UtilValidate.isEmpty(productStoreId)) {
        return error(label('PoslotronUiLabels', 'PoslotronPricelistNotConfigured'))
    }
    GenericValue productStore = from('ProductStore').where('productStoreId', productStoreId).queryOne()
    if (!productStore) {
        return error(label('PoslotronUiLabels', 'PoslotronPricelistStoreNotFound'))
    }
    String currencyUomId = productStore.defaultCurrencyUomId ?: 'EUR'

    // Category-driven product scope (user decision): everything reachable
    // under the browse-root category of the webshop's catalog.
    String browseRootCategoryId = findBrowseRootCategoryId(productStoreId)
    if (!browseRootCategoryId) {
        return error(label('PoslotronUiLabels', 'PoslotronPricelistNoBrowseRoot'))
    }
    Set<String> categoryIds = collectCategoryIds(browseRootCategoryId)

    Set<String> productIds = collectProductIds(categoryIds)
    if (!productIds) {
        return error(label('PoslotronUiLabels', 'PoslotronPricelistNoProducts'))
    }

    Timestamp now = UtilDateTime.nowTimestamp()
    List<GenericValue> products = from('Product')
            .where(EntityCondition.makeCondition('productId', EntityOperator.IN, productIds))
            .queryList()
            .findAll { product ->
                // Same "active for sale" convention as the product-deprecation
                // report: discontinued products leave the pricelist on the date
                // their salesDiscontinuationDate is reached.
                (product.salesDiscontinuationDate == null || product.salesDiscontinuationDate.after(now)) &&
                        // Virtual products are selectable parents, never sellable items.
                        !'Y'.equals(product.isVirtual)
            }

    Map<String, GenericValue> priceByProduct = findPrices(productIds, currencyUomId, 'DEFAULT_PRICE')
    Map<String, GenericValue> basePriceByProduct = findPrices(productIds, currencyUomId, 'BASE_PRICE')
    Map<String, GenericValue> listPriceByProduct = findPrices(productIds, currencyUomId, 'LIST_PRICE')
    Map<String, String> barcodeByProduct = findBarcodes(productIds)
    Map<String, String> uomLabelById = findUomLabels(products)
    Map<String, String> specialFormByProduct = findSpecialFormsByProduct()

    List<List<String>> rows = []
    int skippedUnpriced = 0
    String defaultUnitOfMeasure = UtilProperties.getPropertyValue('poslotron', 'pricelist.defaultUnitOfMeasure', 'kom')
    BigDecimal vatPercentage = findStoreVatPercentage(productStore)
    products.each { product ->
        GenericValue price = priceByProduct[product.productId]
        if (!price) {
            skippedUnpriced++
            return
        }
        String name = product.productName ?: product.internalName
        String netQuantity = product.quantityIncluded != null ? formatNumber(product.quantityIncluded) : ''
        String unitOfMeasure = defaultUnitOfMeasure
        if (product.quantityUomId) {
            unitOfMeasure = uomLabelById[product.quantityUomId] ?: product.quantityUomId
        }
        String unitPrice = ''
        if (product.quantityIncluded != null && product.quantityIncluded.compareTo(BigDecimal.ZERO) > 0 && product.quantityUomId) {
            unitPrice = formatNumber(grossPrice(price, vatPercentage).divide(product.quantityIncluded, 3, RoundingMode.HALF_UP))
        }
        // Bazna cijena: BASE_PRICE with LIST_PRICE and DEFAULT_PRICE as fallbacks,
        // in that order.
        GenericValue basePrice = basePriceByProduct[product.productId] ?: listPriceByProduct[product.productId] ?: price
        rows << [
                name,
                product.productId,
                product.brandName ?: '',
                netQuantity,
                unitOfMeasure,
                formatNumber(grossPrice(price, vatPercentage)),
                specialFormByProduct[product.productId] ?: '',
                unitPrice,
                barcodeByProduct[product.productId] ?: '',
                formatNumber(grossPrice(basePrice, vatPercentage)),
        ]
    }
    rows = rows.sort { row -> (row[0] ?: '').toLowerCase() }

    // Broj pohrane: sequential storage number per prodajno mjesto, so each
    // published file of this store gets a unique, increasing number.
    GenericValue lastPricelist = from('PoslotronPricelist')
            .where('productStoreId', productStoreId)
            .orderBy('-storageNumber')
            .queryFirst()
    long storageNumber = lastPricelist ? ((lastPricelist.getLong('storageNumber') ?: 0L) + 1L) : 1L

    // File name per Art. 12(2): oblik, adresa i oznaka prodajnog mjesta,
    // broj pohrane and the date/time stamp of the publication.
    Timestamp sentDate = UtilDateTime.nowTimestamp()
    String timestamp = new SimpleDateFormat('yyyy-MM-dd_HH-mm-ss').format(sentDate)
    String pointOfSaleForm = UtilProperties.getPropertyValue('poslotron', 'pricelist.pointOfSale.form')
    String pointOfSaleAddress = UtilProperties.getPropertyValue('poslotron', 'pricelist.pointOfSale.address')
    String pointOfSaleIdentifier = UtilProperties.getPropertyValue('poslotron', 'pricelist.pointOfSale.identifier')
    String fileName = "cjenik_${sanitizeFilePart(pointOfSaleForm)}_${sanitizeFilePart(pointOfSaleAddress)}" +
            "_${sanitizeFilePart(pointOfSaleIdentifier)}_${String.format('%06d', storageNumber)}_${timestamp}.csv"

    // Write to a temp sibling first, so a partially written file can never be
    // observed by the download endpoint, then move into place only after the
    // archive record is created (the record is rolled back with the service
    // transaction if anything fails).
    Path outputDir = resolveOutputDir()
    Files.createDirectories(outputDir)
    Path tempFile = Files.createTempFile(outputDir, 'cjenik-', '.tmp')
    writeCsv(tempFile, rows)
    try {
        GenericValue pricelist = delegator.create('PoslotronPricelist', UtilMisc.toMap(
                'pricelistId', delegator.getNextSeqId('PoslotronPricelist'),
                'productStoreId', productStoreId,
                'fileName', fileName,
                'storageNumber', storageNumber,
                'sentDate', sentDate,
                'itemCount', rows.size()))
        Files.move(tempFile, outputDir.resolve(fileName),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING)
        logInfo("Pricelist published: ${fileName} (${rows.size()} items, ${skippedUnpriced} skipped without price)")
        return success(pricelistId: pricelist.pricelistId, fileName: fileName,
                itemCount: rows.size(), storageNumber: storageNumber)
    } finally {
        Files.deleteIfExists(tempFile)
    }
}

/*
 * Removes archive entries (and their files) older than the configured
 * retention. Art. 12(5): published pricelists must stay available for 30 days
 * from the last publication, so the default retention is 30 days.
 */
Map cleanupPricelistArchive() {
    int retentionDays = UtilProperties.getPropertyAsInteger('poslotron', 'pricelist.retention.days', 30)
    Timestamp cutoff = new Timestamp(System.currentTimeMillis() - (retentionDays * 24L * 60L * 60L * 1000L))
    List<GenericValue> expired = from('PoslotronPricelist')
            .where(EntityCondition.makeCondition('sentDate', EntityOperator.LESS_THAN, cutoff))
            .queryList()
    Path outputDir = resolveOutputDir()
    int removed = 0
    expired.each { pricelist ->
        Path file = resolveArchiveFile(outputDir, pricelist.getString('fileName'))
        if (file != null) {
            try {
                Files.deleteIfExists(file)
            } catch (java.io.IOException e) {
                logError("Could not delete pricelist file ${file}: ${e.message}")
            }
        }
        pricelist.remove()
        removed++
    }
    if (removed > 0) {
        logInfo("Pricelist archive cleanup removed ${removed} entries older than ${retentionDays} days")
    }
    return success(removedPricelists: removed)
}

// ===== helpers =====

/*
 * Browse-root category of the first currently active catalog of the store.
 */
private String findBrowseRootCategoryId(String productStoreId) {
    List<GenericValue> storeCatalogs = from('ProductStoreCatalog')
            .where('productStoreId', productStoreId)
            .filterByDate()
            .queryList()
    for (GenericValue storeCatalog : storeCatalogs) {
        GenericValue browseRoot = from('ProdCatalogCategory')
                .where('prodCatalogId', storeCatalog.prodCatalogId, 'prodCatalogCategoryTypeId', 'PCCT_BROWSE_ROOT')
                .filterByDate()
                .queryFirst()
        if (browseRoot) {
            return browseRoot.productCategoryId
        }
    }
    return null
}

/*
 * The browse root and every descendant category via ProductCategoryRollup
 * (breadth-first, date-filtered). Rollups are small, cached reference data, so
 * the per-parent queries are cheap and stay bounded by the number of
 * categories.
 */
private Set<String> collectCategoryIds(String rootCategoryId) {
    Set<String> categoryIds = new HashSet<>()
    Deque<String> pending = new ArrayDeque<>([rootCategoryId])
    while (!pending.isEmpty()) {
        String categoryId = pending.poll()
        if (!categoryIds.add(categoryId)) {
            continue
        }
        from('ProductCategoryRollup')
                .where('parentProductCategoryId', categoryId)
                .cache()
                .filterByDate()
                .queryList()
                .each { rollup -> pending.add(rollup.productCategoryId) }
    }
    return categoryIds
}

/*
 * Currently valid category members of the given categories. Uses an iterator
 * because the membership table can be large (skill: no full queryList for
 * potentially big result sets).
 */
private Set<String> collectProductIds(Set<String> categoryIds) {
    Set<String> productIds = new HashSet<>()
    org.apache.ofbiz.entity.util.EntityListIterator eli = from('ProductCategoryMember')
            .where(EntityCondition.makeCondition('productCategoryId', EntityOperator.IN, categoryIds))
            .filterByDate()
            .queryIterator()
    try {
        GenericValue member
        while ((member = eli.next()) != null) {
            productIds.add(member.productId)
        }
    } finally {
        eli.close()
    }
    return productIds
}

/*
 * Poseban oblik prodaje: categories whose productCategoryId starts with '_'
 * mark special sale forms; their Croatian name is published for member
 * products. If a product belongs to several such categories, the first one
 * by category id wins.
 */
private Map<String, String> findSpecialFormsByProduct() {
    Map<String, String> specialFormByProduct = [:]
    List<GenericValue> specialCategories = from('ProductCategory').queryList()
            .findAll { it.productCategoryId?.startsWith('_') }
            .sort { it.productCategoryId }
    for (GenericValue category in specialCategories) {
        // Croatian version of the category name via the content framework,
        // like CategoryContentWrapper usage in ProductCategories.groovy.
        CategoryContentWrapper wrapper = new CategoryContentWrapper(dispatcher, category, new Locale('hr'), 'text/plain')
        String categoryName = wrapper.get('CATEGORY_NAME', 'text/plain')
        if (!categoryName) {
            categoryName = category.categoryName
        }
        if (!categoryName) {
            continue
        }
        from('ProductCategoryMember')
                .where('productCategoryId', category.productCategoryId)
                .filterByDate()
                .queryList()
                .each { member ->
                    if (!specialFormByProduct[member.productId]) {
                        specialFormByProduct[member.productId] = categoryName
                    }
                }
    }
    return specialFormByProduct
}

/*
 * Valid price rows of the given type for the products in the store currency;
 * store-group '_NA_' (or unscoped) rows win over store-group specific ones.
 */
private Map<String, GenericValue> findPrices(Set<String> productIds, String currencyUomId, String productPriceTypeId) {
    List<GenericValue> prices = from('ProductPrice')
            .where(EntityCondition.makeCondition('productId', EntityOperator.IN, productIds),
                    EntityCondition.makeCondition('productPriceTypeId', EntityOperator.EQUALS, productPriceTypeId),
                    EntityCondition.makeCondition('productPricePurposeId', EntityOperator.EQUALS, 'PURCHASE'),
                    EntityCondition.makeCondition('currencyUomId', EntityOperator.EQUALS, currencyUomId))
            .filterByDate()
            .queryList()
    Map<String, GenericValue> priceByProduct = [:]
    prices.each { price ->
        boolean generic = UtilValidate.isEmpty(price.productStoreGroupId) || '_NA_'.equals(price.productStoreGroupId)
        GenericValue current = priceByProduct[price.productId]
        boolean currentGeneric = current ? (UtilValidate.isEmpty(current.productStoreGroupId) ||
                '_NA_'.equals(current.productStoreGroupId)) : true
        if (!current || (generic && !currentGeneric)) {
            priceByProduct[price.productId] = price
        }
    }
    return priceByProduct
}

/*
 * Barcode per product: EAN preferred, UPCA/UPCE as fallbacks.
 */
private Map<String, String> findBarcodes(Set<String> productIds) {
    List<String> barcodeTypes = ['EAN', 'UPCA', 'UPCE']
    Map<String, String> barcodeByProduct = [:]
    from('GoodIdentification')
            .where(EntityCondition.makeCondition('productId', EntityOperator.IN, productIds),
                    EntityCondition.makeCondition('goodIdentificationTypeId', EntityOperator.IN, barcodeTypes))
            // NOTE: no filterByDate - GoodIdentification has no fromDate/thruDate
            // fields in this entity model.
            .queryList()
            .each { identification ->
                String current = barcodeByProduct[identification.productId]
                if (!current || identification.goodIdentificationTypeId == 'EAN') {
                    barcodeByProduct[identification.productId] = identification.idValue
                }
            }
    return barcodeByProduct
}

private Map<String, String> findUomLabels(List<GenericValue> products) {
    Set<String> uomIds = products.collect { it.quantityUomId }.findAll { it } as Set
    if (!uomIds) {
        return [:]
    }
    Map<String, String> uomLabelById = [:]
    from('Uom')
            .where(EntityCondition.makeCondition('uomId', EntityOperator.IN, uomIds))
            .cache()
            .queryList()
            .each { uom -> uomLabelById[uom.uomId] = uom.abbreviation ?: uom.description }
    return uomLabelById
}

/*
 * Croatian decimal comma, as expected by local consumers of the file.
 */
private String formatNumber(BigDecimal value) {
    return value?.toPlainString()?.replace('.', ',')
}

/*
 * The webshop's standard VAT rate, resolved like
 * component://order's GetQuoteVatRate.groovy: the currently active
 * TaxAuthorityRateProduct of the store's VAT tax authority
 * (ProductStore.vatTaxAuthGeoId / vatTaxAuthPartyId), no category filter,
 * highest taxPercentage first. Falls back to the pricelist.vat.rate
 * property (percentage) when the store has no VAT tax authority.
 */
private BigDecimal findStoreVatPercentage(GenericValue productStore) {
    if (productStore.vatTaxAuthGeoId) {
        Timestamp now = UtilDateTime.nowTimestamp()
        List<EntityCondition> condList = [
                EntityCondition.makeCondition('taxAuthGeoId', EntityOperator.EQUALS, productStore.vatTaxAuthGeoId),
                EntityCondition.makeCondition('productCategoryId', EntityOperator.EQUALS, null),
                EntityCondition.makeCondition([
                        EntityCondition.makeCondition('fromDate', EntityOperator.LESS_THAN_EQUAL_TO, now),
                        EntityCondition.makeCondition('fromDate', EntityOperator.EQUALS, null)
                ], EntityOperator.OR),
                EntityCondition.makeCondition([
                        EntityCondition.makeCondition('thruDate', EntityOperator.GREATER_THAN, now),
                        EntityCondition.makeCondition('thruDate', EntityOperator.EQUALS, null)
                ], EntityOperator.OR)
        ]
        if (productStore.vatTaxAuthPartyId) {
            condList << EntityCondition.makeCondition('taxAuthPartyId', EntityOperator.EQUALS, productStore.vatTaxAuthPartyId)
        }
        GenericValue rate = from('TaxAuthorityRateProduct')
                .where(EntityCondition.makeCondition(condList, EntityOperator.AND))
                .orderBy('-taxPercentage')
                .cache()
                .queryFirst()
        if (rate?.taxPercentage != null) {
            return rate.taxPercentage
        }
    }
    return BigDecimal.valueOf(UtilProperties.getPropertyNumber('poslotron', 'pricelist.vat.rate', 25.0d))
}

/*
 * Art. 12(3): published prices must include VAT. The product store's VAT
 * rate is applied to all product prices (webshop prices are stored net).
 */
private BigDecimal grossPrice(GenericValue price, BigDecimal vatPercentage) {
    if (price == null) {
        return null
    }
    return price.price.multiply(BigDecimal.ONE.add(vatPercentage.movePointLeft(2))).setScale(2, RoundingMode.HALF_UP)
}

/*
 * File-name parts must be filesystem-safe: collapse everything that is not
 * alphanumeric into single underscores.
 */
private String sanitizeFilePart(String part) {
    return part?.replaceAll('[^A-Za-z0-9]+', '_')?.replaceAll('^_+|_+$', '') ?: ''
}

private Path resolveOutputDir() {
    String configured = UtilProperties.getPropertyValue('poslotron', 'pricelist.output.dir', 'runtime/poslotron/pricelists')
    Path path = Paths.get(configured)
    if (!path.isAbsolute()) {
        path = Paths.get(System.getProperty('ofbiz.home'), configured)
    }
    return path
}

/*
 * Resolves an archive file name inside the output directory and guards against
 * path traversal in stored file names.
 */
private Path resolveArchiveFile(Path outputDir, String fileName) {
    if (UtilValidate.isEmpty(fileName)) {
        return null
    }
    Path file = outputDir.resolve(fileName).normalize()
    return file.startsWith(outputDir.normalize()) ? file : null
}

/*
 * Column headers as mandated by Art. 12(2)/(3): naziv robe, sifra robe, marka
 * robe, neto kolicina, jedinica mjere, maloprodajna cijena (+ poseban oblik
 * prodaje), cijena za jedinicu mjere, barkod, bazna cijena. The file content is
 * defined by Croatian law, so the headers are intentionally always Croatian
 * regardless of the runtime locale.
 */
private void writeCsv(Path file, List<List<String>> rows) {
    List<String> csvHeaders = [
            'Naziv robe',
            'Šifra robe',
            'Marka robe',
            'Neto količina',
            'Jedinica mjere',
            'Maloprodajna cijena',
            'Poseban oblik prodaje',
            'Cijena za jedinicu mjere',
            'Barkod',
            'Bazna cijena',
    ]
    Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)
    try {
        // BOM so Croatian Excel opens the file as UTF-8.
        writer.write('\uFEFF')
        writer.write(csvHeaders.collect { escapeCsvCell(it) }.join(';'))
        writer.write('\r\n')
        rows.each { row ->
            writer.write(row.collect { escapeCsvCell(it ?: '') }.join(';'))
            writer.write('\r\n')
        }
    } finally {
        writer.close()
    }
}

private String escapeCsvCell(String value) {
    if (value.contains(';') || value.contains('"') || value.contains('\n') || value.contains('\r')) {
        return '"' + value.replace('"', '""') + '"'
    }
    return value
}
