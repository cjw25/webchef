document.addEventListener("DOMContentLoaded", function () {
    const MAX_FILE_COUNT = 3;

    // ===== 글자수 카운터 =====
    const content = document.getElementById("inquiry-content");
    const charCurrent = document.getElementById("inquiry-char-current");

    if (content && charCurrent) {
        function updateCharCount() {
            charCurrent.textContent = content.value.length;
        }

        updateCharCount();
        content.addEventListener("input", updateCharCount);
    }

    // ===== 파일 첨부 미리보기 =====
    const fileInput = document.getElementById("inquiry-file-input");
    const fileDrop = document.getElementById("inquiry-file-drop");
    const fileList = document.getElementById("inquiry-file-list");

    if (!fileInput || !fileDrop || !fileList) {
        return;
    }

    // 현재 선택된 파일들을 직접 들고 있다가, 인풋에 다시 반영해요
    let currentFiles = [];

    function formatSize(bytes) {
        if (bytes < 1024) return bytes + "B";
        if (bytes < 1024 * 1024) return Math.round(bytes / 1024) + "KB";
        return (bytes / (1024 * 1024)).toFixed(1) + "MB";
    }

    function syncInput() {
        const dataTransfer = new DataTransfer();
        currentFiles.forEach(function (file) {
            dataTransfer.items.add(file);
        });
        fileInput.files = dataTransfer.files;
    }

    function render() {
        fileList.innerHTML = "";

        currentFiles.forEach(function (file, index) {
            const item = document.createElement("div");
            item.className = "inquiry-file-item";

            const nameWrap = document.createElement("div");
            nameWrap.className = "inquiry-file-name";
            nameWrap.innerHTML =
                "<span>📎</span><span>" + file.name + " (" + formatSize(file.size) + ")</span>";

            const removeBtn = document.createElement("button");
            removeBtn.type = "button";
            removeBtn.className = "inquiry-file-remove";
            removeBtn.textContent = "✕";
            removeBtn.addEventListener("click", function () {
                currentFiles.splice(index, 1);
                syncInput();
                render();
            });

            item.appendChild(nameWrap);
            item.appendChild(removeBtn);
            fileList.appendChild(item);
        });
    }

    function addFiles(newFiles) {
        const imageFiles = Array.from(newFiles).filter(function (file) {
            return file.type.startsWith("image/");
        });

        if (imageFiles.length !== newFiles.length) {
            alert("이미지 파일만 첨부할 수 있어요.");
        }

        const combined = currentFiles.concat(imageFiles);

        if (combined.length > MAX_FILE_COUNT) {
            alert("사진은 최대 " + MAX_FILE_COUNT + "개까지 첨부할 수 있어요.");
            currentFiles = combined.slice(0, MAX_FILE_COUNT);
        } else {
            currentFiles = combined;
        }

        syncInput();
        render();
    }

    fileInput.addEventListener("change", function () {
        addFiles(fileInput.files);
    });

    fileDrop.addEventListener("dragover", function (event) {
        event.preventDefault();
        fileDrop.classList.add("dragover");
    });

    fileDrop.addEventListener("dragleave", function () {
        fileDrop.classList.remove("dragover");
    });

    fileDrop.addEventListener("drop", function (event) {
        event.preventDefault();
        fileDrop.classList.remove("dragover");

        if (event.dataTransfer.files && event.dataTransfer.files.length > 0) {
            addFiles(event.dataTransfer.files);
        }
    });
});