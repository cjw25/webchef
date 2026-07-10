// 이미지 클릭 시 확대(라이트박스) 보기
const viewImages = document.querySelectorAll('.view-image');

if (viewImages.length > 0) {
    const modal = document.createElement('div');
    modal.className = 'view-image-modal';
    modal.innerHTML = '<img src="" alt="확대 이미지">';
    document.body.appendChild(modal);

    const modalImg = modal.querySelector('img');

    viewImages.forEach(img => {
        img.addEventListener('click', () => {
            modalImg.src = img.src;
            modal.classList.add('active');
        });
    });

    modal.addEventListener('click', () => {
        modal.classList.remove('active');
        modalImg.src = '';
    });
}

// 답변 등록 폼 - 빈 내용 제출 방지
const answerForm = document.querySelector('.answer-form');

if (answerForm) {
    answerForm.addEventListener('submit', (e) => {
        const textarea = answerForm.querySelector('.answer-input');
        if (!textarea.value.trim()) {
            e.preventDefault();
            alert('답변 내용을 입력해주세요.');
            textarea.focus();
        }
    });
}

// 추천(투표) 버튼 중복 클릭 방지
const voteForm = document.querySelector('.view-vote-box form');

if (voteForm) {
    voteForm.addEventListener('submit', () => {
        const btn = voteForm.querySelector('button[type="submit"]');
        btn.disabled = true;
        btn.style.opacity = '0.6';
    });
}
