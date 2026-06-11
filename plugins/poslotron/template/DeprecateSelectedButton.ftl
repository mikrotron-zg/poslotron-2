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

<#--
    "Deprecate selected products" action button for the Product Deprecation
    screen. Rendered above the ListProductDeprecation multi-form by the
    platform-specific widget in CatalogAppExtScreens.xml.

    The button uses HTML5 form="ListProductDeprecation" so it submits the
    multi-form without having to live inside it.  OFBiz renders the form
    element with name="${formName}" and also adds id="${formName}" only when
    containerId is specified - but our form widget does not set containerId,
    so the safest cross-browser approach is to use JavaScript to find the
    form by name and call .submit() on it.
-->
<div style="margin-bottom:0.5em;text-align:right;">
  <button type="button"
          class="smallSubmit"
          onclick="(function(){
            var f = document.querySelector('form[name=\'ListProductDeprecation\']');
            if (f) { f.submit(); }
          })();">
    ${uiLabelMap.PoslotronDeprecateSelectedProducts}
  </button>
</div>
