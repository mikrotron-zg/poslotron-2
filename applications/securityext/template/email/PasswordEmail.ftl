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

<#setting url_escaping_charset="UTF-8">

<html>
<head>
</head>
<body>
  <div>${uiLabelMap.SecurityExtThisEmailIsInResponseToYourRequestToHave} ${uiLabelMap.SecurityExtANew} ${uiLabelMap.SecurityExtPasswordSentToYou}.</div>
  <div>${uiLabelMap.SecurityExtIgnoreEmail}.</div>
  
  <br />
  <div>
      <#-- Pre-assign URL-encoded parameters -->
      <#assign username = userLogin.userLoginId?url('UTF-8')>
      <#assign tokenParam = token?url('UTF-8')>
      <#assign tenantParam = (tenantId?url('UTF-8'))!>
      <#assign siteParam = (webSiteId!'WebStore')?url('UTF-8')>
      <#assign baseUrl = baseSecureUrl!baseUrl!>
      
      <#-- Build the password reset URL -->
      <#assign passwordUrl = "${baseUrl}/login?USERNAME=${username}&TOKEN=${tokenParam}&forgotPwdFlag=true&requirePasswordChange=Y&tenantId=${tenantParam}&webSiteId=${siteParam}">
      
      <#-- Styled link that looks like a button -->
      <table border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td align="center" style="border-radius: 4px; background-color: #007bff;">
            <a href="${passwordUrl}" target="_blank" style="
              font-size: 16px;
              font-family: Arial, sans-serif;
              color: #ffffff;
              text-decoration: none;
              border-radius: 4px;
              padding: 12px 24px;
              border: 1px solid #007bff;
              display: inline-block;
              font-weight: bold;
            ">${uiLabelMap.ResetPassword}</a>
          </td>
        </tr>
      </table>
      <br />
      ${uiLabelMap.SecurityExtLinkOnce}.
  </div>
</body>
</html>
