document.addEventListener("DOMContentLoaded", function () {
    const writeFileInput = document.getElementById("write-file-input");
    const writeFileDrop = document.getElementById("write-file-drop");
    const writeFileList = document.getElementById("write-file-list");
    const writeMainIndexInput = document.getElementById("write-main-index");

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

    function renderFile() {
        if (!writeFileList) {
            return;
        }

        writeFileList.innerHTML = "";

        if (selectedFiles.length === 0) {
            if (writeMainIndexInput){
                writeMainIndexInput.value = "0";
            }
            return;
        }

        const currentMainIndex = writeMainIndexInput
            ? Number(writeMainIndexInput.value)
            : 0;

        selectedFiles.forEach(function (file, index){
            const item = document.createElement("div");
            item.className = "write-file-item";

            const sizeKb = Math.ceil(file.size / 1024);
            const checkedAttr = index === currentMainIndex ? "checked" : "";

            item.innerHTML =
                '<img class="write-file-thumb" alt="미리보기">'+
                '<span class="write-file-name"> ' +
                file.name +
                ' (' +
                sizeKb +
                'KB)</span>' +
                '<label class="write-file-main">' +
                '<input type="radio" name="writeMainRadio" value="' + index + '" ' + checkedAttr + '> 대표사진' +
                '</label>' +
                '<button type="button" class="write-file-remove">✕</button>';

        writeFileList.appendChild(item);

        const thumbImg = item.querySelector(".write-file-thumb");
        const reader = new FileReader();
        reader.onload = function (e) {
            thumbImg.src = e.target.result;
        };
        reader.readAsDataURL(file);

        const radio = item.querySelector('input[type="radio"]');
                        radio.addEventListener("change", function () {
                            if (writeMainIndexInput) {
                                writeMainIndexInput.value = String(index);
                            }
                        });

        const removeButton = item.querySelector(".write-file-remove");
        removeButton.addEventListener("click", function () {
            selectedFiles.splice(index, 1);

            if (writeMainIndexInput && Number(writeMainIndexInput.value) >= selectedFiles.length){
                writeMainIndexInput.value = "0";
            }

            syncInputFile();
            renderFile();
        });
    });
}

    function clearInputFile() {
        if (!writeFileInput) {
            return;
        }

        writeFileInput.value = "";
    }

    function syncInputFile() {
        if (!writeFileInput) {
            return;
        }

        const dataTransfer = new DataTransfer();
        selectedFiles.forEach(function (file) {
            dataTransfer.items.add(file);
        });
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

    function addSelectedFiles(files) {
        for (let i = 0; i < files.length; i++) {
            const file = files[i];

            if (!validateFile(file)) {
                continue;
            }

            if (selectedFiles.length >= MAX_FILE_COUNT) {
                alert("사진은 최대 " + MAX_FILE_COUNT + "개까지 첨부할 수 있습니다.");
                break;
            }

            selectedFiles.push(file);
        }

        syncInputFile();
        renderFile();
    }

    if (writeFileInput) {
        writeFileInput.addEventListener("change", function (event) {
            const files = event.target.files;

            if (!files || files.length === 0) {
                return;
            }

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

            if (!files || files.length === 0) {
                return;
            }

            addSelectedFiles(files);
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

   const writeForm = document.querySelector(".write-card form");
       const writeSubjectInput = document.querySelector('input[name="subject"]');
       const writeCategorySelect = document.querySelector('select[name="category"]');

       if (writeForm) {
           writeForm.addEventListener("submit", function (event) {
               if (writeCategorySelect && writeCategorySelect.value.trim() === "") {
                   event.preventDefault();
                   alert("카테고리를 선택해주세요.");
                   writeCategorySelect.focus();
                   return;
               }

               if (writeSubjectInput && writeSubjectInput.value.trim() === "") {
                   event.preventDefault();
                   alert("제목을 입력해주세요.");
                   writeSubjectInput.focus();
                   return;
               }

               if (writeContentArea && writeContentArea.value.trim() === "") {
                   event.preventDefault();
                   alert("내용을 입력해주세요.");
                   writeContentArea.focus();
                   return;
               }
           });
       }
});