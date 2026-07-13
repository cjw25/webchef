document.addEventListener("DOMContentLoaded", function () {
    const buttons = document.querySelectorAll(".subscription-pay-btn");

    buttons.forEach(function (button) {
        button.addEventListener("click", function () {
            requestPayment(button);
        });
    });

    async function requestPayment(button) {
        const planType = button.getAttribute("data-plan");
        const courseId = Number(button.getAttribute("data-course-id"));

        try {
            button.disabled = true;
            button.textContent = "결제 준비 중...";

            const response = await fetch("/api/payments/ready", {
                method: "POST",
                headers: createHeaders(),
                body: JSON.stringify({
                    courseId: courseId,
                    planType: planType
                })
            });

            const data = await response.json();

            if (!data.success) {
                alert(data.message);
                return;
            }

            const tossPayments = TossPayments(data.clientKey);

            tossPayments.requestPayment("카드", {
                amount: data.amount,
                orderId: data.orderId,
                orderName: data.orderName,
                customerName: data.customerName,
                customerEmail: data.customerEmail,
                successUrl: window.location.origin + "/payment/success",
                failUrl: window.location.origin + "/payment/fail"
            });

        } catch (error) {
            console.error(error);
            alert("결제 요청 중 오류가 발생했습니다.");
        } finally {
            button.disabled = false;
            button.textContent = "이 구독권으로 결제";
        }
    }

    function createHeaders() {
        const headers = {
            "Content-Type": "application/json"
        };

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        return headers;
    }
});