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
package org.apache.ofbiz.ecommerce.order

import java.math.BigDecimal
import java.math.RoundingMode

import org.apache.ofbiz.base.util.Debug
import org.apache.ofbiz.base.util.UtilDateTime
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.entity.util.EntityUtil

// Computes a VAT multiplier (e.g. 1.25 for 25% VAT) used by order display templates that
// render amounts which are stored without VAT.
//
// The standard VAT rate is looked up from TaxAuthorityRateProduct using the store's configured
// VAT tax authority (ProductStore.vatTaxAuthGeoId / vatTaxAuthPartyId), selecting the currently
// active row with no productCategoryId and the highest taxPercentage (so the standard rate
// wins over reduced rates if multiple are configured).
//
// VAT exemption is detected primarily from the existing tax adjustments OFBiz wrote: any
// SALES_TAX/VAT_TAX adjustment with a non-zero exemptAmount is the explicit signal written
// by TaxAuthorityServices.handlePartyTaxExempt when it determined the customer is exempt.
//
// As a fallback (e.g. early in checkout, before any tax has been calculated), we look up
// PartyTaxAuthInfo.isExempt = "Y" for the bill-to (or placing) party. Two notes:
//  - The lookup is NOT restricted to the store's vatTaxAuthGeoId, because OFBiz's tax-calc
//    resolves the taxing jurisdiction from the SHIPPING DESTINATION (a Slovenian customer
//    of a Croatian store has their exemption recorded against SVN, not HRV).
//  - Only parties of type PARTY_GROUP can be VAT-exempt. A person who is related to an
//    exempt party group is not themselves exempt, so we skip the lookup unless the resolved
//    customer party is a PARTY_GROUP.
//
// Inputs (from context):
//   productStore             - ProductStore GenericValue (set by OrderStatus.groovy / OrderView.groovy /
//                              CheckoutShippingOptions.groovy)
//   orderReadHelper          - OrderReadHelper (set by OrderView.groovy)
//   localOrderReadHelper     - OrderReadHelper (set by OrderStatus.groovy as alias)
//   shoppingCart             - ShoppingCart (used during checkout flow when no order exists yet)
//   userLogin                - UserLogin (last-resort fallback for the customer party)
//
// Outputs (set on context):
//   vatRate                  - BigDecimal taxPercentage of the store's standard VAT rate (0 if none)
//   vatRateProduct           - the TaxAuthorityRateProduct GenericValue used (or null)
//   customerVatExempt        - boolean flag
//   vatMultiplier            - BigDecimal (1 + vatRate/100), or BigDecimal.ONE if exempt or no rate

def store = context.productStore
def orh = context.orderReadHelper ?: context.localOrderReadHelper
def cart = context.shoppingCart

BigDecimal vatRate = BigDecimal.ZERO
boolean customerVatExempt = false
def vatRateProduct = null

