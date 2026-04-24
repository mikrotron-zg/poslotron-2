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
<#assign dateFmtFo = "dd.MM.yyyy.">
<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format"
         <#if defaultFontFamily?has_content>font-family="${defaultFontFamily}"</#if>
         font-size="9pt">
    <fo:layout-master-set>
        <fo:simple-page-master master-name="a4-portrait"
                               page-width="210mm" page-height="297mm"
                               margin-top="15mm" margin-bottom="15mm"
                               margin-left="15mm" margin-right="15mm">
            <fo:region-body margin-top="20mm" margin-bottom="10mm"/>
            <fo:region-before extent="18mm"/>
            <fo:region-after extent="8mm"/>
        </fo:simple-page-master>
    </fo:layout-master-set>

    <fo:page-sequence master-reference="a4-portrait">
        <fo:static-content flow-name="xsl-region-before">
            <fo:block font-size="13pt" font-weight="bold" text-align="center" space-after="2pt">
                ${uiLabelMap.FiscalReportingTitle}
            </fo:block>
            <#if reportFromDate?? || reportThruDate??>
                <fo:block font-size="9pt" text-align="center">
                    ${uiLabelMap.FiscalDateRange}:
                    <#if reportFromDate??>${reportFromDate?string(dateFmtFo)}<#else>&#8211;</#if>
                    &#8211;
                    <#if reportThruDate??>${reportThruDate?string(dateFmtFo)}<#else>&#8211;</#if>
                </fo:block>
            </#if>
        </fo:static-content>

        <fo:static-content flow-name="xsl-region-after">
            <fo:block font-size="8pt" text-align="center" border-top="0.2pt solid black" padding-top="2pt">
                ${uiLabelMap.CommonPage} <fo:page-number/> / <fo:page-number-citation ref-id="theEnd"/>
            </fo:block>
        </fo:static-content>

        <fo:flow flow-name="xsl-region-body">
            <fo:block>
                <fo:table table-layout="fixed" width="100%">
                    <fo:table-column column-width="proportional-column-width(18)"/>
                    <fo:table-column column-width="proportional-column-width(32)"/>
                    <fo:table-column column-width="proportional-column-width(16)"/>
                    <fo:table-column column-width="proportional-column-width(22)"/>
                    <fo:table-column column-width="proportional-column-width(12)"/>
                    <fo:table-header>
                        <fo:table-row font-weight="bold">
                            <fo:table-cell padding="3pt" background-color="#D4D0C8" border="0.1mm solid black">
                                <fo:block>${uiLabelMap.FiscalInvoiceNumber}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="3pt" background-color="#D4D0C8" border="0.1mm solid black">
                                <fo:block>${uiLabelMap.FiscalInvoiceRecipient}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="3pt" background-color="#D4D0C8" border="0.1mm solid black">
                                <fo:block text-align="center">${uiLabelMap.FiscalInvoiceDate}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="3pt" background-color="#D4D0C8" border="0.1mm solid black">
                                <fo:block text-align="right">${uiLabelMap.CommonAmount}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="3pt" background-color="#D4D0C8" border="0.1mm solid black">
                                <fo:block text-align="center">${uiLabelMap.FiscalIsPayed}</fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                    </fo:table-header>
                    <fo:table-body>
                        <#if reportInvoices?has_content>
                            <#assign currentTerminal = "">
                            <#list reportInvoices as row>
                                <#if (row.fiscalPaymentTerminalLabel!"") != currentTerminal>
                                    <#assign currentTerminal = row.fiscalPaymentTerminalLabel!"">
                                    <fo:table-row>
                                        <fo:table-cell number-columns-spanned="5" padding="2pt"
                                                       background-color="#EFEFEF" border="0.1mm solid black">
                                            <fo:block font-weight="bold">
                                                ${uiLabelMap.FiscalPaymentTerminal}: ${currentTerminal}
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </#if>
                                <fo:table-row>
                                    <fo:table-cell padding="2pt" border="0.1mm solid black">
                                        <fo:block>${row.invoiceNumber!}</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell padding="2pt" border="0.1mm solid black">
                                        <fo:block>${row.recipientName!}</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell padding="2pt" border="0.1mm solid black">
                                        <fo:block text-align="center">${row.invoiceDateFormatted!}</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell padding="2pt" border="0.1mm solid black">
                                        <fo:block text-align="right">
                                            <#if row.amount??>
                                                <@ofbizCurrency amount=row.amount isoCode=(row.currencyUomId!"")/>
                                            </#if>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell padding="2pt" border="0.1mm solid black">
                                        <fo:block text-align="center">
                                            <#-- NotoSans (FOP's default) has no Dingbats glyphs, so we
                                                 render the check/cross as inline SVG via Batik. -->
                                            <#if (row.isPayed!) == "Y">
                                                <fo:instream-foreign-object content-width="10pt" content-height="10pt">
                                                    <svg xmlns="http://www.w3.org/2000/svg"
                                                         width="10" height="10" viewBox="0 0 10 10">
                                                        <path d="M1.5,5.5 L4,8 L8.5,2.5"
                                                              fill="none" stroke="#008000" stroke-width="1.8"
                                                              stroke-linecap="round" stroke-linejoin="round"/>
                                                    </svg>
                                                </fo:instream-foreign-object>
                                            <#else>
                                                <fo:instream-foreign-object content-width="10pt" content-height="10pt">
                                                    <svg xmlns="http://www.w3.org/2000/svg"
                                                         width="10" height="10" viewBox="0 0 10 10">
                                                        <path d="M2,2 L8,8 M2,8 L8,2"
                                                              fill="none" stroke="#CC0000" stroke-width="1.8"
                                                              stroke-linecap="round"/>
                                                    </svg>
                                                </fo:instream-foreign-object>
                                            </#if>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </#list>
                        <#else>
                            <fo:table-row>
                                <fo:table-cell number-columns-spanned="5" padding="6pt" border="0.1mm solid black">
                                    <fo:block text-align="center" font-style="italic">
                                        ${uiLabelMap.CommonNoRecordFound}
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                        </#if>
                    </fo:table-body>
                </fo:table>
            </fo:block>
            <fo:block id="theEnd"/>
        </fo:flow>
    </fo:page-sequence>
</fo:root>
</#escape>
