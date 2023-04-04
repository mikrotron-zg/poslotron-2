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
      <span class="bi-basket3-fill text-primary h3"></span>&nbsp;
      ${uiLabelMap.EcommerceCartHas}
      <strong id="microCartQuantity">
        ${shoppingCart.getTotalQuantity()}
      </strong>
      <#if shoppingCart.getTotalQuantity() == 1>
        ${uiLabelMap.OrderItem}
      <#else>
        ${uiLabelMap.OrderItems}
      </#if>,
      <strong id="microCartTotal">
        <@ofbizCurrency amount=shoppingCart.getDisplayGrandTotal() isoCode=shoppingCart.getCurrency()/>
      </strong>
    </p>
    <span id="microCartEmpty" style="display:none">${uiLabelMap.OrderShoppingCartEmpty}</span>
  <#else>
    <p><span class="bi-basket3 text-secondary h3"></span>&nbsp;${uiLabelMap.OrderShoppingCartEmpty}</p>
  </#if>
  <ul class="list-inline">
    <li class="list-inline-item">
      <a href="<@ofbizUrl>view/showcart</@ofbizUrl>">[${uiLabelMap.OrderViewCart}]</a>
    </li>
    <#if (shoppingCartSize > 0)>
      <#if !initialLocaleComplete?? || initialLocaleComplete?length == 2 >
        <#if initialLocaleComplete?? && initialLocaleComplete?length == 2  && "fr" == initialLocaleComplete>
          <#assign initialLocaleComplete = "fr_FR"><#-- same idea can be used with other default locale -->
        <#else>
          <#assign initialLocaleComplete = "en_US">
        </#if>
      </#if>
      <li class="list-inline-item" id="quickCheckoutEnabled">
        <a href="<@ofbizUrl>quickcheckout</@ofbizUrl>">[${uiLabelMap.OrderCheckoutQuick}]</a>
      </li>
      <li class="list-inline-item disabled" id="quickCheckoutDisabled" style="display:none">
        [${uiLabelMap.OrderCheckoutQuick}]
      </li>
      <li class="list-inline-item" id="onePageCheckoutEnabled">
        <a href="<@ofbizUrl>onePageCheckout</@ofbizUrl>">[${uiLabelMap.EcommerceOnePageCheckout}]</a>
      </li>
      <li class="list-inline-item disabled" id="onePageCheckoutDisabled" style="display:none">
        [${uiLabelMap.EcommerceOnePageCheckout}]
      </li>
      <#if shoppingCart?has_content && (shoppingCart.getGrandTotal() > 0)>
        <li class="list-inline-item" id="microCartPayPalCheckout">
          <a href="<@ofbizUrl>setPayPalCheckout</@ofbizUrl>">
            <img src="https://www.paypal.com/${initialLocaleComplete}/i/btn/btn_xpressCheckout.gif"
                alt="[PayPal Express Checkout]"
                onError="this.onerror=null;this.src='https://www.paypal.com/en_US/i/btn/btn_xpressCheckout.gif'"/>
          </a>
        </li>
      </#if>
    <#else>
      <li class="list-inline-item disabled">[${uiLabelMap.OrderCheckoutQuick}]</li>
      <li class="list-inline-item disabled">[${uiLabelMap.EcommerceOnePageCheckout}]</li>
    </#if>
  </ul>
</div>