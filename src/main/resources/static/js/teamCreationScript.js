function changeTeamCopyVisibility(checker) {
    if (checker.checked) {
        document.getElementById('copy-menu').classList.remove('hidden');
    } else {
        document.getElementById('copy-menu').classList.add('hidden');
    }
}