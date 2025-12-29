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
package org.apache.ofbiz.fiscal.invoice;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.ofbiz.base.lang.JSON;

import org.apache.ofbiz.accounting.invoice.InvoiceWorker;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilDateTime;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

/**
 * Fiscal Invoice Services
 */
public class FiscalInvoiceServices {

    private static final String MODULE = FiscalInvoiceServices.class.getName();

    /**
     * Add a fiscal invoice - handles both B2B and B2C cases
     * @param ctx the dispatch context
     * @param context the context
     * @return the result of the service
     */
    public static Map<String, Object> addFiscalInvoice(DispatchContext ctx, Map<String, Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = ctx.getDelegator();

        String orderId = (String) context.get("orderId");
        String partyId = (String) context.get("partyId");
        String partyTaxId = (String) context.get("partyTaxId");
        String fiscalInvoiceType = (String) context.get("fiscalInvoiceType");
        String invoiceId = (String) context.get("invoiceId");

        try {
            // Check if fiscal invoice already exists for this invoiceId
            GenericValue existingFiscalInvoice = EntityQuery.use(delegator)
                    .from("FiscalInvoice")
                    .where("invoiceId", invoiceId)
                    .queryOne();

            if (existingFiscalInvoice != null) {
                return ServiceUtil.returnError("Fiscal invoice already exists for invoice ID: " + invoiceId
                    + " (Fiscal Invoice ID: " + existingFiscalInvoice.getString("fiscalInvoiceId") + ")");
            }

            // Validate invoice exists
            GenericValue invoice = EntityQuery.use(delegator)
                    .from("Invoice")
                    .where("invoiceId", invoiceId)
                    .queryOne();

            if (invoice == null) {
                return ServiceUtil.returnError("Invoice not found with ID: " + invoiceId);
            }

            // Determine if B2B or B2C based on partyTaxId presence
            boolean isB2B = UtilValidate.isNotEmpty(partyTaxId);

            if (isB2B) {
                Debug.logInfo("Processing B2B fiscal invoice for order: " + orderId, MODULE);
                result = processB2BFiscalInvoice(delegator, context, invoice);
            } else {
                Debug.logInfo("Processing B2C fiscal invoice for order: " + orderId, MODULE);
                result = processB2CFiscalInvoice(delegator, context, invoice);
            }

        } catch (GenericEntityException e) {
            Debug.logError(e, "Error creating fiscal invoice", MODULE);
            return ServiceUtil.returnError("Error creating fiscal invoice: " + e.getMessage());
        }

        return result;
    }

