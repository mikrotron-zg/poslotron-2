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

import java.sql.Timestamp

import org.apache.ofbiz.base.util.UtilDateTime
import org.apache.ofbiz.base.util.UtilProperties
import org.apache.ofbiz.base.util.UtilValidate
import org.apache.ofbiz.entity.GenericValue
import org.apache.ofbiz.service.ServiceUtil

/*
 * Bulk product-deprecation service. Backs the
 * `poslotronDeprecateSelectedProducts` service definition; called from
 * DeprecateSelectedProductsEvent.groovy after that event has decoded the
 * multi-form selection into a List<String> of productIds.
 *
 * Per product:
 *   1. Stamp Product.salesDiscontinuationDate = now (skip if already set so
 *      a re-run does not bump the original deprecation timestamp).
 *   2. If the product has any ProductKeyword rows, run the standard
 *      deleteProductKeywords service to wipe them - matching the behaviour
 *      of the "Delete all keywords" button on the product-keyword screen.
 *
 * Transaction shape: the surrounding service framework gives us one JTA
 * transaction. Any error returned from a nested runSync (or any thrown
 * exception) propagates up and rolls the whole batch back - we never
 * leave the user with a half-deprecated set.
 */
Map deprecateSelectedProducts() {
    Locale locale = (Locale) (parameters.locale ?: Locale.getDefault())

    // Coerce + dedupe the input. Caller is the multi-form event which sends
    // raw HTTP-decoded strings; tolerate occasional empty entries that show
    // up when a row was rendered but somehow contributed no productId.
    List rawIds = (parameters.selectedProductIds ?: []) as List
    List<String> ids = rawIds.collect { it as String }
            .findAll { UtilValidate.isNotEmpty(it) }
            .unique()

    if (!ids) {
        return ServiceUtil.returnError(UtilProperties.getMessage(
                'PoslotronUiLabels', 'PoslotronDeprecationNoProductsSelected', locale))
    }

    Timestamp now = UtilDateTime.nowTimestamp()
    int count = 0

    for (String pid : ids) {
        GenericValue product = from('Product').where(productId: pid).queryOne()
        if (product == null) {
            return ServiceUtil.returnError(UtilProperties.getMessage(
                    'PoslotronUiLabels', 'PoslotronDeprecationProductNotFound',
                    [pid] as Object[], locale))
        }
        if (product.salesDiscontinuationDate == null) {
            product.salesDiscontinuationDate = now
            product.store()
        }

        // Only call deleteProductKeywords when there is something to delete.
        // The service itself is idempotent (removeRelated on an empty
        // collection is a no-op), but skipping the call for products with no
        // keywords avoids needless permission checks and log noise.
        long kwCount = from('ProductKeyword').where(productId: pid).queryCount()
        if (kwCount > 0L) {
            Map delRes = run service: 'deleteProductKeywords', with: [productId: pid]
            if (!ServiceUtil.isSuccess(delRes)) {
                return delRes
            }
        }
        count++
    }

    Map result = ServiceUtil.returnSuccess(UtilProperties.getMessage(
            'PoslotronUiLabels', 'PoslotronDeprecatedNProducts',
            [count] as Object[], locale))
    result.deprecatedCount = count
    return result
}
