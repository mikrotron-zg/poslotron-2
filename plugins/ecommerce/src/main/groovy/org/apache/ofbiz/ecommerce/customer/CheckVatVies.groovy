/*
 * Validates a VAT number via the VIES (Vies-on-the-Web) REST API.
 *
 * Inputs (from context):
 *   viesCountryCode - 2-letter ISO country code (e.g. "DE", "AT"); required
 *   viesVatNumber   - VAT number, with or without the country prefix; required
 *
 * Output (set on context):
 *   viesValid = 'Y' iff the API responds HTTP 200 with a body containing valid==true.
 *   In every other case (invalid, NOT_PROCESSED, non-200, network/parse error,
 *   missing inputs) viesValid is left untouched, so callers can keep their
 *   default value.
 */
package org.apache.ofbiz.ecommerce.customer

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.ofbiz.base.util.Debug

final String MODULE = 'CheckVatVies.groovy'
final String ENDPOINT = 'https://ec.europa.eu/taxation_customs/vies/rest-api/check-vat-number'

try {
    String cc = context.viesCountryCode?.toString()?.toUpperCase()
    String vn = (context.viesVatNumber ?: '').toString().trim()

    if (cc && vn.length() > 2 && vn.substring(0, 2).equalsIgnoreCase(cc)) {
        vn = vn.substring(2)
    }
    vn = vn.replaceAll('[^0-9A-Za-z]', '')

    if (cc && vn) {
        URL url = new URL(ENDPOINT)
        def conn = url.openConnection()
        conn.setRequestMethod('POST')
        conn.setDoOutput(true)
        conn.setConnectTimeout(5000)
        conn.setReadTimeout(10000)
        conn.setRequestProperty('Content-Type', 'application/json')
        conn.setRequestProperty('Accept', 'application/json')

        String body = JsonOutput.toJson([countryCode: cc, vatNumber: vn])
        conn.outputStream.withWriter('UTF-8') { it.write(body) }

        if (conn.responseCode == 200) {
            def resp = new JsonSlurper().parse(conn.inputStream, 'UTF-8')
            if (resp?.valid == true) {
                context.viesValid = 'Y'
            }
        } else {
            Debug.logWarning("VIES check returned HTTP ${conn.responseCode} for ${cc}${vn}", MODULE)
        }
    }
} catch (Throwable t) {
    Debug.logWarning(t, 'VIES VAT check failed: ' + t.message, MODULE)
}
