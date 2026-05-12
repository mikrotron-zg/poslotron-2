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
package org.apache.ofbiz.order.quote

import org.apache.ofbiz.base.util.UtilDateTime
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.entity.util.EntityUtil

// Looks up the seller's standard VAT rate from TaxAuthorityRateProduct using the
// store's configured VAT tax authority (ProductStore.vatTaxAuthGeoId / vatTaxAuthPartyId).
// Selects the currently-active row with no productCategoryId, highest taxPercentage first
// (so the standard rate wins over reduced rates if multiple are configured).
// Exposes context.vatRate (BigDecimal taxPercentage) and context.vatRateProduct (GenericValue).

if (store?.vatTaxAuthGeoId) {
    def now = UtilDateTime.nowTimestamp()
    def condList = [
        EntityCondition.makeCondition('taxAuthGeoId', EntityOperator.EQUALS, store.vatTaxAuthGeoId),
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
    if (store.vatTaxAuthPartyId) {
        condList << EntityCondition.makeCondition('taxAuthPartyId', EntityOperator.EQUALS, store.vatTaxAuthPartyId)
    }
    def conds = EntityCondition.makeCondition(condList, EntityOperator.AND)

    def rates = from('TaxAuthorityRateProduct').where(conds).orderBy('-taxPercentage').cache(true).queryList()
    def vatRateProduct = EntityUtil.getFirst(rates)
    if (vatRateProduct) {
        context.vatRateProduct = vatRateProduct
        context.vatRate = vatRateProduct.taxPercentage
    }
}
