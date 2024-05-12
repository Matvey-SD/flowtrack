let team = new URLSearchParams(window.location.search).get("id");

async function makeRequest(path, content) {
    //TODO localhost заменить на глобальную переменную
    const response = await fetch('http://localhost:8080/api/' + path, {
        method: 'POST', body: JSON.stringify(content), headers: {
            'Content-Type': 'application/json'
        }
    });
    return response.json();
}

function openInvitePopup() {
    let popup = document.getElementById("invite-popup");
    popup.classList.add("active");
}

function closeInvitePopup() {
    let popup = document.getElementById("invite-popup");
    popup.classList.remove("active");
}

function inviteUserIfPossible() {
    let login = document.getElementById("login").value;
    makeRequest('add-user', {"login": login, "teamId": team}).then(r => {
        if (r === true) {
            let roleDiv = document.createElement('div');
            roleDiv.innerText = "OBSERVER";
            let nameDiv = document.createElement('div');
            nameDiv.innerText = login;
            let userDiv = document.createElement('div');
            userDiv.appendChild(roleDiv);
            userDiv.appendChild(nameDiv);
            document.getElementById("members-wrapper").appendChild(userDiv);
        }
    })
}

function openColumnPopup() {
    let popup = document.getElementById("column-popup");
    popup.classList.add("active");
}

function closeColumnPopup() {
    let popup = document.getElementById("column-popup");
    popup.classList.remove("active");
}

function addColumnIfPossible() {
    let columnName = document.getElementById("column-name").value;
    let columnType = document.getElementById('column-type-selector').value
    makeRequest('add-column', {"columnName": columnName, "teamId": team, 'columnType': columnType}).then(r => {
        let columnNameDiv = document.createElement('div');
        columnNameDiv.innerText = columnName;
        let cardsDiv = document.createElement('div');
        cardsDiv.classList.add("cards-wrapper");
        let columnDiv = document.createElement('div');
        columnDiv.classList.add("column")
        columnDiv.id = r;
        columnDiv.appendChild(columnNameDiv);
        columnDiv.appendChild(cardsDiv);
        document.getElementById("column-wrapper").appendChild(columnDiv);
    })
}


function addCardToColumn(id) {
    let cardDesc = document.getElementById("card-" + id).value;
    console.log(cardDesc);

    makeRequest('add-card', {"cardDesc": cardDesc, "teamId": team, "columnId": id}).then(r => {
        console.log(r);
        //TODO добавить добавление карточки сразу
    })
}

function openCardModal(id) {
    console.log("Открываем карточку с ID", id)
    makeRequest('get-card', {"teamId": team, "cardId": id}).then(r => {
        console.log(r)
        let modal = document.getElementById("card-modal");
        modal.classList.add("active");
        document.body.classList.add("inactive");
        clearCardModal();
        document.getElementById("card-modal-id").value = id;
        document.getElementById("card-modal-name").value = r['cardName'];
        document.getElementById("card-modal-desc").value = r['cardDescription'];
        document.getElementById("card-modal-doer").value = r['doer'];
        document.getElementById("card-modal-checker").value = r['checker'];
        document.getElementById("card-modal-time-to-do").value = r['timeToDo'];

        for (const comment of r['comments']) {
            let login = document.createElement('div');
            login.innerText = comment["login"];
            let text = document.createElement('div');
            text.innerText = comment["commentText"];
            let time = document.createElement('div');
            time.innerText = comment["time"];
            let commentElement = document.createElement('div');
            commentElement.appendChild(login);
            commentElement.appendChild(text);
            commentElement.appendChild(time);
            document.getElementById("card-modal-comments").appendChild(commentElement);
        }

        for (const file of r['files']) {
            let nameDiv = document.createElement('div');
            nameDiv.innerText = file['fileName'];
            nameDiv.classList.add('document-name');
            nameDiv.id = 'doc-' + file['fileId'];
            let sizeDiv = document.createElement('div');
            sizeDiv.innerText = file['fileSize'];
            sizeDiv.classList.add('document-size');
            let docDiv = document.createElement('div');
            docDiv.id = file['fileId'];
            docDiv.classList.add('document');
            docDiv.appendChild(nameDiv);
            docDiv.appendChild(sizeDiv);
            let clickAtt = document.createAttribute("onclick")
            clickAtt.value = "downloadDocument(this.id)"
            docDiv.attributes.setNamedItem(clickAtt);
            document.getElementById("card-modal-documents").appendChild(docDiv);
        }
        /*let cardTags = document.getElementById("card-modal-tags");
        cardTags.value = r['tags'];*/
    })
}

