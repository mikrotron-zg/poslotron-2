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
<script>
$(document).ready(function() {
  if (jQuery.fn.bsgdprcookies !== undefined) {
    jQuery('body').bsgdprcookies({
        title: '${uiLabelMap.EcommerceCookieConsentTitle}',
        message: '${uiLabelMap.EcommerceCookieConsentMessage}',
        moreLink: '/ecommerce/control/CookiePolicy',
        moreLinkLabel: ' ${uiLabelMap.EcommerceCookieConsentMoreLinkLabel}',
        acceptButtonLabel: '${uiLabelMap.EcommerceCookieConsentAcceptButtonLabel}',
        advancedButtonLabel: '${uiLabelMap.EcommerceCookieConsentAdvancedButtonLabel}',
        allowAdvancedOptions: false
    });
  }
});
</script>
<div class="container-fluid">
  <div class="row align-items-center">
    <div class="col">
      <a class="navbar-brand" href="<@ofbizUrl>main</@ofbizUrl>">
          <#if sessionAttributes.overrideLogo??>
            <img src="<@ofbizContentUrl>${sessionAttributes.overrideLogo}</@ofbizContentUrl>" alt="Logo"/>
          <#elseif catalogHeaderLogo??>
            <img src="<@ofbizContentUrl>${catalogHeaderLogo}</@ofbizContentUrl>" alt="Logo"/>
          <#elseif layoutSettings.VT_HDR_IMAGE_URL?has_content>
            <img src="<@ofbizContentUrl>${layoutSettings.VT_HDR_IMAGE_URL}</@ofbizContentUrl>" alt="Logo"/>
          </#if>
        </a>
    </div>
    <div class="col text-center d-none d-lg-block">
      <#if !productStore??>
            <h3>${uiLabelMap.EcommerceNoProductStore}</h3>
          </#if>
          <#if (productStore.title)??>
            <h3>${productStore.title}</h3>
           </#if>
          <#if (productStore.subtitle)??>
            <div id="company-subtitle">${productStore.subtitle}</div>
          </#if>
          <div>
            <#if sessionAttributes.autoName?has_content>
              <span class="text-success">${uiLabelMap.CommonWelcome}&nbsp;${sessionAttributes.autoName}!</span>
              (${uiLabelMap.CommonNotYou}?&nbsp;
              <a href="<@ofbizUrl>autoLogout</@ofbizUrl>" class="linktext">${uiLabelMap.CommonClickHere}</a>)
            <#else>
              ${uiLabelMap.CommonWelcome}!
            </#if>
          </div>
    </div>
    <div class="col">
      ${screens.render("component://ecommerce/widget/CartScreens.xml#microcart")}
    </div>
  </div>
</div>

<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
  <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
    <span class="navbar-toggler-icon"></span>
  </button>
  <div class="collapse navbar-collapse" id="navbarContent">
    <ul class="navbar-nav mr-auto">
      <li class="nav-item">
        <a class="nav-link" href="<@ofbizUrl>main</@ofbizUrl>"><span class="bi-house-fill h5"></span>&nbsp;${uiLabelMap.CommonMain}</a>
      </li>
      <li class="nav-item">
        <a class="nav-link" href="<@ofbizUrl>advancedsearch</@ofbizUrl>"><span class="bi-search h5"></span>&nbsp;${uiLabelMap.CommonSearch}</a>
      </li>
      <#if !userLogin?has_content || userLogin.userLoginId == "anonymous">
        <li class="nav-item">
          <a class="nav-link" href="<@ofbizUrl>${checkLoginUrl}</@ofbizUrl>"><span class="bi-person-fill-check h5"></span>&nbsp;${uiLabelMap.CommonLogin}</a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="<@ofbizUrl>newcustomer</@ofbizUrl>"><span class="bi-person-fill-add h5"></span>&nbsp;${uiLabelMap.EcommerceRegister}</a>
        </li>
      </#if>
      <li class="nav-item">
        <a class="nav-link" href="<@ofbizUrl>faq</@ofbizUrl>"><span class="bi-question-circle-fill h5"></span>&nbsp;${uiLabelMap.EcommerceFAQ}</a>
      </li>
      <li class="nav-item">
        <#if userLogin?has_content && userLogin.userLoginId != "anonymous">
          <a class="nav-link" href="<@ofbizUrl>contactus</@ofbizUrl>"><span class="bi-envelope-fill h5"></span>&nbsp;${uiLabelMap.EcommerceContact}</a>
        <#else>
          <a class="nav-link" href="<@ofbizUrl>AnonContactus</@ofbizUrl>"><span class="bi-envelope-fill h5"></span>&nbsp;${uiLabelMap.EcommerceContact}</a>
        </#if>
      </li>
    </ul>
    <ul class="navbar-nav ml-auto">
      <#if userLogin?has_content && (userLogin.userLoginId)! != "anonymous">
        <li class="nav-item">
          <a class="nav-link" href="<@ofbizUrl>viewprofile</@ofbizUrl>"><span class="bi-person-lines-fill h5"></span>&nbsp;${uiLabelMap.CommonProfile}</a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="<@ofbizUrl>messagelist</@ofbizUrl>"><span class="bi-envelope-at-fill h5"></span>&nbsp;${uiLabelMap.CommonMessages}</a>
        </li>
        <#--<li class="nav-item">
          <a class="nav-link" href="<@ofbizUrl>ListQuotes</@ofbizUrl>">${uiLabelMap.OrderOrderQuotes}</a>
        </li>-->
        <li class="nav-item">
          <a class="nav-link" href="<@ofbizUrl>ListRequests</@ofbizUrl>"><span class="bi-file-earmark-text-fill h5"></span>&nbsp;${uiLabelMap.OrderRequests}</a>
        </li>
        <#--<li class="nav-item">
          <a class="nav-link" href="<@ofbizUrl>editShoppingList</@ofbizUrl>">${uiLabelMap.EcommerceShoppingLists}</a>
        </li>-->
        <li class="nav-item">
          <a class="nav-link" href="<@ofbizUrl>orderhistory</@ofbizUrl>"><span class="bi-clock-history h5"></span>&nbsp;${uiLabelMap.EcommerceOrderHistory}</a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="<@ofbizUrl>logout</@ofbizUrl>"><span class="bi-person-fill-x h5"></span>&nbsp;${uiLabelMap.CommonLogout}</a>
        </li>
      </#if>
      <li>
        ${screens.render("component://ecommerce/widget/CommonScreens.xml#microlanguage")}
      </li>
      <#if catalogQuickaddUse>
        <li class="nav-item"><a class="nav-link" href="<@ofbizUrl>quickadd</@ofbizUrl>">${uiLabelMap.CommonQuickAdd}</a></li>
      </#if>
    </ul>
  </div>
</nav>





