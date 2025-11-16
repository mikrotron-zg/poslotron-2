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

<#if getUsername>
<script type="application/javascript">
  //<![CDATA[
  lastFocusedName = null;
  function setLastFocused(formElement) {
    lastFocusedName = formElement.name;
  }
  function clickUsername() {
    if (document.getElementById('UNUSEEMAIL').checked) {
      if ("UNUSEEMAIL" == lastFocusedName) {
        jQuery('#PASSWORD').focus();
      } else if ("PASSWORD" == lastFocusedName) {
        jQuery('#UNUSEEMAIL').focus();
      } else {
        jQuery('#PASSWORD').focus();
      }
    }
  }
  function changeEmail() {
    if (document.getElementById('UNUSEEMAIL').checked) {
      document.getElementById('USERNAME').value = jQuery('#CUSTOMER_EMAIL').val();
    }
  }
  function setEmailUsername() {
    if (document.getElementById('UNUSEEMAIL').checked) {
      document.getElementById('USERNAME').value = jQuery('#CUSTOMER_EMAIL').val();
      // don't disable, make the browser not submit the field: document.getElementById('USERNAME').disabled=true;
    } else {
      document.getElementById('USERNAME').value = '';
      // document.getElementById('USERNAME').disabled=false;
    }
  }
  function hideShowUsaStates() {
    var customerStateElement = document.getElementById('newuserform_stateProvinceGeoId');
    var customerCountryElement = document.getElementById('newuserform_countryGeoId');
    if ("USA" == customerCountryElement.value || "UMI" == customerCountryElement.value) {
      customerStateElement.style.display = "block";
    } else {
      customerStateElement.style.display = "none";
    }
  }
  function hideShowSubmit() {
    if (document.getElementById('PRIVACY').checked) {
      document.getElementById('SUBMITBTN').style.display = "inline";
      document.getElementById('SUBMITDISABLED').style.display = "none";
    } else {
      document.getElementById('SUBMITBTN').style.display = "none";
      document.getElementById('SUBMITDISABLED').style.display = "inline";
    }
  }
  //]]>
</script>
</#if>

<#------------------------------------------------------------------------------
NOTE: all page headings should start with an h2 tag, not an H1 tag, as 
there should generally always only be one h1 tag on the page and that 
will generally always be reserved for the logo at the top of the page.
------------------------------------------------------------------------------->
<div class="d-flex justify-content-center">
  <h2>${uiLabelMap.PartyRequestNewAccount}</h2>
</div>
<div class="d-flex justify-content-center">
  <h6>
    ${uiLabelMap.PartyAlreadyHaveAccount},
    <a href='<@ofbizUrl>checkLogin/main</@ofbizUrl>'>${uiLabelMap.CommonLoginHere}</a>
  </h6>
</div>
<div class="d-flex justify-content-center">
    <h6>
      ${uiLabelMap.RegisterCompany}
      <a href='<@ofbizUrl>newcompany</@ofbizUrl>'>${uiLabelMap.AtThisLink}</a>
    </h6>
</div>

<#macro fieldErrors fieldName>
  <#if errorMessageList?has_content>
    <#assign fieldMessages =
        Static["org.apache.ofbiz.base.util.MessageString"].getMessagesForField(fieldName, true, errorMessageList)>

        <#list fieldMessages as errorMsg>
          <div style="color:red; float:right; text-align:right; font-size:small; font-style:italic;">${errorMsg}</div>
        </#list>

  </#if>
</#macro>
<#macro fieldErrorsMulti fieldName1 fieldName2 fieldName3 fieldName4>
  <#if errorMessageList?has_content>
    <#assign fieldMessages =
        Static["org.apache.ofbiz.base.util.MessageString"].getMessagesForField(fieldName1, fieldName2,
        fieldName3, fieldName4, true, errorMessageList)>
  <ul>
    <#list fieldMessages as errorMsg>
      <li class="errorMessage">${errorMsg}</li>
    </#list>
  </ul>
  </#if>
