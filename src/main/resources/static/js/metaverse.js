document.addEventListener("DOMContentLoaded", function () {
    const canvas = document.querySelector("#unity-canvas");
    const loadingBar = document.querySelector("#unity-loading-bar");
    const progressBarFull = document.querySelector("#unity-progress-bar-full");
    const fullscreenBtn = document.querySelector("#fullscreenBtn");
    const warningBanner = document.querySelector("#unity-warning");

    if (!canvas || !loadingBar || !progressBarFull || !fullscreenBtn || !warningBanner) {
        console.error("Unity WebGL 실행에 필요한 HTML 요소를 찾을 수 없습니다.");
        return;
    }

    function unityShowBanner(message, type) {
        function updateBannerVisibility() {
            warningBanner.style.display = warningBanner.children.length ? "block" : "none";
        }

        const div = document.createElement("div");
        div.innerHTML = message;
        div.className = type === "error" ? "unity-error" : "unity-warning";

        warningBanner.appendChild(div);

        if (type !== "error") {
            setTimeout(function () {
                if (warningBanner.contains(div)) {
                    warningBanner.removeChild(div);
                }

                updateBannerVisibility();
            }, 5000);
        }

        updateBannerVisibility();
    }

    const buildUrl = "/metaverse/WebChefUnity/Build";

    const loaderUrl = buildUrl + "/WebChefUnity.loader.js";

    const config = {
        dataUrl: buildUrl + "/WebChefUnity.data",
        frameworkUrl: buildUrl + "/WebChefUnity.framework.js",
        codeUrl: buildUrl + "/WebChefUnity.wasm",
        streamingAssetsUrl: "/metaverse/WebChefUnity/StreamingAssets",
        companyName: "FiveChef",
        productName: "WebChefUnity",
        productVersion: "1.0",
        showBanner: unityShowBanner
    };

    loadingBar.style.display = "flex";

    const script = document.createElement("script");
    script.src = loaderUrl;

    script.onload = function () {
        createUnityInstance(canvas, config, function (progress) {
            progressBarFull.style.width = (100 * progress) + "%";
        }).then(function (unityInstance) {
            loadingBar.style.display = "none";

            fullscreenBtn.addEventListener("click", function () {
                unityInstance.SetFullscreen(1);
            });

        }).catch(function (message) {
            loadingBar.style.display = "none";
            alert("Unity WebGL 실행 오류: " + message);
        });
    };

    script.onerror = function () {
        loadingBar.style.display = "none";
        alert("Unity loader 파일을 찾을 수 없습니다. WebChefUnity.loader.js 경로를 확인하세요.");
    };

    document.body.appendChild(script);
});