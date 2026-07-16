document.addEventListener("DOMContentLoaded", function () {
    const intro = document.getElementById("practiceCompleteIntro");
    const result = document.getElementById("practiceCompleteResult");
    const button = document.getElementById("showPracticeResultButton");

    if (!intro || !result || !button) {
        return;
    }

    button.addEventListener("click", function () {
        intro.hidden = true;
        result.hidden = false;
    });
});