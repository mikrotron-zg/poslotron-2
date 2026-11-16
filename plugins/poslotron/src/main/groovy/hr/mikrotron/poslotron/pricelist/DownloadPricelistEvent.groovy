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

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.apache.ofbiz.base.util.UtilValidate
import org.apache.ofbiz.entity.util.EntityQuery

/*
 * Public download endpoint for published pricelist CSVs (Croatian Trade Act,
 * Art. 12(1)/(5): the pricelist must be available to anyone visiting the
 * webshop). Looks the file up through the PoslotronPricelist archive entity so
 * only really published files can ever be streamed, then serves it from the
 * directory configured in poslotron.properties.
 *
 * Mounted from poslotron's ecommerce-controller-ext.xml (downloadPricelist)
 * with response type "none"; the event writes the CSV straight to the
 * servlet output stream.
 */

// The id comes either from ?pricelistId=... or, for the stable
// /downloadPricelist/{pricelistId} URL, from the request attribute set by
// the URI-template request-map.
String pricelistId = parameters.pricelistId ?: request.getAttribute('pricelistId')
if (UtilValidate.isEmpty(pricelistId)) {
    response.sendError(404)
    return 'success'
}

def pricelist
if ('current'.equals(pricelistId)) {
    // Stable URL for automated tools: always the latest published pricelist
    // of the configured store (newest sentDate first).
    String productStoreId = org.apache.ofbiz.base.util.UtilProperties.getPropertyValue(
            'poslotron', 'pricelist.productStoreId')
    EntityQuery query = EntityQuery.use(delegator).from('PoslotronPricelist')
    if (UtilValidate.isNotEmpty(productStoreId)) {
        query.where('productStoreId', productStoreId)
    }
    pricelist = query.orderBy('-sentDate').queryFirst()
} else {
    pricelist = EntityQuery.use(delegator).from('PoslotronPricelist')
            .where('pricelistId', pricelistId)
            .queryOne()
}
if (!pricelist) {
    response.sendError(404)
    return 'success'
}

String configuredDir = org.apache.ofbiz.base.util.UtilProperties.getPropertyValue(
        'poslotron', 'pricelist.output.dir', 'runtime/poslotron/pricelists')
Path outputDir = Paths.get(configuredDir)
if (!outputDir.isAbsolute()) {
    outputDir = Paths.get(System.getProperty('ofbiz.home'), configuredDir)
}
// Path-traversal guard: the stored file name must resolve inside outputDir.
Path file = outputDir.resolve(pricelist.getString('fileName')).normalize()
if (!file.startsWith(outputDir.normalize()) || !Files.isReadable(file)) {
    response.sendError(404)
    return 'success'
}

response.setContentType('text/csv;charset=UTF-8')
response.setContentLength((int) Files.size(file))
response.setHeader('Content-Disposition', "attachment; filename=\"${pricelist.getString('fileName')}\"")

OutputStream out = response.getOutputStream()
try {
    Files.copy(file, out)
    out.flush()
} finally {
    out.close()
}
return 'success'
