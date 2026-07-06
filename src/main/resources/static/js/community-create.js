const writeFileInput = document.getElementById('write-file-input');
    const writeFileDrop = document.getElementById('write-file-drop');
    const writeFileList = document.getElementById('write-file-list');
    let writeSelectedFiles = [];

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

    writeFileInput.addEventListener('change', (e) => {
        writeSelectedFiles = writeSelectedFiles.concat(Array.from(e.target.files));
        syncWriteInputFiles();
        renderWriteFiles();
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
            writeSelectedFiles = writeSelectedFiles.concat(Array.from(e.dataTransfer.files));
            syncWriteInputFiles();
            renderWriteFiles();
        }
    });

    const writeContentArea = document.getElementById('write-content');
    const writeCharCurrent = document.getElementById('write-char-current');
    function updateWriteCharCount(){
        writeCharCurrent.textContent = writeContentArea.value.length;
    }
    writeContentArea.addEventListener('input', updateWriteCharCount);
    updateWriteCharCount();