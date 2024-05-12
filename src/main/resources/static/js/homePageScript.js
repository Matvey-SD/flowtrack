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
    userAction()
}

document.getElementById("submit-user-button").addEventListener("click", updateUserIfPossible);