</#macro>

<div class="d-flex justify-content-center">
<div class="card p-2 m-3">

<form method="post" action="<@ofbizUrl>createcustomer${previousParams}</@ofbizUrl>" id="newuserform" name="newuserform">

  <div class="card-block text-muted font-italic">
    ${uiLabelMap.CommonFieldsMarkedAreRequired}
  </div>

<div class="row">
  <div class="col-12">
    <fieldset>
      <legend>${uiLabelMap.PartyFullName}</legend>
      <input type="hidden" name="emailProductStoreId" value="${productStoreId}"/>

      <div class="row form-group">
        <div class="col-12 col-md-6">
          <label class="required-field" for="USER_FIRST_NAME">${uiLabelMap.PartyFirstName}</label>
          <@fieldErrors fieldName="USER_FIRST_NAME"/>
          <input type="text" name="USER_FIRST_NAME" id="USER_FIRST_NAME" value="${requestParameters.USER_FIRST_NAME!}"
            maxlength="50"class="form-control form-control-sm"/>
        </div>

        <div class="col-12 col-md-6">
          <label class="required-field" for="USER_LAST_NAME">${uiLabelMap.PartyLastName}</label>
          <@fieldErrors fieldName="USER_LAST_NAME"/>
          <input type="text" name="USER_LAST_NAME" id="USER_LAST_NAME" value="${requestParameters.USER_LAST_NAME!}"
            maxlength="50" class="form-control form-control-sm"/>
        </div>
      </div>

      <div class="row form-group">
        <div class="col-12">
          <label class="required-field" for="CUSTOMER_EMAIL">${uiLabelMap.PartyEmailAddress}</label>
          <@fieldErrors fieldName="CUSTOMER_EMAIL"/>
          <input type="text" inputmode="email" name="CUSTOMER_EMAIL" id="CUSTOMER_EMAIL" value="${requestParameters.CUSTOMER_EMAIL!}"
            maxlength="255"class="form-control form-control-sm" onchange="changeEmail()" onkeyup="changeEmail()"/>
        </div>
      </div>
    </fieldset>
    <hr/>
    <fieldset>
      <legend>${uiLabelMap.PartyShippingAddress}</legend>
      <div class="row form-group">
        <div class="col-12 col-md-6">
          <label class="required-field" for="CUSTOMER_ADDRESS1">${uiLabelMap.PartyAddressLine1}</label>
          <@fieldErrors fieldName="CUSTOMER_ADDRESS1"/>
          <input type="text" name="CUSTOMER_ADDRESS1" id="CUSTOMER_ADDRESS1"
            maxlength="255" value="${requestParameters.CUSTOMER_ADDRESS1!}" class="form-control form-control-sm"/>
        </div>
        <div class="col-12 col-md-6">
          <label for="CUSTOMER_ADDRESS2">${uiLabelMap.PartyAddressLine2}</label>
          <@fieldErrors fieldName="CUSTOMER_ADDRESS2"/>
            <input type="text" name="CUSTOMER_ADDRESS2" id="CUSTOMER_ADDRESS2"
              maxlength="255" value="${requestParameters.CUSTOMER_ADDRESS2!}" class="form-control form-control-sm"/>
        </div>
      </div>
      <div class="row form-group">
        <div class="col-12 col-md-6">
          <label class="required-field" for="CUSTOMER_CITY">${uiLabelMap.PartyCity}</label>
          <@fieldErrors fieldName="CUSTOMER_CITY"/>
          <input type="text" name="CUSTOMER_CITY" id="CUSTOMER_CITY" value="${requestParameters.CUSTOMER_CITY!}"
            maxlength="100" class="form-control form-control-sm"/>
        </div>
        <div class="col-12 col-md-6">
          <label class="required-field" for="CUSTOMER_POSTAL_CODE">${uiLabelMap.PartyZipCode}</label>
          <@fieldErrors fieldName="CUSTOMER_POSTAL_CODE"/>
          <input type="text" inputmode="numeric" name="CUSTOMER_POSTAL_CODE" id="CUSTOMER_POSTAL_CODE"
            value="${requestParameters.CUSTOMER_POSTAL_CODE!}" maxlength="60" class="form-control form-control-sm"/>
        </div>
      </div>
      <div class="row form-group">
        <div class="col-12 col-md-6">
          <label class="required-field" for="customerCountry">${uiLabelMap.CommonCountry}</label>
          <@fieldErrors fieldName="CUSTOMER_COUNTRY"/>
          <select name="CUSTOMER_COUNTRY" id="newuserform_countryGeoId" class="form-control form-control-sm">
            ${screens.render("component://common/widget/CommonScreens.xml#countries")}
            <#assign defaultCountryGeoId =
                Static["org.apache.ofbiz.entity.util.EntityUtilProperties"].getPropertyValue("general",
                "country.geo.id.default", delegator)>
            <option selected="selected" value="${defaultCountryGeoId}">
              <#assign countryGeo = delegator.findOne("Geo",Static["org.apache.ofbiz.base.util.UtilMisc"]
                  .toMap("geoId",defaultCountryGeoId), false)>
              ${countryGeo.get("geoName",locale)}
            </option>
          </select>
        </div>
      </div>
    </fieldset>
    <hr/>
    <fieldset>
      <legend>${uiLabelMap.PartyPhoneNumbers}</legend>
      <div class="row form-group">
      <div class="col-12 text-muted font-italic mb-3" style="max-width:600px;">
        <strong>${uiLabelMap.CommonNote}:</strong>${uiLabelMap.TelecomForShippingNote}
      </div>
      </div>
      <div class="row form-group">
      <div class="col-12 col-md-6 table-responsive">
        <table class="table"
            summary="Tabular form for entering multiple telecom numbers for different purposes.
            Each row allows user to enter telecom number for a purpose">
          <thead class="thead-light">
            <tr>
              <th>${uiLabelMap.CommonType}</th>
              <th>${uiLabelMap.PartyContactNumber}</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <th scope="row" style="vertical-align:middle;">${uiLabelMap.PartyMobilePhone}</th>
              <td>
                <input type="text" inputmode="tel" name="CUSTOMER_MOBILE_CONTACT" class="form-control form-control-sm"
                  value="${requestParameters.CUSTOMER_MOBILE_CONTACT!}"/>
              </td>
            </tr>
            <tr>
              <th scope="row" style="vertical-align:middle;">${uiLabelMap.PartyHomePhone}</th>
              <td>
                <input type="text" inputmode="tel" name="CUSTOMER_HOME_CONTACT" class="form-control form-control-sm"
                  value="${requestParameters.CUSTOMER_HOME_CONTACT!}"/>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      </div>
    </fieldset>
  </div>
