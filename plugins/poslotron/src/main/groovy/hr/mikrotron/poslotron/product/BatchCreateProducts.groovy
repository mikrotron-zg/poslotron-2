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

import java.math.BigDecimal
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParsePosition

import groovy.transform.Field

import org.apache.ofbiz.base.util.UtilDateTime
import org.apache.ofbiz.base.util.UtilProperties
import org.apache.ofbiz.base.util.UtilValidate
import org.apache.ofbiz.service.ServiceUtil

/*
 * Batch-create products from a semicolon-separated CSV.
 *
 * Columns (in order): productName ; defaultPrice ; KPD/CPA ; weight(g).
 * Only productName and defaultPrice are required; KPD/CPA defaults to
 * '46.50.22' and weight defaults to 15 g. Numbers are parsed with the
 * caller's locale so the decimal separator follows the operator's UI.
 *
 * Validation runs first over the whole file. If any row is invalid the
 * service returns ServiceUtil.returnError(messageList) with one message
 * per offending row and no DB writes happen.
 *
 * The creation pass runs inside the wrapper service's own transaction:
 * the sub-services (createProduct / createProductPrice /
 * createGoodIdentification) join that transaction and a returned error
 * map sets it rollback-only, so any failure undoes every prior insert
 * for the batch and the offending row's message is surfaced through
 * the standard OFBiz error channel.
 */

// @Field promotes these from script-local variables (which are not visible
// inside the methods declared below) to fields on the generated script
// class, so the constants can be referenced from batchCreateProductsCsv()
// and the helpers without being redeclared in each method.
@Field static final String RESOURCE = 'PoslotronUiLabels'
@Field static final String DEFAULT_KPD_CPA = '46.50.22'
@Field static final BigDecimal DEFAULT_WEIGHT_G = new BigDecimal('15')

