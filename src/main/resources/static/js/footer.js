document.addEventListener("DOMContentLoaded", function () {
    const dock = document.getElementById("metaverseDock");
    const launchBtn = document.getElementById("metaverseLaunchBtn");

    if (dock) {
        dock.addEventListener("click", function () {
            dock.classList.add("dock-open");
        });

        dock.addEventListener("mouseleave", function () {
            dock.classList.remove("dock-open");
        });
    }

    if (launchBtn) {
        launchBtn.addEventListener("click", function () {
            window.location.href = "/metaverse";
        });
    }
});