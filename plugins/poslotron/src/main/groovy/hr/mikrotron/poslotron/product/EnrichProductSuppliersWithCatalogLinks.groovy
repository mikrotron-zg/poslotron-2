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

/*
 * Runs inside the ListSupplierProducts grid on the product detail page
 * (applications/product/widget/catalog/ProductForms.xml). Reads the
 * `productSuppliers` list (populated by ProductScreens.xml via <entity-and>)
 * and rewrites it as a list of plain Maps enriched with `supplierProductIdHtml`.
 *
 * The grid's `supplierProductId` column then renders that pre-built HTML with
 * encode-output="false", giving us a deep link into the supplier's own
 * catalog whenever the supplier has a purpose-less WEB_ADDRESS on file.
 * See SupplierCatalogLinks for the discovery rules; the warehouse status grid
 * (WarehouseStatus.groovy) uses the same helper for the same behavior.
 *
 * Silently no-ops if there are no rows.
 */

List productSuppliers = context.productSuppliers as List
if (!productSuppliers) {
    return
}

// Batch-load one catalog base URL per distinct supplier.
Set<String> supplierIds = new HashSet<String>()
productSuppliers.each { sp ->
    String partyId = sp?.partyId as String
    if (partyId) supplierIds << partyId
}
Map<String, String> baseUrlByPartyId = SupplierCatalogLinks.loadCatalogBaseUrls(delegator, supplierIds)

def htmlEncoder = UtilCodec.getEncoder('html')

// Copy each row into a mutable Map so we can attach supplierProductIdHtml.
// GenericValue itself implements Map but only tolerates keys that match the
// underlying entity fields - adding an ad-hoc key on it would fail.
List enriched = productSuppliers.collect { sp ->
    Map row = new LinkedHashMap(sp as Map)
    String partyId = sp.partyId as String
    String baseUrl = partyId ? baseUrlByPartyId[partyId] : null
    row.supplierProductIdHtml = SupplierCatalogLinks.renderSupplierProductIdHtml(
            sp.supplierProductId as String, baseUrl, htmlEncoder)
    return row
}
context.productSuppliers = enriched
