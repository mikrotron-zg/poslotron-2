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

<html>
<head>
    <title>${uiLabelMap.OrderCustRequestConfirmation}</title>
    <style type="text/css">
        body {
            font-family: Arial, Helvetica, sans-serif;
            font-size: 12pt;
            color: #000000;
        }
        .header {
            background-color: #f0f0f0;
            padding: 10px;
            border-bottom: 2px solid #333;
        }
        .content {
            padding: 20px;
        }
        .footer {
            font-size: 10pt;
            color: #666666;
            padding: 10px;
            border-top: 1px solid #999999;
        }
        .label {
            font-weight: bold;
        }
        .value {
            margin-bottom: 10px;
        }
        table.emailTable {
            width: 100%;
            border-collapse: collapse;
            margin-top: 15px;
        }
        table.emailTable th,
        table.emailTable td {
            border-bottom: 1px solid #999999;
            padding: 8px;
            vertical-align: top;
            text-align: left;
        }
        table.emailTable th {
            background-color: #f0f0f0;
            font-weight: bold;
        }
    </style>
</head>
<body>

<div class="header">
    <h2>${uiLabelMap.OrderCustRequestConfirmation}</h2>
</div>

<div class="content">
    <p>${uiLabelMap.OrderCustRequestConfirmationIntro}</p>
    
    <div class="value">
        <span class="label">${uiLabelMap.OrderCustRequestId}:</span>
        ${custRequestId!}
    </div>
    
    <div class="value">
        <span class="label">${uiLabelMap.CommonStatus}:</span>
        ${custRequest.statusId!}
    </div>
    
    <div class="value">
        <span class="label">${uiLabelMap.OrderCustRequestDate}:</span>
        ${custRequest.custRequestDate!}
    </div>
    
    <#if custRequest.custRequestName?has_content>
    <div class="value">
        <span class="label">${uiLabelMap.OrderCustRequestName}:</span>
        ${custRequest.custRequestName!}
    </div>
    </#if>
    
    <#if custRequest.description?has_content>
    <div class="value">
        <span class="label">${uiLabelMap.CommonDescription}:</span>
        ${custRequest.description!}
    </div>
    </#if>

    <#if note?has_content>
    <div class="value">
        <span class="label">${uiLabelMap.CommonNote}:</span>
        ${note!}
    </div>
    </#if>

    <p>${uiLabelMap.OrderCustRequestConfirmationOutro}</p>
</div>

<div class="footer">
    <p>${uiLabelMap.OrderCustRequestEmailFooter}</p>
</div>

</body>
</html>

</#escape>