    /**
     * Process B2B fiscal invoice - create FiscalInvoice record and update counter
     */
    private static Map<String, Object> processB2BFiscalInvoice(Delegator delegator, Map<String, Object> context, GenericValue invoice)
            throws GenericEntityException {

        String orderId = (String) context.get("orderId");
        String invoiceId = (String) context.get("invoiceId");
        String fiscalInvoiceType = (String) context.get("fiscalInvoiceType");
        String poNumber = (String) context.get("poNumber");
        String isPayed = (String) context.get("isPayed");

        // Calculate invoice total using InvoiceWorker
        BigDecimal amount = InvoiceWorker.getInvoiceTotal(delegator, invoiceId);

        if (amount == null) {
            return ServiceUtil.returnError("Unable to calculate invoice total for invoice: " + invoiceId);
        }

        // Get fiscal payment terminal from fiscal invoice type
        if (UtilValidate.isEmpty(fiscalInvoiceType)) {
            return ServiceUtil.returnError("Fiscal invoice type is required");
        }

        GenericValue fiscalInvoiceTypeEntity = EntityQuery.use(delegator)
                .from("FiscalInvoiceType")
                .where("fiscalInvoiceTypeId", fiscalInvoiceType)
                .queryOne();

        if (fiscalInvoiceTypeEntity == null) {
            return ServiceUtil.returnError("Fiscal invoice type not found: " + fiscalInvoiceType);
        }

        String fiscalPaymentTerminalId = fiscalInvoiceTypeEntity.getString("fiscalPaymentTerminalId");

        // Get the fiscal payment terminal to update counter
        GenericValue fiscalPaymentTerminal = EntityQuery.use(delegator)
                .from("FiscalPaymentTerminal")
                .where("fiscalPaymentTerminalId", fiscalPaymentTerminalId)
                .queryOne();

        if (fiscalPaymentTerminal == null) {
            return ServiceUtil.returnError("Fiscal payment terminal not found: " + fiscalPaymentTerminalId);
        }

        // Get the fiscal store
        String fiscalStoreId = fiscalPaymentTerminal.getString("fiscalStoreId");
        GenericValue fiscalStore = EntityQuery.use(delegator)
                .from("FiscalStore")
                .where("fiscalStoreId", fiscalStoreId)
                .queryOne();

        if (fiscalStore == null) {
            return ServiceUtil.returnError("Fiscal store not found: " + fiscalStoreId);
        }

        // Generate next invoice number
        String lastInvoiceNumber = fiscalPaymentTerminal.getString("lastInvoiceNumber");
        Long nextInvoiceNumber = 1L;

        if (UtilValidate.isNotEmpty(lastInvoiceNumber)) {
            try {
                nextInvoiceNumber = Long.parseLong(lastInvoiceNumber) + 1;
            } catch (NumberFormatException e) {
                Debug.logWarning("Invalid lastInvoiceNumber format, starting from 1", MODULE);
            }
        }

        // Build fiscal invoice number with format: nextInvoiceNumber-fiscalStoreLabel-fiscalPaymentTerminalLabel
        String fiscalStoreLabel = fiscalStore.getString("fiscalStoreLabel");
        String fiscalPaymentTerminalLabel = fiscalPaymentTerminal.getString("fiscalPaymentTerminalLabel");

        StringBuilder fiscalInvoiceNumberBuilder = new StringBuilder();
        fiscalInvoiceNumberBuilder.append(nextInvoiceNumber.toString());
        if (UtilValidate.isNotEmpty(fiscalStoreLabel)) {
            fiscalInvoiceNumberBuilder.append("-").append(fiscalStoreLabel);
        }
        if (UtilValidate.isNotEmpty(fiscalPaymentTerminalLabel)) {
            fiscalInvoiceNumberBuilder.append("-").append(fiscalPaymentTerminalLabel);
        }

        String fiscalInvoiceNumber = fiscalInvoiceNumberBuilder.toString();

        // Create fiscal invoice record
        GenericValue fiscalInvoice = delegator.makeValue("FiscalInvoice");
        fiscalInvoice.set("fiscalInvoiceId", delegator.getNextSeqId("FiscalInvoice"));
        fiscalInvoice.set("invoiceId", invoiceId);
        fiscalInvoice.set("orderId", orderId);
        fiscalInvoice.set("fiscalPaymentTerminalId", fiscalPaymentTerminalId);
        fiscalInvoice.set("fiscalInvoiceNumber", fiscalInvoiceNumber);
        fiscalInvoice.set("fiscalInvoiceDate", UtilDateTime.nowTimestamp());
        fiscalInvoice.set("amount", amount);
        fiscalInvoice.set("poNumber", poNumber);
        fiscalInvoice.set("isPayed", isPayed);
        fiscalInvoice.create();

        // Update the terminal's last invoice number with only the sequential number
        fiscalPaymentTerminal.set("lastInvoiceNumber", nextInvoiceNumber.toString());
        fiscalPaymentTerminal.store();

        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("fiscalInvoiceId", fiscalInvoice.getString("fiscalInvoiceId"));
        result.put("fiscalInvoiceNumber", fiscalInvoiceNumber);

        Debug.logInfo("Successfully created B2B fiscal invoice with number: " + fiscalInvoiceNumber, MODULE);
        return result;
    }

