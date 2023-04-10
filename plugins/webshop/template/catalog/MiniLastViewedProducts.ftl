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

<#assign maxToShow = 4/>
<#assign lastViewedProducts = sessionAttributes.lastViewedProducts!/>
<#if lastViewedProducts?has_content>
  <#if (lastViewedProducts?size > maxToShow)>
    <#assign limit=maxToShow/>
  <#else>
    <#assign limit=(lastViewedProducts?size-1)/>
  </#if>
  <div id="minilastviewedproducts" class="card">
    <div class="card-header">
        ${uiLabelMap.EcommerceLastProducts}
        <div class="float-right">
          <h6>
            <a href="<@ofbizUrl>clearLastViewed</@ofbizUrl>" class="badge badge-lg badge-secondary">
              ${uiLabelMap.CommonClear}
            </a>
            <#if (lastViewedProducts?size > maxToShow)>
                <a href="<@ofbizUrl>lastviewedproducts</@ofbizUrl>" class="badge badge-lg badge-secondary">
                  ${uiLabelMap.CommonMore}
                </a>
            </#if>
          </h6>
        </div>
    </div>
    <div class="card-body">
      <ul class="list-unstyled">
        <#list lastViewedProducts[0..limit] as productId>
          <li>
            ${setRequestAttribute("miniProdQuantity", "1")}
            ${setRequestAttribute("optProductId", productId)}
            ${setRequestAttribute("miniProdFormName", "lastviewed" + productId_index + "form")}
            ${screens.render("component://ecommerce/widget/CatalogScreens.xml#miniproductsummary")}
          </li>
        </#list>
      </ul>
    </div>
  </div>
</#if>
