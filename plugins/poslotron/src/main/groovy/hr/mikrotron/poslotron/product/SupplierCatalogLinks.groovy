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
import org.apache.ofbiz.entity.Delegator
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.entity.util.EntityQuery

/**
 * Helpers for turning a supplier's product id into a deep link into that
 * supplier's own catalog.
 *
 * A supplier is considered to have a "catalog" web address when it has a
 * WEB_ADDRESS ContactMech attached via PartyContactMech WITHOUT any active
 * PartyContactMechPurpose entry - this is intentionally distinct from
 * PRIMARY_WEB_URL so a supplier can carry both a homepage (rendered as the
 * supplier-name link) and a catalog root (used here as the prefix for
 * per-productId deep links). Callers append the supplierProductId to that
 * base URL verbatim, url-encoded.
 *
 * Consumers:
 *   - WarehouseStatus.groovy (poslotron warehouse status grid)
 *   - EnrichProductSuppliersWithCatalogLinks.groovy (core ListSupplierProducts
 *     grid on the product detail page)
 */
class SupplierCatalogLinks {

    /**
     * Load one "catalog" base URL per supplier. Suppliers without a
     * purpose-less WEB_ADDRESS are simply absent from the returned map.
     *
     * @param delegator  active OFBiz delegator
     * @param supplierPartyIds  distinct supplier partyIds to look up; nulls
     *                          and duplicates are tolerated
     * @return partyId -> catalog base URL (never null)
     */
    static Map<String, String> loadCatalogBaseUrls(Delegator delegator,
                                                    Collection<String> supplierPartyIds) {
        Map<String, String> result = [:]
        if (!supplierPartyIds) {
            return result
        }
        List<String> partyIdList = supplierPartyIds
                .findAll { it != null }
                .unique()
                .collect { it as String }
        if (partyIdList.isEmpty()) {
            return result
        }

        // Any active (partyId, contactMechId) that is bound to *some* purpose
        // is excluded - we only accept purpose-less web addresses here.
        Set<String> mechsWithPurpose = new HashSet<String>()
        EntityQuery.use(delegator)
                .from('PartyContactMechPurpose')
                .where(EntityCondition.makeCondition('partyId', EntityOperator.IN, partyIdList))
                .filterByDate()
                .queryList()
                .each { row ->
                    mechsWithPurpose << ((row.partyId as String) + '|' + (row.contactMechId as String))
                }

        EntityQuery.use(delegator)
                .from('PartyAndContactMech')
                .where(EntityCondition.makeCondition([
                        EntityCondition.makeCondition('partyId', EntityOperator.IN, partyIdList),
                        EntityCondition.makeCondition('contactMechTypeId', 'WEB_ADDRESS'),
                ], EntityOperator.AND))
                .filterByDate()
                .queryList()
                .each { row ->
                    String partyId = row.partyId as String
                    String contactMechId = row.contactMechId as String
                    if (mechsWithPurpose.contains(partyId + '|' + contactMechId)) return
                    if (!row.infoString) return
                    // First match wins to keep results deterministic when a
                    // supplier has multiple purpose-less web addresses.
                    if (!result.containsKey(partyId)) {
                        result[partyId] = row.infoString as String
                    }
                }
        return result
    }

    /**
     * Render the supplier's product id cell as an HTML string. Returns a
     * hyperlink (target="_blank", rel="nofollow noreferrer noopener") when a
     * baseUrl is available, otherwise the HTML-encoded id as plain text,
     * otherwise an empty string.
     *
     * The grid field must be declared with encode-output="false" so the
     * anchor markup is not double-encoded.
     */
    static String renderSupplierProductIdHtml(String supplierProductId, String baseUrl,
                                              UtilCodec.SimpleEncoder htmlEncoder) {
        if (!supplierProductId) {
            return ''
        }
        String safeText = htmlEncoder.encode(supplierProductId)
        if (!baseUrl) {
            return safeText
        }
        String href = baseUrl + URLEncoder.encode(supplierProductId, 'UTF-8')
        return '<a href="' + htmlEncoder.encode(href) +
                '" target="_blank" rel="nofollow noreferrer noopener">' + safeText + '</a>'
    }
}
