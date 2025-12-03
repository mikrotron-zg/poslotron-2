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
    <#if orderHeader?has_content>
        <fo:table table-layout="fixed" border-spacing="3pt" font-size="9pt">
            <fo:table-column column-width="105mm"/>
            <fo:table-column column-width="2mm"/>
            <fo:table-column column-width="20mm"/>
            <fo:table-column column-width="25mm"/>
            <fo:table-column column-width="30mm"/>
            <fo:table-header>
                <fo:table-row font-family="NotoSans-Bold">
                    <fo:table-cell number-columns-spanned="2">
                        <fo:block>${uiLabelMap.OrderProduct}</fo:block>
                    </fo:table-cell>
                    <fo:table-cell text-align="center">
                        <fo:block>${uiLabelMap.OrderQuantity}</fo:block>
                    </fo:table-cell>
                    <fo:table-cell text-align="right">
                        <fo:block>${uiLabelMap.OrderUnitList}</fo:block>
                    </fo:table-cell>
                    <fo:table-cell text-align="right">
                        <fo:block>${uiLabelMap.OrderSubTotal}</fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-header>
            <fo:table-body>
                <#list orderItemList as orderItem>
                    <#assign orderItemType = orderItem.getRelatedOne("OrderItemType", false)!>
                    <#assign productId = orderItem.productId!>
                    <#assign remainingQuantity = (orderItem.quantity?default(0) - orderItem.cancelQuantity?default(0))>
                    <#assign itemAdjustment = Static["org.apache.ofbiz.order.order.OrderReadHelper"].getOrderItemAdjustmentsTotal(orderItem, orderAdjustments, true, false, false)>
                    <#assign internalImageUrl = Static["org.apache.ofbiz.product.imagemanagement.ImageManagementHelper"].getInternalImageUrl(request, productId!)!>
                    <#-- FIXME: should be implemented in Groovy script -->
                    <#assign pdv = 1.25>
                    <fo:table-row>
                        <fo:table-cell>
                            <fo:block>
                                <#if orderItem.supplierProductId?has_content>
                                    ${orderItem.supplierProductId} - <#noescape>${orderItem.itemDescription!}</#noescape>
                                <#elseif productId??>
                                    ${orderItem.productId?default("N/A")} - <#noescape>${orderItem.itemDescription!}</#noescape>
                                <#elseif orderItemType??>
                                    ${orderItemType.get("description",locale)} - <#noescape>${orderItem.itemDescription!}</#noescape>
                                <#else>
                                    <#noescape>${orderItem.itemDescription!}</#noescape>
                                </#if>
                            </fo:block>
                        </fo:table-cell>
                        <fo:table-cell>
                            <fo:block>
                                <#if "PURCHASE_ORDER" == orderHeader.orderTypeId>
                                    <#if internalImageUrl?has_content>
                                        <fo:external-graphic src="${internalImageUrl}" overflow="hidden" content-width="100"/>
                                    </#if>
                                </#if>
                            </fo:block>
                        </fo:table-cell>
                        <fo:table-cell text-align="center">
                            <fo:block>${remainingQuantity}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell text-align="right">
                            <fo:block><@ofbizCurrency amount=orderItem.unitPrice*pdv isoCode=currencyUomId/></fo:block>
                        </fo:table-cell>
                        <fo:table-cell text-align="right">
                            <fo:block>
                                <#if orderItem.statusId != "ITEM_CANCELLED">
                                    <@ofbizCurrency amount=Static["org.apache.ofbiz.order.order.OrderReadHelper"].getOrderItemSubTotal(orderItem, orderAdjustments)*pdv isoCode=currencyUomId/>
                                <#else>
                                    <@ofbizCurrency amount=0.00 isoCode=currencyUomId/>
                                </#if>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                    <#if itemAdjustment != 0>
                        <fo:table-row>
                            <fo:table-cell number-columns-spanned="2">
                                <fo:block text-indent="0.2in">
                                    <fo:inline font-style="italic">${uiLabelMap.OrderAdjustments}</fo:inline>
                                    : <@ofbizCurrency amount=itemAdjustment isoCode=currencyUomId/>
                                </fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                    </#if>
                </#list>
                <#-- <#list orderHeaderAdjustments as orderHeaderAdjustment>
                    <#assign adjustmentType = orderHeaderAdjustment.getRelatedOne("OrderAdjustmentType", false)>
                    <#assign adjustmentAmount = Static["org.apache.ofbiz.order.order.OrderReadHelper"].calcOrderAdjustment(orderHeaderAdjustment, orderSubTotal)>
                    <#if adjustmentAmount != 0>
                        <fo:table-row>
                            <fo:table-cell><fo:block></fo:block></fo:table-cell>
                            <fo:table-cell><fo:block></fo:block></fo:table-cell>
                            <fo:table-cell number-columns-spanned="2">
                                <fo:block font-family="NotoSans-Bold">
                                    ${adjustmentType.get("description",locale)} :
                                    <#if orderHeaderAdjustment.get("description")?has_content>
                                        (${orderHeaderAdjustment.get("description")!})
                                    </#if>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell text-align="right">
                                <fo:block><@ofbizCurrency amount=adjustmentAmount isoCode=currencyUomId/></fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                    </#if>
                </#list> -->
                <#-- summary of order amounts -->
                <fo:table-row>
                    <fo:table-cell><fo:block></fo:block></fo:table-cell>
                    <fo:table-cell><fo:block></fo:block></fo:table-cell>
                    <fo:table-cell border-top-style="solid" border-top-width="thin" padding-top="4pt" number-columns-spanned="2">
                        <fo:block font-family="NotoSans-Bold">${uiLabelMap.OrderItemsSubTotal}</fo:block>
                    </fo:table-cell>
                    <fo:table-cell border-top-style="solid" border-top-width="thin" padding-top="4pt" text-align="right">
                        <fo:block><@ofbizCurrency amount=orderSubTotal*pdv isoCode=currencyUomId/></fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <#-- <#if otherAdjAmount != 0>
                    <fo:table-row>
                        <fo:table-cell><fo:block></fo:block></fo:table-cell>
                        <fo:table-cell><fo:block></fo:block></fo:table-cell>
                        <fo:table-cell number-columns-spanned="2">
                            <fo:block font-family="NotoSans-Bold">${uiLabelMap.OrderTotalOtherOrderAdjustments}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell text-align="right">
                            <fo:block><@ofbizCurrency amount=otherAdjAmount isoCode=currencyUomId/></fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </#if> -->
                <#if shippingAmount != 0>
                    <fo:table-row>
                        <fo:table-cell><fo:block></fo:block></fo:table-cell>
                        <fo:table-cell><fo:block></fo:block></fo:table-cell>
                        <fo:table-cell number-columns-spanned="2">
                            <fo:block font-family="NotoSans-Bold">${uiLabelMap.OrderTotalShippingAndHandling}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell text-align="right">
                            <fo:block><@ofbizCurrency amount=shippingAmount*pdv isoCode=currencyUomId/></fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </#if>
                <#-- <#if taxAmount != 0>
                    <fo:table-row>
                        <fo:table-cell><fo:block></fo:block></fo:table-cell>
                        <fo:table-cell><fo:block></fo:block></fo:table-cell>
                        <fo:table-cell number-columns-spanned="2">
                            <fo:block font-family="NotoSans-Bold">${uiLabelMap.OrderTotalSalesTax}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell text-align="right">
                            <fo:block><@ofbizCurrency amount=taxAmount isoCode=currencyUomId/></fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </#if> -->
                <#if grandTotal != 0>
                    <fo:table-row>
                        <fo:table-cell><fo:block></fo:block></fo:table-cell>
                        <fo:table-cell><fo:block></fo:block></fo:table-cell>
                        <fo:table-cell number-columns-spanned="2" background-color="#EEEEEE">
                            <fo:block font-family="NotoSans-Bold">${uiLabelMap.OrderTotalDue}</fo:block>
                        </fo:table-cell>
                        <fo:table-cell text-align="right" background-color="#EEEEEE">
                            <fo:block font-family="NotoSans-Bold"><@ofbizCurrency amount=grandTotal isoCode=currencyUomId/></fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                    <#-- Payment 2D barcode -->
                    <fo:table-row>
                        <fo:table-cell number-columns-spanned="2"><fo:block></fo:block></fo:table-cell>
                        <fo:table-cell number-columns-spanned="3" padding-top="8pt" text-align="right">
                            <fo:block>
                                <fo:instream-foreign-object>
                                    <barcode:barcode xmlns:barcode="http://barcode4j.krysalis.org/ns" 
                                        message="HRVHUB30\u000A${currencyUomId}\u000A${grandTotalCentsFormated}\u000A\u000A\u000A\u000AMIKROTRON d.o.o.\u000APAKOSTANSKA 5 K2-9\u000A10000 ZAGREB\u000AHR8023400091110675464\u000AHR00\u000A${orderId}\u000A\u000ANarudzba ${orderId}">
                                        <barcode:pdf417>
                                            <barcode:row-height>0.5mm</barcode:row-height>
                                            <barcode:module-width>0.6mm</barcode:module-width>
                                        </barcode:pdf417>
                                    </barcode:barcode>
                                </fo:instream-foreign-object>
                            </fo:block>
                            <fo:block font-size="8pt">
                                ********** 2D barkod za plaćanje ***********
                            </fo:block>
                            <fo:block text-align="right" font-size="11pt" margin-right="10pt">
                                OVO NIJE FISKALIZIRANI RAČUN
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </#if>
                <#-- notes -->
                <#if orderNotes?has_content>
                    <#if showNoteHeadingOnPDF>
                        <fo:table-row>
                            <fo:table-cell number-columns-spanned="3">
                                <fo:block font-family="NotoSans-Bold">${uiLabelMap.OrderNotes}</fo:block>
                                <fo:block>
                                    <fo:leader leader-length="19cm" leader-pattern="rule"/>
                                </fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                    </#if>
                    <#list orderNotes as note>
                        <#if (note.internalNote?has_content) && (note.internalNote != "Y")>
                            <fo:table-row>
                                <fo:table-cell number-columns-spanned="1">
                                    <fo:block>${note.noteInfo!}</fo:block>
                                </fo:table-cell>
                                <fo:table-cell number-columns-spanned="2">
                                    <fo:block>
                                    <#if note.noteParty?has_content>
                                        <#assign notePartyNameResult = dispatcher.runSync("getPartyNameForDate", Static["org.apache.ofbiz.base.util.UtilMisc"].toMap("partyId", note.noteParty, "compareDate", note.noteDateTime, "lastNameFirst", "Y", "userLogin", userLogin))/>
                                        ${uiLabelMap.CommonBy}: ${notePartyNameResult.fullName?default("${uiLabelMap.OrderPartyNameNotFound}")}
                                    </#if>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell number-columns-spanned="1">
                                    <fo:block>${uiLabelMap.CommonAt}: ${note.noteDateTime?string!}</fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                        </#if>
                    </#list>
                </#if>
            </fo:table-body>
        </fo:table>
    </#if>
</#escape>
