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
package hr.mikrotron.poslotron.quote

import java.math.BigDecimal
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParsePosition

import groovy.transform.Field

import org.apache.ofbiz.base.util.UtilProperties
import org.apache.ofbiz.base.util.UtilValidate
import org.apache.ofbiz.service.ServiceUtil

/*
 * Batch-add QuoteItems from a semicolon-separated CSV upload.
 *
 * Columns (in order): productId ; quantity ; price.
 * productId and quantity are required; price is optional and, when
 * missing, createQuoteItem itself derives the price from the product's
 * configured ProductPrice (DEFAULT_PRICE/PURCHASE) via calculateProductPrice.
 *
 * Validation runs first across the whole file (including a Product
 * existence check). If any row is invalid the service returns
 * ServiceUtil.returnError(messageList) with one message per offending
 * row and no QuoteItems are written.
 *
 * The creation pass runs inside this service's own transaction:
 * createQuoteItem joins it, and a returned error map flips the
 * transaction to rollback-only, so any failure undoes every prior
 * insert in the batch.
 */

@Field static final String RESOURCE = 'PoslotronUiLabels'

Map batchAddQuoteItems() {
    Locale locale = (Locale) (parameters.locale ?: Locale.getDefault())
    String quoteId = parameters.quoteId
    ByteBuffer fileBytes = (ByteBuffer) parameters.uploadedFile

    if (UtilValidate.isEmpty(quoteId)) {
        return ServiceUtil.returnError(UtilProperties.getMessage(RESOURCE, 'PoslotronQuoteBatchQuoteIdRequired', locale))
    }
    // Confirm the target Quote exists - cheaper to fail early than to
    // discover it inside the loop after partial work would be rolled back.
    if (!from('Quote').where('quoteId', quoteId).queryOne()) {
        return ServiceUtil.returnError(UtilProperties.getMessage('OrderErrorUiLabels', 'OrderQuoteDoesNotExists', locale))
    }
    if (fileBytes == null || fileBytes.remaining() == 0) {
        return ServiceUtil.returnError(UtilProperties.getMessage(RESOURCE, 'PoslotronBatchEmptyFile', locale))
    }

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
        // Limit -1 keeps trailing empty fields, e.g. "PROD;1;" -> ["PROD","1",""].
        String[] cols = line.split(';', -1)
        if (cols.length > 3) {
            errors << UtilProperties.getMessage(RESOURCE, 'PoslotronQuoteBatchInvalidColumnCount',
                    [rowNum, cols.length] as Object[], locale)
            return
        }
        String productId = cols.length > 0 ? cols[0]?.trim() : ''
        String qtyStr = cols.length > 1 ? cols[1]?.trim() : ''
        String priceStr = cols.length > 2 ? cols[2]?.trim() : ''

        if (UtilValidate.isEmpty(productId)) {
            errors << UtilProperties.getMessage(RESOURCE, 'PoslotronQuoteBatchProductIdRequired',
                    [rowNum] as Object[], locale)
            return
        }
        if (!from('Product').where('productId', productId).cache().queryOne()) {
            errors << UtilProperties.getMessage(RESOURCE, 'PoslotronQuoteBatchProductNotFound',
                    [rowNum, productId] as Object[], locale)
            return
        }
        if (UtilValidate.isEmpty(qtyStr)) {
            errors << UtilProperties.getMessage(RESOURCE, 'PoslotronQuoteBatchQuantityRequired',
                    [rowNum] as Object[], locale)
            return
        }
        BigDecimal qty = parseDecimalStrict(qtyStr, numberFormat)
        if (qty == null || qty.signum() <= 0) {
            errors << UtilProperties.getMessage(RESOURCE, 'PoslotronQuoteBatchQuantityInvalid',
                    [rowNum, qtyStr] as Object[], locale)
            return
        }

        BigDecimal price = null
        if (UtilValidate.isNotEmpty(priceStr)) {
            price = parseDecimalStrict(priceStr, numberFormat)
            if (price == null) {
                errors << UtilProperties.getMessage(RESOURCE, 'PoslotronQuoteBatchPriceInvalid',
                        [rowNum, priceStr] as Object[], locale)
                return
            }
        }

        rows << [
                rowNum   : rowNum,
                productId: productId,
                quantity : qty,
                price    : price,
        ]
    }

    if (!errors.isEmpty()) {
        return ServiceUtil.returnError(errors)
    }

    // Pass 2 - persist every row inside this service's transaction.
    for (Map row : rows) {
        Map ctx = [
                quoteId  : quoteId,
                productId: row.productId,
                quantity : row.quantity,
        ]
        // Leaving quoteUnitPrice unset triggers createQuoteItem's own
        // pricing path (calculateProductPrice / config wrapper), which
        // is exactly the "use the product's default price" behaviour.
        if (row.price != null) {
            ctx.quoteUnitPrice = row.price
        }
        Map createRes = run service: 'createQuoteItem', with: ctx
        if (!ServiceUtil.isSuccess(createRes)) {
            return ServiceUtil.returnError(UtilProperties.getMessage(RESOURCE, 'PoslotronQuoteBatchCreateError',
                    [row.rowNum, row.productId, ServiceUtil.getErrorMessage(createRes)] as Object[], locale))
        }
    }

    Map result = success()
    // Echo quoteId so the controller can redirect to ViewQuote.
    result.quoteId = quoteId
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
