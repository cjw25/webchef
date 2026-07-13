document.addEventListener("DOMContentLoaded", function () {
    const writeFileInput = document.getElementById("write-file-input");
    const writeFileDrop = document.getElementById("write-file-drop");
    const writeFileList = document.getElementById("write-file-list");

    const writeContentArea = document.getElementById("write-content");
    const writeCharCurrent = document.getElementById("write-char-current");

    const MAX_FILE_SIZE = 10 * 1024 * 1024;
    const MAX_FILE_COUNT = 5;
    const ALLOWED_TYPES = [
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp"
    ];

    let selectedFiles = [];

    function existingKeptCount() {
        const deleteChecks = document.querySelectorAll('input[name="deleteImageIds"]');
        let checked = 0;
        deleteChecks.forEach(function (cb) {
            if (cb.checked) checked++;
        });
        return deleteChecks.length - checked;
    }

    function syncInputFiles() {
        const dataTransfer = new DataTransfer();
        selectedFiles.forEach(function (file) {
            dataTransfer.items.add(file);
        });
        writeFileInput.files = dataTransfer.files;
    }

    function renderFiles() {
        writeFileList.innerHTML = "";

        selectedFiles.forEach(function (file, index) {
            const item = document.createElement("div");
            item.className = "write-file-item";

            const sizeKb = Math.ceil(file.size / 1024);

            item.innerHTML =
                '<img class="write-file-thumb" alt="미리보기">' +
                '<span class="write-file-name">' + file.name + ' (' + sizeKb + 'KB)</span>' +
                '<label class="write-file-main">' +
                '<input type="radio" name="mainSelect" value="new-' + index + '"> 대표사진' +
                '</label>' +
                '<button type="button" class="write-file-remove">✕</button>';

            writeFileList.appendChild(item);

            const thumbImg = item.querySelector(".write-file-thumb");
            const reader = new FileReader();
            reader.onload = function (e) {
                thumbImg.src = e.target.result;
            };
            reader.readAsDataURL(file);

            const removeButton = item.querySelector(".write-file-remove");
            removeButton.addEventListener("click", function () {
                selectedFiles.splice(index, 1);
                syncInputFiles();
                renderFiles();
            });
        });
    }

    function validateFile(file) {
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

    function addSelectedFiles(files) {
        for (let i = 0; i < files.length; i++) {
            const file = files[i];

            if (!validateFile(file)) {
                continue;
            }

            if (existingKeptCount() + selectedFiles.length >= MAX_FILE_COUNT) {
                alert("사진은 최대 " + MAX_FILE_COUNT + "개까지 첨부할 수 있습니다.");
                break;
            }

            selectedFiles.push(file);
        }

        syncInputFiles();
        renderFiles();
    }

    if (writeFileInput) {
        writeFileInput.addEventListener("change", function (event) {
            const files = event.target.files;
            if (!files || files.length === 0) return;
            addSelectedFiles(files);
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
            if (!files || files.length === 0) return;
            addSelectedFiles(files);
        });
    }

    function updateWriteCharCount() {
        if (!writeContentArea || !writeCharCurrent) return;
        writeCharCurrent.textContent = writeContentArea.value.length;
    }

    if (writeContentArea && writeCharCurrent) {
        writeContentArea.addEventListener("input", updateWriteCharCount);
        updateWriteCharCount();
    }

    document.querySelectorAll(".existing-remove-btn").forEach(function (btn) {
         btn.addEventListener("click", function () {
             const confirmed = confirm("이 사진을 삭제하시겠어요? '수정 완료'를 눌러야 실제로 반영돼요.");

             if (!confirmed) {
                 return;
             }
             const item = btn.closest(".existing-image-item");
             const checkbox = item.querySelector(".existing-delete-checkbox");

             checkbox.checked = true;
             item.style.display = "none";
         });
    });
});