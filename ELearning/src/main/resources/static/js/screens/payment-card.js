(function () {
  "use strict";

  function showError(messageElement, message) {
    messageElement.textContent = message;
    messageElement.hidden = false;
  }

  function requestHeaders(form) {
    var headers = { "Content-Type": "application/json" };
    var csrfInput = form.querySelector("input[name='_csrf']");
    if (csrfInput && csrfInput.value) {
      headers["X-CSRF-TOKEN"] = csrfInput.value;
    }
    return headers;
  }

  async function createOrder(form) {
    var response = await window.fetch("/api/payments/toss/ready", {
      method: "POST",
      headers: requestHeaders(form),
      body: JSON.stringify({
        productCode: form.querySelector("input[name='productCode']").value,
        idempotencyToken: form.querySelector("input[name='idempotencyToken']").value
      })
    });
    var payload = await response.json();
    if (!response.ok || !payload.success) {
      throw new Error(payload.error && payload.error.detail ? payload.error.detail : "주문을 생성하지 못했습니다.");
    }
    return payload.data;
  }

  document.addEventListener("DOMContentLoaded", function () {
    var screen = document.querySelector("[data-toss-client-key]");
    var form = document.querySelector("[data-toss-card-form]");
    var submitButton = document.querySelector("[data-toss-payment-submit]");
    var errorMessage = document.querySelector("[data-toss-payment-error]");

    if (!screen || !form || !submitButton || !errorMessage) {
      return;
    }
    if (screen.dataset.premiumActive === "true") {
      return;
    }

    submitButton.addEventListener("click", async function () {
      var clientKey = screen.dataset.tossClientKey;
      if (!clientKey || !window.TossPayments) {
        showError(errorMessage, "토스페이먼츠 결제 설정을 불러오지 못했습니다.");
        return;
      }

      submitButton.disabled = true;
      errorMessage.hidden = true;
      try {
        var order = await createOrder(form);
        var tossPayments = window.TossPayments(clientKey);
        var payment = tossPayments.payment({ customerKey: order.customerKey });

        await payment.requestPayment({
          method: "CARD",
          amount: {
            currency: "KRW",
            value: order.amount
          },
          orderId: order.orderId,
          orderName: order.orderName,
          successUrl: window.location.origin + "/payments/toss/success",
          failUrl: window.location.origin + "/payments/toss/fail"
        });
      } catch (error) {
        showError(errorMessage, error && error.message ? error.message : "결제창을 열지 못했습니다. 다시 시도해주세요.");
        submitButton.disabled = false;
      }
    });
  });
})();