if (store?.vatTaxAuthGeoId) {
    def now = UtilDateTime.nowTimestamp()
    def rateCondList = [
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
        rateCondList << EntityCondition.makeCondition('taxAuthPartyId', EntityOperator.EQUALS, store.vatTaxAuthPartyId)
    }
    def rateConds = EntityCondition.makeCondition(rateCondList, EntityOperator.AND)
    vatRateProduct = EntityUtil.getFirst(from('TaxAuthorityRateProduct').where(rateConds).orderBy('-taxPercentage').cache(true).queryList())
    if (vatRateProduct?.taxPercentage != null) {
        vatRate = vatRateProduct.taxPercentage
    }

    // Primary check: any tax-type adjustment with a non-zero exemptAmount is the explicit
    // signal written by OFBiz's TaxAuthorityServices.handlePartyTaxExempt when it determined
    // the customer is exempt (it sets `amount=0` and `exemptAmount=<originalTax>`).
    def TAX_ADJ_TYPES = ['SALES_TAX', 'VAT_TAX'] as Set
    def hasExemptMarker = { List adjList ->
        if (!adjList) {
            return false
        }
        return adjList.any { adj ->
            if (!(adj?.get('orderAdjustmentTypeId') in TAX_ADJ_TYPES)) {
                return false
            }
            def exempt = adj.get('exemptAmount')
            return exempt != null && (exempt as BigDecimal).signum() > 0
        }
    }

    List allAdjustments = []
    if (orh) {
        allAdjustments.addAll(orh.getAdjustments() ?: [])
    }
    if (cart) {
        allAdjustments.addAll(cart.getAdjustments() ?: [])
        cart.items().each { item -> allAdjustments.addAll(item.getAdjustments() ?: []) }
    }
    customerVatExempt = hasExemptMarker(allAdjustments)

    Debug.logInfo("VAT multiplier: store=${store.productStoreId} vatTaxAuthGeoId=${store.vatTaxAuthGeoId}" +
            " vatTaxAuthPartyId=${store.vatTaxAuthPartyId} vatRate=${vatRate}" +
            " adjustments=${allAdjustments.size()} taxAdjustments=" +
            allAdjustments.findAll { it?.get('orderAdjustmentTypeId') in TAX_ADJ_TYPES }
                    .collect { "[type=${it.orderAdjustmentTypeId} amount=${it.amount} exempt=${it.exemptAmount}]" } +
            " -> customerVatExempt=${customerVatExempt}",
            'GetOrderVatMultiplier.groovy')

    // Fallback: if no exempt-marked tax adjustment was found (e.g. early in the checkout
    // flow before calcTax has run), look up any PartyTaxAuthInfo.isExempt='Y' record for the
    // bill-to (or placing) party. We do NOT restrict to the store's vatTaxAuthGeoId because
    // OFBiz's tax-calc uses the shipping-destination jurisdiction (which is typically what
    // an exemption would be recorded against).
    String customerPartyId = null
    if (!customerVatExempt) {
        // Only call getBillToParty/getPlacingParty when the helper actually wraps an
        // OrderHeader. CheckoutReview.groovy constructs an OrderReadHelper from cart data
        // alone (no orderHeader) — in that case those methods would NPE on getDelegator().
        if (orh?.getOrderHeader()) {
            def party = orh.getBillToParty() ?: orh.getPlacingParty()
            customerPartyId = party?.partyId
        }
        if (!customerPartyId && cart) {
            customerPartyId = cart.getBillToCustomerPartyId() ?: cart.getOrderPartyId() ?: cart.getPartyId()
        }
        if (!customerPartyId && context.userLogin) {
            customerPartyId = context.userLogin.partyId
        }

        // Only PARTY_GROUP-type parties can be VAT-exempt; a person related to an exempt
        // party group is not themselves exempt, so skip the lookup for non-group parties.
        if (customerPartyId) {
            def party = from('Party').where('partyId', customerPartyId).cache().queryOne()
            if (party?.partyTypeId == 'PARTY_GROUP') {
                def ptiConds = EntityCondition.makeCondition([
                    EntityCondition.makeCondition('partyId', EntityOperator.EQUALS, customerPartyId),
                    EntityCondition.makeCondition('isExempt', EntityOperator.EQUALS, 'Y')
                ], EntityOperator.AND)
                def partyTaxInfo = from('PartyTaxAuthInfo').where(ptiConds).orderBy('-fromDate').filterByDate().queryFirst()
                if (partyTaxInfo) {
                    customerVatExempt = true
                }
            }
        }
    }

    Debug.logInfo("VAT multiplier fallback: customerPartyId=${customerPartyId} -> customerVatExempt=${customerVatExempt}",
            'GetOrderVatMultiplier.groovy')
}

context.vatRate = vatRate
context.vatRateProduct = vatRateProduct
context.customerVatExempt = customerVatExempt
context.vatMultiplier = customerVatExempt ?
        BigDecimal.ONE :
        (BigDecimal.ONE + vatRate.divide(new BigDecimal('100'), 6, RoundingMode.HALF_UP))
