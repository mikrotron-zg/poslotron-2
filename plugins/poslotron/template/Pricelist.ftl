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
<#-- Webshop pricelist archive (Croatian Trade Act, Art. 12). -->
<h1>${uiLabelMap.PoslotronPricelistPageTitle}</h1>
<p>${uiLabelMap.PoslotronPricelistArchiveNote}</p>
<#if pricelists?has_content>
  <p>${uiLabelMap.PoslotronPricelistCurrentDownloadNote}
    <a href="<@ofbizUrl>downloadPricelist/current</@ofbizUrl>"><@ofbizUrl>downloadPricelist/current</@ofbizUrl></a>
  </p>
  <div class="table-responsive">
    <table class="table table-striped">
      <thead>
        <tr>
          <th>${uiLabelMap.PoslotronPricelistFileName}</th>
          <th>${uiLabelMap.PoslotronPricelistSentDate}</th>
          <th>${uiLabelMap.PoslotronPricelistItemCount}</th>
        </tr>
      </thead>
      <tbody>
        <#list pricelists as pricelist>
          <tr>
            <td>
              <a href="<@ofbizUrl>downloadPricelist?pricelistId=${pricelist.pricelistId}</@ofbizUrl>">
                ${pricelist.fileName}
              </a>
            </td>
            <td>${pricelist.sentDate?string("dd.MM.yyyy HH:mm:ss")}</td>
            <td>${pricelist.itemCount?c}</td>
          </tr>
        </#list>
      </tbody>
    </table>
  </div>
<#else>
  <p>${uiLabelMap.PoslotronPricelistEmpty}</p>
</#if>
