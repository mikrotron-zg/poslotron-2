<#--
This file is part of Poslotron project by Mikrotron d.o.o.
licensed under the Apache License, Version 2.0 (the
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

<div class="card">
  <div class="card-header">
    <b>${uiLabelMap.EcommerceAboutUs}</b>
  </div>

  <div class="card-body text-secondary">
  <#if logoImageUrl?has_content>
    <img src="<@ofbizContentUrl>${logoImageUrl}</@ofbizContentUrl>" overflow="hidden" height="40px" content-height="scale-to-fit" content-width="150px"/>
  </#if>
  <div>
    <#--${companyName}-->
  </div>
  <ul class="list-group list-group-flush">
    <li class="list-group-item">
      <b><#if (locale == "hr")>Tvrtka: <#else>Company: </#if></b>
      Mikrotron d.o.o.
    </li>
    <li class="list-group-item">
      <b><#if (locale == "hr")>Adresa: <#else>Address: </#if></b>
      Pakoštanska 5 K2-9,
      10000 Zagreb,
      <#if (locale == "hr")>Hrvatska<#else>Croatia</#if>
    </li>
    <li class="list-group-item"><b>OIB:</b> 43227166836</li>
    <li class="list-group-item"><b>VAT ID:</b> HR43227166836</li>
    <li class="list-group-item">
      <a href="tel:385017999194">
        <span class="bi-telephone-fill"></span> +385 (0)1 7999 194
      </a>
    </li>
    <li class="list-group-item">
      <a href="mailto:mikrotron@mikrotron.hr">
        <span class="bi-envelope-fill"></span> mikrotron@mikrotron.hr
      </a>
    </li>
    <li class="list-group-item">
      <a href="http://www.mikrotron.hr" target="_blank">
        <span class="bi-link-45deg"></span> www.mikrotron.hr
      </a>
    </li>
    <li class="list-group-item">
      <span class="bi-bank"></span> Privredna Banka Zagreb<br>
      <b>SWIFT:</b> PBZGHR2X<br>
      <b>IBAN:</b> HR8023400091110675464
    </li>
  </ul>
  </div>
</div>