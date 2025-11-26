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

<div>
  <div class="card">
    <div class="card-header">
      <strong>${uiLabelMap.OrderSalesHistory}</strong>
    </div>
    <div class="card-body">
      <table class="table table-responsive-sm" id="orderSalesHistory" summary="This table display order sales history.">
        <thead class="thead-light">
          <tr>
            <th>${uiLabelMap.CommonDate}</th>
            <th>${uiLabelMap.OrderOrder} ${uiLabelMap.CommonNbr}</th>
            <th>${uiLabelMap.CommonAmount}</th>
            <th>${uiLabelMap.CommonStatus}</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <#if orderHeaderList?has_content>
            <#list orderHeaderList as orderHeader>
              <#assign status = orderHeader.getRelatedOne("StatusItem", true) />
              <tr>
                <td>${orderHeader.orderDate?string["dd.MM.yyyy."]}</td>
                <td>${orderHeader.orderId}</td>
                <td><@ofbizCurrency amount=orderHeader.grandTotal isoCode=orderHeader.currencyUom /></td>
                <td>${status.get("description",locale)}</td>
                <#-- invoices -->
                <#assign invoices = EntityQuery.use(delegator).from("OrderItemBilling").where("orderId", orderHeader.orderId).orderBy("invoiceId").queryList()!/>
                <#assign distinctInvoiceIds = Static["org.apache.ofbiz.entity.util.EntityUtil"].getFieldListFromEntityList(invoices, "invoiceId", true)>
                <td>
                  <a href="<@ofbizUrl>orderstatus?orderId=${orderHeader.orderId}</@ofbizUrl>" class="button">
                    ${uiLabelMap.CommonView}
                  </a>
                </td>
              </tr>
            </#list>
          <#else>
            <tr><td colspan="6">${uiLabelMap.OrderNoOrderFound}</td></tr>
          </#if>
        </tbody>
      </table>
    </div>
  </div>
</div>
