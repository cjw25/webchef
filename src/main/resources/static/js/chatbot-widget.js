document.addEventListener("DOMContentLoaded", function () {
    const floatingTools = document.getElementById("floatingTools");
    const openBtn = document.getElementById("chatbotOpenBtn");
    const closeBtn = document.getElementById("chatbotCloseBtn");
    const resetBtn = document.getElementById("chatbotResetBtn");
    const panel = document.getElementById("chatbotPanel");
    const input = document.getElementById("chatbotInput");
    const sendBtn = document.getElementById("chatbotSendBtn");
    const messages = document.getElementById("chatbotMessages");
    const suggestButtons = document.querySelectorAll(".wc-suggest-btn");

    let isSending = false;

    if (!openBtn || !closeBtn || !panel || !input || !sendBtn || !messages) {
        return;
    }

    openBtn.addEventListener("click", function () {
        openChatbot();
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

            if (!question) {
                return;
            }

            input.value = question;
            sendMessage();
        });
    });

    function openChatbot() {
        panel.classList.add("is-open");
        panel.setAttribute("aria-hidden", "false");

        document.body.classList.add("wc-chatbot-open");

        if (floatingTools) {
            floatingTools.classList.add("is-chat-open");
        }

        openBtn.style.display = "none";
        openBtn.style.visibility = "hidden";
        openBtn.style.pointerEvents = "none";

        setTimeout(function () {
            input.focus();
        }, 150);
    }

    function closeChatbot() {
        panel.classList.remove("is-open");
        panel.setAttribute("aria-hidden", "true");

        document.body.classList.remove("wc-chatbot-open");

        if (floatingTools) {
            floatingTools.classList.remove("is-chat-open");
        }

        openBtn.style.display = "";
        openBtn.style.visibility = "";
        openBtn.style.pointerEvents = "";
    }

    async function sendMessage() {
        if (isSending) {
            return;
        }

        const text = input.value.trim();

        if (text.length === 0) {
            return;
        }

        if (text.length > 1000) {
            addMessage("질문은 1000자 이하로 입력해주세요.", "bot");
            return;
        }

        isSending = true;
        sendBtn.disabled = true;

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

            const rawText = await response.text();

            let data = null;

            try {
                data = JSON.parse(rawText);
            } catch (e) {
                data = null;
            }

            if (!response.ok) {
                loadingMessage.textContent =
                    "챗봇 서버 오류 (" + response.status + "): " + rawText.substring(0, 120);
                return;
            }

            if (data && data.reply) {
                loadingMessage.textContent = data.reply;
                loadingMessage.classList.remove("loading");
                return;
            }

            loadingMessage.textContent = "답변 형식이 올바르지 않습니다.";
            loadingMessage.classList.remove("loading");

        } catch (error) {
            loadingMessage.textContent = "AI 챗봇 연결 중 오류가 발생했습니다.";
            loadingMessage.classList.remove("loading");
            console.error(error);

        } finally {
            isSending = false;
            sendBtn.disabled = false;
            input.focus();
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