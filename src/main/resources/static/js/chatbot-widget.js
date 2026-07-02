document.addEventListener("DOMContentLoaded", function () {
    const openBtn = document.getElementById("chatbotOpenBtn");
    const closeBtn = document.getElementById("chatbotCloseBtn");
    const resetBtn = document.getElementById("chatbotResetBtn");
    const panel = document.getElementById("chatbotPanel");
    const input = document.getElementById("chatbotInput");
    const sendBtn = document.getElementById("chatbotSendBtn");
    const messages = document.getElementById("chatbotMessages");
    const suggestButtons = document.querySelectorAll(".wc-suggest-btn");

    if (!openBtn || !closeBtn || !panel || !input || !sendBtn || !messages) {
        return;
    }

    openBtn.addEventListener("click", function () {
        panel.classList.add("is-open");
        panel.setAttribute("aria-hidden", "false");

        setTimeout(function () {
            input.focus();
        }, 150);
    });

    closeBtn.addEventListener("click", function () {
        closeChatbot();
    });

    if (resetBtn) {
        resetBtn.addEventListener("click", function () {
            resetMessages();
        });
    }

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            closeChatbot();
        }
    });

    sendBtn.addEventListener("click", function () {
        sendMessage();
    });

    input.addEventListener("keydown", function (event) {
        if (event.key === "Enter") {
            sendMessage();
        }
    });

    suggestButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            const question = button.getAttribute("data-question");
            input.value = question;
            sendMessage();
        });
    });

    function closeChatbot() {
        panel.classList.remove("is-open");
        panel.setAttribute("aria-hidden", "true");
    }

    function sendMessage() {
        const text = input.value.trim();

        if (text.length === 0) {
            return;
        }

        addMessage(text, "user");
        input.value = "";

        setTimeout(function () {
            addMessage(createBotReply(text), "bot");
        }, 500);
    }

    function addMessage(text, type) {
        const message = document.createElement("div");
        message.className = "wc-chat-message " + type;
        message.textContent = text;

        messages.appendChild(message);
        messages.scrollTop = messages.scrollHeight;
    }

    function createBotReply(text) {
        if (text.includes("계란") || text.includes("김치") || text.includes("요리") || text.includes("레시피")) {
            return "계란과 김치가 있다면 김치볶음밥, 김치계란덮밥, 계란찜을 추천해요. 초보라면 김치볶음밥이 가장 쉬워요.";
        }

        if (text.includes("청소") || text.includes("자취방")) {
            return "자취방 청소는 욕실, 주방, 바닥 순서로 나누면 좋아요. 하루에 한 구역씩 10분만 해도 훨씬 깔끔해져요.";
        }

        if (text.includes("식비") || text.includes("절약") || text.includes("돈")) {
            return "식비를 줄이려면 냉동밥, 계란, 두부, 김치, 참치캔처럼 오래 보관 가능한 재료 중심으로 한 주 식단을 짜는 게 좋아요.";
        }

        if (text.includes("강의")) {
            return "요리 강의에서는 초급부터 시작하는 걸 추천해요. 먼저 10분 완성 한 끼 요리 강의를 들어보세요.";
        }

        return "좋아요. WebChef 기준으로 요리, 청소, 생활비, 자취 팁 중에서 필요한 내용을 더 자세히 알려드릴게요.";
    }

    function resetMessages() {
        const oldMessages = messages.querySelectorAll(".wc-chat-message");

        oldMessages.forEach(function (message) {
            message.remove();
        });

        messages.scrollTop = 0;
    }
});