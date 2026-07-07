document.addEventListener("DOMContentLoaded", function () {
    const writeFileInput = document.getElementById("write-file-input");
    const writeFileDrop = document.getElementById("write-file-drop");
    const writeFileList = document.getElementById("write-file-list");

    const writeContentArea = document.getElementById("write-content");
    const writeCharCurrent = document.getElementById("write-char-current");

    const MAX_FILE_SIZE = 10 * 1024 * 1024;
    const ALLOWED_TYPES = [
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp"
    ];

    let selectedFile = null;

    function renderFile() {
        if (!writeFileList) {
            return;
        }

        writeFileList.innerHTML = "";

        if (!selectedFile) {
            return;
        }

        const item = document.createElement("div");
        item.className = "write-file-item";

        const sizeKb = Math.ceil(selectedFile.size / 1024);

        item.innerHTML =
            '<span class="write-file-name">🖼️ ' +
            selectedFile.name +
            ' (' +
            sizeKb +
            'KB)</span>' +
            '<button type="button" class="write-file-remove">✕</button>';

        writeFileList.appendChild(item);

        const removeButton = item.querySelector(".write-file-remove");

        removeButton.addEventListener("click", function () {
            selectedFile = null;
            clearInputFile();
            renderFile();
        });
    }

    function clearInputFile() {
        if (!writeFileInput) {
            return;
        }

        writeFileInput.value = "";
    }

    function syncInputFile() {
        if (!writeFileInput || !selectedFile) {
            return;
        }

        const dataTransfer = new DataTransfer();
        dataTransfer.items.add(selectedFile);
        writeFileInput.files = dataTransfer.files;
    }

    function validateFile(file) {
        if (!file) {
            return false;
        }

        if (!ALLOWED_TYPES.includes(file.type)) {
            alert("이미지 파일만 업로드할 수 있습니다. JPG, JPEG, PNG, GIF, WEBP 파일만 가능합니다.");
            return false;
        }

        if (file.size > MAX_FILE_SIZE) {
            alert("파일 용량은 10MB를 초과할 수 없습니다.");
            return false;
        }

        return true;
    }

    function setSelectedFile(file) {
        if (!validateFile(file)) {
            clearInputFile();
            return;
        }

        selectedFile = file;
        syncInputFile();
        renderFile();
    }

    if (writeFileInput) {
        writeFileInput.addEventListener("change", function (event) {
            const files = event.target.files;

            if (!files || files.length === 0) {
                selectedFile = null;
                renderFile();
                return;
            }

            setSelectedFile(files[0]);
        });
    }

    if (writeFileDrop) {
        ["dragover", "dragenter"].forEach(function (eventName) {
            writeFileDrop.addEventListener(eventName, function (event) {
                event.preventDefault();
                writeFileDrop.classList.add("dragover");
            });
        });

        ["dragleave", "drop"].forEach(function (eventName) {
            writeFileDrop.addEventListener(eventName, function (event) {
                event.preventDefault();
                writeFileDrop.classList.remove("dragover");
            });
        });

        writeFileDrop.addEventListener("drop", function (event) {
            const files = event.dataTransfer.files;

            if (!files || files.length === 0) {
                return;
            }

            setSelectedFile(files[0]);
        });
    }

    function updateWriteCharCount() {
        if (!writeContentArea || !writeCharCurrent) {
            return;
        }

        writeCharCurrent.textContent = writeContentArea.value.length;
    }

    if (writeContentArea && writeCharCurrent) {
        writeContentArea.addEventListener("input", updateWriteCharCount);
        updateWriteCharCount();
    }
});