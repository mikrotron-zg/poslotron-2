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

<fo:block font-size="10pt">
  <#if "PURCHASE_ORDER" == orderHeader.getString("orderTypeId")>
      <#if supplierGeneralContactMechValueMap??>
          <#assign contactMech = supplierGeneralContactMechValueMap.contactMech>
          <fo:block font-family="NotoSans-Bold">${uiLabelMap.OrderPurchasedFrom}:</fo:block>
          <#assign postalAddress = supplierGeneralContactMechValueMap.postalAddress>
          <#if postalAddress?has_content>
              <fo:block text-indent="5mm">
                  <#if postalAddress.toName?has_content><fo:block>${postalAddress.toName}</fo:block></#if>
                  <#if postalAddress.attnName?has_content><fo:block>${postalAddress.attnName!}</fo:block></#if>
                  <fo:block>${postalAddress.address1!}</fo:block>
                  <#if postalAddress.address2?has_content><fo:block>${postalAddress.address2!}</fo:block></#if>
                  <fo:block>
                      <#assign stateGeo = (delegator.findOne("Geo", {"geoId", postalAddress.stateProvinceGeoId!}, false))! />
                      ${postalAddress.city}<#if stateGeo?has_content>, ${stateGeo.geoName!}</#if> ${postalAddress.postalCode!}
                  </fo:block>
                  <fo:block>
                      <#assign countryGeo = (delegator.findOne("Geo", {"geoId", postalAddress.countryGeoId!}, false))! />
                      <#if countryGeo?has_content>${countryGeo.geoName!}</#if>
                  </fo:block>
              </fo:block>                
          </#if>
      <#else>
          <#-- here we just display the name of the vendor, since there is no address -->
          <#assign vendorParty = orderReadHelper.getBillFromParty()>
          <fo:block>
              <fo:inline font-family="NotoSans-Bold">${uiLabelMap.OrderPurchasedFrom}:</fo:inline> ${Static['org.apache.ofbiz.party.party.PartyHelper'].getPartyName(vendorParty)}
          </fo:block>
      </#if>
  </#if>

  <#-- list all postal addresses of the order in a table layout -->
  <fo:table table-layout="fixed" border-spacing="3pt">
      <fo:table-column column-width="3.75in"/>
      <fo:table-column column-width="3.75in"/>
      <fo:table-body>
          <fo:table-row>
              <#list orderContactMechValueMaps as orderContactMechValueMap>
                  <#assign contactMech = orderContactMechValueMap.contactMech>
                  <#assign contactMechPurpose = orderContactMechValueMap.contactMechPurposeType>
                  <#if "POSTAL_ADDRESS" == contactMech.contactMechTypeId>
                      <#assign postalAddress = orderContactMechValueMap.postalAddress>
                      <#assign contactMechPurposeTypeId = orderContactMechValueMap.orderContactMech.contactMechPurposeTypeId!>
                      <fo:table-cell>
                          <fo:block>
                              <fo:block font-family="NotoSans-Bold">${contactMechPurpose.get("description",locale)}:</fo:block>
                              <#if postalAddress?has_content>
                                  <#if postalAddress.toName?has_content><fo:block>${postalAddress.toName}</fo:block></#if>
                                  <#if postalAddress.attnName?has_content><fo:block>${uiLabelMap.CommonAttn}: ${postalAddress.attnName}</fo:block></#if>
                                  ${setContextField("postalAddress", postalAddress)}
                                  ${screens.render("component://party/widget/partymgr/PartyScreens.xml#postalAddressPdfFormatter")}
                              </#if>
                          </fo:block>
                      </fo:table-cell>
                  </#if>
              </#list>
          </fo:table-row>
      </fo:table-body>
  </fo:table>

  <fo:block space-after="5mm"/>

  <#if orderPaymentPreferences?has_content>
      <fo:block font-family="NotoSans-Bold">${uiLabelMap.AccountingPaymentInformation}:</fo:block>
      <#list orderPaymentPreferences as orderPaymentPreference>
          <fo:block text-indent="5mm">
              <#assign paymentMethodType = orderPaymentPreference.getRelatedOne("PaymentMethodType", false)!>
              <#if (orderPaymentPreference?? && ("CREDIT_CARD" == orderPaymentPreference.getString("paymentMethodTypeId")) && (orderPaymentPreference.getString("paymentMethodId")?has_content))>
                  <#assign creditCard = orderPaymentPreference.getRelatedOne("PaymentMethod", false).getRelatedOne("CreditCard", false)>
                  ${Static["org.apache.ofbiz.party.contact.ContactHelper"].formatCreditCard(creditCard)}
              <#else>
                  ${paymentMethodType.get("description",locale)!}
              </#if>
          </fo:block>
      </#list>
  </#if>
  <#if "SALES_ORDER" == orderHeader.getString("orderTypeId") && shipGroups?has_content>
      <fo:block font-family="NotoSans-Bold">${uiLabelMap.OrderShipmentInformation}:</fo:block>
      <#list shipGroups as shipGroup>
          <fo:block text-indent="5mm">
              <#if shipGroups.size() gt 1>${shipGroup.shipGroupSeqId} - </#if>
              <#if (shipGroup.shipmentMethodTypeId)??>
                  ${(shipGroup.getRelatedOne("ShipmentMethodType", false).get("description", locale))?default(shipGroup.shipmentMethodTypeId)}
              </#if>
              <#if (shipGroup.shipAfterDate)?? || (shipGroup.shipByDate)??>
                  <#if (shipGroup.shipAfterDate)??> - ${uiLabelMap.OrderShipAfterDate}: ${Static["org.apache.ofbiz.base.util.UtilDateTime"].toDateString(shipGroup.shipAfterDate)}</#if><#if (shipGroup.shipByDate)??> - ${uiLabelMap.OrderShipBeforeDate}: ${Static["org.apache.ofbiz.base.util.UtilDateTime"].toDateString(shipGroup.shipByDate)}</#if>
              </#if>
          </fo:block>
      </#list>
  </#if>

  <#if orderTerms?has_content && orderTerms.size() gt 0>
      <fo:block font-family="NotoSans-Bold">${uiLabelMap.OrderOrderTerms}:</fo:block>
      <#list orderTerms as orderTerm>
          <fo:block text-indent="5mm">
              ${orderTerm.getRelatedOne("TermType", false).get("description",locale)} ${orderTerm.termValue?default("")} ${orderTerm.termDays?default("")} ${orderTerm.textValue?default("")}
          </fo:block>
      </#list>
  </#if>

  <fo:block space-after="5mm"/>
</fo:block>
</#escape>
