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

import javax.servlet.http.HttpServletRequest

import org.apache.ofbiz.base.util.UtilHttp
import org.apache.ofbiz.base.util.UtilProperties
import org.apache.ofbiz.base.util.UtilValidate
import org.apache.ofbiz.service.ServiceUtil

/*
 * Controller event for the per-row "Deprecate" button on the Product
 * Deprecation page. Reads a single `productId` request parameter,
 * delegates to poslotronDeprecateSelectedProducts with a one-element
 * list, and promotes the success/error message to the session so it
 * survives the redirect back to ProductDeprecation.
 *
 * Checkbox selection on other rows is irrelevant — this event only
 * ever acts on the one productId passed in the URL parameter.
 *
 * Both success and error responses redirect to ProductDeprecation
 * (see catalog-controller-ext.xml) so the grid always refreshes.
 */

HttpServletRequest req = request as HttpServletRequest
Locale userLocale = UtilHttp.getLocale(req)

String productId = req.getParameter('productId')
if (UtilValidate.isEmpty(productId)) {
    request.session.setAttribute('_ERROR_MESSAGE_', UtilProperties.getMessage(
            'PoslotronUiLabels', 'PoslotronDeprecationProductNotFound',
            [''] as Object[], userLocale))
    return 'error'
}

Map result
try {
    result = dispatcher.runSync('poslotronDeprecateSelectedProducts', [
            selectedProductIds: [productId],
            userLogin         : userLogin,
            locale            : userLocale,
    ])
} catch (Exception e) {
    request.session.setAttribute('_ERROR_MESSAGE_', e.message)
    return 'error'
}

if (ServiceUtil.isError(result) || ServiceUtil.isFailure(result)) {
    request.session.setAttribute('_ERROR_MESSAGE_', ServiceUtil.getErrorMessage(result))
    return 'error'
}

String successMsg = result.successMessage as String
if (UtilValidate.isNotEmpty(successMsg)) {
    request.session.setAttribute('_EVENT_MESSAGE_', successMsg)
}
return 'success'