function saveCardModal() {
    let id = document.getElementById("card-modal-id").value;
    let cardName = document.getElementById("card-modal-name").value;
    let cardDes = document.getElementById("card-modal-desc").value;
    let doer = document.getElementById("card-modal-doer").value;
    let checker = document.getElementById("card-modal-checker").value;
    let timeToDo = document.getElementById("card-modal-time-to-do").value;
    console.log(id, cardName, cardDes, doer, checker, timeToDo);

    makeRequest("update-card", {
        "cardId": id,
        "cardName": cardName,
        "cardDesc": cardDes,
        "teamId": team,
        "doer": doer,
        "checker": checker,
        "timeToDo": timeToDo
    }).then(() => {
        document.getElementById('cardname-' + id).textContent = cardName;
        closeCardModal();
    })
}

function saveCardComment() {
    let id = document.getElementById("card-modal-id").value;
    let comment = document.getElementById("card-modal-comment").value;
    console.log(id, comment);
    makeRequest("save-comment", {"cardId": id, "comment": comment, "teamId": team});
}

function closeCardModal() {
    let modal = document.getElementById("card-modal");
    modal.classList.remove("active");
    document.body.classList.remove("inactive");
    clearCardModal();
}

function clearCardModal() {
    document.getElementById("card-modal-id").value = null;
    document.getElementById("card-modal-name").value = null;
    document.getElementById("card-modal-desc").value = null;
    document.getElementById("card-modal-comments").replaceChildren();
    document.getElementById("card-modal-documents").replaceChildren();
    document.getElementById("card-modal-time-to-do").value = 0;
}

const cardsWrappers = document.getElementsByClassName("cards-wrapper");
for (const cardsWrapper of cardsWrappers) {
    cardsWrapper.addEventListener('dragstart', (evt) => {
        evt.target.classList.add('dragged')
    })
    cardsWrapper.addEventListener('dragend', (evt) => {
        evt.target.classList.remove('dragged')
        if (evt.target.classList.contains('card')) {
            updateCardPosition(evt.target);
        }
    })
    cardsWrapper.addEventListener(`dragover`, (evt) => {
        evt.preventDefault();

        const activeElement = document.querySelector('.dragged');
        const currentElement = evt.target;
        if (!(activeElement !== currentElement &&
            currentElement.classList.contains('card') && activeElement.classList.contains('card'))) {
            return;
        }

        const nextElement = (currentElement === activeElement.nextElementSibling) ?
            currentElement.nextElementSibling :
            currentElement;

        cardsWrapper.insertBefore(activeElement, nextElement);
    });
}

function updateCardPosition(card) {
    let columnId = card.parentElement.id.substring(14);
    console.log('Updating card with id', card.id, 'to be in column with id', columnId);

    makeRequest('update-card-position', {
        'teamId': team, 'cardId': card.id, 'columnId': columnId
    }).then(() => {
        console.log('Success')
    })
}

const columnTitles = document.getElementsByClassName("column-title");
for (const columnTitle of columnTitles) {
    columnTitle.addEventListener(`dragover`, (evt) => {
        evt.preventDefault();

        const activeElement = document.querySelector('.dragged');
        if (!activeElement.classList.contains('card'))
            return;

        const currentWrapper = document.getElementById('cards-wrapper-' + evt.target.id.substring(6));

        if (currentWrapper.children.length === 0) {
            currentWrapper.appendChild(activeElement);
        } else {
            currentWrapper.insertBefore(activeElement, currentWrapper.firstChild);
        }
    })
}

const columnsWrapper = document.getElementById('column-wrapper');
columnsWrapper.addEventListener('dragstart', (evt) => {
    evt.target.classList.add('dragged')
})
columnsWrapper.addEventListener('dragend', (evt) => {
    evt.target.classList.remove('dragged')
    if (evt.target.classList.contains('column')) {
        updateColumnPosition(evt.target);
    }
})

columnsWrapper.addEventListener(`dragover`, (evt) => {
    evt.preventDefault();

    const activeElement = document.querySelector('.dragged');
    const currentElement = evt.target;
    console.log(currentElement.classList.value)
    if (!(activeElement !== currentElement &&
        currentElement.classList.contains(`column`) && activeElement.classList.contains('column'))) {
        return;
    }

    const nextElement = (currentElement === activeElement.nextElementSibling) ?
        currentElement.nextElementSibling :
        currentElement;

    columnsWrapper.insertBefore(activeElement, nextElement);
});

function updateColumnPosition(target) {
    let wrapper = document.getElementById('column-wrapper');
    let position = 0;
    for (let i = 0; i < wrapper.children.length; i++) {
        if (wrapper.children[i].id === target.id) {
            position = i;
            break;
        }
    }

    makeRequest('update-column-position', {'columnId': target.id, 'teamId': team, 'position': position}).then(() => {
        console.log('Success')
    })
}


