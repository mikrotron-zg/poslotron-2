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
<#assign shoppingCart = sessionAttributes.shoppingCart!>
<#if shoppingCart?has_content>
  <#assign shoppingCartSize = shoppingCart.size()>
<#else>
  <#assign shoppingCartSize = 0>
</#if>
<div id="microcart">
  <#if (shoppingCartSize > 0)>
    <p id="microCartNotEmpty">
      <a href="<@ofbizUrl>view/showcart</@ofbizUrl>" class="cart-notification">
        <span class="bi-basket3-fill text-primary h2"></span>
        <span class="badge">${shoppingCart.getTotalQuantity()}</span>
      </a>
      &nbsp;
      <span id="microCartTotal" class="text-primary h4">
        <@ofbizCurrency amount=shoppingCart.getDisplayGrandTotal() isoCode=shoppingCart.getCurrency()/>
      </span>
    </p>
    <span id="microCartEmpty" style="display:none">${uiLabelMap.OrderShoppingCartEmpty}</span>
  <#else>
    <p><span class="bi-basket3 text-secondary h3"></span>&nbsp;&nbsp;${uiLabelMap.OrderShoppingCartEmpty}</p>
  </#if>
  <ul class="list-inline">
    <li class="list-inline-item">
      <a href="<@ofbizUrl>view/showcart</@ofbizUrl>">${uiLabelMap.OrderViewCart}</a>
    </li>
    <#if (shoppingCartSize > 0)>
      <li class="list-inline-item">
        <a href="<@ofbizUrl>checkoutoptions</@ofbizUrl>">${uiLabelMap.OrderCheckoutQuick}</a>
      </li>
      <li class="list-inline-item">
        <a href="<@ofbizUrl>createCustRequestFromCart</@ofbizUrl>">${uiLabelMap.OrderCreateCustRequestFromCart}</a>
      </li>
    <#else>
      <li class="list-inline-item disabled text-muted">${uiLabelMap.OrderCheckoutQuick}</li>
      <li class="list-inline-item disabled text-muted">${uiLabelMap.OrderCreateCustRequestFromCart}</li>
    </#if>
  </ul>
</div>
