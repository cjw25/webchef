// community-update.js

const writeFileInput = document.getElementById('write-file-input');
const writeFileDrop = document.getElementById('write-file-drop');
const writeFileList = document.getElementById('write-file-list');
let writeSelectedFiles = [];

// 기존 첨부 이미지 개수 (수정 페이지에서만 존재)
const existingFileItems = document.querySelectorAll('#write-existing-file-list .write-file-item');
const MAX_FILE_COUNT = 5;

function getRemainingImgCount(){
    const deletingCount = document.querySelectorAll('#write-existing-file-list input[type="checkbox"]:checked').length;
    const remainingExisting = existingFileItems.length - deletingCount;
    return remainingExisting;
}

function renderWriteFiles(){
    writeFileList.innerHTML = '';
    writeSelectedFiles.forEach((file, idx) => {
        const item = document.createElement('div');
        item.className = 'write-file-item';
        const sizeKb = (file.size / 1024).toFixed(0);
        item.innerHTML =
            '<span class="write-file-name">🖼️ ' + file.name + ' (' + sizeKb + 'KB)</span>' +
            '<button type="button" class="write-file-remove" data-idx="' + idx + '">✕</button>';
        writeFileList.appendChild(item);
    });
    writeFileList.querySelectorAll('.write-file-remove').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const idx = parseInt(e.currentTarget.getAttribute('data-idx'), 10);
            writeSelectedFiles.splice(idx, 1);
            syncWriteInputFiles();
            renderWriteFiles();
        });
    });
}

function syncWriteInputFiles(){
    const dt = new DataTransfer();
    writeSelectedFiles.forEach(f => dt.items.add(f));
    writeFileInput.files = dt.files;
}

function addFiles(newFiles){
    const remaining = getRemainingImgCount();
    const availableSlots = MAX_FILE_COUNT - remaining - writeSelectedFiles.length;

    if (availableSlots <= 0){
        alert('사진은 최대 ' + MAX_FILE_COUNT + '개까지 첨부할 수 있어요.');
        return;
    }

    const filesToAdd = newFiles.slice(0, availableSlots);
    if (newFiles.length > filesToAdd.length){
        alert('사진은 최대 ' + MAX_FILE_COUNT + '개까지 첨부할 수 있어요. 일부 파일만 추가되었습니다.');
    }

    writeSelectedFiles = writeSelectedFiles.concat(filesToAdd);
    syncWriteInputFiles();
    renderWriteFiles();
}

writeFileInput.addEventListener('change', (e) => {
    addFiles(Array.from(e.target.files));
});

['dragover', 'dragenter'].forEach(evt => {
    writeFileDrop.addEventListener(evt, (e) => {
        e.preventDefault();
        writeFileDrop.classList.add('dragover');
    });
});
['dragleave', 'drop'].forEach(evt => {
    writeFileDrop.addEventListener(evt, (e) => {
        e.preventDefault();
        writeFileDrop.classList.remove('dragover');
    });
});
writeFileDrop.addEventListener('drop', (e) => {
    if (e.dataTransfer.files && e.dataTransfer.files.length){
        addFiles(Array.from(e.dataTransfer.files));
    }
});

// 기존 이미지 삭제 체크박스 - 체크 시 시각적 표시
document.querySelectorAll('#write-existing-file-list input[type="checkbox"]').forEach(checkbox => {
    checkbox.addEventListener('change', (e) => {
        const item = e.currentTarget.closest('.write-file-item');
        item.classList.toggle('to-delete', e.currentTarget.checked);
    });
});

const writeContentArea = document.getElementById('write-content');
const writeCharCurrent = document.getElementById('write-char-current');
function updateWriteCharCount(){
    writeCharCurrent.textContent = writeContentArea.value.length;
}
writeContentArea.addEventListener('input', updateWriteCharCount);
updateWriteCharCount();