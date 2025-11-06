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
        <strong>${uiLabelMap.CommonContactUs}</strong>
      </div>
      <script type="application/javascript">
        function reloadCaptcha(fieldName) {
          var captchaUri = "<@ofbizUrl>captcha.jpg?captchaCodeId=" + fieldName + "&amp;unique=_PLACEHOLDER_</@ofbizUrl>";
          var unique = Date.now();
          captchaUri = captchaUri.replace("_PLACEHOLDER_", unique);
          document.getElementById(fieldName).src = captchaUri;
        }
      </script>
      <div class="card-body text-secondary">
        <form id="contactForm" method="post" action="<@ofbizUrl>submitAnonContact</@ofbizUrl>">
          <input type="hidden" name="partyIdFrom" value="${(userLogin.partyId)!}"/>
          <input type="hidden" name="partyIdTo" value="${productStore.payToPartyId!}"/>
          <input type="hidden" name="contactMechTypeId" value="WEB_ADDRESS"/>
          <input type="hidden" name="communicationEventTypeId" value="WEB_SITE_COMMUNICATI"/>
          <input type="hidden" name="productStoreId" value="${productStore.productStoreId}"/>
          <input type="hidden" name="emailType" value="CONT_NOTI_EMAIL"/>

          <div class="form-group">
            <label for="${uiLabelMap.EcommerceSubject}">${uiLabelMap.EcommerceSubject}</label>
            <input type="text" name="subject" id="subject" class="required form-control form-control-sm" value="${requestParameters.subject!}"/>
          </div>
          <div class="form-group">
            <label for="${uiLabelMap.CommonMessage}">${uiLabelMap.CommonMessage}</label>
            <textarea name="content" id="message" class="required form-control form-control-sm" rows="8">
              ${requestParameters.content!}
            </textarea>
          </div>
          <div class="form-group">
            <label for="${uiLabelMap.FormFieldTitle_emailAddress}">${uiLabelMap.FormFieldTitle_emailAddress}</label>
            <input type="email" name="emailAddress" id="emailAddress" class="required form-control form-control-sm" value="${requestParameters.emailAddress!}"/>
          </div>
          <div class="form-row align-items-center">
            <div class="col-md-6 col-sm-12">
              <label for="${uiLabelMap.PartyFirstName}">${uiLabelMap.PartyFirstName}</label>
              <input type="text" name="firstName" id="firstName" class="required form-control form-control-sm" value="${requestParameters.firstName!}"/>
            </div>
            <div class="col-md-6 col-sm-12">
              <label for="${uiLabelMap.PartyLastName}">${uiLabelMap.PartyLastName}</label>
              <input type="text" name="lastName" id="lastName" class="required form-control form-control-sm" value="${requestParameters.lastName!}"/>
            </div>
          </div>
          <div class="form-row mt-4">
            <div>
            <label for="${uiLabelMap.CommonCaptchaCode}">${uiLabelMap.CommonCaptchaCode}</label>&nbsp;&nbsp;
            <img id="captchaImage" src="<@ofbizUrl>captcha.jpg?captchaCodeId=captchaImage&amp;unique=${nowTimestamp.getTime()}</@ofbizUrl>" alt=""/>
            &nbsp;&nbsp;
            <a href="javascript:reloadCaptcha('captchaImage');">${uiLabelMap.CommonReloadCaptchaCode}</a>
            </div>
          </div>
          <div class="form-group mt-4">
            <label for="${uiLabelMap.CommonVerifyCaptchaCode}">${uiLabelMap.CommonVerifyCaptchaCode}</label>
            <input type="text" autocomplete="off" maxlength="30" size="23" name="captcha" class="form-control form-control-sm"/>
          </div>
          <div class="row">
            <div class="col-12 text-right">
              <input type="submit" value="${uiLabelMap.CommonSubmit}" class="btn btn-outline-primary"/>
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>
</div>
