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

    async function sendMessage() {
        const text = input.value.trim();

        if (text.length === 0) {
            return;
        }

        addMessage(text, "user");
        input.value = "";

        const loadingMessage = addMessage("답변을 생성하고 있어요...", "bot loading");

        try {
            const response = await fetch("/api/chat/message", {
                method: "POST",
                headers: createHeaders(),
                body: JSON.stringify({
                    message: text
                })
            });

            if (!response.ok) {
                loadingMessage.textContent = "챗봇 서버 연결에 실패했습니다.";
                return;
            }

            const data = await response.json();

            if (data && data.reply) {
                loadingMessage.textContent = data.reply;
            } else {
                loadingMessage.textContent = "답변을 받지 못했습니다.";
            }

        } catch (error) {
            loadingMessage.textContent = "AI 챗봇 연결 중 오류가 발생했습니다.";
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

    function addMessage(text, type) {
        const message = document.createElement("div");
        message.className = "wc-chat-message " + type;
        message.textContent = text;

        messages.appendChild(message);
        messages.scrollTop = messages.scrollHeight;

        return message;
    }

    function resetMessages() {
        const oldMessages = messages.querySelectorAll(".wc-chat-message");

        oldMessages.forEach(function (message) {
            message.remove();
        });

        messages.scrollTop = 0;
    }
});