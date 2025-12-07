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
        <fo:block>
            <fo:table table-layout="fixed" font-size="8pt">
                <fo:table-column column-width="15mm"/> <#-- product id -->
                <fo:table-column column-width="100mm"/> <#-- product description -->
                <fo:table-column column-width="10mm"/> <#-- quantity -->
                <fo:table-column column-width="25mm"/> <#-- price excl. VAT -->
                <fo:table-column column-width="30mm"/> <#-- amount excl. tax -->
                <fo:table-header>
                    <fo:table-row>
                        <fo:table-cell border-bottom="thin solid grey"><fo:block font-weight="bold">${uiLabelMap.ProductItem}</fo:block></fo:table-cell>
                        <fo:table-cell border-bottom="thin solid grey"><fo:block font-weight="bold">${uiLabelMap.ProductProduct}</fo:block></fo:table-cell>
                        <fo:table-cell border-bottom="thin solid grey"><fo:block font-weight="bold" text-align="right">${uiLabelMap.ProductQuantity}</fo:block></fo:table-cell>
                        <fo:table-cell border-bottom="thin solid grey"><fo:block font-weight="bold" text-align="right">${uiLabelMap.OrderOrderQuoteUnitPrice}</fo:block></fo:table-cell>
                        <fo:table-cell border-bottom="thin solid grey"><fo:block font-weight="bold" text-align="right">${uiLabelMap.CommonSubtotal}</fo:block></fo:table-cell>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                    <#assign rowColor = "white">
                    <#assign totalQuoteAmount = 0.0>
                    <#if quoteItems?has_content>
                    <#list quoteItems as quoteItem>
                        <#if quoteItem.productId??>
                            <#assign product = quoteItem.getRelatedOne("Product", false)>
                        </#if>
                        <#assign quoteItemAmount = quoteItem.quoteUnitPrice?default(0) * quoteItem.quantity?default(0)>
                        <#assign quoteItemAdjustments = quoteItem.getRelated("QuoteAdjustment", null, null, false)>
                        <#assign totalQuoteItemAdjustmentAmount = 0.0>
                        <#list quoteItemAdjustments as quoteItemAdjustment>
                            <#assign totalQuoteItemAdjustmentAmount = quoteItemAdjustment.amount?default(0) + totalQuoteItemAdjustmentAmount>
                        </#list>
                        <#assign totalQuoteItemAmount = quoteItemAmount + totalQuoteItemAdjustmentAmount>
                        <#assign totalQuoteAmount = totalQuoteAmount + totalQuoteItemAmount>

                        <fo:table-row>
                            <fo:table-cell padding="2pt" background-color="${rowColor}">
                                <fo:block>${quoteItem.quoteItemSeqId}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" background-color="${rowColor}">
                                <fo:block>${(product.internalName)!} [${quoteItem.productId!}]</fo:block>
                                <#if quoteItem.quoteItemSeqId?has_content>
                                    <#assign quoteItemLevelTerms = Static["org.apache.ofbiz.entity.util.EntityUtil"].filterByAnd(quoteTerms, {"quoteItemSeqId": quoteItem.quoteItemSeqId})!>
                                    <#if quoteItemLevelTerms?has_content>
                                        <fo:block>${uiLabelMap.CommonQuoteTerms}:</fo:block>
                                        <#list quoteItemLevelTerms as quoteItemLevelTerm>
                                            <fo:block text-indent="0.1in">
                                                ${quoteItemLevelTerm.getRelatedOne("TermType", false).get("description",locale)} ${quoteItemLevelTerm.termValue?default("")} ${quoteItemLevelTerm.termDays?default("")} ${quoteItemLevelTerm.textValue?default("")}
                                            </fo:block>
                                        </#list>
                                    </#if>
                                </#if>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" background-color="${rowColor}">
                                <fo:block text-align="right">${quoteItem.quantity!}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" background-color="${rowColor}">
                                <fo:block text-align="right"><@ofbizCurrency amount=quoteItem.quoteUnitPrice isoCode=quote.currencyUomId/></fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" background-color="${rowColor}">
                                <fo:block text-align="right"><@ofbizCurrency amount=totalQuoteItemAmount isoCode=quote.currencyUomId/></fo:block>
                            </fo:table-cell>

                        </fo:table-row>
                        <#--<#list quoteItemAdjustments as quoteItemAdjustment>
                            <#assign adjustmentType = quoteItemAdjustment.getRelatedOne("OrderAdjustmentType", false)>
                            <fo:table-row>
                                <fo:table-cell padding="2pt" background-color="${rowColor}">
                                </fo:table-cell>
                                <fo:table-cell padding="2pt" background-color="${rowColor}">
                                </fo:table-cell>
                                <fo:table-cell padding="2pt" background-color="${rowColor}">
                                </fo:table-cell>
                                <fo:table-cell padding="2pt" background-color="${rowColor}">
                                </fo:table-cell>
                                <fo:table-cell padding="2pt" background-color="${rowColor}">
                                    <fo:block font-size="7pt" text-align="right">${adjustmentType.get("description",locale)!}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding="2pt" background-color="${rowColor}">
                                    <fo:block font-size="7pt" text-align="right"><@ofbizCurrency amount=quoteItemAdjustment.amount isoCode=quote.currencyUomId/></fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding="2pt" background-color="${rowColor}">
                                </fo:table-cell>
                            </fo:table-row>
                        </#list>-->

                        <#if "white" == rowColor>
                            <#assign rowColor = "#D4D0C8">
                        <#else>
                            <#assign rowColor = "white">
                        </#if>
                    </#list>
                    <#else>
                      <fo:table-row>
                         <fo:table-cell number-columns-spanned="5" padding="2pt" background-color="${rowColor}">
                             <fo:block>${uiLabelMap.OrderNoItemsQuote}</fo:block>
                         </fo:table-cell>
                      </fo:table-row>
                    </#if>
                </fo:table-body>
            </fo:table>

            <fo:block text-align="right">
                <fo:table table-layout="fixed" font-size="8pt">
                    <fo:table-column column-width="150mm"/>
                    <fo:table-column column-width="30mm"/>
                    <fo:table-body>
                        <fo:table-row>
                            <fo:table-cell border-top="thin solid grey" padding="2pt">
                                <fo:block font-weight="bold" text-align="right">${uiLabelMap.CommonSubtotal}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell border-top="thin solid grey" padding="2pt">
                                <fo:block text-align="right"><@ofbizCurrency amount=totalQuoteAmount isoCode=quote.currencyUomId/></fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                        <#assign totalQuoteHeaderAdjustmentAmount = 0.0>
                        <#list quoteAdjustments as quoteAdjustment>
                            <#assign adjustmentType = quoteAdjustment.getRelatedOne("OrderAdjustmentType", false)>
                            <#if !quoteAdjustment.quoteItemSeqId??>
                                <#assign totalQuoteHeaderAdjustmentAmount = quoteAdjustment.amount?default(0) + totalQuoteHeaderAdjustmentAmount>
                                <fo:table-row>
                                    <fo:table-cell padding="2pt">
                                        <fo:block font-weight="bold" text-align="right">${adjustmentType.get("description", locale)!}</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell padding="2pt">
                                        <fo:block text-align="right"><@ofbizCurrency amount=quoteAdjustment.amount isoCode=quote.currencyUomId/></fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </#if>
                        </#list>
                        <#-- Calculate VAT -->
                        <#assign vatRate = 25> <#-- store.defaultSalesTaxPercentage!0 doesn't work -->
                        <#if vatRate?has_content && vatRate != 0>
                            <#assign vatAmount = (totalQuoteAmount + totalQuoteHeaderAdjustmentAmount) * (vatRate / 100)>
                            <fo:table-row>
                                <fo:table-cell padding="2pt">
                                    <fo:block font-weight="bold" text-align="right">${uiLabelMap.OrderSalesTax} ${vatRate}%</fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding="2pt">
                                    <fo:block text-align="right"><@ofbizCurrency amount=vatAmount isoCode=quote.currencyUomId/></fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <#assign totalQuoteHeaderAdjustmentAmount = totalQuoteHeaderAdjustmentAmount + vatAmount>
                        </#if>
                        <#assign grandTotalQuoteAmount = totalQuoteAmount + totalQuoteHeaderAdjustmentAmount>
                        <fo:table-row font-size="10pt">
                            <fo:table-cell padding="2pt">
                                <fo:block font-family="NotoSans-Bold" text-align="right">${uiLabelMap.OrderTotalDue}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt">
                                <fo:block font-family="NotoSans-Bold" text-align="right"><@ofbizCurrency amount=grandTotalQuoteAmount isoCode=quote.currencyUomId/></fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                    </fo:table-body>
                </fo:table>
            </fo:block>
            <#assign grandTotalQuoteAmountCents = grandTotalQuoteAmount * 100>
            <fo:block text-align="right" font-size="7pt" margin-right="12mm" margin-top="5mm">
                ************* 2D barkod za plaćanje *************
            </fo:block>
            <fo:block text-align="right" margin-right="12mm">
                <fo:instream-foreign-object>
                    <barcode:barcode xmlns:barcode="http://barcode4j.krysalis.org/ns" message="HRVHUB30\u000A${quote.currencyUomId}\u000A${grandTotalQuoteAmountCents}\u000A\u000A\u000A\u000AMIKROTRON d.o.o.\u000APAKOSTANSKA 5 K2-9\u000A10000 ZAGREB\u000AHR8023400091110675464\u000AHR00\u000A${quote.quoteId}\u000A\u000APonuda ${quote.quoteId}">
                        <barcode:pdf417><barcode:row-height>0.4mm</barcode:row-height><barcode:module-width>0.6mm</barcode:module-width></barcode:pdf417>
                    </barcode:barcode>
                </fo:instream-foreign-object>
            </fo:block>
            <fo:block text-align="right" font-size="11pt" margin-right="15mm">
                OVO NIJE FISKALIZIRANI RAČUN
            </fo:block>
        </fo:block>
</#escape>
