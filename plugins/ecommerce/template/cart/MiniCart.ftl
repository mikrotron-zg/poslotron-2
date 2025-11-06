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

<div id="minicart" class="card">
  <div class="card-header">
  ${uiLabelMap.OrderCartSummary}
    <#if (shoppingCartSize > 0)>
      <div class="float-right">
        <h6>
          <a href="<@ofbizUrl>view/showcart</@ofbizUrl>" class="badge badge-lg badge-secondary">
            ${uiLabelMap.CommonEdit}
          </a>
        </h6>
      </div>
    </#if>
  </div>
  <div class="card-body">
  <#if (shoppingCartSize > 0)>
    <table class="table">
      <thead>
      <tr>
        <th>${uiLabelMap.OrderQty}</th>
        <th>${uiLabelMap.OrderItem}</th>
        <th>${uiLabelMap.CommonSubtotal}</th>
      </tr>
      </thead>
      <tfoot>
      <tr>
        <td colspan="3">
          <span class="font-weight-bold float-right">
            ${uiLabelMap.OrderTotal}: <@ofbizCurrency amount=shoppingCart.getDisplayGrandTotal() isoCode=shoppingCart.getCurrency()/>
          </span>
        </td>
      </tr>
      </tfoot>
      <tbody>
        <#list shoppingCart.items() as cartLine>
        <tr>
          <td>${cartLine.getQuantity()?string.number}</td>
          <td>
            <#if cartLine.getProductId()??>
              <#if cartLine.getParentProductId()??>
                <a href="<@ofbizCatalogAltUrl productId=cartLine.getParentProductId()/>" class="linktext">${cartLine.getName(dispatcher)}</a>
              <#else>
                <a href="<@ofbizCatalogAltUrl productId=cartLine.getProductId()/>" class="linktext">${cartLine.getName(dispatcher)}</a>
              </#if>
            <#else>
              <strong>${cartLine.getItemTypeDescription()!}</strong>
            </#if>
          </td>
          <td><@ofbizCurrency amount=cartLine.getDisplayItemSubTotal() isoCode=shoppingCart.getCurrency()/></td>
        </tr>
          <#if cartLine.getReservStart()??>
          <tr><td>&nbsp;</td><td colspan="2">(${cartLine.getReservStart()?string("yyyy-MM-dd")}, ${cartLine.getReservLength()} <#if cartLine.getReservLength() == 1>${uiLabelMap.CommonDay}<#else>${uiLabelMap.CommonDays}</#if>)</td></tr>
          </#if>
        </#list>
      </tbody>
    </table>
    <a href="<@ofbizUrl>checkoutoptions</@ofbizUrl>" class="btn btn-outline-primary float-right">${uiLabelMap.OrderCheckoutQuick}</a>
  <#else>
    <p>${uiLabelMap.OrderShoppingCartEmpty}</p>
  </#if>
  </div>
</div>
