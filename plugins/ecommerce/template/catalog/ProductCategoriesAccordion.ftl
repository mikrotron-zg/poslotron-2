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

<script type="application/javascript">
    $(document).ready(function(){
    // Toggle chevron icon on show hide of collapse element
    $(".collapse").on('show.bs.collapse', function(){
    $(this).parent(".card").find(".toggle").addClass("rotate");
    }).on('hide.bs.collapse', function(){
    $(this).parent(".card").find(".toggle").removeClass("rotate");
    });
    });
</script>

<#assign curCategoryId = requestAttributes.curCategoryId!>
<#if completedTree?has_content>
<div class="accordion accordion-flush" id="accordionSideNavigation">
  <#list completedTree?sort_by("productCategoryId") as category>
    <#assign catId = category.productCategoryId!>
    <#assign catName = category.categoryName!category.categoryDescription!catId>
    <#assign expandThis = (catId == curCategoryId)>
    <#if category.child?has_content>
      <#list category.child as subcat>
        <#if subcat.productCategoryId! == curCategoryId>
          <#assign expandThis = true>
        </#if>
      </#list>
    <div class="card">
        <div class="card-header" id="heading_${catId}">
            <h2 class="mb-0">
                <a data-toggle="collapse" data-target="#collapse_${catId}" aria-expanded="${expandThis?string}" aria-controls="collapse_${catId}">
                    <span>${catName}</span>
                    <i class="bi bi-chevron-down toggle<#if expandThis> rotate</#if>"></i>
                </a>
            </h2>
        </div>
        <div id="collapse_${catId}" class="collapse<#if expandThis> show</#if>" aria-labelledby="heading_${catId}" data-parent="#accordionSideNavigation">
            <div class="card-body">
                <ul class="list-group">
                  <#list category.child as subcat>
                    <#assign subcatId = subcat.productCategoryId!>
                    <#assign subcatName = subcat.categoryName!subcat.categoryDescription!subcatId>
                    <li class="list-group-item"<#if subcatId == curCategoryId> class="active"</#if>><a href="<@ofbizUrl>category?category_id=${subcatId}</@ofbizUrl>">${subcatName}</a></li>
                  </#list>
                </ul>
            </div>
        </div>
    </div>
    <#else>
    <div class="card">
        <div class="card-header" id="heading_${catId}">
            <h2 class="mb-0"><a href="<@ofbizUrl>category?category_id=${catId}</@ofbizUrl>"<#if expandThis> class="active"</#if>>${catName}</a></h2>
        </div>
    </div>
    </#if>
  </#list>
</div>
</#if>
