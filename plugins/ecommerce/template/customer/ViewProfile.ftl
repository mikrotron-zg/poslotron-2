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

<#if party??>
  <#if person??>
    <div class="card">
      <div class="card-header">
        <div class="row">
        <div class="col-lg-3">
          <strong>${uiLabelMap.PartyPersonalInformation}</strong>
        </div>
        <div class="col-lg-9 text-right">
        <a href="<@ofbizUrl>editperson</@ofbizUrl>">
        <#if person??>${uiLabelMap.CommonUpdate}<#else>${uiLabelMap.CommonCreate}</#if></a>
        </div>
        </div>
      </div>
      <div class="card-body">
        <#if person??>
        <div class="row">
          <div class="col-lg-6">
          <dl class="row">
            <dt class="col-lg-2">${uiLabelMap.PartyName}</dt>
            <dd class="col-lg-10">
              ${person.firstName!}
              ${person.lastName!}
            </dd>
            <#if person.nickname?has_content>
              <dt class="col-lg-2">${uiLabelMap.PartyNickName}</dt>
              <dd class="col-lg-10">${person.nickname}</dd>
            </#if>
            <#if person.gender?has_content>
              <dt class="col-lg-2">${uiLabelMap.PartyGender}</dt>
              <dd class="col-lg-10">${person.gender}</dd>
            </#if>
          <#if person.birthDate??>
            <dt class="col-lg-2">${uiLabelMap.PartyBirthDate}</dt>
            <dd class="col-lg-10">${person.birthDate.toString()}</dd>
          </#if>
          <#if person.height??>
            <dt class="col-lg-2">${uiLabelMap.PartyHeight}</dt>
            <dd class="col-lg-10">${person.height}</dd>
          </#if>
          <#if person.weight??>
            <dt class="col-lg-2">${uiLabelMap.PartyWeight}</dt>
            <dd class="col-lg-10">${person.weight}</dd>
          </#if>
          <#if person.maritalStatusEnumId?has_content>
            <#assign maritalStatus = EntityQuery.use(delegator).from("Enumeration").where("enumId", person.maritalStatusEnumId!).cache(true).queryOne()!>
            <dt class="col-lg-2">${uiLabelMap.PartyMaritalStatus}</dt>
            <dd class="col-lg-10">${maritalStatus.description!person.maritalStatusEnumId}</dd>
          </#if>
        </dl>
        </div>
        <div class="col-lg-6">
          <dl class="row">
            <#if person.mothersMaidenName?has_content>
              <dt class="col-lg-3">${uiLabelMap.PartyMaidenName}</dt>
              <dd class="col-lg-9">${person.mothersMaidenName}</dd>
            </#if>
            <#if person.socialSecurityNumber?has_content>
              <dt class="col-lg-3">${uiLabelMap.PartySocialSecurityNumber}</dt>
              <dd class="col-lg-9">${person.socialSecurityNumber}</dd>
            </#if>
            <#if person.passportNumber?has_content>
              <dt class="col-lg-3">${uiLabelMap.PartyPassportNumber}</dt>
              <dd class="col-lg-9">${person.passportNumber}</dd>
            </#if>
            <#if person.passportExpireDate??>
              <dt class="col-lg-3">${uiLabelMap.PartyPassportExpireDate}</dt>
              <dd class="col-lg-9">${person.passportExpireDate.toString()}</dd>
            </#if>
            <#if person.totalYearsWorkExperience??>
              <dt class="col-lg-3">${uiLabelMap.PartyYearsWork}</dt>
              <dd class="col-lg-9">${person.totalYearsWorkExperience}</dd>
            </#if>
            <#if person.comments?has_content>
              <dt class="col-lg-3">${uiLabelMap.CommonComments}</dt>
              <dd class="col-lg-9">${person.comments}</dd>
            </#if>
          </dl>
        </div>
        </div>
        <#else>
          <label>${uiLabelMap.PartyPersonalInformationNotFound}</label>
        </#if>
        </div>
    </div>
  <#else>
    <#-- TODO: show party group data, including TAX ID -->
  </#if>

    <#-- ============================================================= -->
    <div class="card">
      <div class="card-header">
        <div class="row">
          <div class="col-lg-3"><strong>${uiLabelMap.PartyContactInformation}</strong></div>
          <#--<div class="col-lg-9 text-right"><a href="<@ofbizUrl>editcontactmech</@ofbizUrl>" class="card-link">${uiLabelMap.CommonCreate}</a></div>-->
        </div>
      </div>

      <div class="card-body">
      <#if partyContactMechValueMaps?has_content>
        <table class="table table-responsive-sm">
          <thead class="thead-light">
          <tr>
            <th>${uiLabelMap.PartyContactType}</th>
            <th>${uiLabelMap.CommonInformation}</th>
            <th></th>
          </tr>
          </thead>
          <#list partyContactMechValueMaps as partyContactMechValueMap>
            <#assign contactMech = partyContactMechValueMap.contactMech! />
            <#assign contactMechType = partyContactMechValueMap.contactMechType! />
            <#assign partyContactMech = partyContactMechValueMap.partyContactMech! />
              <tbody>
              <tr>
                <th>
                  ${contactMechType.get("description",locale)}
                </th>
                <td>
                  <#list partyContactMechValueMap.partyContactMechPurposes! as partyContactMechPurpose>
                    <#assign contactMechPurposeType = partyContactMechPurpose.getRelatedOne("ContactMechPurposeType", true) />
                      <#if contactMechPurposeType??>
                        <em>${contactMechPurposeType.get("description",locale)}</em>
                        <#if "SHIPPING_LOCATION" == contactMechPurposeType.contactMechPurposeTypeId && (profiledefs.defaultShipAddr)?default("") == contactMech.contactMechId>
                          <span class="font-weight-bold">(${uiLabelMap.EcommerceIsDefault})</span>
                        <#elseif "SHIPPING_LOCATION" == contactMechPurposeType.contactMechPurposeTypeId>
                          <form name="defaultShippingAddressForm" method="post" action="<@ofbizUrl>setprofiledefault/viewprofile</@ofbizUrl>">
                            <input type="hidden" name="productStoreId" value="${productStoreId}" />
                            <input type="hidden" name="defaultShipAddr" value="${contactMech.contactMechId}" />
                            <input type="hidden" name="partyId" value="${party.partyId}" />
                            <input type="submit" value="${uiLabelMap.EcommerceSetDefault}" class="btn btn-outline-secondary" />
                          </form>
                        </#if>
                        <br>
                      <#else>
                        ${uiLabelMap.PartyPurposeTypeNotFound}: "${partyContactMechPurpose.contactMechPurposeTypeId}"
                      </#if>
                      <#--<#if partyContactMechPurpose.thruDate??>(${uiLabelMap.CommonExpire}:${partyContactMechPurpose.thruDate.toString()})</#if>-->
                  </#list>
                  <#if contactMech.contactMechTypeId! = "POSTAL_ADDRESS">
                    <#assign postalAddress = partyContactMechValueMap.postalAddress! />
                    <div class="mt-2 font-weight-bold">
                      <#if postalAddress??>
                        <#if postalAddress.toName?has_content>${uiLabelMap.CommonTo}: ${postalAddress.toName}<br /></#if>
                        <#if postalAddress.attnName?has_content>${uiLabelMap.PartyAddrAttnName}: ${postalAddress.attnName}<br /></#if>
                        ${postalAddress.address1}<br />
                        <#if postalAddress.address2?has_content>${postalAddress.address2}<br /></#if>
                        ${postalAddress.city}<#if partyContactMechValueMap.stateProvinceGeoName?has_content>,&nbsp;${partyContactMechValueMap.stateProvinceGeoName}</#if>&nbsp;${postalAddress.postalCode!}
                        <#if partyContactMechValueMap.countryGeoName?has_content><br />${partyContactMechValueMap.countryGeoName}</#if>
                        <#if (!postalAddress.countryGeoId?has_content || postalAddress.countryGeoId! = "USA")>
                          <#assign addr1 = postalAddress.address1! />
                          <#if (addr1.indexOf(" ") > 0)>
                            <#assign addressNum = addr1.substring(0, addr1.indexOf(" ")) />
                            <#assign addressOther = addr1.substring(addr1.indexOf(" ")+1) />
                            <a target="_blank" href="${uiLabelMap.CommonLookupWhitepagesAddressLink}" class="linktext">(${uiLabelMap.CommonLookupWhitepages})</a>
                          </#if>
                        </#if>
                      <#else>
                        ${uiLabelMap.PartyPostalInformationNotFound}.
                      </#if>
                      </div>
                  <#elseif contactMech.contactMechTypeId! = "TELECOM_NUMBER">
                    <#assign telecomNumber = partyContactMechValueMap.telecomNumber!>
                    <div>
                    <#if telecomNumber??>
                      <strong>
                      ${telecomNumber.countryCode!}
                      <#if telecomNumber.areaCode?has_content>${telecomNumber.areaCode}-</#if>${telecomNumber.contactNumber!}
                      <#if partyContactMech.extension?has_content>ext&nbsp;${partyContactMech.extension}</#if>
                      <#if (!telecomNumber.countryCode?has_content || telecomNumber.countryCode = "011")>
                        <a target="_blank" href="${uiLabelMap.CommonLookupAnywhoLink}" class="linktext">${uiLabelMap.CommonLookupAnywho}</a>
                        <a target="_blank" href="${uiLabelMap.CommonLookupWhitepagesTelNumberLink}" class="linktext">${uiLabelMap.CommonLookupWhitepages}</a>
                      </#if>
                      </strong>
                    <#else>
                      ${uiLabelMap.PartyPhoneNumberInfoNotFound}.
                    </#if>
                    </div>
                  <#elseif contactMech.contactMechTypeId! = "EMAIL_ADDRESS">
                    <div>
                      <strong>${contactMech.infoString}</strong>
                    </div>
                  <#elseif contactMech.contactMechTypeId! = "WEB_ADDRESS">
                    <div>
                      ${contactMech.infoString}
                      <#assign openAddress = contactMech.infoString! />
                      <#if !openAddress.startsWith("http") && !openAddress.startsWith("HTTP")><#assign openAddress = "http://" + openAddress /></#if>
                      <a target="_blank" href="${openAddress}" class="linktext">(${uiLabelMap.CommonOpenNewWindow})</a>
                    </div>
                  <#else>
                    ${contactMech.infoString!}
                  </#if>
                    <#--<div>(${uiLabelMap.CommonUpdated}:&nbsp;${partyContactMech.fromDate.toString()})</div>
                  <#if partyContactMech.thruDate??><div>${uiLabelMap.CommonDelete}:&nbsp;${partyContactMech.thruDate.toString()}</div></#if>-->
                </td>
                <td>
                  <form name= "deleteContactMech_${contactMech.contactMechId}" method= "post" action= "<@ofbizUrl>deleteContactMech</@ofbizUrl>">
                    <input type= "hidden" name= "contactMechId" value= "${contactMech.contactMechId}"/>
                    <a href="<@ofbizUrl>editcontactmech?contactMechId=${contactMech.contactMechId}</@ofbizUrl>" class="btn btn-outline-secondary">${uiLabelMap.CommonUpdate}</a>
                    <#--<a href='javascript:document.deleteContactMech_${contactMech.contactMechId}.submit()' class='btn btn-outline-secondary'>${uiLabelMap.CommonExpire}</a>-->
                  </form>
                </td>
              </tr>
              </tbody>
          </#list>
        </table>
      <#else>
        <label>${uiLabelMap.PartyNoContactInformation}.</label>
      </#if>
      </div>
    </div>

    <#-- ============================================================= -->
    <div class="card">
      <div class="card-header">
        <div class="row">
          <div class="col">
            <strong>${uiLabelMap.CommonUsername}</strong>
          </div>
          <div class="col text-right">
            <a href="<@ofbizUrl>passwordChange</@ofbizUrl>">${uiLabelMap.PartyChangePassword}</a>
          </div>
        </div>
      </div>
      <div class="card-body">
        <dl>
        <dt>${uiLabelMap.CommonUsername}</dt>
        <dd>${userLogin.userLoginId}</dd>
        </dl>
      </div>
    </div>

    <#-- ============================================================= -->
    <#-- only 5 messages will show; edit the ViewProfile.groovy to change this number
    ${screens.render("component://ecommerce/widget/CustomerScreens.xml#messagelist-include")}-->

<#else>
    <#if userLogin??>
        <h3>${uiLabelMap.PartyNoPartyForCurrentUserName}: ${userLogin.userLoginId}</h3>
    </#if>
</#if>