async function saveDocument() {
    const fileInput = document.getElementById("uploading-document");
    const file = fileInput.files[0];
    const data = new FormData()
    data.append("file", file);
    data.append("team-id", team);
    let path = "upload-file";
    const response = await fetch('http://localhost:8080/api/' + path, {
        method: 'POST', body: data
    });

    return response.json();
}

async function downloadDocument(id) {
    let content = {"teamId": team, "documentId": id};

    const response = await fetch('http://localhost:8080/api/download-file', {
        method: 'POST', body: JSON.stringify(content), headers: {
            'Content-Type': 'application/json'
        }
    });

    response.blob().then(b => {
        const a = document.createElement("a");
        const url = URL.createObjectURL(b);
        a.href = url;
        a.download = document.getElementById("doc-" + id).textContent;
        a.click();
        URL.revokeObjectURL(url);
    });
}

function openRoleModal(id) {
    console.log("Открываем роль с ID", id)
    makeRequest('get-role', {"teamId": team, "roleId": id}).then(r => {
        console.log(r)
        let modal = document.getElementById("role-modal");
        modal.classList.add("active");
        document.body.classList.add("inactive");
        document.getElementById("role-modal-save-button").onclick = saveRoleModal
        clearRoleModal();
        document.getElementById("role-modal-id").value = id;
        document.getElementById("role-modal-name").value = r['roleName'];
        document.getElementById("role-modal-admin").checked = r['permissions'][1]
        document.getElementById("role-modal-inviter").checked = r['permissions'][2]
        document.getElementById("role-modal-card-editor").checked = r['permissions'][3]
        document.getElementById("role-modal-assigner").checked = r['permissions'][4]
        document.getElementById("role-modal-column-editor").checked = r['permissions'][5]
        document.getElementById("role-modal-doc-editor").checked = r['permissions'][6]

        for (const column of r['columns']) {
            let columnElem = document.createElement('div');
            columnElem.id = column["id"];
            columnElem.innerText = column["columnName"];
            document.getElementById("role-modal-columns").appendChild(columnElem);
        }

        let selector = document.getElementById("role-modal-columns-select");
        let defaultSelect = document.createElement("option");
        defaultSelect.value = "";
        defaultSelect.innerText = "Выберите колонку";
        selector.appendChild(defaultSelect);
        for (const column of r['columnsToAdd']) {
            let option = document.createElement("option");
            option.value = column['id'];
            option.innerText = column['columnName'];
            selector.appendChild(option);
        }

        if (r['permissions'][0] === true) {
            disableRolePermissions();
        } else {
            enableRolePermissions()
        }
    })
}

function openRoleModalNew() {
    let modal = document.getElementById("role-modal");
    modal.classList.add("active");
    document.body.classList.add("inactive");
    document.getElementById("role-modal-save-button").onclick = saveNewRoleModal;
    clearRoleModal();
    enableRolePermissions();
}

function saveRoleModal() {
    //TODO отдельный запрос на добавление/удаление колонок
    //TODO отображать недоступность редактирования (+ недоступность редактирования владельца)
    let id = document.getElementById("role-modal-id").value;
    let roleName = document.getElementById("role-modal-name").value;
    let permissions = getRolePermissions();
    console.log(id, roleName, permissions);

    makeRequest("update-role", {
        "teamId": team, "roleId": id, "roleName": roleName, "permissions": permissions
    }).then(() => {
        document.getElementById(id).textContent = roleName;
        closeRoleModal()
    })
}

function saveNewRoleModal() {
    let roleName = document.getElementById("role-modal-name").value;
    let permissions = getRolePermissions();
    console.log(roleName, permissions);

    makeRequest("create-role", {
        "teamId": team, "roleName": roleName, "permissions": permissions
    }).then(r => {
        let roleList = document.getElementById("role-list");
        let roleNameElem = document.createElement('div');
        roleNameElem.classList.add("role-name");
        roleNameElem.id = "role-" + r;
        roleNameElem.innerText = roleName;
        let roleElem = document.createElement('div');
        roleElem.classList.add("role");
        roleElem.id = r;
        let clickAtt = document.createAttribute("onclick")
        clickAtt.value = "openRoleModal(this.id)"
        roleElem.attributes.setNamedItem(clickAtt);
        roleElem.appendChild(roleNameElem);
        roleList.appendChild(roleElem);
        closeRoleModal()
    })
}

function closeRoleModal() {
    let modal = document.getElementById("role-modal");
    modal.classList.remove("active");
    document.body.classList.remove("inactive");
    clearRoleModal();
}

function getRolePermissions() {
    let permissions = [];
    permissions[0] = false;
    permissions[1] = document.getElementById("role-modal-admin").checked;
    permissions[2] = document.getElementById("role-modal-inviter").checked;
    permissions[3] = document.getElementById("role-modal-card-editor").checked;
    permissions[4] = document.getElementById("role-modal-assigner").checked;
    permissions[5] = document.getElementById("role-modal-column-editor").checked;
    permissions[6] = document.getElementById("role-modal-doc-editor").checked;
    permissions[7] = false;
    return permissions;
}

