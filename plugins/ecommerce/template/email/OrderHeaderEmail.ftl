<#if baseEcommerceSecureUrl??><#assign urlPrefix = baseEcommerceSecureUrl/></#if>

<div class="screenlet">
  <h3>${uiLabelMap.OrderOrder}</h3>
  <ul>
    <#if orderHeader?has_content>
      <li>
        ${uiLabelMap.OrderOrder}:
        <strong>${orderHeader.orderId}</strong>
      </li>
    </#if>

    <#if localOrderReadHelper?? && orderHeader?has_content>
      <#assign displayParty = localOrderReadHelper.getPlacingParty()!/>
      <#if displayParty?has_content>
        <#assign displayPartyNameResult = dispatcher.runSync("getPartyNameForDate", Static["org.apache.ofbiz.base.util.UtilMisc"].toMap("partyId", displayParty.partyId, "compareDate", orderHeader.orderDate, "userLogin", userLogin))/>
        <li>${uiLabelMap.PartyName}: ${(displayPartyNameResult.fullName)?default("[Name Not Found]")}</li>
      </#if>
    </#if>

    <li>
      ${uiLabelMap.CommonStatus}:
      <#if orderHeader?has_content>
        ${localOrderReadHelper.getStatusString(locale)}
      <#else>
        ${uiLabelMap.OrderNotYetOrdered}
      </#if>
    </li>

    <#if orderHeader?has_content>
      <li>${uiLabelMap.CommonDate}: ${orderHeader.orderDate?string("dd. MMMM yyyy.")}</li>
    </#if>
  </ul>
</div>

<#if paymentMethods?has_content || paymentMethodType?has_content || billingAccount?has_content>
  <div class="screenlet">
    <h3>${uiLabelMap.AccountingPaymentInformation}</h3>
    <ul>
      <#if !paymentMethod?has_content && paymentMethodType?has_content>
        <li>
          <#if "EXT_OFFLINE" == paymentMethodType.paymentMethodTypeId>
            <strong>${uiLabelMap.AccountingOfflinePayment}</strong>
            <#if orderHeader?has_content && paymentAddress?has_content>
              <br/>
              ${uiLabelMap.OrderSendPaymentTo}:<br/>
              <#if paymentAddress.toName?has_content>${paymentAddress.toName}<br/></#if>
              <#if paymentAddress.attnName?has_content>${uiLabelMap.CommonAttn}: ${paymentAddress.attnName}<br/></#if>
              ${setContextField("postalAddress", paymentAddress)}
              ${screens.render("component://party/widget/partymgr/PartyScreens.xml#postalAddressHtmlFormatter")}
              <em>${uiLabelMap.EcommerceBeSureToIncludeYourOrderNb}</em>
            </#if>
          <#else>
            <strong>${paymentMethodType.get("description",locale)}</strong>
          </#if>
        </li>
      </#if>

      <#if paymentMethods?has_content>
        <#list paymentMethods as paymentMethod>
          <#if "CREDIT_CARD" == paymentMethod.paymentMethodTypeId>
            <#assign creditCard = paymentMethod.getRelatedOne("CreditCard", false)>
            <#assign formattedCardNumber = Static["org.apache.ofbiz.party.contact.ContactHelper"].formatCreditCard(creditCard)>
          <#elseif "GIFT_CARD" == paymentMethod.paymentMethodTypeId>
            <#assign giftCard = paymentMethod.getRelatedOne("GiftCard", false)>
          <#elseif "EFT_ACCOUNT" == paymentMethod.paymentMethodTypeId>
            <#assign eftAccount = paymentMethod.getRelatedOne("EftAccount", false)>
          </#if>

          <#if "CREDIT_CARD" == paymentMethod.paymentMethodTypeId && creditCard?has_content>
            <li>${uiLabelMap.AccountingCreditCard}: ${formattedCardNumber}</li>
          <#elseif "GIFT_CARD" == paymentMethod.paymentMethodTypeId && giftCard?has_content>
            <li>${uiLabelMap.AccountingGiftCard}: ${giftCard.cardNumber!}</li>
          <#elseif "EFT_ACCOUNT" == paymentMethod.paymentMethodTypeId && eftAccount?has_content>
            <li>${uiLabelMap.AccountingEFTAccount}: ${eftAccount.bankName!}</li>
          </#if>
        </#list>
      </#if>

      <#if billingAccount?has_content>
        <li>${uiLabelMap.AccountingBillingAccount} #${billingAccount.billingAccountId!} - ${billingAccount.description!}</li>
      </#if>

      <#if (customerPoNumberSet?has_content)>
        <li>
          ${uiLabelMap.OrderPurchaseOrderNumber}:
          <#list customerPoNumberSet as customerPoNumber>${customerPoNumber!} </#list>
        </li>
      </#if>
    </ul>
  </div>
</#if>

<#if orderItemShipGroups?has_content>
  <div class="screenlet">
    <h3>${uiLabelMap.OrderShippingInformation}</h3>
    <#assign groupIdx = 0>
    <#list orderItemShipGroups as shipGroup>
      <#if orderHeader?has_content>
        <#assign shippingAddress = shipGroup.getRelatedOne("PostalAddress", false)!>
      <#else>
        <#assign shippingAddress = cart.getShippingAddress(groupIdx)!>
      </#if>

      <#if shippingAddress?has_content>
        <ul>
          <li>
            <#if shippingAddress.toName?has_content><strong>${shippingAddress.toName}</strong><br/></#if>
            <#if shippingAddress.attnName?has_content>${uiLabelMap.CommonAttn}: ${shippingAddress.attnName}<br/></#if>
            ${setContextField("postalAddress", shippingAddress)}
            ${screens.render("component://party/widget/partymgr/PartyScreens.xml#postalAddressHtmlFormatter")}
          </li>
        </ul>
      </#if>


      <ul>
          <li>
              <br>${uiLabelMap.OrderMethod}:<br>
              <#if orderHeader?has_content>
                  <#assign shipmentMethodType = shipGroup.getRelatedOne("ShipmentMethodType", false)!>
                  <#assign carrierPartyId = shipGroup.carrierPartyId!>
              <#else>
                  <#assign shipmentMethodType = cart.getShipmentMethodType(groupIdx)!>
                  <#assign carrierPartyId = cart.getCarrierPartyId(groupIdx)!>
              </#if>
              <#--<#if carrierPartyId?? && carrierPartyId != "_NA_">${carrierPartyId!}</#if>-->
              <strong>${(shipmentMethodType.description)?default("N/A")}</strong>
          </li>
          <#if shippingAccount??>
            <li>
                ${uiLabelMap.AccountingUseAccount}: ${shippingAccount}
            </li>
          </#if>
          <#-- shipping instructions -->
          <#if orderHeader?has_content>
            <#assign shippingInstructions = shipGroup.shippingInstructions!>
          <#else>
            <#assign shippingInstructions =  cart.getShippingInstructions(groupIdx)!>
          </#if>
          <#if shippingInstructions?has_content>
            <li>
              <br>${uiLabelMap.OrderInstructions}:<br>
              <em>${shippingInstructions}</em>
            </li>
          </#if>
      </ul>


      <#assign groupIdx = groupIdx + 1>
    </#list>
  </div>
</#if>
