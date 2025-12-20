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
<div id="newFiscalInvoiceForm" class="popup" style="display: none;">
  <form id="addFiscalInvoice" name="addFiscalInvoice" method="post" action="addFiscalInvoice" style="font-size:1.2em;">
    <input type="hidden" name="orderId" value="${orderId!}"/>
    <input type="hidden" name="partyId" value="${partyId!}"/>
    <input type="hidden" name="partyTaxId" value="${partyTaxId!}"/>
    <input type="hidden" name="fiscalInvoiceType" value="${fiscalInvoiceType!}"/>
    <input type="hidden" name="invoiceId" value=""/>
    <#if partyTaxId?has_content> <#-- e-racun -->
      <div class="form-row">
        ${uiLabelMap.FiscalB2BInfoMessage}
      </div>
      <hr/>
      <div class="form-row">
        ${orderId!} -- ${partyId!} -- <span id="displayInvoiceId"></span> -- ${partyTaxId!}
      </div>
    <#else> <#-- Solo API -->
      <div class="form-row">
        ${uiLabelMap.FiscalB2CInfoMessage}
      </div>
    </#if>
    <div class="form-row">
      <input id="submitAddFiscalInvoice" type="button" value="${uiLabelMap.CommonCreate}" class="large-button" style="display:none"/>
      <form action="">
        <input class="popup_closebox buttontext large-button" type="button" value="${uiLabelMap.CommonClose}" style="display:none"/>
      </form>
    </div>
    <style>
      .large-button {
        padding: 5px 10px !important;
        font-size: 14px !important;
        min-height: 30px !important;
      }
      .ui-dialog .ui-dialog-buttonpane .ui-dialog-buttonset button {
        padding: 5px 10px !important;
        font-size: 14px !important;
        min-height: 30px !important;
        margin: 5px !important;
      }
    </style>
  </form>
</div>

<script type="application/javascript">
  jQuery(document).ready( function() {
    jQuery("#newFiscalInvoiceForm").dialog({autoOpen: false, modal: true, width: 500,
      buttons: {
      '${uiLabelMap.CommonSubmit}': function() {
        var addFiscalInvoice = jQuery("#addFiscalInvoice");
        jQuery("<p>${uiLabelMap.CommonUpdatingData}</p>").insertBefore(addFiscalInvoice);
    addFiscalInvoice.submit();
      },
      '${uiLabelMap.CommonClose}': function() {
        jQuery(this).dialog('close');
        }
      }
      });
  });
</script>