function clearRoleModal() {
    document.getElementById("role-modal-id").value = null;
    document.getElementById("role-modal-name").value = null;
    document.getElementById("role-modal-admin").checked = false;
    document.getElementById("role-modal-inviter").checked = false;
    document.getElementById("role-modal-card-editor").checked = false;
    document.getElementById("role-modal-assigner").checked = false;
    document.getElementById("role-modal-column-editor").checked = false;
    document.getElementById("role-modal-doc-editor").checked = false;
    document.getElementById("role-modal-columns").replaceChildren();
    document.getElementById("role-modal-columns-select").replaceChildren();
}

function disableRolePermissions() {
    document.getElementById("role-modal-admin").setAttribute("disabled", "disabled");
    document.getElementById("role-modal-inviter").setAttribute("disabled", "disabled");
    document.getElementById("role-modal-card-editor").setAttribute("disabled", "disabled");
    document.getElementById("role-modal-assigner").setAttribute("disabled", "disabled");
    document.getElementById("role-modal-column-editor").setAttribute("disabled", "disabled");
    document.getElementById("role-modal-doc-editor").setAttribute("disabled", "disabled");
    document.getElementById("role-modal-columns-select").setAttribute("disabled", "disabled");
    document.getElementById("role-modal-column-save-button").setAttribute("disabled", "disabled");
}

function enableRolePermissions() {
    document.getElementById("role-modal-admin").removeAttribute("disabled");
    document.getElementById("role-modal-inviter").removeAttribute("disabled");
    document.getElementById("role-modal-card-editor").removeAttribute("disabled");
    document.getElementById("role-modal-assigner").removeAttribute("disabled");
    document.getElementById("role-modal-column-editor").removeAttribute("disabled");
    document.getElementById("role-modal-doc-editor").removeAttribute("disabled");
    document.getElementById("role-modal-columns-select").removeAttribute("disabled");
    document.getElementById("role-modal-columns-save-button").removeAttribute("disabled");
}

function changeUserRole(selector) {
    let teamMemberId = selector.id;
    let roleId = selector.value;

    console.log(teamMemberId, roleId);
    makeRequest("set-role", {
        "teamId": team, "teamMemberId": teamMemberId, "roleId": roleId
    }).then(() => {
    })
}

function addColumnToRole() {
    let columnToAdd = document.getElementById("role-modal-columns-select").value;
    let roleId = document.getElementById("role-modal-id").value;

    makeRequest("add-allowed-column", {
        'teamId': team, "columnId": columnToAdd, "roleId": roleId
    }).then(r => {
        console.log(r);
        for (const column of r) {
            let option = document.createElement("option");
            option.value = column['id'];
            option.innerText = column['columnName'];
            selector.appendChild(option);
        }

    })
}

async function saveDocumentCard() {
    const fileInput = document.getElementById("uploading-document-card");
    const cardId = document.getElementById("card-modal-id").value;
    const file = fileInput.files[0];
    const data = new FormData()
    data.append("file", file);
    data.append("team-id", team);
    data.append("card-id", cardId);
    let path = "upload-file-card";
    const response = await fetch('http://localhost:8080/api/' + path, {
        method: 'POST', body: data
    });

    return response.json();
}

function openDeskPage() {
    unlockAllButtons();
    hideAllMenus();
    document.getElementById("desk-page-button").setAttribute("disabled", "disabled");
    document.getElementById("desk-page-wrapper").classList.add("active");
}

function openMembersPage() {
    unlockAllButtons();
    hideAllMenus();
    document.getElementById("members-page-button").setAttribute("disabled", "disabled");
    document.getElementById("member-page-wrapper").classList.add("active");
}

function openRolesPage() {
    unlockAllButtons();
    hideAllMenus();
    document.getElementById("roles-page-button").setAttribute("disabled", "disabled");
    document.getElementById("roles-page-wrapper").classList.add("active");
}

function openDocsPage() {
    unlockAllButtons();
    hideAllMenus();
    document.getElementById("docs-page-button").setAttribute("disabled", "disabled");
    document.getElementById("docs-page-wrapper").classList.add("active");
}

function openSettingsPage() {
    unlockAllButtons();
    hideAllMenus();
    document.getElementById("settings-page-button").setAttribute("disabled", "disabled");
    document.getElementById("settings-page-wrapper").classList.add("active");
}

function unlockAllButtons() {
    let buttons = document.getElementsByClassName("menu-show-button");
    for (const button of buttons) {
        button.removeAttribute("disabled");
    }
}

function hideAllMenus() {
    let menus = document.getElementsByClassName("menu-wrapper");
    for (const menu of menus) {
        menu.classList.remove("active");
    }
}