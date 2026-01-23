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

<#assign username = ""/>
<#if requestParameters.USERNAME?has_content>
  <#assign username = requestParameters.USERNAME/>
<#elseif autoUserLogin?has_content>
  <#assign username = autoUserLogin.userLoginId/>
</#if>

<#-- Check if this is a password reset (token in session) -->
<#assign isPasswordReset = sessionAttributes._PASSWORD_RESET_TOKEN_?has_content/>

<div class="d-flex justify-content-center">
  <div class="card p-6">
    <div class="card-header">
      <h3>${uiLabelMap.CommonPasswordChange}</h3>
    </div>
    <div class="card-block p-1 m-2">
      <#if isPasswordReset>
        <#-- Password reset form - no current password needed -->
        <form method="post" action="<@ofbizUrl>updatePassword</@ofbizUrl>" name="loginform">
          <input type="hidden" name="USERNAME" value="${username}"/>
          <input type="hidden" name="token" value="${sessionAttributes._PASSWORD_RESET_TOKEN_}"/>
          <div class="form-group">
            <label>${uiLabelMap.CommonUsername}: <strong>${username}</strong></label>
          </div>
          <div class="form-group">
            <label for="newPassword">${uiLabelMap.CommonNewPassword}</label>
            <input type="password" class="form-control" id="newPassword" name="newPassword" autocomplete="off" value=""/>
          </div>
          <div class="form-group">
            <label for="newPasswordVerify">${uiLabelMap.CommonNewPasswordVerify}</label>
            <input type="password" class="form-control" id="newPasswordVerify" name="newPasswordVerify" autocomplete="off" value=""/>
          </div>
          <div class="form-group">
            <input type="submit" class="btn btn-primary btn-block" value="${uiLabelMap.CommonUpdate}"/>
          </div>
        </form>
      <#else>
        <#-- Normal password change form -->
        <form method="post" action="<@ofbizUrl>login${previousParams}</@ofbizUrl>" name="loginform">
          <input type="hidden" name="requirePasswordChange" value="Y"/>
          <input type="hidden" name="USERNAME" value="${username}"/>
          <div class="form-group">
            <label>${uiLabelMap.CommonUsername}: ${username}</label>
            <#if autoUserLogin?has_content>
              <p>(${uiLabelMap.CommonNot} ${autoUserLogin.userLoginId}? <a href="<@ofbizUrl>${autoLogoutUrl}</@ofbizUrl>">${uiLabelMap.CommonClickHere}</a>)</p>
            </#if>
          </div>
          <div class="form-group">
            <label for="password">${uiLabelMap.CommonPassword}</label>
            <input type="password" class="form-control" id="password" name="PASSWORD" autocomplete="off" value=""/>
          </div>
          <div class="form-group">
            <label for="newPassword">${uiLabelMap.CommonNewPassword}</label>
            <input type="password" class="form-control" id="newPassword" name="newPassword" autocomplete="off" value=""/>
          </div>
          <div class="form-group">
            <label for="newPasswordVerify">${uiLabelMap.CommonNewPasswordVerify}</label>
            <input type="password" class="form-control" id="newPasswordVerify" name="newPasswordVerify" autocomplete="off" value=""/>
          </div>
          <div class="form-group">
            <input type="submit" class="btn btn-primary btn-block" value="${uiLabelMap.CommonLogin}"/>
          </div>
        </form>
      </#if>
    </div>
  </div>
</div>

<script type="application/javascript">
  <#if autoUserLogin?has_content>document.loginform.PASSWORD.focus();</#if>
  <#if !autoUserLogin?has_content>document.loginform.USERNAME.focus();</#if>
</script>
