// ===== 댓글 이미지 미리보기 =====
(function () {
  var imgInput = document.getElementById('commentImg');
  var previewBox = document.getElementById('commentImgPreview');

  if (!imgInput || !previewBox) return;

  imgInput.addEventListener('change', function () {
    previewBox.innerHTML = '';

    var files = Array.from(imgInput.files).slice(0, 3); // 최대 3장만 미리보기

    files.forEach(function (file) {
      if (!file.type.startsWith('image/')) return;

      var reader = new FileReader();
      reader.onload = function (e) {
        var img = document.createElement('img');
        img.src = e.target.result;
        img.alt = '첨부 이미지 미리보기';
        previewBox.appendChild(img);
      };
      reader.readAsDataURL(file);
    });
  });
})();

// ===== 대표 영상(유튜브) 재생 완료 시 퀴즈 버튼 노출 =====
var ytPlayer;

function onYouTubeIframeAPIReady() {
  var iframeEl = document.getElementById('mainYoutubePlayer');
  if (!iframeEl) return; // 대표 영상이 유튜브가 아니면 실행 안 함

  ytPlayer = new YT.Player('mainYoutubePlayer', {
    events: {
      'onStateChange': onPlayerStateChange
    }
  });
}

function onPlayerStateChange(event) {
  if (event.data === YT.PlayerState.ENDED) {
    var modalOverlay = document.getElementById('quizModalOverlay');
    if (modalOverlay) {
      modalOverlay.style.display = 'flex';
    }
  }
}

// 모달 닫기 (X 버튼 / 나중에 할게요 버튼 / 오버레이 바깥 클릭)
(function () {
  var overlay = document.getElementById('quizModalOverlay');
  if (!overlay) return;

  var closeBtn = document.getElementById('quizModalClose');
  var laterBtn = document.getElementById('quizModalLater');

  function closeModal() {
    overlay.style.display = 'none';
  }

  if (closeBtn) closeBtn.addEventListener('click', closeModal);
  if (laterBtn) laterBtn.addEventListener('click', closeModal);

  overlay.addEventListener('click', function (e) {
    if (e.target === overlay) closeModal(); // 오버레이 바깥(어두운 배경) 클릭 시 닫기
  });
})();

(function loadYoutubeIframeApi() {
  if (!document.getElementById('mainYoutubePlayer')) return; // 유튜브 영상 없으면 API 로드 안 함

  var tag = document.createElement('script');
  tag.src = 'https://www.youtube.com/iframe_api';
  document.head.appendChild(tag);
})();