function validateUserCreation() {
    let password = document.getElementById("password").value;
    let repeatPassword = document.getElementById("repeatPassword").value;
    if (password !== repeatPassword) {
        alert("Пароли не совпадают");
        return false;
    }
    return true;
}