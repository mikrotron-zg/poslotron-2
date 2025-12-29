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
        <strong>${uiLabelMap.FiscalB2BInfoMessage}</strong>
      </div>
      <hr/><br/>
      <div class="form-row">
        <label for="poNumber">${uiLabelMap.OrderPurchaseOrderNumber}</label>
        <div class="form-field"><input type="text" name="poNumber" id="poNumber" value="" size="50" maxlength="255" /></div>
      </div>
    <#else> <#-- Solo API -->
      <div class="form-row">
        <strong>${uiLabelMap.FiscalB2CInfoMessage}</strong>
      </div>
    </#if>
      <br/>
      <div class="form-row">
        <div class="form-field">
          <input type="hidden" name="isPayed" id="isPayedHidden" value="Y"/>
          <input type="checkbox" id="isPayed" checked="checked" onchange="document.getElementById('isPayedHidden').value = this.checked ? 'Y' : 'N'" />
          <label for="isPayed" style="display: inline; margin-left: 5px;">${uiLabelMap.AccountingInvoicePaid}</label>
        </div>
      </div>
      <div class="form-row">
      <input id="submitAddFiscalInvoice" type="button" value="${uiLabelMap.FiscalIssueInvoice}" class="large-button" style="display:none"/>
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

<#assign fiscalIssueInvoiceLabel = StringUtil.wrapString(uiLabelMap.FiscalIssueInvoice) />
<script type="application/javascript">
  jQuery(document).ready( function() {
    jQuery("#newFiscalInvoiceForm").dialog({autoOpen: false, modal: true, width: 500,
      buttons: {
      '${fiscalIssueInvoiceLabel}': function() {
        var dialog = jQuery(this);
        var addFiscalInvoice = jQuery("#addFiscalInvoice");
        var buttons = dialog.dialog('option', 'buttons');
        
        // Ensure isPayed value is set correctly before submission
        var isPayedCheckbox = jQuery("#isPayed");
        var isPayedHidden = jQuery("#isPayedHidden");
        isPayedHidden.val(isPayedCheckbox.prop('checked') ? 'Y' : 'N');
        
        // Disable all buttons
        dialog.dialog('option', 'buttons', {});
        dialog.find('.ui-dialog-buttonpane button').prop('disabled', true);
        
        // Show loading message
        jQuery("<p style='text-align: center;'><img src='/images/ajax-loader.gif' alt='Loading...'/> ${uiLabelMap.CommonUpdatingData}</p>").insertBefore(addFiscalInvoice);
        
        // Submit form
        addFiscalInvoice.submit();
      },
      '${uiLabelMap.CommonClose}': function() {
        jQuery(this).dialog('close');
        }
      }
      });
  });
</script>