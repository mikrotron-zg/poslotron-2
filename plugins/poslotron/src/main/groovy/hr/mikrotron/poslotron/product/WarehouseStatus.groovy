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
package hr.mikrotron.poslotron.product

import org.apache.ofbiz.base.util.UtilCodec
import org.apache.ofbiz.base.util.UtilDateTime
import org.apache.ofbiz.base.util.UtilValidate
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.condition.EntityFunction
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.entity.util.EntityQuery

/*
 * Populates `warehouseRows` for the ListWarehouseStatus grid.
 *
 * Filters:
 *  - productId             (case-insensitive substring on Product.productId)
 *  - productTypeId         ("" = ANY; defaults to FINISHED_GOOD via the form)
 *  - productCategoryId     ("" = ANY; restricts to ProductCategoryMember.productId)
 *  - supplierPartyId       ("" = ANY; "__NONE__" = only products with no active
 *                          SupplierProduct row; else specific supplier partyId)
 *  - quantityOnHandOp +
 *    quantityOnHandValue   compares summed InventoryItem.quantityOnHandTotal
 *                          (LT / LTE / EQ / GTE / GT)
 *
 * Result rows: one per (product, supplier) pair. Products that have no active
 * SupplierProduct emit a single row with the supplier columns blank, except
 * when the user picked a specific supplier (in which case those products are
 * filtered out). When supplierPartyId == "__NONE__" we only emit the
 * no-supplier rows.
 *
 * Default order: QOH ascending. The grid columns are all marked sortable;
 * sortField=<name> / sortField=-<name> applies an in-memory sort here so the
 * ordering applies before the form widget paginates the list.
 */

Closure<String> trimToNull = { String s -> UtilValidate.isNotEmpty(s) ? s.trim() : null }

// `searchSubmitted` is a hidden marker in LookupWarehouseStatus. It tells us
// whether we are rendering a fresh page (first GET) or a real form submission.
// We need this distinction because the form's no-current-selected-key for
// productTypeId (FINISHED_GOOD) is purely a display default - on the very
// first load the request has no productTypeId at all, so without this guard
// we would silently report all product types instead of just finished goods.
boolean searchSubmitted = 'Y' == (parameters.searchSubmitted as String)

String productIdFilter = trimToNull(parameters.productId as String)
String productTypeFilter
if (searchSubmitted) {
    // Honor the user's explicit choice, including an empty pick for "any".
    productTypeFilter = trimToNull(parameters.productTypeId as String)
} else {
    // Fresh page load: apply the same default the form widget shows.
    productTypeFilter = 'FINISHED_GOOD'
}
String productCategoryFilter = trimToNull(parameters.productCategoryId as String)
String supplierFilter = trimToNull(parameters.supplierPartyId as String)
String qohOp = trimToNull(parameters.quantityOnHandOp as String)
String qohValueStr = trimToNull(parameters.quantityOnHandValue as String)

BigDecimal qohValue = null
if (qohValueStr != null) {
    try {
        // Tolerate the Croatian-locale comma separator alongside the dot.
        qohValue = new BigDecimal(qohValueStr.replace(',', '.'))
    } catch (NumberFormatException ignored) {
        // Bad number -> drop the QOH filter silently. The form re-renders
        // the user's typed value so they can correct it; we deliberately
        // avoid pushing into request._ERROR_MESSAGE_ because request is
        // not always present in form-action script context.
        qohValue = null
    }
}

// ===== Step 1: candidate products =====
// Always exclude sales-discontinued products. Convention used elsewhere in
// the OFBiz codebase (see ProductUtilServices, ViewFacilityInventoryByProduct):
// a product is "active for sale" iff salesDiscontinuationDate is unset OR
// still in the future. Future-dated discontinuations stay visible so the
// warehouse can wind them down before the cutoff.
java.sql.Timestamp nowTimestamp = UtilDateTime.nowTimestamp()
List productConds = [
        EntityCondition.makeCondition([
                EntityCondition.makeCondition('salesDiscontinuationDate', EntityOperator.EQUALS, null),
                EntityCondition.makeCondition('salesDiscontinuationDate', EntityOperator.GREATER_THAN, nowTimestamp),
        ], EntityOperator.OR),
]

