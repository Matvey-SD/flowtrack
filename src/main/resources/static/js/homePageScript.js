function updateUserIfPossible() {
    let mail = document.getElementById("mail").value;
    let fullName = document.getElementById("fullName").value;
    console.log(mail, fullName)
    const userAction = async () => {
        const response = await fetch('http://localhost:8080/api/update-user', {
            method: 'POST',
            body: JSON.stringify({"fullName": fullName, "mail": mail}),
            headers: {
                'Content-Type': 'application/json'
            }
        });
        const myJson = await response.json();
        console.log(myJson);
        //window.location.reload();
    }
    userAction().then(() => hideUserSettings())
}

function showUserSettings() {
    document.getElementById("user-settings-button").classList.add("hidden");
    document.getElementById("user-settings").classList.remove("hidden");
}

function hideUserSettings() {
    document.getElementById("user-settings").classList.add("hidden");
    document.getElementById("user-settings-button").classList.remove("hidden");
}
