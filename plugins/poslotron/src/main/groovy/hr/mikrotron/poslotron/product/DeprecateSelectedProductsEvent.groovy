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
 * Controller event for the "Deprecate selected products" button on the
 * Product Deprecation page. The submitting form is a multi-form
 * (CatalogAppExtForms.xml#ListProductDeprecation) with use-row-submit="true"
 * and a hidden `productId` per row, so each rendered row contributes
 * `_rowSubmit_o_<N>` and `productId_o_<N>` parameters.
 *
 * We decode the multi-form selection here (instead of using
 * ServiceMultiEventHandler) because we want a *single* user-friendly
 * success message ("Deprecated N products.") on the redirected landing
 * page rather than N per-row messages from the standard handler.
 *
 * Behaviour on selection:
 *   - empty selection                    -> _ERROR_MESSAGE_  + "error"
 *   - poslotronDeprecateSelectedProducts -> _EVENT_MESSAGE_  + "success"
 *     returned success                       (request is then redirected
 *                                             back to ProductDeprecation)
 *   - service returned error / threw     -> _ERROR_MESSAGE_  + "error"
 */

HttpServletRequest req = request as HttpServletRequest
Locale userLocale = UtilHttp.getLocale(req)

int rowCount = UtilHttp.getMultiFormRowCount(req)
List<String> selectedIds = []
for (int i = 0; i < rowCount; i++) {
    String suffix = UtilHttp.getMultiRowDelimiter() + i
    String submitFlag = req.getParameter(UtilHttp.getRowSubmitPrefix() + i)
    if ('Y'.equalsIgnoreCase(submitFlag)) {
        String pid = req.getParameter('productId' + suffix)
        if (UtilValidate.isNotEmpty(pid)) {
            selectedIds << pid
        }
    }
}

if (!selectedIds) {
    request.setAttribute('_ERROR_MESSAGE_', UtilProperties.getMessage(
            'PoslotronUiLabels', 'PoslotronDeprecationNoProductsSelected', userLocale))
    return 'error'
}

Map result
try {
    result = dispatcher.runSync('poslotronDeprecateSelectedProducts', [
            selectedProductIds: selectedIds,
            userLogin         : userLogin,
            locale            : userLocale,
    ])
} catch (Exception e) {
    request.setAttribute('_ERROR_MESSAGE_', e.message)
    return 'error'
}

if (ServiceUtil.isError(result) || ServiceUtil.isFailure(result)) {
    request.setAttribute('_ERROR_MESSAGE_', ServiceUtil.getErrorMessage(result))
    return 'error'
}

String successMsg = result.successMessage as String
if (UtilValidate.isNotEmpty(successMsg)) {
    request.setAttribute('_EVENT_MESSAGE_', successMsg)
}
return 'success'