if (productIdFilter) {
    productConds << EntityCondition.makeCondition(
            EntityFunction.upperField('productId'),
            EntityOperator.LIKE,
            EntityFunction.upper('%' + productIdFilter + '%'))
}
if (productTypeFilter) {
    productConds << EntityCondition.makeCondition('productTypeId', productTypeFilter)
}

if (productCategoryFilter) {
    List<String> productIdsByCategory = EntityQuery.use(delegator)
            .from('ProductCategoryMember')
            .where('productCategoryId', productCategoryFilter)
            .filterByDate()
            .queryList()
            .collect { it.productId as String }
            .unique()
    if (!productIdsByCategory) {
        context.warehouseRows = []
        return
    }
    productConds << EntityCondition.makeCondition('productId', EntityOperator.IN, productIdsByCategory)
}

// When a specific supplier is selected, restrict candidate products to the
// ones they currently supply. Reduces the InventoryItem scan below.
if (supplierFilter && supplierFilter != '__NONE__') {
    List<String> productIdsBySupplier = EntityQuery.use(delegator)
            .from('SupplierProduct')
            .where('partyId', supplierFilter)
            .filterByDate('availableFromDate', 'availableThruDate')
            .queryList()
            .collect { it.productId as String }
            .unique()
    if (!productIdsBySupplier) {
        context.warehouseRows = []
        return
    }
    productConds << EntityCondition.makeCondition('productId', EntityOperator.IN, productIdsBySupplier)
}

List products = EntityQuery.use(delegator)
        .from('Product')
        .where(EntityCondition.makeCondition(productConds, EntityOperator.AND))
        .queryList()

if (!products) {
    context.warehouseRows = []
    return
}

Map<String, Object> productById = [:]
products.each { productById[it.productId as String] = it }
List<String> productIds = new ArrayList<String>(productById.keySet())

// ===== Step 2: aggregate inventory totals + last received date =====
// Sum quantityOnHandTotal / availableToPromiseTotal / accountingQuantityTotal
// and track max(datetimeReceived) across all InventoryItem rows per product.
Map<String, Map> invByProduct = [:]
productIds.each { pid ->
    invByProduct[pid] = [
            qoh             : BigDecimal.ZERO,
            atp             : BigDecimal.ZERO,
            aqt             : BigDecimal.ZERO,
            lastReceivedDate: null,
    ]
}

EntityQuery.use(delegator)
        .from('InventoryItem')
        .where(EntityCondition.makeCondition('productId', EntityOperator.IN, productIds))
        .queryList()
        .each { ii ->
            Map agg = invByProduct[ii.productId as String]
            if (agg == null) return
            agg.qoh = (agg.qoh as BigDecimal) + ((ii.quantityOnHandTotal ?: BigDecimal.ZERO) as BigDecimal)
            agg.atp = (agg.atp as BigDecimal) + ((ii.availableToPromiseTotal ?: BigDecimal.ZERO) as BigDecimal)
            agg.aqt = (agg.aqt as BigDecimal) + ((ii.accountingQuantityTotal ?: BigDecimal.ZERO) as BigDecimal)
            if (ii.datetimeReceived
                    && (agg.lastReceivedDate == null || ii.datetimeReceived > agg.lastReceivedDate)) {
                agg.lastReceivedDate = ii.datetimeReceived
            }
        }

// Drop products whose summed QOH is negative. A negative aggregate means
// the InventoryItem rows are out of balance (typically a data-entry bug)
// and the user should not see them on the operational warehouse report.
// This is applied unconditionally, before the user's QOH operator filter.
List<String> nonNegativeProductIds = []
invByProduct.each { pid, agg ->
    if ((agg.qoh as BigDecimal).signum() >= 0) {
        nonNegativeProductIds << (pid as String)
    }
}
if (!nonNegativeProductIds) {
    context.warehouseRows = []
    return
}
invByProduct = invByProduct.subMap(nonNegativeProductIds)
productIds = nonNegativeProductIds

