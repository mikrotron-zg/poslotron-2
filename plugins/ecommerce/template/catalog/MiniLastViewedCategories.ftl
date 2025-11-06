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

<#assign maxToShow = 8/>
<#assign lastViewedCategories = sessionAttributes.lastViewedCategories!/>
<#if lastViewedCategories?has_content>
  <#if (lastViewedCategories?size > maxToShow)>
    <#assign limit=maxToShow/>
  <#else>
    <#assign limit=(lastViewedCategories?size-1)/>
  </#if>
  <div id="minilastviewedcategories" class="card">
    <div class="card-header">
      ${uiLabelMap.EcommerceLastCategories}
      <div class="float-right">
        <h6>
          <a href="<@ofbizUrl>clearLastViewed</@ofbizUrl>" class="badge badge-lg badge-secondary">
            ${uiLabelMap.CommonClear}
          </a>
        </h6>
      </div>
    </div>
    <div class="card-body">
      <ul class="list-group list-group-flush">
        <#list lastViewedCategories[0..limit] as categoryId>
          <#assign category = delegator.findOne("ProductCategory",
              Static["org.apache.ofbiz.base.util.UtilMisc"].toMap("productCategoryId", categoryId), true)!>
          <#if category?has_content>
              <#if catContentWrappers?? && catContentWrappers[category.productCategoryId]?? &&
                  catContentWrappers[category.productCategoryId].get("CATEGORY_NAME", "html")??>
                <a href="<@ofbizCatalogAltUrl productCategoryId=categoryId/>"
                  class="list-group-item">
                  ${catContentWrappers[category.productCategoryId].get("CATEGORY_NAME", "html")}
                </a>
              <#elseif catContentWrappers?? && catContentWrappers[category.productCategoryId]?? &&
                  catContentWrappers[category.productCategoryId].get("DESCRIPTION", "html")??>
                <a href="<@ofbizCatalogAltUrl productCategoryId=categoryId/>"
                  class="list-group-item">
                  ${catContentWrappers[category.productCategoryId].get("DESCRIPTION", "html")}
                </a>
              <#else>
                <a href="<@ofbizCatalogAltUrl productCategoryId=categoryId/>"
                  class="list-group-item">
                  ${category.description?default(categoryId)!}
                </a>
              </#if>
          </#if>
        </#list>
      </ul>
    </div>
  </div>
</#if>
