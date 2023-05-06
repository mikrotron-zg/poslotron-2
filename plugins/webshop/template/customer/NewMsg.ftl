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

<div class="row justify-content-center">
  <div class="col-lg-4">
    <div class="card">
      <div class="card-header">
        <div class="boxlink">
          <#if "TRUE" == showMessageLinks?default("false")?upper_case>
            <a href="<@ofbizUrl>messagelist</@ofbizUrl>" class="submenutextright">${uiLabelMap.EcommerceViewList}</a>
          </#if>
        </div>
        <strong>${pageHeader}</strong>
      </div>
      <div class="card-body text-secondary">
        <form name="contactus" method="post" action="<@ofbizUrl>${submitRequest}</@ofbizUrl>" style="margin: 0;">
          <input type="hidden" name="partyIdFrom" value="${userLogin.partyId}"/>
          <input type="hidden" name="contactMechTypeId" value="WEB_ADDRESS"/>
          <input type="hidden" name="communicationEventTypeId" value="WEB_SITE_COMMUNICATI"/>
          <#if productStore?has_content>
            <input type="hidden" name="partyIdTo" value="${productStore.payToPartyId!}"/>
          </#if>
            <input type="hidden" name="note"
                  value="${Static["org.apache.ofbiz.base.util.UtilHttp"].getFullRequestUrl(request)}"/>
          <#if message?has_content>
            <input type="hidden" name="parentCommEventId" value="${communicationEvent.communicationEventId}"/>
            <#if (communicationEvent.origCommEventId?? && communicationEvent.origCommEventId?length > 0)>
              <#assign orgComm = communicationEvent.origCommEventId>
            <#else>
              <#assign orgComm = communicationEvent.communicationEventId>
            </#if>
            <input type="hidden" name="origCommEventId" value="${orgComm}"/>
          </#if>
          <div class="form-group">
            <label for="${uiLabelMap.CommonFrom}">${uiLabelMap.CommonFrom}</label>
            <div>
              ${sessionAttributes.autoName!} [${userLogin.partyId}]<br> 
              (${uiLabelMap.CommonNotYou}?&nbsp;
              <a href="<@ofbizUrl>autoLogout</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonClickHere}</a>)
            </div>
          </div>
          <#if partyIdTo?has_content>
            <#assign partyToName =
                Static["org.apache.ofbiz.party.party.PartyHelper"].getPartyName(delegator, partyIdTo, true)>
            <input type="hidden" name="partyIdTo" value="${partyIdTo}"/>
            <div class="form-group">
              <label for="${uiLabelMap.CommonTo}">${uiLabelMap.CommonTo}</label>
              </div>
              <div>
                ${partyToName?default("N/A")}
              </div>
            </div>
          </#if>
          <#assign defaultSubject = (communicationEvent.subject)?default("")>
          <#if (defaultSubject?length == 0)>
            <#assign replyPrefix = "RE: ">
            <#if parentEvent?has_content>
              <#if !parentEvent.subject?default("")?upper_case?starts_with(replyPrefix)>
                <#assign defaultSubject = replyPrefix>
              </#if>
              <#assign defaultSubject = defaultSubject + parentEvent.subject?default("")>
            </#if>
          </#if>
          <div class="form-group">
            <label for="${uiLabelMap.EcommerceSubject}">${uiLabelMap.EcommerceSubject}</label>
            <input type="text" class="required form-control form-control-sm" name="subject" size="20" value="${defaultSubject}"/>
          </div>
          <div class="form-group">
            <label for="${uiLabelMap.CommonMessage}">${uiLabelMap.CommonMessage} :</label>
            <textarea name="content" class="required form-control form-control-sm" rows="8"></textarea>
          </div>
          <div class="row">
            <div class="col-12 text-right">
              <input type="submit" class="btn btn-outline-primary" value="${uiLabelMap.CommonSend}"/>
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>
</div>
