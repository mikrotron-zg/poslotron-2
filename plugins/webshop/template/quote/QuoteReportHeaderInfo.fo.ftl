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

<fo:list-block provisional-distance-between-starts="35mm" font-size="10pt">
    <fo:list-item margin-bottom="5mm">
        <fo:list-item-label>
            <fo:block font-size="14pt" font-family="NotoSans-Bold">${uiLabelMap.OrderOrderQuote}</fo:block>
        </fo:list-item-label>
        <fo:list-item-body start-indent="body-start()">
            <fo:block></fo:block>
        </fo:list-item-body>
    </fo:list-item>
      <fo:list-item>
        <fo:list-item-label>
            <fo:block>${uiLabelMap.OrderOrderQuoteId}</fo:block>
        </fo:list-item-label>
        <fo:list-item-body start-indent="body-start()">
            <fo:block font-family="NotoSans-Bold">${quote.quoteId}</fo:block>
        </fo:list-item-body>
    </fo:list-item>
    <fo:list-item>
        <fo:list-item-label>
            <fo:block>${uiLabelMap.OrderOrderQuoteIssueDate}</fo:block>
        </fo:list-item-label>
        <fo:list-item-body start-indent="body-start()">
            <fo:block>${(quote.issueDate?string("dd. MMMM yyyy."))!}</fo:block>
        </fo:list-item-body>
    </fo:list-item>
    <fo:list-item>
        <fo:list-item-label>
            <fo:block>${uiLabelMap.CommonStatus}</fo:block>
        </fo:list-item-label>
        <fo:list-item-body start-indent="body-start()">
            <fo:block>${(statusItem.get("description", locale))?default(quote.statusId!)}</fo:block>
        </fo:list-item-body>
    </fo:list-item>
</fo:list-block>
</#escape>
