document.addEventListener("DOMContentLoaded", function () {
    const questionList = document.getElementById("questionList");
    const addQuestionBtn = document.getElementById("addQuestionBtn");
    const questionTemplate = document.getElementById("questionTemplate");
    const choiceTemplate = document.getElementById("choiceTemplate");

    function renumber() {
        const questions = questionList.querySelectorAll(".quiz-question");

        questions.forEach(function (questionEl, qIdx) {
            questionEl.querySelector(".quiz-question-index").textContent = "문제 " + (qIdx + 1);

            const contentEl = questionEl.querySelector(".question-content");
            contentEl.name = "questions[" + qIdx + "].content";

            const choices = questionEl.querySelectorAll(".quiz-choice");
            choices.forEach(function (choiceEl, cIdx) {
                const correctEl = choiceEl.querySelector(".choice-correct");
                const contentInputEl = choiceEl.querySelector(".choice-content");

                correctEl.name = "questions[" + qIdx + "].choices[" + cIdx + "].correct";
                contentInputEl.name = "questions[" + qIdx + "].choices[" + cIdx + "].content";
                contentInputEl.placeholder = "보기 " + (cIdx + 1);
            });

            // 보기가 1개만 남으면 삭제 버튼 비활성화 (최소 1개는 있어야 함)
            const removeChoiceBtns = questionEl.querySelectorAll(".btn-remove-choice");
            removeChoiceBtns.forEach(function (btn) {
                btn.disabled = choices.length <= 1;
            });
        });

        // 문제가 1개만 남으면 문제 삭제 버튼 비활성화
        const removeQuestionBtns = questionList.querySelectorAll(".btn-remove-question");
        removeQuestionBtns.forEach(function (btn) {
            btn.disabled = questions.length <= 1;
        });
    }

    function bindChoiceButtons(scopeEl) {
        scopeEl.querySelectorAll(".btn-add-choice").forEach(function (btn) {
            if (btn.dataset.bound) return;
            btn.dataset.bound = "true";

            btn.addEventListener("click", function () {
                const questionEl = btn.closest(".quiz-question");
                const choiceList = questionEl.querySelector(".choice-list");
                const newChoice = choiceTemplate.content.firstElementChild.cloneNode(true);
                choiceList.appendChild(newChoice);
                bindRemoveChoiceButtons(newChoice.parentElement);
                renumber();
            });
        });
    }

    function bindRemoveChoiceButtons(scopeEl) {
        scopeEl.querySelectorAll(".btn-remove-choice").forEach(function (btn) {
            if (btn.dataset.bound) return;
            btn.dataset.bound = "true";

            btn.addEventListener("click", function () {
                const choiceEl = btn.closest(".quiz-choice");
                const choiceList = choiceEl.parentElement;

                if (choiceList.querySelectorAll(".quiz-choice").length <= 1) {
                    return;
                }

                choiceEl.remove();
                renumber();
            });
        });
    }

    function bindRemoveQuestionButtons(scopeEl) {
        scopeEl.querySelectorAll(".btn-remove-question").forEach(function (btn) {
            if (btn.dataset.bound) return;
            btn.dataset.bound = "true";

            btn.addEventListener("click", function () {
                if (questionList.querySelectorAll(".quiz-question").length <= 1) {
                    return;
                }

                btn.closest(".quiz-question").remove();
                renumber();
            });
        });
    }

    function bindAll(scopeEl) {
        bindChoiceButtons(scopeEl);
        bindRemoveChoiceButtons(scopeEl);
        bindRemoveQuestionButtons(scopeEl);
    }

    addQuestionBtn.addEventListener("click", function () {
        const newQuestion = questionTemplate.content.firstElementChild.cloneNode(true);
        questionList.appendChild(newQuestion);
        bindAll(newQuestion);
        renumber();
    });

    bindAll(questionList);
    renumber();
});