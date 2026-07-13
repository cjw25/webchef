document.addEventListener("DOMContentLoaded", function () {
    const description = document.getElementById("description");
    const descriptionCount = document.getElementById("descriptionCount");

    function updateDescriptionCount() {
        descriptionCount.textContent = description.value.length;
    }

    updateDescriptionCount();
    description.addEventListener("input", updateDescriptionCount);

    function setupFileDrop({ inputId, dropId, listId, nameId, removeId, isValidType, invalidMessage, previewId }) {
        const input = document.getElementById(inputId);
        const drop = document.getElementById(dropId);
        const list = document.getElementById(listId);
        const nameEl = document.getElementById(nameId);
        const removeBtn = document.getElementById(removeId);
        const previewEl = previewId ? document.getElementById(previewId) : null;

        function showFile(file) {
            nameEl.textContent = file.name;
            list.style.display = "flex";

            if (previewEl && file.type.startsWith("image/")) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    previewEl.src = e.target.result;
                    previewEl.style.display = "block";
                };
                reader.readAsDataURL(file);
            }
        }

        function clearFile() {
            input.value = "";
            nameEl.textContent = "";
            list.style.display = "none";

            if (previewEl) {
                previewEl.src = "";
                previewEl.style.display = "none";
            }
        }

        input.addEventListener("change", function () {
            const file = input.files[0];

            if (!file) {
                clearFile();
                return;
            }

            showFile(file);
        });

        removeBtn.addEventListener("click", clearFile);

        drop.addEventListener("dragover", function (event) {
            event.preventDefault();
            drop.classList.add("dragover");
        });

        drop.addEventListener("dragleave", function () {
            drop.classList.remove("dragover");
        });

        drop.addEventListener("drop", function (event) {
            event.preventDefault();
            drop.classList.remove("dragover");

            const files = event.dataTransfer.files;

            if (!files || files.length === 0) {
                return;
            }

            const file = files[0];

            if (!isValidType(file)) {
                alert(invalidMessage);
                return;
            }

            const dataTransfer = new DataTransfer();
            dataTransfer.items.add(file);
            input.files = dataTransfer.files;

            showFile(file);
        });
    }

    setupFileDrop({
        inputId: "img",
        dropId: "fileDrop",
        listId: "fileList",
        nameId: "fileName",
        removeId: "fileRemove",
        previewId: "filePreview",
        isValidType: (file) => file.type.startsWith("image/"),
        invalidMessage: "이미지 파일만 등록할 수 있습니다."
    });

    setupFileDrop({
        inputId: "video",
        dropId: "videoDrop",
        listId: "videoFileList",
        nameId: "videoFileName",
        removeId: "videoFileRemove",
        isValidType: (file) => file.type.startsWith("video/"),
        invalidMessage: "영상 파일만 등록할 수 있습니다."
    });
});
