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

import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat

import org.apache.ofbiz.base.util.Debug
import org.apache.ofbiz.base.util.GroovyUtil
import org.apache.ofbiz.base.util.UtilProperties

final String MODULE = 'GenerateFiscalReport.groovy'
final String PREPARE_SCRIPT =
        'component://fiscal/src/main/groovy/org/apache/ofbiz/fiscal/invoice/PrepareFiscalReportData.groovy'

// --- Parameters ---------------------------------------------------------
String exportFormat = parameters.exportFormat ?: 'CSV'
if (exportFormat != 'CSV' && exportFormat != 'PDF') {
    request.setAttribute('_ERROR_MESSAGE_', "Export format '${exportFormat}' is not supported.")
    return 'error'
}

// --- PDF is rendered by a screenfop view; the screen re-runs the prep. --
if (exportFormat == 'PDF') {
    return 'pdf'
}

// --- Query (shared prep script) ----------------------------------------
Map<String, Object> prepCtx = new HashMap<>(binding.variables)
try {
    GroovyUtil.runScriptAtLocation(PREPARE_SCRIPT, null, prepCtx)
} catch (Exception e) {
    Debug.logError(e, 'Error preparing fiscal report data', MODULE)
    request.setAttribute('_ERROR_MESSAGE_', "Error preparing fiscal report data: ${e.message}")
    return 'error'
}

List<Map<String, Object>> reportInvoices = (List<Map<String, Object>>) prepCtx.reportInvoices

// --- CSV generation ----------------------------------------------------
String csvDelimiter = parameters.csvDelimiter
if (!csvDelimiter) {
    csvDelimiter = ';'
}
// Enforce single character as per UI contract.
csvDelimiter = csvDelimiter.substring(0, 1)

Closure<String> csvEscape = { String value ->
    if (value == null) {
        return ''
    }
    if (value.contains(csvDelimiter) || value.contains('"') || value.contains('\n') || value.contains('\r')) {
        return '"' + value.replace('"', '""') + '"'
    }
    return value
}

StringBuilder csv = new StringBuilder()
csv.append(csvEscape(UtilProperties.getMessage('FiscalUiLabels', 'FiscalInvoiceNumber', locale))).append(csvDelimiter)
   .append(csvEscape(UtilProperties.getMessage('FiscalUiLabels', 'FiscalInvoiceRecipient', locale))).append(csvDelimiter)
   .append(csvEscape(UtilProperties.getMessage('FiscalUiLabels', 'FiscalInvoiceDate', locale))).append(csvDelimiter)
   .append(csvEscape(UtilProperties.getMessage('CommonUiLabels', 'CommonAmount', locale))).append(csvDelimiter)
   .append(csvEscape(UtilProperties.getMessage('FiscalUiLabels', 'FiscalIsPayed', locale))).append('\r\n')

reportInvoices.each { Map<String, Object> row ->
    BigDecimal amount = (BigDecimal) row.amount
    // CSV amount is a raw number (no currency symbol, no grouping, dot decimal).
    String amountStr = amount != null ? amount.toPlainString() : ''
    csv.append(csvEscape(row.invoiceNumber as String)).append(csvDelimiter)
       .append(csvEscape(row.recipientName as String)).append(csvDelimiter)
       .append(csvEscape(row.invoiceDateFormatted as String)).append(csvDelimiter)
       .append(csvEscape(amountStr)).append(csvDelimiter)
       .append(row.isPayed as String).append('\r\n')
}

// --- Write response -----------------------------------------------------
// UTF-8 BOM so Excel opens the file with the correct encoding by default.
byte[] bom = [(byte) 0xEF, (byte) 0xBB, (byte) 0xBF] as byte[]
byte[] body = csv.toString().getBytes(StandardCharsets.UTF_8)

String filename = 'fiscal-invoices-report-' + new SimpleDateFormat('yyyyMMdd-HHmmss').format(new Date()) + '.csv'
response.setContentType('text/csv; charset=UTF-8')
response.setHeader('Content-Disposition', "attachment; filename=\"${filename}\"")
response.setContentLength(bom.length + body.length)
try {
    OutputStream out = response.getOutputStream()
    out.write(bom)
    out.write(body)
    out.flush()
} catch (IOException e) {
    Debug.logError(e, 'Error writing CSV report', MODULE)
    return 'error'
}

return 'success'