Map batchCreateProductsCsv() {
    Locale locale = (Locale) (parameters.locale ?: Locale.getDefault())
    ByteBuffer fileBytes = (ByteBuffer) parameters.uploadedFile

    if (fileBytes == null || fileBytes.remaining() == 0) {
        return ServiceUtil.returnError(UtilProperties.getMessage(RESOURCE, 'PoslotronBatchEmptyFile', locale))
    }

    // Read the bytes as UTF-8 (CSV produced by spreadsheet tools is typically UTF-8 these days).
    // duplicate() is used so we don't permanently advance the buffer's position.
    String csv = StandardCharsets.UTF_8.decode(fileBytes.duplicate()).toString().replaceAll('\\r', '')
    List<String> lines = []
    csv.split('\\n', -1).each { String line ->
        if (UtilValidate.isNotEmpty(line?.trim())) {
            lines << line
        }
    }
    if (lines.isEmpty()) {
        return ServiceUtil.returnError(UtilProperties.getMessage(RESOURCE, 'PoslotronBatchEmptyFile', locale))
    }

    NumberFormat numberFormat = NumberFormat.getNumberInstance(locale)
    if (numberFormat instanceof DecimalFormat) {
        ((DecimalFormat) numberFormat).setParseBigDecimal(true)
    }

    // Pass 1 - validate every row before doing any DB work.
    List<Map> rows = []
    List<String> errors = []
    lines.eachWithIndex { String line, int idx ->
        int rowNum = idx + 1
        // Limit -1 keeps trailing empty fields, e.g. "name;1.00;;" -> ["name","1.00","",""].
        String[] cols = line.split(';', -1)
        if (cols.length > 4) {
            errors << UtilProperties.getMessage(RESOURCE, 'PoslotronBatchInvalidColumnCount',
                    [rowNum, cols.length] as Object[], locale)
            return
        }
        String name = cols.length > 0 ? cols[0]?.trim() : ''
        String priceStr = cols.length > 1 ? cols[1]?.trim() : ''
        String kpd = cols.length > 2 ? cols[2]?.trim() : ''
        String weightStr = cols.length > 3 ? cols[3]?.trim() : ''

        if (UtilValidate.isEmpty(name)) {
            errors << UtilProperties.getMessage(RESOURCE, 'PoslotronBatchProductNameRequired',
                    [rowNum] as Object[], locale)
            return
        }
        if (UtilValidate.isEmpty(priceStr)) {
            errors << UtilProperties.getMessage(RESOURCE, 'PoslotronBatchPriceRequired',
                    [rowNum] as Object[], locale)
            return
        }

        BigDecimal price = parseDecimalStrict(priceStr, numberFormat)
        if (price == null) {
            errors << UtilProperties.getMessage(RESOURCE, 'PoslotronBatchPriceInvalid',
                    [rowNum, priceStr] as Object[], locale)
            return
        }

        BigDecimal weight = DEFAULT_WEIGHT_G
        if (UtilValidate.isNotEmpty(weightStr)) {
            weight = parseDecimalStrict(weightStr, numberFormat)
            if (weight == null) {
                errors << UtilProperties.getMessage(RESOURCE, 'PoslotronBatchWeightInvalid',
                        [rowNum, weightStr] as Object[], locale)
                return
            }
        }

        rows << [
                rowNum: rowNum,
                name  : name,
                price : price,
                kpdCpa: UtilValidate.isNotEmpty(kpd) ? kpd : DEFAULT_KPD_CPA,
                weight: weight,
        ]
    }

    if (!errors.isEmpty()) {
        return ServiceUtil.returnError(errors)
    }

    // Pass 2 - persist everything inside the wrapper service's transaction.
    List<Map> createdProducts = []
    for (Map row : rows) {
        Map createRes = run service: 'createProduct', with: [
                productTypeId     : 'FINISHED_GOOD',
                internalName      : row.name,
                productName       : row.name,
                productWeight     : row.weight,
                shippingWeight    : row.weight,
                weightUomId       : 'WT_g',
                autoCreateKeywords: 'N',
        ]
        if (!ServiceUtil.isSuccess(createRes)) {
            return wrapRowError(row, createRes, locale)
        }
        String productId = createRes.productId

        Map priceRes = run service: 'createProductPrice', with: [
                productId            : productId,
                productPriceTypeId   : 'DEFAULT_PRICE',
                productPricePurposeId: 'PURCHASE',
                currencyUomId        : 'EUR',
                productStoreGroupId  : '_NA_',
                fromDate             : UtilDateTime.nowTimestamp(),
                price                : row.price,
                taxInPrice           : 'Y',
        ]
        if (!ServiceUtil.isSuccess(priceRes)) {
            return wrapRowError(row, priceRes, locale)
        }

        Map giRes = run service: 'createGoodIdentification', with: [
                productId               : productId,
                goodIdentificationTypeId: 'KPD_CPA',
                idValue                 : row.kpdCpa,
        ]
        if (!ServiceUtil.isSuccess(giRes)) {
            return wrapRowError(row, giRes, locale)
        }

        createdProducts << [
                productId    : productId,
                productName  : row.name,
                price        : row.price,
                idValue      : row.kpdCpa,
                productWeight: row.weight,
        ]
    }

    Map result = success()
    result.createdProducts = createdProducts
    return result
}

/*
 * Strict locale-aware decimal parse: requires the input to be fully
 * consumed, otherwise a string like "12abc" with a Croatian locale
 * would silently parse as 12.
 */
private BigDecimal parseDecimalStrict(String s, NumberFormat nf) {
    ParsePosition pp = new ParsePosition(0)
    Number n = nf.parse(s, pp)
    if (n == null || pp.index != s.length()) {
        return null
    }
    return n instanceof BigDecimal ? (BigDecimal) n : new BigDecimal(n.toString())
}

private Map wrapRowError(Map row, Map serviceResult, Locale locale) {
    return ServiceUtil.returnError(UtilProperties.getMessage(RESOURCE, 'PoslotronBatchCreateError',
            [row.rowNum, row.name, ServiceUtil.getErrorMessage(serviceResult)] as Object[], locale))
}