    /**
     * Process B2C fiscal invoice - external API call
     */
    private static Map<String, Object> processB2CFiscalInvoice(Delegator delegator, Map<String, Object> context, GenericValue invoice)
            throws GenericEntityException {

        String orderId = (String) context.get("orderId");
        String invoiceId = (String) context.get("invoiceId");
        String fiscalInvoiceType = (String) context.get("fiscalInvoiceType");

        // Calculate invoice total using InvoiceWorker
        BigDecimal amount = InvoiceWorker.getInvoiceTotal(delegator, invoiceId);

        if (amount == null) {
            return ServiceUtil.returnError("Unable to calculate invoice total for invoice: " + invoiceId);
        }

        // Get fiscal payment terminal from fiscal invoice type
        if (UtilValidate.isEmpty(fiscalInvoiceType)) {
            return ServiceUtil.returnError("Fiscal invoice type is required");
        }

        GenericValue fiscalInvoiceTypeEntity = EntityQuery.use(delegator)
                .from("FiscalInvoiceType")
                .where("fiscalInvoiceTypeId", fiscalInvoiceType)
                .queryOne();

        if (fiscalInvoiceTypeEntity == null) {
            return ServiceUtil.returnError("Fiscal invoice type not found: " + fiscalInvoiceType);
        }

        String fiscalPaymentTerminalId = fiscalInvoiceTypeEntity.getString("fiscalPaymentTerminalId");

        // Get the fiscal payment terminal
        GenericValue fiscalPaymentTerminal = EntityQuery.use(delegator)
                .from("FiscalPaymentTerminal")
                .where("fiscalPaymentTerminalId", fiscalPaymentTerminalId)
                .queryOne();

        if (fiscalPaymentTerminal == null) {
            return ServiceUtil.returnError("Fiscal payment terminal not found: " + fiscalPaymentTerminalId);
        }

        // Get the fiscal store
        String fiscalStoreId = fiscalPaymentTerminal.getString("fiscalStoreId");
        GenericValue fiscalStore = EntityQuery.use(delegator)
                .from("FiscalStore")
                .where("fiscalStoreId", fiscalStoreId)
                .queryOne();

        if (fiscalStore == null) {
            return ServiceUtil.returnError("Fiscal store not found: " + fiscalStoreId);
        }

        // Get fiscal service and API details
        String fiscalServiceId = fiscalPaymentTerminal.getString("fiscalServiceId");
        GenericValue fiscalService = EntityQuery.use(delegator)
                .from("FiscalService")
                .where("fiscalServiceId", fiscalServiceId)
                .queryOne();

        if (fiscalService == null) {
            return ServiceUtil.returnError("Fiscal service not found: " + fiscalServiceId);
        }

        String fiscalServiceApiId = fiscalService.getString("fiscalServiceApiId");
        GenericValue fiscalServiceApi = EntityQuery.use(delegator)
                .from("FiscalServiceApi")
                .where("fiscalServiceApiId", fiscalServiceApiId)
                .queryOne();

        if (fiscalServiceApi == null) {
            return ServiceUtil.returnError("Fiscal service API not found: " + fiscalServiceApiId);
        }

        // Generate API request
        String isPayed = (String) context.get("isPayed");
        String apiRequest = generateFiscalInvoiceRequest(delegator, invoiceId, orderId, isPayed, fiscalServiceApi);

        if (apiRequest == null) {
            return ServiceUtil.returnError("Failed to generate fiscal invoice request");
        }

        // Save request to FiscalServiceApiRequest entity
        GenericValue fiscalServiceApiRequest = delegator.makeValue("FiscalServiceApiRequest");
        fiscalServiceApiRequest.set("fiscalServiceApiRequestId", delegator.getNextSeqId("FiscalServiceApiRequest"));
        fiscalServiceApiRequest.set("fiscalServiceApiId", fiscalServiceApiId);
        fiscalServiceApiRequest.set("invoiceId", invoiceId);
        fiscalServiceApiRequest.set("requestRawText", apiRequest);
        fiscalServiceApiRequest.create();

        Debug.logInfo("Saved fiscal API request to database with ID: " + fiscalServiceApiRequest.getString("fiscalServiceApiRequestId"), MODULE);

        // Log the generated request
        Debug.logInfo("Generated fiscal invoice request: " + apiRequest, MODULE);

        // Get the CREATE_INVOICE command details
        GenericValue fiscalServiceApiCommand = EntityQuery.use(delegator)
                .from("FiscalServiceApiCommand")
                .where("fiscalServiceApiId", fiscalServiceApiId, "fiscalServiceApiCommandId", "CREATE_INVOICE")
                .queryOne();

        if (fiscalServiceApiCommand == null) {
            return ServiceUtil.returnError("CREATE_INVOICE command not found for fiscal service API");
        }

        String httpMethod = fiscalServiceApiCommand.getString("httpMethod");
        String apiCommand = fiscalServiceApiCommand.getString("apiCommand");
        String apiEndpointUrl = fiscalServiceApi.getString("apiEndpointUrl");

        // Combine base URL and command to get the full endpoint URL
        String fullApiUrl = apiEndpointUrl + apiCommand;
        Debug.logInfo("Using API endpoint URL: " + fullApiUrl, MODULE);

        if (UtilValidate.isEmpty(apiEndpointUrl)) {
            return ServiceUtil.returnError("API endpoint URL is missing for fiscal service API");
        }
        if (UtilValidate.isEmpty(apiCommand)) {
            return ServiceUtil.returnError("API command is missing for fiscal service API command");
        }

        // Send request to external API and process response
        Map<String, Object> apiResponse;
        try {
            // Currently only POST is supported, but we check the method from command
            if (!"POST".equals(httpMethod)) {
                return ServiceUtil.returnError("Only POST method is supported for CREATE_INVOICE command");
            }
            apiResponse = sendFiscalInvoiceRequest(fullApiUrl, apiRequest);
        } catch (Exception e) {
            Debug.logError("Error sending fiscal invoice request: " + e.getMessage(), MODULE);
            return ServiceUtil.returnError("Failed to send fiscal invoice request: " + e.getMessage());
        }

        // Save response to database
        String fiscalServiceApiRequestId = fiscalServiceApiRequest.getString("fiscalServiceApiRequestId");
        GenericValue fiscalServiceApiResponse = delegator.makeValue("FiscalServiceApiResponse");
        fiscalServiceApiResponse.set("fiscalServiceApiResponseId", delegator.getNextSeqId("FiscalServiceApiResponse"));
        fiscalServiceApiResponse.set("fiscalServiceApiRequestId", fiscalServiceApiRequestId);
        fiscalServiceApiResponse.set("status", String.valueOf(apiResponse.get("responseCode")));
        fiscalServiceApiResponse.set("responseRawText", apiResponse.get("responseText"));
        fiscalServiceApiResponse.create();

        Debug.logInfo("Saved fiscal API response to database with ID: " + fiscalServiceApiResponse.getString("fiscalServiceApiResponseId"), MODULE);

        // Process the response
        Integer responseCode = (Integer) apiResponse.get("responseCode");
        String responseText = (String) apiResponse.get("responseText");

        if (responseCode == null || responseCode < 200 || responseCode >= 300) {
            return ServiceUtil.returnError("API request failed with status " + responseCode + ": " + responseText);
        }

        // Parse JSON response to extract invoice details
        Map<String, Object> result = ServiceUtil.returnSuccess();
        try {
            // Use OFBiz's JSON utilities to parse the response
            JSON jsonResponse = JSON.from(responseText);
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = jsonResponse.toObject(Map.class);

            // Check the status field - 0 means success, anything else is an error
            Integer status = (Integer) responseMap.get("status");
            if (status == null || status != 0) {
                String errorMessage = (String) responseMap.get("message");
                if (UtilValidate.isEmpty(errorMessage)) {
                    errorMessage = "Unknown API error";
                }
                String errorMsg = "Fiscal API error (status " + status + "): " + errorMessage;
                Debug.logError(errorMsg, MODULE);
                return ServiceUtil.returnError(errorMsg);
            }

            // Extract values from the racun object if it exists
            Map<String, Object> racunData = null;
            if (responseMap.containsKey("racun")) {
                Object racunObj = responseMap.get("racun");
                if (racunObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> racunMap = (Map<String, Object>) racunObj;
                    racunData = racunMap;
                } else {
                    racunData = responseMap;
                }
            } else {
                racunData = responseMap;
            }

            String fiscalInvoiceIdExternal = (String) racunData.get("id");
            String fiscalInvoiceNumber = (String) racunData.get("broj_racuna");
            String fiscalInvoicePdfExternal = (String) racunData.get("pdf");
            String fiscalInvoiceZki = (String) racunData.get("zki");
            String fiscalInvoiceJir = (String) racunData.get("jir");
            String datumRacuna = (String) racunData.get("datum_racuna");
            String brutoSuma = (String) racunData.get("bruto_suma");

            if (UtilValidate.isNotEmpty(fiscalInvoiceIdExternal)) {
                // Create FiscalInvoice record
                GenericValue fiscalInvoice = delegator.makeValue("FiscalInvoice");
                fiscalInvoice.set("fiscalInvoiceId", delegator.getNextSeqId("FiscalInvoice"));
                fiscalInvoice.set("invoiceId", invoiceId);
                fiscalInvoice.set("orderId", orderId);
                fiscalInvoice.set("fiscalPaymentTerminalId", fiscalPaymentTerminalId);
                fiscalInvoice.set("fiscalInvoiceIdExternal", fiscalInvoiceIdExternal);
                fiscalInvoice.set("fiscalInvoiceNumber", fiscalInvoiceNumber);
                fiscalInvoice.set("fiscalInvoicePdfExternal", fiscalInvoicePdfExternal);
                fiscalInvoice.set("zki", fiscalInvoiceZki);
                fiscalInvoice.set("jir", fiscalInvoiceJir);
                // Parse and set the invoice date from API response
                if (UtilValidate.isNotEmpty(datumRacuna)) {
                    try {
                        // Expected format: "dd.MM.yyyy HH:mm:ss"
                        Timestamp fiscalDate = UtilDateTime.stringToTimeStamp(datumRacuna, "dd.MM.yyyy HH:mm:ss", TimeZone.getDefault(), null);
                        if (fiscalDate != null) {
                            fiscalInvoice.set("fiscalInvoiceDate", fiscalDate);
                        } else {
                            fiscalInvoice.set("fiscalInvoiceDate", UtilDateTime.nowTimestamp());
                        }
                    } catch (Exception e) {
                        Debug.logWarning("Failed to parse datum_racuna '" + datumRacuna + "', using current time", MODULE);
                        fiscalInvoice.set("fiscalInvoiceDate", UtilDateTime.nowTimestamp());
                    }
                } else {
                    fiscalInvoice.set("fiscalInvoiceDate", UtilDateTime.nowTimestamp());
                }
                // Parse and set the amount from API response
                BigDecimal fiscalAmount = amount; // Default to calculated amount
                if (UtilValidate.isNotEmpty(brutoSuma)) {
                    try {
                        // Replace comma with dot for decimal parsing
                        String brutoSumaFormatted = brutoSuma.replace(',', '.');
                        fiscalAmount = new BigDecimal(brutoSumaFormatted);
                    } catch (NumberFormatException e) {
                        Debug.logWarning("Failed to parse bruto_suma '" + brutoSuma + "', using calculated amount", MODULE);
                        fiscalAmount = amount;
                    }
                }
                fiscalInvoice.set("amount", fiscalAmount);
                fiscalInvoice.set("isPayed", "Y".equals(isPayed) ? "Y" : "N");
                fiscalInvoice.create();

                result.put("fiscalInvoiceId", fiscalInvoice.getString("fiscalInvoiceId"));
                result.put("fiscalInvoiceNumber", fiscalInvoiceNumber);
                result.put("fiscalInvoiceIdExternal", fiscalInvoiceIdExternal);

                Debug.logInfo("Successfully created fiscal invoice with external ID: " + fiscalInvoiceIdExternal, MODULE);
            } else {
                return ServiceUtil.returnError("Failed to extract invoice ID from API response");
            }
        } catch (Exception e) {
            Debug.logError("Error processing API response: " + e.getMessage(), MODULE);
            return ServiceUtil.returnError("Failed to process API response: " + e.getMessage());
        }

        return result;
    }

