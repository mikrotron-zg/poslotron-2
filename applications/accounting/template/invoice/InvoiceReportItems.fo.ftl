<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
<#escape x as x?xml>
    <#-- list of orders -->
    <#if orders?has_content>
    <fo:table table-layout="fixed" width="100%">
        <fo:table-column column-width="1in"/>
        <fo:table-column column-width="5.5in"/>

        <fo:table-body>
          <fo:table-row>
            <fo:table-cell>
              <fo:block>${uiLabelMap.CommonOrder}:</fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block><#list orders as order>${order} </#list></fo:block>
            </fo:table-cell>
          </fo:table-row>
        </fo:table-body>
    </fo:table>
    </#if>

    <#-- list of terms -->
    <#if terms?has_content>
    <fo:table table-layout="fixed" width="100%" space-before="0.1in">
        <fo:table-column column-width="6.5in"/>

        <fo:table-header height="14px">
          <fo:table-row>
            <fo:table-cell>
              <fo:block font-family="NotoSans-Bold">${uiLabelMap.AccountingAgreementItemTerms}</fo:block>
            </fo:table-cell>
          </fo:table-row>
        </fo:table-header>

        <fo:table-body>
          <#list terms as term>
          <#assign termType = term.getRelatedOne("TermType", false)/>
          <fo:table-row>
            <fo:table-cell>
              <fo:block font-size ="10pt" font-family="NotoSans-Bold">${termType.description!} ${term.description!} ${term.termDays!} ${term.textValue!}</fo:block>
            </fo:table-cell>
          </fo:table-row>
          </#list>
        </fo:table-body>
    </fo:table>
    </#if>

    <fo:table table-layout="fixed" width="100%" space-before="0.2in">
    <fo:table-column column-width="18mm"/>
    <fo:table-column column-width="18mm"/>
    <fo:table-column column-width="85mm"/>
    <fo:table-column column-width="15mm"/>
    <fo:table-column column-width="25mm"/>
    <fo:table-column column-width="25mm"/>

    <fo:table-header font-size="9pt">
      <fo:table-row border-bottom-style="solid" border-bottom-width="thin" border-bottom-color="black">
        <fo:table-cell>
          <fo:block font-family="NotoSans-Bold">Id</fo:block>
        </fo:table-cell>
        <fo:table-cell>
            <fo:block font-family="NotoSans-Bold">KPD/CPA</fo:block>
        </fo:table-cell>
        <fo:table-cell>
          <fo:block font-family="NotoSans-Bold">${uiLabelMap.CommonDescription}</fo:block>
        </fo:table-cell>
        <fo:table-cell>
          <fo:block font-family="NotoSans-Bold" text-align="right">${uiLabelMap.CommonQty}</fo:block>
        </fo:table-cell>
        <fo:table-cell>
          <fo:block font-family="NotoSans-Bold" text-align="right">${uiLabelMap.AccountingUnitPrice}</fo:block>
        </fo:table-cell>
        <fo:table-cell>
          <fo:block font-family="NotoSans-Bold" text-align="right">${uiLabelMap.CommonAmount}</fo:block>
        </fo:table-cell>
      </fo:table-row>
    </fo:table-header>


    <fo:table-body font-size="8pt">
        <#assign currentShipmentId = "">
        <#assign newShipmentId = "">
        <#-- if the item has a description, then use its description.  Otherwise, use the description of the invoiceItemType -->
        <#list invoiceItems as invoiceItem>
            <#assign itemType = invoiceItem.getRelatedOne("InvoiceItemType", false)>
            <#assign isItemAdjustment = Static["org.apache.ofbiz.entity.util.EntityTypeUtil"].hasParentType(delegator, "InvoiceItemType", "invoiceItemTypeId", itemType.getString("invoiceItemTypeId"), "parentTypeId", "INVOICE_ADJ")/>

            <#assign taxRate = invoiceItem.getRelatedOne("TaxAuthorityRateProduct", false)!>
            <#assign itemBillings = invoiceItem.getRelated("OrderItemBilling", null, null, false)!>
            <#if itemBillings?has_content>
                <#assign itemBilling = Static["org.apache.ofbiz.entity.util.EntityUtil"].getFirst(itemBillings)>
                <#if itemBilling?has_content>
                    <#assign itemIssuance = itemBilling.getRelatedOne("ItemIssuance", false)!>
                    <#if itemIssuance?has_content>
                        <#assign newShipmentId = itemIssuance.shipmentId>
                        <#assign issuedDateTime = itemIssuance.issuedDateTime/>
                    </#if>
                </#if>
            </#if>
            <#assign description = Static["org.apache.ofbiz.accounting.invoice.InvoiceWorker"].getInvoiceItemDescription(dispatcher, invoiceItem, locale)>

            <#if !isItemAdjustment>
                <fo:table-row height="14px" space-start=".15in">
                    <fo:table-cell display-align="center">
                        <fo:block text-align="left">${invoiceItem.productId!} </fo:block>
                    </fo:table-cell>
                    <fo:table-cell display-align="center">
                        <#if invoiceItem.productId??>
                            <#assign kpdCpaLookup = Static["org.apache.ofbiz.base.util.UtilMisc"].toMap("productId", invoiceItem.productId, "goodIdentificationTypeId", "KPD_CPA")/>
                            <#assign kpdCpa = delegator.findOne("GoodIdentification", kpdCpaLookup, true)!/>
                            <fo:block text-align="left">${kpdCpa.idValue!""}</fo:block>
                        <#else>
                            <fo:block text-align="left">N/A</fo:block>
                        </#if>
                    </fo:table-cell>
                    <fo:table-cell display-align="center">
                        <fo:block text-align="left">${description!}</fo:block>
                    </fo:table-cell>
                      <fo:table-cell display-align="center">
                        <fo:block text-align="right"> <#if invoiceItem.quantity??>${invoiceItem.quantity?string.number}</#if> </fo:block>
                    </fo:table-cell>
                    <fo:table-cell display-align="center" text-align="right">
                        <fo:block> <#if invoiceItem.quantity??><@ofbizCurrency amount=invoiceItem.amount! isoCode=invoice.currencyUomId!/></#if> </fo:block>
                    </fo:table-cell>
                    <fo:table-cell display-align="center" text-align="right">
                        <fo:block> <@ofbizCurrency amount=(Static["org.apache.ofbiz.accounting.invoice.InvoiceWorker"].getInvoiceItemTotal(invoiceItem)) isoCode=invoice.currencyUomId!/> </fo:block>
                    </fo:table-cell>
                </fo:table-row>
            <#else>
                <#-- Shipping -->
                <#if !(invoiceItem.parentInvoiceId?? && invoiceItem.parentInvoiceItemSeqId??) &&
                 (invoiceItem.invoiceItemTypeId == "ITM_SHIPPING_CHARGES" || 
                  invoiceItem.invoiceItemTypeId == "PITM_SHIP_CHARGES" || 
                  invoiceItem.invoiceItemTypeId == "INV_SHIPPING_CHARGES")>
                    <#assign shipmentCost = Static["org.apache.ofbiz.accounting.invoice.InvoiceWorker"].getInvoiceItemTotal(invoiceItem)>
                    <fo:table-row height="14px" space-start=".15in">
                        <fo:table-cell display-align="center">
                            <fo:block text-align="left">SHPMNT</fo:block>
                        </fo:table-cell>
                        <fo:table-cell display-align="center">
                            <fo:block text-align="left">53.30.00 </fo:block>
                        </fo:table-cell>
                        <fo:table-cell display-align="center">
                            <fo:block text-align="left">${description!}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell display-align="center">
                            <fo:block text-align="right">1</fo:block>
                        </fo:table-cell>
                        <fo:table-cell display-align="center" text-align="right">
                            <fo:block> <@ofbizCurrency amount=shipmentCost isoCode=invoice.currencyUomId!/> </fo:block>
                        </fo:table-cell>
                        <fo:table-cell display-align="center" text-align="right">
                            <fo:block> <@ofbizCurrency amount=shipmentCost isoCode=invoice.currencyUomId!/> </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </#if>
            </#if>
        </#list>

        <#-- the grand total -->
        <fo:table-row height="14px" border-top-style="solid" border-top-width="thin" border-top-color="black">
            <fo:table-cell number-columns-spanned="3">
                <fo:block/>
            </fo:table-cell>
            <fo:table-cell number-columns-spanned="2">
                <fo:block>${uiLabelMap.AccountingTotalExclTax}</fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="right">
                <fo:block>
                    <@ofbizCurrency amount=invoiceNoTaxTotal isoCode=invoice.currencyUomId!/>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
        <#if vatTaxIds?has_content>
            <#list vatTaxIds as vatTaxId>
                <#assign taxRate = delegator.findOne("TaxAuthorityRateProduct", Static["org.apache.ofbiz.base.util.UtilMisc"].toMap("taxAuthorityRateSeqId", vatTaxId), true)/>
                <fo:table-row>
                    <fo:table-cell number-columns-spanned="3">
                        <fo:block/>
                    </fo:table-cell>
                    <fo:table-cell number-columns-spanned="2">
                        <fo:block>${taxRate.description}</fo:block>
                    </fo:table-cell>
                    <fo:table-cell text-align="right">
                        <fo:block><@ofbizCurrency amount=vatTaxesByType[vatTaxId] isoCode=invoice.currencyUomId!/></fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </#list>
        </#if>
        <fo:table-row height="5mm">
           <fo:table-cell number-columns-spanned="6">
              <fo:block/>
           </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
           <fo:table-cell number-columns-spanned="3">
              <fo:block/>
           </fo:table-cell>
           <fo:table-cell number-columns-spanned="2">
              <fo:block font-size="10pt" font-family="NotoSans-Bold">${uiLabelMap.AccountingTotalCapital}</fo:block>
           </fo:table-cell>
           <fo:table-cell text-align="right">
              <fo:block font-size="10pt" font-family="NotoSans-Bold"><@ofbizCurrency amount=invoiceTotal isoCode=invoice.currencyUomId!/></fo:block>
           </fo:table-cell>
        </fo:table-row>
        <fo:table-row height="7px">
           <fo:table-cell number-columns-spanned="6">
              <fo:block/>
           </fo:table-cell>
        </fo:table-row>
    </fo:table-body>
 </fo:table>



 <#-- a block with the invoice message-->
 <#if invoice.invoiceMessage?has_content><fo:block>${invoice.invoiceMessage}</fo:block></#if>
 <fo:block></fo:block>
</#escape>
