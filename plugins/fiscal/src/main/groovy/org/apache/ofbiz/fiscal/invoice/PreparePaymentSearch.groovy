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

import org.apache.ofbiz.base.util.UtilDateTime

// Build the inputFields map for performFind. Start from request parameters and,
// if the user picked a "dateRange" option, override the fiscalInvoiceDate range.
// A separate map is used (instead of mutating parameters) so that the date-find
// form field renders correctly: its macro casts _fld0_value/_fld1_value to String
// and would choke on Timestamp values.
Map<String, Object> inputFields = [:]
inputFields.putAll(parameters)

String dateRange = parameters.dateRange
if (dateRange) {
    Timestamp now = UtilDateTime.nowTimestamp()
    Timestamp fromDate = null
    Timestamp thruDate = null

    switch (dateRange) {
        case 'THIS_MONTH':
            fromDate = UtilDateTime.getMonthStart(now, timeZone, locale)
            thruDate = UtilDateTime.getMonthEnd(now, timeZone, locale)
            break
        case 'LAST_MONTH':
            fromDate = UtilDateTime.getMonthStart(now, 0, -1, timeZone, locale)
            thruDate = UtilDateTime.getMonthEnd(fromDate, timeZone, locale)
            break
        case 'THIS_YEAR':
            fromDate = UtilDateTime.getYearStart(now, timeZone, locale)
            thruDate = UtilDateTime.getYearEnd(now, timeZone, locale)
            break
        case 'LAST_YEAR':
            fromDate = UtilDateTime.getYearStart(now, 0, -1, timeZone, locale)
            thruDate = UtilDateTime.getYearEnd(fromDate, timeZone, locale)
            break
        default:
            break
    }

    if (fromDate && thruDate) {
        inputFields.fiscalInvoiceDate_fld0_op = 'greaterThanEqualTo'
        inputFields.fiscalInvoiceDate_fld0_value = fromDate
        inputFields.fiscalInvoiceDate_fld1_op = 'lessThanEqualTo'
        inputFields.fiscalInvoiceDate_fld1_value = thruDate
    }
}

context.paymentSearchFields = inputFields