</div>
<hr/>
<div class="row form-group">
  <div class="col-12">
  <fieldset>
    <legend><#if getUsername>${uiLabelMap.CommonUsername}</#if></legend>
    <#if getUsername>

      <#if !requestParameters.preferredUsername?has_content>
        <div class="form-check">
            <input type="checkbox" name="UNUSEEMAIL" class="form-check-input" id="UNUSEEMAIL" value="on"
                onclick="setEmailUsername();" onfocus="setLastFocused(this);"/>
            <label for="UNUSEEMAIL">${uiLabelMap.EcommerceUseEmailAddress}</label>
        </div>
      </#if>

      <div class="row form-group">
      <div class="col-12 col-md-6">
        <label class="required-field" for="USERNAME">${uiLabelMap.CommonUsername}</label>
        <@fieldErrors fieldName="USERNAME"/>
        <#if requestParameters.preferredUsername?has_content>
          <input type="text" name="showUserName" id="showUserName" value="${requestParameters.USERNAME!}"
              maxlength="255" disabled="disabled"/>
          <input type="hidden" name="USERNAME" id="USERNAME" value="${requestParameters.USERNAME!}"/>
        <#else>
          <input type="text" name="USERNAME" id="USERNAME" value="${requestParameters.USERNAME!}"
              maxlength="255" class="form-control form-control-sm" onfocus="clickUsername();" onchange="changeEmail();"/>
        </#if>
      </div>
      </div>
    </#if>
  </fieldset>
  </div>
  <div class="col-12">
    <fieldset>
      <legend>${uiLabelMap.CommonPassword}</legend>
      <#if createAllowPassword>
        <div class="row form-group">
        <div class="col-12 col-md-6">
          <label class="required-field" for="PASSWORD">${uiLabelMap.CommonPassword}</label>
          <@fieldErrors fieldName="PASSWORD"/>
          <input type="password" name="PASSWORD" class="form-control form-control-sm" autocomplete="off"
            maxlength"255" id="PASSWORD" onfocus="setLastFocused(this);"/>
        </div>
        <div class="col-12 col-md-6">
          <label class="required-field" for="CONFIRM_PASSWORD">${uiLabelMap.PartyRepeatPassword}</label>
          <@fieldErrors fieldName="CONFIRM_PASSWORD"/>
          <input type="password" class="form-control form-control-sm" name="CONFIRM_PASSWORD" id="CONFIRM_PASSWORD"
            maxlength"255" autocomplete="off" value="" maxlength="50"/>
        </div>
        </div>

        <div class="row form-group">
        <div class="col-12 col-md-6">
          <label for="PASSWORD_HINT">${uiLabelMap.PartyPasswordHint}</label>
          <@fieldErrors fieldName="PASSWORD_HINT"/>
          <input type="text" class="form-control form-control-sm" name="PASSWORD_HINT" id="PASSWORD_HINT"
              maxlength="255" value="${requestParameters.PASSWORD_HINT!}" maxlength="100"/>
        </div>
        </div>
      <#else>
        <div>
          <label>${uiLabelMap.PartyReceivePasswordByEmail}.</div>
        </div>
      </#if>
    </fieldset>
  </div>
  </div>
  <hr/>
  <div class="row form-group">
    <div class="col-12">
      <fieldset>
        <legend>${uiLabelMap.TermsOfUse}</legend>
        <div class="text-muted font-italic" style="max-width:600px;">
          ${uiLabelMap.GdprInfo}&nbsp;
          <a target="_blank" href='<@ofbizUrl>policies</@ofbizUrl>'>${uiLabelMap.AtThisLink}.</a>
          ${uiLabelMap.GdprNote}
        </div>
        <div class="form-check font-italic mt-3" style="max-width:600px;">
          <input type="checkbox" name="PRIVACY" id="PRIVACY" value="on" class="form-check-input"
            onclick="hideShowSubmit();" onfocus="setLastFocused(this);"/>
          <label for="PRIVACY">${uiLabelMap.GdprAccept}</label>
        </div>
      </fieldset>
    </div>
  </div>
</form>
</div>
</div>

<#------------------------------------------------------------------------------
To create a consistent look and feel for all buttons, input[type=submit], 
and a tags acting as submit buttons, all button actions should have a 
class name of "button". No other class names should be used to style 
button actions.
------------------------------------------------------------------------------->
<div class="d-flex justify-content-center">
  <a href="<@ofbizUrl>${donePage}</@ofbizUrl>" class="btn btn-outline-secondary btn-sm">
    ${uiLabelMap.CommonCancel}
  </a>
    &nbsp;
  <a href="javascript:document.getElementById('newuserform').submit()" class="btn btn-primary btn-sm"
    id="SUBMITBTN" style="display:none">
    ${uiLabelMap.CommonSave}
  </a>
  <a href="#" class="btn btn-secondary btn-sm" disabled id="SUBMITDISABLED"
    style="pointer-events: none; cursor: default; text-decoration: none;">
    ${uiLabelMap.CommonSave}
  </a>
</div>

<script type="application/javascript">
  //<![CDATA[
  hideShowUsaStates();
  //]]>
</script>
