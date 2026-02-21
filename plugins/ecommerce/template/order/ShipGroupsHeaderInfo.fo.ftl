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
  <#-- Load customer information using direct entity queries -->
  <#if shipGroups?has_content>
    <#assign firstShipGroup = shipGroups[0]>
    <#assign orderId = firstShipGroup.orderId>
    <#-- Get order header for order date -->
    <#assign orderHeader = delegator.findOne("OrderHeader", {"orderId", orderId}, false)!>
    <#-- Get customer (BILL_TO) from OrderRole -->
    <#assign customerRoles = delegator.findByAnd("OrderRole", {"orderId", orderId, "roleTypeId", "BILL_TO"}, null, false)!>
    <#if !customerRoles?has_content>
      <#assign customerRoles = delegator.findByAnd("OrderRole", {"orderId", orderId, "roleTypeId", "PLACING_CUSTOMER"}, null, false)!>
    </#if>
    <#if customerRoles?has_content>
      <#assign customerRole = customerRoles[0]>
      <#assign partyId = customerRole.partyId>
    </#if>
    <#-- Get party's general location address -->
    <#assign partyContactMechPurposes = delegator.findByAnd("PartyContactMechPurpose", {"partyId", partyId, "contactMechPurposeTypeId", "GENERAL_LOCATION"}, null, false)!>
    <#if partyContactMechPurposes?has_content>
      <#assign partyContactMechPurpose = partyContactMechPurposes[0]>
      <#assign contactMechId = partyContactMechPurpose.contactMechId>
      <#assign contactMech = delegator.findOne("ContactMech", {"contactMechId", contactMechId}, false)!>
      <#if contactMech?? && contactMech.contactMechTypeId == "POSTAL_ADDRESS">
        <#assign billingAddress = contactMech.getRelatedOne("PostalAddress", false)!>
      </#if>
    </#if>
  </#if>
  
  <#list shipGroups as shipGroup>
    <fo:table table-layout="fixed" font-size="12pt" margin-top="10mm" border-spacing="3pt">
      <fo:table-column column-width="95mm"/>
      <fo:table-body>
        <fo:table-row>
          <fo:table-cell>
            <fo:block font-size="15pt" font-family="NotoSans-Bold">
              ${uiLabelMap.OrderShipGroup} ${shipGroup.orderId}
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block font-family="NotoSans-Italic">
              ${uiLabelMap.FacilityShipping} #${shipGroup.shipGroupSeqId}
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block margin-top="10mm">
              <fo:block>${uiLabelMap.PartyAddrToName}: </fo:block>
              <#if partyId??>
                <#assign partyNameResult = dispatcher.runSync("getPartyNameForDate", Static["org.apache.ofbiz.base.util.UtilMisc"].toMap("partyId", partyId, "compareDate", orderHeader.orderDate?default(""), "userLogin", userLogin))/>
                <fo:block font-family="NotoSans-Bold">
                  ${partyNameResult.fullName?default("[${uiLabelMap.OrderPartyNameNotFound}]")}
                </fo:block>
              <#else>
                <fo:block>[${uiLabelMap.OrderPartyNameNotFound}]</fo:block>
              </#if>
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell>
            <fo:block>
              <#if billingAddress??>
                ${setContextField("postalAddress", billingAddress)}
                ${screens.render("component://party/widget/partymgr/PartyScreens.xml#postalAddressPdfFormatter")}
              <#else>
                <fo:block>[${uiLabelMap.PartyBillingAddressNotFound}]</fo:block>
              </#if>
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
      </fo:table-body>
    </fo:table>
    <#if shipGroup_has_next><fo:block break-before="page"/></#if>
  </#list>
</#escape>
