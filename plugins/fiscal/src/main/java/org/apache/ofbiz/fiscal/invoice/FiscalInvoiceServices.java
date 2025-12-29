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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.ofbiz.accounting.invoice.InvoiceWorker;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilDateTime;
import org.apache.ofbiz.base.util.UtilMisc;
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
                return ServiceUtil.returnError("Fiscal invoice already exists for invoice ID: " + invoiceId + 
                    " (Fiscal Invoice ID: " + existingFiscalInvoice.getString("fiscalInvoiceId") + ")");
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
        
        // Log the generated request
        Debug.logInfo("Generated fiscal invoice request: " + apiRequest, MODULE);
        
        // TODO: Send request to external API and process response
        // This will be implemented in the next step
        
        // For now, return a placeholder response
        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("fiscalInvoiceId", "B2C_PLACEHOLDER_" + System.currentTimeMillis());
        result.put("fiscalInvoiceIdExternal", "EXTERNAL_API_PLACEHOLDER");
        
        return result;
    }
    
    /**
     * Generate fiscal invoice API request string for Solo API
     */
    private static String generateFiscalInvoiceRequest(Delegator delegator, String invoiceId, String orderId, String isPayed, GenericValue fiscalServiceApi) 
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
        requestBuilder.append("&nacin_placanja=1"); // Transaction account
        
        // Process invoice items
        int itemIndex = 1;
        Debug.logInfo("Total invoice items found: " + invoiceItems.size(), MODULE);
        
        for (GenericValue item : invoiceItems) {
            String invoiceItemTypeId = item.getString("invoiceItemTypeId");
            String description = item.getString("description");
            BigDecimal quantity = item.getBigDecimal("quantity");
            BigDecimal amount = item.getBigDecimal("amount");
            
            Debug.logInfo("Processing invoice item: type=" + invoiceItemTypeId + ", description=" + description + 
                         ", quantity=" + quantity + ", amount=" + amount, MODULE);
            
            // Skip VAT tax items and other tax-related items
            if ("ITM_VAT_TAX".equals(invoiceItemTypeId) || 
                "ITM_SALES_TAX".equals(invoiceItemTypeId) ||
                invoiceItemTypeId != null && invoiceItemTypeId.contains("TAX") ||
                description != null && (description.contains("PDV") || description.contains("VAT") || description.contains("porez"))) {
                Debug.logInfo("Skipping tax item: " + invoiceItemTypeId + " with description: " + description, MODULE);
                continue;
            }
            
            // Skip items with no quantity or amount
            if (quantity == null || amount == null) {
                continue;
            }
            
            // Calculate net price (amount is already net in OFBiz)
            BigDecimal netPrice = amount.divide(quantity, 2, RoundingMode.HALF_UP);
            
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
            requestBuilder.append("&cijena_").append(itemIndex).append("=").append(netPrice.setScale(2, RoundingMode.HALF_UP).toString().replace('.', ','));
            requestBuilder.append("&porez_stopa_").append(itemIndex).append("=25"); // Constant VAT rate
            requestBuilder.append("&popust_").append(itemIndex).append("=0"); // No discount for now
            requestBuilder.append("&jed_mjera_").append(itemIndex).append("=1"); // 1 = -
            
            itemIndex++;
        }
        
        // Add other constant parameters
        requestBuilder.append("&jezik_racuna=1"); // Croatian
        requestBuilder.append("&valuta_racuna=1"); // EUR
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
