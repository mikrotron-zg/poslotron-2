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

import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.entity.util.EntityQuery

fiscalInvoiceId = parameters.fiscalInvoiceId
if (!fiscalInvoiceId) {
    return
}

fiscalInvoice = EntityQuery.use(delegator).from("FiscalInvoice").where("fiscalInvoiceId", fiscalInvoiceId).queryOne()
if (!fiscalInvoice) {
    return
}
context.fiscalInvoice = fiscalInvoice

invoiceId = fiscalInvoice.invoiceId
orderId = fiscalInvoice.orderId
fiscalPaymentTerminalId = fiscalInvoice.fiscalPaymentTerminalId

// Load linked Invoice for items and party info
if (invoiceId) {
    invoice = EntityQuery.use(delegator).from("Invoice").where("invoiceId", invoiceId).queryOne()
    context.invoice = invoice

    // Invoice items (with order item info)
    invItemAndOrdItems = EntityQuery.use(delegator).from("InvItemAndOrdItem")
            .where("invoiceId", invoiceId)
            .orderBy("invoiceItemSeqId")
            .queryList()
    context.invItemAndOrdItems = invItemAndOrdItems

    // Fiscal API request/response (B2C path)
    fiscalServiceApiRequests = EntityQuery.use(delegator).from("FiscalServiceApiRequest")
            .where("invoiceId", invoiceId)
            .queryList()
    if (fiscalServiceApiRequests) {
        context.fiscalServiceApiRequests = fiscalServiceApiRequests
        // Load responses for each request
        fiscalServiceApiResponses = []
        fiscalServiceApiRequests.each { req ->
            responses = EntityQuery.use(delegator).from("FiscalServiceApiResponse")
                    .where("fiscalServiceApiRequestId", req.fiscalServiceApiRequestId)
                    .queryList()
            fiscalServiceApiResponses.addAll(responses)
        }
        context.fiscalServiceApiResponses = fiscalServiceApiResponses
    }
}

// Load fiscal payment terminal and store
if (fiscalPaymentTerminalId) {
    fiscalPaymentTerminal = EntityQuery.use(delegator).from("FiscalPaymentTerminal")
            .where("fiscalPaymentTerminalId", fiscalPaymentTerminalId)
            .queryOne()
    context.fiscalPaymentTerminal = fiscalPaymentTerminal

    if (fiscalPaymentTerminal?.fiscalStoreId) {
        fiscalStore = EntityQuery.use(delegator).from("FiscalStore")
                .where("fiscalStoreId", fiscalPaymentTerminal.fiscalStoreId)
                .queryOne()
        context.fiscalStore = fiscalStore
    }
}
