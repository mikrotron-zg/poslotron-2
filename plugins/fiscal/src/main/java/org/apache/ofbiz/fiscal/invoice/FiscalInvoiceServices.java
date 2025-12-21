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
import java.sql.Timestamp;
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
     * Process B2C fiscal invoice - placeholder for external API call
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
        
        // TODO: Implement B2C fiscal invoice processing
        // This should involve:
        // 1. Prepare data for external API call
        // 2. Call external fiscal service API using fiscalPaymentTerminalId
        // 3. Process response and create FiscalInvoice record
        // 4. Handle API errors and retries
        
        Debug.logInfo("B2C fiscal invoice processing not yet implemented - placeholder for order: " + orderId, MODULE);
        
        // For now, return a placeholder response
        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("fiscalInvoiceId", "B2C_PLACEHOLDER_" + System.currentTimeMillis());
        result.put("fiscalInvoiceIdExternal", "EXTERNAL_API_PLACEHOLDER");
        
        return result;
    }
}
