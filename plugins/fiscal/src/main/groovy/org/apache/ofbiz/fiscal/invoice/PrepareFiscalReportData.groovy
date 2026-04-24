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

import java.math.BigDecimal
import java.sql.Timestamp
import java.text.SimpleDateFormat

import org.apache.ofbiz.base.util.Debug
import org.apache.ofbiz.base.util.UtilDateTime
import org.apache.ofbiz.base.util.UtilValidate
import org.apache.ofbiz.entity.GenericValue
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.entity.util.EntityQuery

/*
 * Shared data preparation script for the fiscal reporting page.
 *
 * Reads filters from `parameters` and publishes the following variables on
 * the screen/event `context` map so both CSV and PDF renderers can reuse
 * the same rows without re-running the query:
 *
 *   reportInvoices         List<Map> - one entry per invoice (see keys below)
 *   reportFromDate         Timestamp - inclusive lower bound of the date range (or null)
 *   reportThruDate         Timestamp - inclusive upper bound of the date range (or null)
 *   reportDateRangeKey     String    - 'THIS_MONTH' | 'LAST_MONTH' | ... (or null)
 *
 * Each map in `reportInvoices` carries:
 *   invoiceNumber          String
 *   recipientName          String  (firstName + lastName, or groupName; middle name omitted)
 *   invoiceDate            Timestamp
 *   invoiceDateFormatted   String  (dd.MM.yyyy.)
 *   amount                 BigDecimal
 *   currencyUomId          String
 *   isPayed                String  ('Y' or 'N', never null)
 *   fiscalPaymentTerminalLabel String
 */

final String MODULE = 'PrepareFiscalReportData.groovy'

// --- Build entity conditions -------------------------------------------
List<EntityCondition> conds = []

Timestamp fromDate = null
Timestamp thruDate = null
String dateRange = parameters.dateRange
if (dateRange) {
    Timestamp now = UtilDateTime.nowTimestamp()
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
} else {
    // date-find widget posts fld0 (from) and fld1 (thru) in ISO format.
    String fld0 = parameters.fiscalInvoiceDate_fld0_value
    String fld1 = parameters.fiscalInvoiceDate_fld1_value
    if (fld0) {
        try {
            fromDate = UtilDateTime.stringToTimeStamp(fld0, 'yyyy-MM-dd', timeZone, locale)
        } catch (Exception e) {
            Debug.logWarning("Could not parse fiscalInvoiceDate_fld0_value '${fld0}'", MODULE)
        }
    }
    if (fld1) {
        try {
            Timestamp parsed = UtilDateTime.stringToTimeStamp(fld1, 'yyyy-MM-dd', timeZone, locale)
            // Include the entire "thru" day.
            thruDate = UtilDateTime.getDayEnd(parsed, timeZone, locale)
        } catch (Exception e) {
            Debug.logWarning("Could not parse fiscalInvoiceDate_fld1_value '${fld1}'", MODULE)
        }
    }
}
if (fromDate) {
    conds.add(EntityCondition.makeCondition('fiscalInvoiceDate', EntityOperator.GREATER_THAN_EQUAL_TO, fromDate))
}
if (thruDate) {
    conds.add(EntityCondition.makeCondition('fiscalInvoiceDate', EntityOperator.LESS_THAN_EQUAL_TO, thruDate))
}

if (UtilValidate.isNotEmpty(parameters.fiscalStoreId)) {
    conds.add(EntityCondition.makeCondition('fiscalStoreId', parameters.fiscalStoreId))
}
if (UtilValidate.isNotEmpty(parameters.fiscalPaymentTerminalId)) {
    conds.add(EntityCondition.makeCondition('fiscalPaymentTerminalId', parameters.fiscalPaymentTerminalId))
}
if (UtilValidate.isNotEmpty(parameters.isPayed)) {
    conds.add(EntityCondition.makeCondition('isPayed', parameters.isPayed))
}

EntityCondition whereCond = conds ? EntityCondition.makeCondition(conds, EntityOperator.AND) : null

// --- Query --------------------------------------------------------------
List<GenericValue> invoices = EntityQuery.use(delegator)
        .from('FiscalInvoiceSearchView')
        .where(whereCond)
        .orderBy('fiscalPaymentTerminalLabel', 'fiscalInvoiceDate')
        .queryList()

// --- Enrich with recipient name ----------------------------------------
SimpleDateFormat dateFmt = new SimpleDateFormat('dd.MM.yyyy.')
dateFmt.setTimeZone(timeZone)

List<Map<String, Object>> reportInvoices = []
invoices.each { GenericValue inv ->
    String recipientPartyId = inv.getString('partyId')
    Timestamp invDate = (Timestamp) inv.get('fiscalInvoiceDate')

    String recipientName = ''
    if (UtilValidate.isNotEmpty(recipientPartyId)) {
        try {
            Map<String, Object> nameRes = dispatcher.runSync('getPartyNameForDate', [
                partyId: recipientPartyId,
                compareDate: invDate,
                userLogin: userLogin
            ])
            if (nameRes?.groupName) {
                recipientName = nameRes.groupName as String
            } else {
                // "firstName lastName" - middle name deliberately omitted.
                recipientName = [nameRes?.firstName, nameRes?.lastName]
                        .findAll { it }
                        .join(' ')
            }
        } catch (Exception e) {
            Debug.logWarning("Could not resolve party name for '${recipientPartyId}': ${e.message}", MODULE)
        }
    }

    reportInvoices << [
        invoiceNumber             : inv.getString('fiscalInvoiceNumber') ?: '',
        recipientName             : recipientName,
        invoiceDate               : invDate,
        invoiceDateFormatted      : invDate ? dateFmt.format(invDate) : '',
        amount                    : (BigDecimal) inv.get('amount'),
        currencyUomId             : inv.getString('currencyUomId') ?: '',
        isPayed                   : 'Y' == inv.getString('isPayed') ? 'Y' : 'N',
        fiscalPaymentTerminalLabel: inv.getString('fiscalPaymentTerminalLabel') ?: ''
    ]
}

// --- Publish to the caller's context -----------------------------------
context.reportInvoices = reportInvoices
context.reportFromDate = fromDate
context.reportThruDate = thruDate
context.reportDateRangeKey = dateRange