// Apply QOH operator filter (in-memory because qoh is an aggregate).
if (qohOp && qohValue != null) {
    List<String> kept = []
    invByProduct.each { pid, agg ->
        BigDecimal q = agg.qoh as BigDecimal
        boolean keep
        switch (qohOp) {
            case 'LT':  keep = q.compareTo(qohValue) <  0; break
            case 'LTE': keep = q.compareTo(qohValue) <= 0; break
            case 'EQ':  keep = q.compareTo(qohValue) == 0; break
            case 'GTE': keep = q.compareTo(qohValue) >= 0; break
            case 'GT':  keep = q.compareTo(qohValue) >  0; break
            default:    keep = true
        }
        if (keep) kept << pid
    }
    if (!kept) {
        context.warehouseRows = []
        return
    }
    invByProduct = invByProduct.subMap(kept)
    productIds = kept
}

// ===== Step 3: SupplierProduct rows per matched product =====
// SupplierProductAndProduct joins Product so currencyUomId / lastPrice / partyId
// come back in one query. filterByDate keeps only currently-active offerings.
Map<String, List<Map>> spByProduct = [:]
EntityQuery.use(delegator)
        .from('SupplierProductAndProduct')
        .where(EntityCondition.makeCondition('productId', EntityOperator.IN, productIds))
        .filterByDate('availableFromDate', 'availableThruDate')
        .queryList()
        .each { sp ->
            // Defensive: when a specific supplier was selected, drop other suppliers.
            if (supplierFilter && supplierFilter != '__NONE__' && sp.partyId != supplierFilter) return
            spByProduct.computeIfAbsent(sp.productId as String) { [] } << [
                    partyId           : sp.partyId as String,
                    supplierProductId : sp.supplierProductId as String,
                    lastPrice         : sp.lastPrice,
                    currencyUomId     : sp.currencyUomId as String,
            ]
        }

// Batch-load supplier (PartyGroup) names to avoid per-row lookups.
Set<String> supplierIds = new HashSet<String>()
spByProduct.values().each { it.each { row -> supplierIds << (row.partyId as String) } }
Map<String, String> supplierNameById = [:]
Map<String, String> supplierWebUrlById = [:]
if (supplierIds) {
    List<String> supplierIdList = new ArrayList<String>(supplierIds)
    EntityQuery.use(delegator)
            .from('PartyGroup')
            .where(EntityCondition.makeCondition('partyId', EntityOperator.IN, supplierIdList))
            .queryList()
            .each { supplierNameById[it.partyId as String] = it.groupName as String }

    // Primary web URL per supplier (mirrors FindSuppliers.groovy). First active
    // PRIMARY_WEB_URL row wins; suppliers without one stay out of the map and
    // their names are rendered as plain text instead of a link.
    EntityQuery.use(delegator)
            .from('PartyContactDetailByPurpose')
            .where(EntityCondition.makeCondition([
                    EntityCondition.makeCondition('partyId', EntityOperator.IN, supplierIdList),
                    EntityCondition.makeCondition('contactMechPurposeTypeId', 'PRIMARY_WEB_URL'),
            ], EntityOperator.AND))
            .filterByDate()
            .queryList()
            .each { row ->
                if (row.infoString && !supplierWebUrlById.containsKey(row.partyId as String)) {
                    supplierWebUrlById[row.partyId as String] = row.infoString as String
                }
            }
}

