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

import java.sql.Timestamp

import org.apache.ofbiz.fiscal.util.FiscalDateRangeResolver

// The find form's hidden noConditionFind=Y field marks any submission that
// originates from the find form (vs. a fresh menu visit or a grid row update).
boolean fromFindForm = parameters.noConditionFind != null

// The grid (a "list" form, one HTML form per row) submits each row's columns
// with plain names because display/hyperlink fields default to also-hidden=true.
// Those names overlap with the find form's fields (fiscalInvoiceNumber,
// fiscalInvoiceDate, partyId, isPayed) and would otherwise pollute both the
// find form's render and the search criteria after the row Update redirects
// back to FindFiscalInvoicePayment. fiscalInvoiceId is unique to the row form,
// so its presence reliably identifies a grid-row submission.
if (parameters.fiscalInvoiceId != null) {
    // Always drop fields that exist only in the grid row.
    ['fiscalInvoiceId', 'orderId', 'partyIdFrom', 'amount', 'poNumber',
     'submitButton'].each { String key -> parameters.remove(key) }
    if (!fromFindForm) {
        // No earlier find-form submission to fall back on: drop the grid row's
        // values for fields shared with the find form so the form doesn't
        // re-render populated with that single invoice's data.
        ['fiscalInvoiceNumber', 'fiscalInvoiceDate', 'partyId', 'isPayed']
            .each { String key -> parameters.remove(key) }
    }
}

// Build the inputFields map for performFind. Start from the (possibly cleaned)
// request parameters and, if the user picked a "dateRange" option, override
// the fiscalInvoiceDate range. A separate map is used (instead of mutating
// parameters) so that the date-find form field renders correctly: its macro
// casts _fld0_value/_fld1_value to String and would choke on Timestamp values.
Map<String, Object> inputFields = [:]
inputFields.putAll(parameters)

// On initial load (or after a grid row update with no prior find-form
// submission) apply the form's default values so the results grid is populated
// immediately. We must not re-apply defaults when the find form has been
// submitted, otherwise the user could not clear filters (e.g. choose an empty
// isPayed to list both paid and unpaid invoices).
if (!fromFindForm) {
    inputFields.noConditionFind = 'Y'
    inputFields.isPayed = 'N'
    parameters.isPayed = 'N'
}

Map<String, Timestamp> range = FiscalDateRangeResolver.resolve(parameters.dateRange as String, timeZone, locale)
if (range) {
    inputFields.fiscalInvoiceDate_fld0_op = 'greaterThanEqualTo'
    inputFields.fiscalInvoiceDate_fld0_value = range.fromDate
    inputFields.fiscalInvoiceDate_fld1_op = 'lessThanEqualTo'
    inputFields.fiscalInvoiceDate_fld1_value = range.thruDate
}

context.paymentSearchFields = inputFields