    /**
     * Send HTTP POST request to Solo API and return response
     */
    private static Map<String, Object> sendFiscalInvoiceRequest(String apiEndpointUrl, String requestData)
            throws Exception {

        URL url = new URL(apiEndpointUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            // Set up the connection
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000); // 30 seconds timeout
            connection.setReadTimeout(30000); // 30 seconds timeout

            // Send the request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Get the response
            int responseCode = connection.getResponseCode();
            BufferedReader reader;

            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            Map<String, Object> result = new java.util.HashMap<>();
            result.put("responseCode", responseCode);
            result.put("responseText", response.toString());

            Debug.logInfo("API Response Code: " + responseCode, MODULE);
            Debug.logInfo("API Response: " + response.toString(), MODULE);

            return result;

        } finally {
            connection.disconnect();
        }
    }

    /**
     * Generate fiscal invoice API request string for Solo API
     */
    private static String generateFiscalInvoiceRequest(Delegator delegator, String invoiceId, String orderId,
            String isPayed, GenericValue fiscalServiceApi)
            throws GenericEntityException {

        String apiToken = fiscalServiceApi.getString("apiToken");
        if (UtilValidate.isEmpty(apiToken)) {
            Debug.logError("API token is missing for fiscal service API", MODULE);
            return null;
        }

        // Get invoice items, excluding VAT tax items
        List<GenericValue> invoiceItems = EntityQuery.use(delegator)
                .from("InvoiceItem")
                .where("invoiceId", invoiceId)
                .queryList();

        // Get invoice to retrieve party information
        GenericValue invoiceRecord = EntityQuery.use(delegator)
                .from("Invoice")
                .where("invoiceId", invoiceId)
                .queryOne();

        if (invoiceRecord == null) {
            Debug.logError("Invoice not found: " + invoiceId, MODULE);
            return null;
        }

        // Get customer party information
        String partyId = invoiceRecord.getString("partyId");
        String kupacNaziv = "";
        String kupacAdresa = "";

        if (UtilValidate.isNotEmpty(partyId)) {
            // Get party name - check if it's a Person or PartyGroup
            GenericValue party = EntityQuery.use(delegator)
                    .from("Party")
                    .where("partyId", partyId)
                    .queryOne();

            if (party != null) {
                String partyTypeId = party.getString("partyTypeId");

                if ("PERSON".equals(partyTypeId)) {
                    // Get person name
                    GenericValue person = EntityQuery.use(delegator)
                            .from("Person")
                            .where("partyId", partyId)
                            .queryOne();

                    if (person != null) {
                        String firstName = person.getString("firstName");
                        String lastName = person.getString("lastName");

                        if (UtilValidate.isNotEmpty(firstName) && UtilValidate.isNotEmpty(lastName)) {
                            kupacNaziv = firstName + " " + lastName;
                        } else if (UtilValidate.isNotEmpty(lastName)) {
                            kupacNaziv = lastName;
                        } else if (UtilValidate.isNotEmpty(firstName)) {
                            kupacNaziv = firstName;
                        }
                    }
                } else if ("PARTY_GROUP".equals(partyTypeId)) {
                    // Get party group name
                    GenericValue partyGroup = EntityQuery.use(delegator)
                            .from("PartyGroup")
                            .where("partyId", partyId)
                            .queryOne();

                    if (partyGroup != null) {
                        kupacNaziv = partyGroup.getString("groupName");
                    }
                }
            }

            // Get party postal address - use billing address
            GenericValue billingAddress = InvoiceWorker.getBillToAddress(invoiceRecord);
            if (billingAddress != null) {
                StringBuilder address = new StringBuilder();
                if (UtilValidate.isNotEmpty(billingAddress.getString("address1"))) {
                    address.append(billingAddress.getString("address1"));
                }
                if (UtilValidate.isNotEmpty(billingAddress.getString("postalCode"))) {
                    if (address.length() > 0) address.append(", ");
                    address.append(billingAddress.getString("postalCode"));
                }
                if (UtilValidate.isNotEmpty(billingAddress.getString("city"))) {
                    if (address.length() > 0) address.append(" ");
                    address.append(billingAddress.getString("city"));
                }
                kupacAdresa = address.toString();
            }
        }

        if (UtilValidate.isEmpty(invoiceItems)) {
            Debug.logError("No invoice items found for invoice: " + invoiceId, MODULE);
            return null;
        }

        // Build request parameters
        StringBuilder requestBuilder = new StringBuilder();

        // Required parameters
        requestBuilder.append("token=").append(apiToken);
        requestBuilder.append("&tip_usluge=1"); // Constant service type
        requestBuilder.append("&tip_racuna=4"); // No type
        requestBuilder.append("&tip_kupca=1"); // B2C type

        // Add customer information if available
        if (UtilValidate.isNotEmpty(kupacNaziv)) {
            requestBuilder.append("&kupac_naziv=").append(URLEncoder.encode(kupacNaziv, StandardCharsets.UTF_8));
        }
        if (UtilValidate.isNotEmpty(kupacAdresa)) {
            requestBuilder.append("&kupac_adresa=").append(URLEncoder.encode(kupacAdresa, StandardCharsets.UTF_8));
        }
        // For testing purposes: use payment method 3 if current date is before 2026-01-01
        if (java.time.LocalDate.now().isBefore(java.time.LocalDate.of(2026, 1, 1))) {
            requestBuilder.append("&nacin_placanja=3"); // Test payment method
        } else {
            requestBuilder.append("&nacin_placanja=1"); // Transaction account
        }
        requestBuilder.append("&rok_placanja=").append(java.time.LocalDate.now().toString());

        // Process invoice items
        int itemIndex = 1;
        Debug.logInfo("Total invoice items found: " + invoiceItems.size(), MODULE);

        for (GenericValue item : invoiceItems) {
            String invoiceItemTypeId = item.getString("invoiceItemTypeId");
            String description = item.getString("description");
            BigDecimal quantity = item.getBigDecimal("quantity");
            BigDecimal amount = item.getBigDecimal("amount");

            Debug.logInfo("Processing invoice item: type=" + invoiceItemTypeId + ", description=" + description
                         + ", quantity=" + quantity + ", amount=" + amount, MODULE);

            // Skip VAT tax items and other tax-related items
            if ("ITM_VAT_TAX".equals(invoiceItemTypeId)
                || "ITM_SALES_TAX".equals(invoiceItemTypeId)
                || invoiceItemTypeId != null && invoiceItemTypeId.contains("TAX")
                || description != null && (description.contains("PDV") || description.contains("VAT") || description.contains("porez"))) {
                Debug.logInfo("Skipping tax item: " + invoiceItemTypeId + " with description: " + description, MODULE);
                continue;
            }

            // Skip items with no quantity or amount
            if (quantity == null || amount == null) {
                continue;
            }

            // Handle shipping charges with default description
            if ("ITM_SHIPPING_CHARGES".equals(invoiceItemTypeId)) {
                description = "Troškovi dostave";
            }

            // Use description from product if not available
            if (UtilValidate.isEmpty(description)) {
                String productId = item.getString("productId");
                if (UtilValidate.isNotEmpty(productId)) {
                    GenericValue product = EntityQuery.use(delegator)
                            .from("Product")
                            .where("productId", productId)
                            .queryOne();
                    if (product != null) {
                        description = product.getString("productName");
                    }
                }
            }

            // Fallback description if still empty
            if (UtilValidate.isEmpty(description)) {
                description = "Proizvod/Usluga";
            }

            // Add item parameters
            requestBuilder.append("&usluga=").append(itemIndex);
            requestBuilder.append("&opis_usluge_").append(itemIndex).append("=").append(URLEncoder.encode(description, StandardCharsets.UTF_8));
            requestBuilder.append("&kolicina_").append(itemIndex).append("=").append(quantity.setScale(4, RoundingMode.HALF_UP).toString().replace('.', ','));
            requestBuilder.append("&cijena_").append(itemIndex).append("=")
                    .append(amount.setScale(2, RoundingMode.HALF_UP).toString().replace('.', ','));
            requestBuilder.append("&porez_stopa_").append(itemIndex).append("=25"); // Constant VAT rate
            requestBuilder.append("&popust_").append(itemIndex).append("=0"); // No discount for now
            requestBuilder.append("&jed_mjera_").append(itemIndex).append("=1"); // 1 = -

            itemIndex++;
        }

        // Add other constant parameters
        requestBuilder.append("&jezik_racuna=1"); // Croatian
        requestBuilder.append("&valuta_racuna=14"); // EUR (numeric ID)
        requestBuilder.append("&tecaj=1"); // EUR to EUR rate

        // Add optional parameters
        if (UtilValidate.isNotEmpty(orderId)) {
            requestBuilder.append("&napomene=").append(URLEncoder.encode("Narudžba " + orderId, StandardCharsets.UTF_8));
        }

        // Add status based on isPayed field
        if ("Y".equals(isPayed)) {
            requestBuilder.append("&status=5"); // Paid
        } else {
            requestBuilder.append("&status=1"); // Open
        }

        return requestBuilder.toString();
    }
}