// Pre-render the supplier-name cell HTML once per supplier (HTML-encoded,
// linkified when a primary web URL is on file). The grid field renders with
// encode-output="false" so we control the full anchor markup here -
// target="_blank" + rel="nofollow noreferrer noopener" - which the OFBiz
// <hyperlink> element does not expose. Same pattern as FindSuppliers.groovy.
def htmlEncoder = UtilCodec.getEncoder('html')
Map<String, String> supplierNameHtmlById = [:]
supplierNameById.each { partyId, name ->
    String safeName = htmlEncoder.encode((name ?: partyId) as String)
    String webUrl = supplierWebUrlById[partyId]
    supplierNameHtmlById[partyId] = webUrl
            ? '<a href="' + htmlEncoder.encode(webUrl) + '" target="_blank" rel="nofollow noreferrer noopener">' + safeName + '</a>'
            : safeName
}

// ===== Step 4: assemble rows =====
List<Map> rows = []
productIds.each { pid ->
    def product = productById[pid]
    if (product == null) return
    Map inv = invByProduct[pid]
    List<Map> sps = spByProduct[pid] ?: Collections.<Map> emptyList()

    Closure addRow = { String supplierPartyId, String supplierName, String supplierNameHtml,
                       String supplierProductId, BigDecimal lastPrice, String currencyUomId ->
        rows << [
                productId             : pid,
                internalName          : product.internalName,
                quantityOnHandTotal   : inv.qoh,
                availableToPromiseTotal: inv.atp,
                accountingQuantityTotal: inv.aqt,
                lastReceivedDate      : inv.lastReceivedDate,
                supplierPartyId       : supplierPartyId,
                // supplierName is kept as the raw text used by the sort logic;
                // supplierNameHtml is what the grid actually renders.
                supplierName          : supplierName,
                supplierNameHtml      : supplierNameHtml ?: '',
                supplierProductId     : supplierProductId,
                lastPrice             : lastPrice,
                currencyUomId         : currencyUomId,
        ]
    }

    if (sps.isEmpty()) {
        // Product has no active suppliers.
        if (supplierFilter && supplierFilter != '__NONE__') {
            // Filter to specific supplier - this product cannot match.
            return
        }
        addRow(null, null, null, null, null, null)
    } else {
        if (supplierFilter == '__NONE__') {
            // User explicitly asked for products with no supplier; skip those that have one.
            return
        }
        sps.each { sp ->
            String spPartyId = sp.partyId as String
            addRow(spPartyId, supplierNameById[spPartyId], supplierNameHtmlById[spPartyId],
                    sp.supplierProductId as String,
                    sp.lastPrice as BigDecimal, sp.currencyUomId as String)
        }
    }
}

// ===== Step 5: sort =====
// Honor column-header clicks. All result columns are sortable; the default
// is QOH ascending. partyId/productId act as deterministic tiebreakers.
Set sortableFields = [
        'productId', 'internalName',
        'quantityOnHandTotal', 'availableToPromiseTotal', 'accountingQuantityTotal',
        'lastReceivedDate', 'supplierName', 'supplierProductId', 'lastPrice',
] as Set
String requestedSort = parameters.sortField as String
String sortField = 'quantityOnHandTotal'
boolean descending = false
if (UtilValidate.isNotEmpty(requestedSort)) {
    String bare = requestedSort.startsWith('-') ? requestedSort.substring(1) : requestedSort
    if (sortableFields.contains(bare)) {
        sortField = bare
        descending = requestedSort.startsWith('-')
    }
}

int dir = descending ? -1 : 1
Comparator nullSafe = { a, b ->
    if (a == null && b == null) return 0
    if (a == null) return 1   // nulls last on ascending
    if (b == null) return -1
    return (a as Comparable).compareTo(b as Comparable)
} as Comparator

rows.sort { Map a, Map b ->
    int cmp = nullSafe.compare(a[sortField], b[sortField]) * dir
    if (cmp != 0) return cmp
    cmp = nullSafe.compare(a.productId, b.productId)
    if (cmp != 0) return cmp
    return nullSafe.compare(a.supplierPartyId, b.supplierPartyId)
}

context.warehouseRows = rows
