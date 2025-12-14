<#if baseEcommerceSecureUrl??><#assign urlPrefix = baseEcommerceSecureUrl/></#if>
<#assign currencyIso = currencyUomId!((orderHeader.currencyUomId)!"USD")>

<div class="screenlet">
  <h3>${uiLabelMap.OrderOrderItems}</h3>

  <table class="emailTable">
    <thead>
      <tr>
        <th>${uiLabelMap.OrderProduct}</th>
        <th class="amount">${uiLabelMap.OrderQtyOrdered}</th>
        <th class="amount">${uiLabelMap.EcommerceUnitPrice}</th>
        <th class="amount">${uiLabelMap.CommonSubtotal}</th>
      </tr>
    </thead>
    <tbody>
      <#list (orderItems![]) as orderItem>
        <tr>
          <#if !orderItem.productId?? || "_?_" == orderItem.productId>
            <td>${orderItem.itemDescription?default("")}</td>
          <#else>
            <td>${orderItem.productId} - ${orderItem.itemDescription?default("")}</td>
          </#if>
          <td class="amount">${orderItem.quantity?string.number}</td>
          <td class="amount"><@ofbizCurrency amount=orderItem.unitPrice*1.25 isoCode=currencyIso/></td>
          <td class="amount">
            <#if workEfforts??>
              <#assign rentalQuantity = 1>
              <#list workEfforts as workEffort>
                <#if workEffort.workEffortId == orderItem.orderItemSeqId>
                  <#assign rentalQuantity = localOrderReadHelper.getWorkEffortRentalQuantity(workEffort)>
                  <#break>
                </#if>
              </#list>
              <@ofbizCurrency amount=localOrderReadHelper.getOrderItemTotal(orderItem)*rentalQuantity isoCode=currencyIso/>
            <#else>
              <@ofbizCurrency amount=localOrderReadHelper.getOrderItemTotal(orderItem) isoCode=currencyIso/>
            </#if>
          </td>
        </tr>
      </#list>

      <#if orderItems?size == 0 || !orderItems?has_content>
        <tr>
          <td colspan="4">${uiLabelMap.OrderSalesOrderLookupFailed}</td>
        </tr>
      </#if>
    </tbody>
    <tfoot>
      <tr>
        <th colspan="3">${uiLabelMap.CommonSubtotal}</th>
        <td class="amount"><@ofbizCurrency amount=(orderSubTotal!0)*1.25 isoCode=currencyIso/></td>
      </tr>
      <#list (headerAdjustmentsToShow![]) as orderHeaderAdjustment>
        <tr>
          <th colspan="3">${localOrderReadHelper.getAdjustmentType(orderHeaderAdjustment)}</th>
          <td class="amount"><@ofbizCurrency amount=localOrderReadHelper.getOrderAdjustmentTotal(orderHeaderAdjustment) isoCode=currencyIso/></td>
        </tr>
      </#list>
      <tr>
        <th colspan="3">${uiLabelMap.OrderShippingAndHandling}</th>
        <td class="amount"><@ofbizCurrency amount=(orderShippingTotal!0)*1.25 isoCode=currencyIso/></td>
      </tr>
      <tr>
        <th colspan="3">${uiLabelMap.OrderGrandTotal}</th>
        <td class="amount"><@ofbizCurrency amount=(orderGrandTotal!0) isoCode=currencyIso/></td>
      </tr>
    </tfoot>
  </table>
</div>
