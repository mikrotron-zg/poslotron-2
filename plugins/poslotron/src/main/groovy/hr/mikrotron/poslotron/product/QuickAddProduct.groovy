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

import org.apache.ofbiz.base.util.UtilDateTime
import org.apache.ofbiz.base.util.UtilValidate
import org.apache.ofbiz.service.ServiceUtil

/*
 * Quick-add product wrapper.
 *
 * Creates a Product via the standard createProduct service, then -- when a
 * non-empty price was supplied -- follows up with createProductPrice using
 * a fixed configuration: DEFAULT_PRICE / PURCHASE / EUR / _NA_ / taxInPrice=Y
 * / fromDate=now. The companion form
 * (component://poslotron/widget/CatalogAppExtForms.xml#QuickAddProduct)
 * pre-fills these constants so the operator only types the numeric price.
 *
 * Returns the new productId so the controller can redirect to EditProduct.
 */
Map quickCreateProduct() {
    // Pass every Product-shaped attribute straight through to createProduct.
    // `price` and `idValue` are dropped because createProduct does not
    // understand them (and the wrapper service definition only accepts what
    // its <implements> clause covers, so unknown form fields are filtered
    // before we get here).
    Map productCtx = new LinkedHashMap(parameters)
    productCtx.remove('price')
    productCtx.remove('idValue')

    Map createRes = run service: 'createProduct', with: productCtx
    if (!ServiceUtil.isSuccess(createRes)) {
        return createRes
    }
    String productId = createRes.productId

    if (UtilValidate.isNotEmpty(parameters.price)) {
        Map priceRes = run service: 'createProductPrice', with: [
                productId           : productId,
                productPriceTypeId  : 'DEFAULT_PRICE',
                productPricePurposeId: 'PURCHASE',
                currencyUomId       : 'EUR',
                productStoreGroupId : '_NA_',
                fromDate            : UtilDateTime.nowTimestamp(),
                price               : parameters.price,
                taxInPrice          : 'Y',
        ]
        if (!ServiceUtil.isSuccess(priceRes)) {
            return priceRes
        }
    }

    if (UtilValidate.isNotEmpty(parameters.idValue)) {
        Map giRes = run service: 'createGoodIdentification', with: [
                productId              : productId,
                goodIdentificationTypeId: 'KPD_CPA',
                idValue                : parameters.idValue,
        ]
        if (!ServiceUtil.isSuccess(giRes)) {
            return giRes
        }
    }

    Map result = success()
    result.productId = productId
    return result
}
