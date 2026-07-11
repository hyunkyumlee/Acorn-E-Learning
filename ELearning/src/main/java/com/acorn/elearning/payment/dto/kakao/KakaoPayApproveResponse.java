package com.acorn.elearning.payment.dto.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoPayApproveResponse(
        String aid,
        String tid,
        String cid,
        @JsonProperty("partner_order_id") String partnerOrderId,
        @JsonProperty("partner_user_id") String partnerUserId,
        @JsonProperty("payment_method_type") String paymentMethodType,
        @JsonProperty("item_name") String itemName,
        Integer quantity,
        @JsonProperty("approved_at") String approvedAt
) {}
