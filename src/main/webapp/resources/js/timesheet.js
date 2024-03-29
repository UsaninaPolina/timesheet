function deleteRow(id_row) {
    var tsRow = document.getElementById("ts_row_" + id_row);
    if (tsRow !== null) {
        tsRow.parentNode.removeChild(tsRow);
    }
    recalculateRowNumbers();
    recalculateDuration();
}

function al() {
    alert("!!");
}

/* Добавляет новую строку в табличную часть отчёта. */
function addNewRow() {
    var tsTable = dojo.byId("time_sheet_table");
    var tsRows = dojo.query(".time_sheet_row");
    var tsRowCount = tsRows.length; // Количество активных строк таблицы.
    var tsTableTrCount = tsRows.length + 2; // +2 это tr с заголовком и tr с ИТОГО
    // Добавляем новую строку перед строкой ИТОГО
    var newTsRow = tsTable.insertRow(tsTableTrCount - 1);
    dojo.addClass(newTsRow, "time_sheet_row");
    // Индекс новой строки
    var newRowIndex;
    if (tsRowCount == 0) {
        newRowIndex = 0;
    }
    else {
        // Получаем идентификатор последней строки для получения ее индекса (не
        // путать с номером строки, они могут не совпадать).
        var lastRow = tsRows[tsRowCount - 1];
        var lastRowId = dojo.attr(lastRow, "id");
        var lastRowIndex = parseInt(lastRowId.substring(lastRowId.lastIndexOf("_") + 1, lastRowId.length));
        newRowIndex = lastRowIndex + 1;
    }
    dojo.attr(newTsRow,
        { id:"ts_row_" + newRowIndex });

    //ячейка с кнопкой(картинкой) удаления строки
    var deleteCell = newTsRow.insertCell(0);
    dojo.addClass(deleteCell, "text_center_align");
    var img = dojo.doc.createElement("img");
    dojo.addClass(img, "pointer");
    dojo.attr(img, {
        id:"delete_button_" + newRowIndex,
        src:"resources/img/delete.png",
        alt:"Удалить",
        //без px так как IE не понимает
        height:"15",
        width:"15"
    });

    //неведома ошибка исправляется для IE добавлением onclick именно через функцию
    img.onclick = function () {
        deleteRow(newRowIndex);
    }
    deleteCell.appendChild(img);
    //dojo.connect(dojo.byId("img"), "onclick", function update() { alert('click!'); });

    // Первая ячейка новой строки с чекбоксом
    //убрано
//    var cbCell = newTsRow.insertCell(0);
//    dojo.addClass(cbCell, "text_center_align");
//    var cbInput = dojo.doc.createElement("input");
//    dojo.attr(cbInput, {
//        type:"checkbox",
//        name:"selectedRow[" + newRowIndex + "]",
//        id:"selected_row_id_" + newRowIndex
//    });
//    dojo.addClass(cbInput, "selectedRow");
//    cbCell.appendChild(cbInput);

    // Ячейка с номером строки
    var rowNumCell = newTsRow.insertCell(1);
    dojo.addClass(rowNumCell, "text_center_align row_number");
    rowNumCell.innerHTML = tsRowCount + 1;
    // Ячейка с типами активности
    var actTypeCell = newTsRow.insertCell(2);
    dojo.addClass(actTypeCell, "top_align");
    var actTypeSelect = dojo.doc.createElement("select");
    dojo.attr(actTypeSelect, {
        id:"activity_type_id_" + newRowIndex,
        name:"timeSheetTablePart[" + newRowIndex + "].activityTypeId"
    });
    dojo.addClass(actTypeSelect, "activityType");
    insertEmptyOption(actTypeSelect);
    for (var i = 0; i < actTypeList.length; i++) {
        var actTypeOption = dojo.doc.createElement("option");
        dojo.attr(actTypeOption, {
            value:actTypeList[i].id
        });
        actTypeOption.title = actTypeList[i].value;

        actTypeOption.innerHTML = actTypeList[i].value;
        actTypeSelect.appendChild(actTypeOption);
    }
    actTypeCell.appendChild(actTypeSelect);
    // Ячейка с названиями проектов/пресейлов
    var projectNameCell = newTsRow.insertCell(3);
    dojo.addClass(projectNameCell, "top_align");
    var projectSelect = dojo.doc.createElement("select");
    dojo.attr(projectSelect, {
        id:"project_id_" + newRowIndex,
        name:"timeSheetTablePart[" + newRowIndex + "].projectId"
    });
    insertEmptyOption(projectSelect);
    projectNameCell.appendChild(projectSelect);
    // Ячейка с проектной ролью
    var projectRoleCell = newTsRow.insertCell(4);
    dojo.addClass(projectRoleCell, "top_align");
    var projectRoleSelect = dojo.doc.createElement("select");
    dojo.attr(projectRoleSelect, {
        id:"project_role_id_" + newRowIndex,
        name:"timeSheetTablePart[" + newRowIndex + "].projectRoleId"
    });
    insertEmptyOption(projectRoleSelect);
    for (var i = 0; i < projectRoleList.length; i++) {
        var projectRoleOption = dojo.doc.createElement("option");
        dojo.attr(projectRoleOption, {
            value:projectRoleList[i].id
        });
        projectRoleOption.title = projectRoleList[i].value;

        projectRoleOption.innerHTML = projectRoleList[i].value;
        projectRoleSelect.appendChild(projectRoleOption);
    }
    sortSelectOptions(projectRoleSelect);
    projectRoleCell.appendChild(projectRoleSelect);
    // Ячейка с категорией активности
    var actCatCell = newTsRow.insertCell(5);
    dojo.addClass(actCatCell, "top_align");
    var actCatSelect = dojo.doc.createElement("select");
    dojo.attr(actCatSelect, {
        id:"activity_category_id_" + newRowIndex,
        name:"timeSheetTablePart[" + newRowIndex + "].activityCategoryId"
    });
    insertEmptyOption(actCatSelect);
    actCatCell.appendChild(actCatSelect);
    // Ячейка с проектными задачами
    var projectTasksCell = newTsRow.insertCell(6);
    dojo.addClass(projectTasksCell, "top_align");
    var projectTasksSelect = dojo.doc.createElement("select");
    dojo.attr(projectTasksSelect, {
        id:"cqId_id_" + newRowIndex,
        name:"timeSheetTablePart[" + newRowIndex + "].cqId"
    });
    insertEmptyOption(projectTasksSelect);
    projectTasksCell.appendChild(projectTasksSelect);
    // Ячейка с часами
    var durationCell = newTsRow.insertCell(7);
    dojo.addClass(durationCell, "top_align");
    var durationInput = dojo.doc.createElement("input");
    dojo.attr(durationInput, {
        id:"duration_id_" + newRowIndex,
        name:"timeSheetTablePart[" + newRowIndex + "].duration",
        //type:"number",
        type:"text"
    });
    dojo.addClass(durationInput, "text_right_align duration");
    durationCell.appendChild(durationInput);
    // Ячейка с комментариями
    var descriptionCell = newTsRow.insertCell(8);
    dojo.addClass(descriptionCell, "top_align");
    var descriptionTextarea = dojo.doc.createElement("textarea");
    dojo.attr(descriptionTextarea, {
        id:"description_id_" + newRowIndex,
        name:"timeSheetTablePart[" + newRowIndex + "].description",
        wrap:"soft",
        rows:"4",
        cols:"30"
    });
    descriptionCell.appendChild(descriptionTextarea);
    // Ячейка с проблемами
    var problemCell = newTsRow.insertCell(9);
    dojo.addClass(problemCell, "top_align");
    var problemTextarea = dojo.doc.createElement("textarea");
    dojo.attr(problemTextarea, {
        id:"problem_id_" + newRowIndex,
        name:"timeSheetTablePart[" + newRowIndex + "].problem",
        wrap:"soft",
        rows:"4",
        cols:"30"
    });
    problemCell.appendChild(problemTextarea);
    // Помещаем новую строку в конец таблицы
    recalculateRowNumbers();
    resetRowState(newRowIndex, true);
    /*подключаем функции показа тултипов для селктов */
    //для типа активности
    dojo.connect(actTypeSelect, "onmouseover", actTypeSelect, showTooltip);
    dojo.connect(actTypeSelect, "onmouseout", actTypeSelect, hideTooltip);
    //для проекта
    dojo.connect(projectSelect, "onmouseover", projectSelect, showTooltip);
    dojo.connect(projectSelect, "onmouseout", projectSelect, hideTooltip);
    //для проектной роли
    dojo.connect(projectRoleSelect, "onmouseover", projectRoleSelect, showTooltip);
    dojo.connect(projectRoleSelect, "onmouseout", projectRoleSelect, hideTooltip);
    //для категории активности
    dojo.connect(actCatSelect, "onmouseover", actCatSelect, showTooltip);
    dojo.connect(actCatSelect, "onmouseout", actCatSelect, hideTooltip);
    //для задачи
    dojo.connect(projectTasksSelect, "onmouseover", projectTasksSelect, showTooltip);
    dojo.connect(projectTasksSelect, "onmouseout", projectTasksSelect, hideTooltip);
    dojo.connect(actTypeSelect, "onchange", actTypeSelect, typeActivityChange);
    dojo.connect(projectSelect, "onchange", projectSelect, projectChange);
    dojo.connect(projectRoleSelect, "onchange", projectRoleSelect, projectRoleChange);
    dojo.connect(durationInput, "onchange", durationInput, checkDuration);
    dojo.connect(descriptionTextarea, "onkeyup", descriptionTextarea, textareaAutoGrow);
    dojo.connect(problemTextarea, "onkeyup", problemTextarea, textareaAutoGrow);
}

function showTooltip(obj) {
    tooltip.show(getTitle(obj));
}

function hideTooltip(obj) {
    tooltip.hide();
}

/* Проверяет, включены ли куки в браузере
 * если нет индикатор серый, если да-синий*/
function showCookieIndicator() {
    if (navigator.cookieEnabled) {
        document.getElementById('indicator').style.display = 'none';
    }
    else {
        document.getElementById('indicator').style.display = 'block';
    }
}

/* Возвращает массив текущих Id строк в таблице */
function getRowsId(obj) {
    var listId = [];
    for (var i = 0; i < obj.length; i++) {
        var id = dojo.attr(obj[i], "id");
        var id_num = parseInt(id.substring(id.lastIndexOf("_") + 1, id.length));
        listId[i] = id_num;
    }
    return listId;
}

/*Проверяем, есть ли в табличной части отчета непустые строки
 * когда кликаем на чекбокс отпуска/болезни
 * если находим, передаем тру, и потом эти строки очищаем.*/
function tablePartNotEmpty() {
    var notEmpty = false;
    var tsRows = getRowsId(dojo.query(".time_sheet_row"));
    for (var i = 0; i < tsRows.length; i++) {
        var actTypeSelect = dojo.byId('activity_type_id_' + tsRows[i]);
        if (actTypeSelect.value != 0) {
            notEmpty = true;
            break;
        }
    }
    return notEmpty;
}

function planBoxNotEmpty() {
    var planBox = dojo.query("#plan");
    if (planBox && (planBox.length > 0)) planBox = planBox[0];
    if (!planBox) return false
    else return !!planBox.value;
}

/*проверяет, выделен ли чекбокс отпуска
 * если да, то чекбокс болезни не выделяется
 * и вызывается диалог - стирать или нет строки отчета*/
function checkLongVacation() {
    var otp = document.getElementById('long_vacation');
    var ill = document.getElementById('long_illness');
    if (otp.checked == true) {
        ill.checked = false;
        if (tablePartNotEmpty()) {
            if (confirm("Выбор списания занятости по отпуску/болезни удалит" +
                " введенные вами выше данные! Очистить все поля?")) {
                //По условию все поля табличной части должны быть очищены и недоступны.
                disableRows();
                dojo.attr("plan", { value:"" });
            } else {
                otp.checked = false;
            }
        } else {
            disableRows();
        }
    } else {
        enableRows();
    }
    showOrHideDatePickers();
}
/*проверяет, выделен ли чекбокс болезни
 * если да, то чекбокс отпуска не выделяется
 * и вызывается диалог - стирать или нет строки отчета*/
function checkLongIllness() {
    var otp = document.getElementById('long_vacation');
    var ill = document.getElementById('long_illness');
    if (ill.checked == true) {
        otp.checked = false;
        if (tablePartNotEmpty()) {
            if (confirm("Выбор списания занятости по отпуску/болезни удалит" +
                " введенные вами выше данные! Очистить все поля?")) {
                disableRows();
                dojo.attr("plan", { value:"" });
            } else {
                ill.checked = false;
            }
        } else {
            disableRows();
        }
    } else {
        enableRows();
    }
    showOrHideDatePickers();
}

/*проверяет, выделены ли чекбоксы если нет
 * то дизейблит выбор дат отпуска/болезни
 * энейблит выбор даты самого отчета
 * если выделены то дейтпикер самого отчета
 * лочится и стирается его значение а дейтпикеры
 * отпуска/болезни становятся доступными*/
function showOrHideDatePickers() {
    var beginLongDate = dijit.byId("begin_long_date");
    var endLongDate = dijit.byId("end_long_date");
    var calDate = dijit.byId('calDate');
    var otp = dojo.byId('long_vacation');
    var ill = dojo.byId('long_illness');
    if (otp.checked == false && ill.checked == false) {
        beginLongDate.set({
            disabled:true,
            displayedValue:""
        });
        endLongDate.set({
            disabled:true,
            displayedValue:""
        });
        calDate.set("disabled", false);
        setDuringDate();
    } else {
        beginLongDate.set("disabled", false);
        endLongDate.set("disabled", false);
        calDate.set({
            disabled:true,
            displayedValue:""
        });
    }
}

/* Устанавливает компоненту calDate текущую дату в качестве значения по умолчанию. */
function setDuringDate() {
    var d = new Date();
    var day = d.getDate();
    var month = d.getMonth(); // 0-11
    var year = d.getFullYear();
    var during_date = day + "." + (month + 1) + "." + year;
    var lang = navigator.userLanguage || navigator.language || navigator.browserLanguage;
    if (/en/.exec(lang) != null)
        during_date = (month + 1) + "/" + day + "/" + year;
    var date_picker = dijit.byId("calDate");
    date_picker.set("displayedValue", during_date);
}

/* Создает cookie с указанными параметрами */
function setCookie(name, value, exp, pth, dmn, sec) {
    document.cookie = name + '=' + escape(value)
        + ((exp) ? '; expires=' + exp : '')
        + ((pth) ? '; path=' + pth : '')
        + ((dmn) ? '; domain=' + dmn : '')
        + ((sec) ? '; secure' : '');
}

/* Выдает время через d дней h часов m минут */
function TimeAfter(d, h, m) {
    var now = new Date(), // объект класса Data
        nowMS = now.getTime(), // в миллисекундах (мс)
        newMS = ((d * 24 + h) * 60 + m) * 60 * 1000 + nowMS;
    now.setTime(newMS);    // новое время в мс
    return now.toGMTString();
}

/* Удаляет куки с данным именем */
function deleteCookie(CookieName) {
    setCookie(CookieName, '', TimeAfter(-1, 0, 0));
}

/* Узнает, имеется ли куки с данным именем */
function existsCookie(CookieName) {
    return (document.cookie.split(CookieName + '=').length > 1);
}

/* Выдает значение куки с данным именем */
function CookieValue(CookieName) {
    var razrez = document.cookie.split(CookieName + '=');
    if (razrez.length > 1) { // Значит, куки с этим именем существует
        var hvost = razrez[1],
            tzpt = hvost.indexOf(';'),
            EndOfValue = (tzpt > -1) ? tzpt : hvost.length;
        return unescape(hvost.substring(0, EndOfValue));
    }
}

/*
 * Растягивает по высоте текстовую область, если введённый 
 * в неё текст не умещается.
 */
function textareaAutoGrow(obj) {
    var textarea = null;
    if (obj.target == null) {
        textarea = obj;
    }
    else {
        textarea = obj.target;
    }
    var rowsMin = 4;
    var rowsMax = 16;
    var str = textarea.value;
    var cols = textarea.cols;
    var linecount = 0;
    var arStr = str.split("\n");
    for (var i = arStr.length - 1; i >= 0; --i) {
        linecount = linecount + 1 + Math.floor(arStr[i].length / cols);
    }
    linecount++;
    linecount = Math.max(rowsMin, linecount);
    linecount = Math.min(rowsMax, linecount);
    textarea.rows = linecount;
}

/*
 * Срабатывает при смене значения в списке подразделений.
 * Управляет содержимым списка сотрудников в зависимости от выбранного
 * значения в списке подразделений.
 */
function divisionChange(obj) {
    var divisionId = null;
    var employeeSelect = dojo.byId("employeeId");
    var employeeOption = null;
//    нет кнопки
//    var but = document.getElementById("view_reports_button");
//    if (but != null) {
//        dojo.attr("view_reports_button", {disabled:"disabled"});
//    }
    if (obj.target == null) {
        divisionId = obj.value;
    }
    else {
        divisionId = obj.target.value;
    }
    //Очищаем список сотрудников.
    employeeSelect.options.length = 0;
    for (var i = 0; i < employeeList.length; i++) {
        if (divisionId == employeeList[i].divId) {
            insertEmptyOption(employeeSelect);
            for (var j = 0; j < employeeList[i].divEmps.length; j++) {
                if (employeeList[i].divEmps[j].id != 0 && employeeList[i].divEmps[j].id != 27) {
                    employeeOption = dojo.doc.createElement("option");
                    dojo.attr(employeeOption, {
                        value:employeeList[i].divEmps[j].id
                    });
                    employeeOption.title = employeeList[i].divEmps[j].value;
                    employeeOption.innerHTML = employeeList[i].divEmps[j].value;
                    employeeSelect.appendChild(employeeOption);
                }
            }
        }
    }
    sortSelectOptions(employeeSelect);
    if (divisionId == 0) {
        insertEmptyOption(employeeSelect);
    }
    if (divisionId == 1) {
        var employeeOption = dojo.doc.createElement("option");
        dojo.attr(employeeOption, {
            value:'27'
        });
        employeeOption.innerHTML = 'Тестовый';
        employeeSelect.appendChild(employeeOption);
    }
    var rows = dojo.query(".row_number");
    for (var i = 0; i < rows.length; i++) {
        fillProjectList(i, dojo.byId("activity_type_id_" + i).value);
    }
}

/*
 * Срабатывает при смене значения в списке "Тип активности".
 * Управляет доступностью компонентов соответсвующей строки
 * табличной части отчёта в соответствии с определённой логикой.
 */
function typeActivityChange(obj) {
    var select = null;
    if (obj.target == null) {
        select = obj;
    }
    else {
        select = obj.target;
    }
    var selectId = dojo.attr(select, "id");
    var rowIndex = selectId.substring(selectId.lastIndexOf("_") + 1, selectId.length);
    // Проект или Пресейл
    if ((select.value == "12") || (select.value == "13")) {
        dojo.removeAttr("project_id_" + rowIndex, "disabled");
        dojo.removeAttr("project_role_id_" + rowIndex, "disabled");
        fillProjectList(rowIndex, select.value);

    }
    // Внепроектная активность
    else if (select.value == "14") {
        dojo.attr("project_id_" + rowIndex, {
            disabled:"disabled",
            value:"0"
        });
        dojo.attr("cqId_id_" + rowIndex, {
            disabled:"disabled",
            value:"0"
        });
    } else if (select.value == "0") {
        resetRowState(rowIndex, true);
    } else if (select.value == "17") { //Болезнь
        var duration = parseFloat(dojo.attr("duration_id_" + rowIndex, "value"));
        resetRowState(rowIndex, false);
        dojo.removeAttr("duration_id_" + rowIndex, "disabled");
        if (!isNaN(duration)) {
            dojo.attr("duration_id_" + rowIndex, { value:duration });
        }
    } else if ((select.value == "15") || (select.value == "24")) { //Отгулы
        var duration = parseFloat(dojo.attr("duration_id_" + rowIndex, "value"));
        var description = dojo.byId("description_id_" + rowIndex).innerHTML;
        resetRowState(rowIndex, false);
        dojo.removeAttr("duration_id_" + rowIndex, "disabled");
        dojo.removeAttr("description_id_" + rowIndex, "disabled");
        if (rowIndex == GetFirstIdDescription()) {
            dojo.removeAttr("add_in_comments", "disabled");
        }
        dojo.byId("description_id_" + rowIndex).innerHTML = description;
        if (!isNaN(duration)) {
            dojo.attr("duration_id_" + rowIndex, { value:duration });
        }
    } else if (select.value == "16") { //Отпуск
        resetRowState(rowIndex, false);
    } else if (select.value == "18") { //Не рабочий день
        resetRowState(rowIndex, false);
    }
    if (select.value == "13") {
        dojo.attr("cqId_id_" + rowIndex, {
            disabled:"disabled",
            value:"0"
        });
    }
    if ((select.value == "12") || (select.value == "13") || (select.value == "14")) {
        dojo.removeAttr("activity_category_id_" + rowIndex, "disabled");
        dojo.removeAttr("description_id_" + rowIndex, "disabled");
        if (rowIndex == GetFirstIdDescription()) {
            dojo.removeAttr("add_in_comments", "disabled");
        }
        dojo.removeAttr("problem_id_" + rowIndex, "disabled");
        dojo.removeAttr("duration_id_" + rowIndex, "disabled");
        dojo.removeAttr("project_role_id_" + rowIndex, "disabled");
        setDefaultEmployeeJob(rowIndex);
        fillAvailableActivityCategoryList(rowIndex);
    }
}

/* Заполняет список доступных проектов/пресейлов */
function fillProjectList(rowIndex, projectState) {
    var projectSelect = dojo.byId("project_id_" + rowIndex);
    var division = dojo.byId("divisionId").value;
    if (division != 0) {
        for (var i = 0; i < projectList.length; i++) {
            if (division == projectList[i].divId) {
                projectSelect.options.length = 0;
                insertEmptyOption(projectSelect);
                for (var j = 0; j < projectList[i].divProjs.length; j++) {
                    if (projectList[i].divProjs[j].state == projectState) {
                        projectOption = dojo.doc.createElement("option");
                        dojo.attr(projectOption, {
                            value:projectList[i].divProjs[j].id
                        });
                        projectOption.title = projectList[i].divProjs[j].value;
                        projectOption.innerHTML = projectList[i].divProjs[j].value;
                        projectSelect.appendChild(projectOption);
                    }
                }
            }
        }
        if (existsCookie('aplanaProject')) {
            projectSelect.value = CookieValue('aplanaProject');
            projectChange(projectSelect);
        }
    } else {

    }
    sortSelectOptions(projectSelect);
    //Добавляем в конец списка "Неучтённый пресейл", если тип активности "Пресейловая"
    if (projectState == 13) {
        projectOption = dojo.doc.createElement("option");
        dojo.attr(projectOption, {
            value:"18"
        });
        projectOption.innerHTML = "Неучтённый пресейл";
        projectSelect.appendChild(projectOption);
    }
}

/* Добавляет в указанный select пустой option. */
function insertEmptyOption(select) {
    var option = dojo.doc.createElement("option");
    dojo.attr(option, {
        value:"0"
    });
    option.innerHTML = "";
    select.appendChild(option);
}

/*
 * Срабатывает при смене значения в списке проектов\пресейлов.
 * Управляет доступностью и содержимым списка проектных задач.
 */
function projectChange(obj) {
    var select = null;
    if (obj.target == null) {
        select = obj;
    }
    else {
        select = obj.target;
    }
    var selectId = dojo.attr(select, "id");
    var projectId = select.value;
    var rowIndex = selectId.substring(selectId.lastIndexOf("_") + 1, selectId.length);
    var taskSelect = dojo.byId("cqId_id_" + rowIndex);
    var taskOption = null;
    taskSelect.options.length = 0;
    dojo.attr(taskSelect, {
        disabled:"disabled",
        value:"0"
    });
    for (var i = 0; i < projectTaskList.length; i++) {
        if (projectId == projectTaskList[i].projId) {
            dojo.removeAttr(taskSelect, "disabled");
            insertEmptyOption(taskSelect);
            for (var j = 0; j < projectTaskList[i].projTasks.length; j++) {
                taskOption = dojo.doc.createElement("option");
                dojo.attr(taskOption, {
                    value:projectTaskList[i].projTasks[j].id
                });
                taskOption.title = projectTaskList[i].projTasks[j].value;
                taskOption.innerHTML = projectTaskList[i].projTasks[j].value;
                taskSelect.appendChild(taskOption);
            }
        }
    }
    sortSelectOptions(taskSelect);
}

/* Выставляет должность сотрудника (проектная роль по умолчанию) */
function setDefaultEmployeeJob(rowIndex) {
    var selectedEmployeeId = dojo.byId("employeeId").value;
    var divisionId = dojo.byId("divisionId").value;
    var defaultEmployeeJobId = 0;
//    кнопки нет
//    var but = document.getElementById("view_reports_button");
//    if (but != null) {
//        if (selectedEmployeeId != 0) {
//            //dojo.removeAttr("view_reports_button", "disabled");
//        }
//        if (selectedEmployeeId == 0) {
//            //dojo.attr("view_reports_button", {disabled:"disabled"});
//        }
    //}
    for (var i = 0; i < employeeList.length; i++) {
        if (divisionId == employeeList[i].divId) {
            for (var j = 0; j < employeeList[i].divEmps.length; j++) {
                if (employeeList[i].divEmps[j].id == selectedEmployeeId) {
                    defaultEmployeeJobId = employeeList[i].divEmps[j].jobId;
                    break;
                }
            }
        }
    }
    var actTypeLists = new Array();
    if (rowIndex >= 0) {
        actTypeLists.push(dojo.byId("activity_type_id_" + rowIndex));
    } else { //Если функция вызвана при выборе сотрудника
        actTypeLists = dojo.query(".activityType");
    }

    for (var j = 0; j < actTypeLists.length; j++) {
        var listId = dojo.attr(actTypeLists[j], "id");
        var row = listId.substring(listId.lastIndexOf("_") + 1, listId.length);
        // Проект Пресейл
        if ((actTypeLists[j].value == "12") || (actTypeLists[j].value == "13")) {
            var projectRoleList = dojo.byId("project_role_id_" + row);
            dojo.attr(projectRoleList, { value:defaultEmployeeJobId });
            fillAvailableActivityCategoryList(row);
        }
        // Внепроектная активность
        else if (actTypeLists[j].value == "14") {
            var projectRoleList = dojo.byId("project_role_id_" + row);
            dojo.attr(projectRoleList, { value:defaultEmployeeJobId });
            fillAvailableActivityCategoryList(row);
        }
    }
}

/*
 * функция чтобы показать хинт для уже выбранных значений селектов
 * здесь title-это атрибут у селекта - он же хинт
 */
function getTitle(processed) {
    var select = null;
    if (processed.target == null) {
        select = processed;
    }
    else {
        select = processed.target;
    }
    var index = select.selectedIndex;
    if (select.options != null) {
        if ((index > -1) && (select.options[index].text != "")) {
            return select.options[index].text;
        }
        else {
            return 'значение еще не выбрано';
        }
    }
    else if (select.textbox != null) {
        if (select.textbox.value != "") return select.textbox.value
        else return 'значение еще не выбрано';
    }
}

/*
 * Восстанавливает содержимое компонентов каждой строки табличной части отчёта
 * после возврата страницы валидатором.
 */
function reloadRowsState() {
    var rowsCount = dojo.query(".time_sheet_row").length;
    var rows=dojo.query(".time_sheet_row");
    for (var i = 0; i < rowsCount; i++) {
        var actTypeSelect = dojo.byId("activity_type_id_" + i);
        typeActivityChange(actTypeSelect);
        var projectSelect = dojo.byId("project_id_" + i);
        if (dojo.attr(projectSelect, "disabled") != "disabled") {
            for (var k = 0; k < selectedProjects.length; k++) {
                if (selectedProjects[k].row == i) {
                    dojo.attr(projectSelect, { value:selectedProjects[k].project });
                }
            }
            projectChange(projectSelect);
        }
        var projectRoleSelect = dojo.byId("project_role_id_" + i);
        if (dojo.attr(projectRoleSelect, "disabled") != "disabled") {
            for (var p = 0; p < selectedProjectRoles.length; p++) {
                if (selectedProjectRoles[p].row == i) {
                    dojo.attr(projectRoleSelect, { value:selectedProjectRoles[p].role });
                }
            }
        }
        var taskSelect = dojo.byId("cqId_id_" + i);
        if (dojo.attr(taskSelect, "disabled") != "disabled") {
            for (var j = 0; j < selectedProjectTasks.length; j++) {
                if (selectedProjectTasks[j].row == i) {
                    dojo.attr(taskSelect, { value:selectedProjectTasks[j].task });
                }
            }
        }
        var actCatSelect = dojo.byId("activity_category_id_" + i);
        if ((dojo.attr(actCatSelect, "disabled") != "disabled")) {
            for (var q = 0; q < selectedActCategories.length; q++) {
                if (selectedActCategories[q].row == i) {
                    fillAvailableActivityCategoryList(i);
                    dojo.attr(actCatSelect, { value:selectedActCategories[q].actCat });
                }
            }
        }

        if(dojo.byId("delete_button_"+i)===null || dojo.byId("delete_button_"+i) === undefined) {
            var deleteCell = rows[i].cells[0];
            var img = dojo.doc.createElement("img");
            dojo.addClass(img, "pointer");
            dojo.attr(img, {
                id:"delete_button_" + i,
                src:"resources/img/delete.png",
                alt:"Удалить",
                //без px так как IE не понимает
                height:"15",
                width:"15"
            });

            //неведома ошибка исправляется для IE добавлением onclick именно через функцию
            //индускод
            img.onclick = function () {
                var id = dojo.attr(this, "id");
                var id_num = parseInt(id.substring(id.lastIndexOf("_") + 1, id.length));
                console.log(id_num);
                deleteRow(id_num);

            }
            deleteCell.appendChild(img);
        }

        sortSelectOptions(actCatSelect);
        sortSelectOptions(projectSelect);
        sortSelectOptions(projectRoleSelect);
        sortSelectOptions(taskSelect);
    }
}

/* Восстанавливает содержимое компонентов отчёта после возврата страницы валидатором. */
function reloadTimeSheetState() {
    /* После аутентификации ldap id сотрудника приходит с сервера
     dojo.byId("divisionId").onchange();
     //если айди сотрудника есть в куки - берем его иначе выставляем ноль
     if (existsCookie('aplanaEmployee')) {
     dojo.byId("employeeId").value = CookieValue('aplanaEmployee');
     }
     else {
     dojo.byId("employeeId").value = "0";
     } */

    if (selectedCalDate !== "") {
        var date_picker = dijit.byId("calDate");
        date_picker.set("displayedValue", selectedCalDate);
    }

    var beginDateStr = selectedLongVacationIllness[0].beginDate;
    var endDateStr = selectedLongVacationIllness[0].endDate;
    dijit.byId("begin_long_date").set("displayedValue", timestampStrToDisplayStr(beginDateStr));
    dijit.byId("end_long_date").set("displayedValue", timestampStrToDisplayStr(endDateStr));

    if (selectedLongVacationIllness[0].vacation == "true") {
        var longVacation = dojo.byId("long_vacation");
        longVacation.checked = true;
        longVacation.onclick();
    } else if (selectedLongVacationIllness[0].illness == "true") {
        var longIllness = dojo.byId("long_illness");
        longIllness.checked = true;
        longIllness.onclick();
    } else {
        reloadRowsState();
    }
}

/*
 * Очищает содержимое компонентов каждой строки табличной части отчёта.
 * Если resetActType == true, "Тип активности" тоже очищается.
 */
function resetRowState(rowIndex, resetActType) {
    if (resetActType) {
        //dojo.byId("selected_row_id_" + rowIndex).checked = false;
        dojo.attr("activity_type_id_" + rowIndex, {
            value:"0"
        });
    }
    dojo.attr("project_id_" + rowIndex, {
        disabled:"disabled",
        value:"0"
    });
    dojo.attr("project_role_id_" + rowIndex, {
        disabled:"disabled",
        value:"0"
    });
    dojo.attr("activity_category_id_" + rowIndex, {
        disabled:"disabled",
        value:"0"
    });
    dojo.attr("cqId_id_" + rowIndex, {
        disabled:"disabled",
        value:"0"
    });
    dojo.byId("description_id_" + rowIndex).value = "";
    dojo.attr("description_id_" + rowIndex, {
        disabled:"disabled"
    });


    if (rowIndex == GetFirstIdDescription()) {
        dojo.attr("add_in_comments", {
            disabled:"disabled"
        });
    }

    dojo.byId("problem_id_" + rowIndex).value = "";
    dojo.attr("problem_id_" + rowIndex, {
        disabled:"disabled"
    });

    dojo.attr("duration_id_" + rowIndex, {
        disabled:"disabled",
        value:""
    });
    recalculateDuration();
}

/* Делает строку табличной части отчёта с номером rowIndex недоступной. */
function disableRow(rowIndex) {
    resetRowState(rowIndex, true);
    dojo.attr("activity_type_id_" + rowIndex, { disabled:"disabled" });
}

/* Делает все строки табличной части отчёта недоступными. */
function disableRows() {
    var rows = getRowsId(dojo.query(".time_sheet_row"));
    for (var i = 0; i < rows.length; i++) {
        disableRow(rows[i]);
    }
}

/* Делает строку табличной части отчёта с номером rowIndex доступной. */
function enableRow(rowIndex) {
    var actType = dojo.byId("activity_type_id_" + rowIndex);
    dojo.removeAttr(actType, "disabled");
}

/* Делает все строки табличной части отчёта с номером rowIndex доступными. */
function enableRows() {
    var rows = getRowsId(dojo.query(".time_sheet_row"));
    for (var i = 0; i < rows.length; i++) {
        enableRow(rows[i]);
    }
}

/* Проверяет введённое значение часов, потраченных на выполнение задачи на валидность. */
function checkDuration(processed) {
    var input = null;
    if (processed.target == null) {
        input = processed;
    }
    else {
        input = processed.target;
    }
    var duration = dojo.attr(input, "value");
    var reg = /^\d{1,2}((\.|\,)\d)?$/;
    if (!reg.test(duration)) {
        duration = "";
    }
    dojo.attr(input, { value:duration    });
    recalculateDuration();
}

/* Производит пересчет общего количества часов, потраченных на все задачи. */
function recalculateDuration() {
    var totalDurationValue = 0.0;
    var hoursNodes = dojo.query(".duration");
    for (var i = 0; i < hoursNodes.length; i++) {
        var hours = parseFloat(dojo.attr(hoursNodes[i], "value").replace(",", "."));
        if (!isNaN(hours)) {
            totalDurationValue += hours;
        }
    }
    /* Округление 1 цифры после запятой APLANATS-336 */
    dojo.byId("total_duration").innerHTML = totalDurationValue.toFixed(1);
    return totalDurationValue;
}

/* Добавляет в табличную часть отчёта указанное количество новых строк. */
function addNewRows(rowsCount) {
    var rows = dojo.query(".row_number");
    if (rows.length == 0) {
        for (var i = 0; i < rowsCount; i++) {
            addNewRow();
        }
    }
}

/* Удаляет из табличной части отчёта выделенные строки. */
function delSelectedRows() {
    var allCheckboxes = dojo.query(".selectedRow");
    var checkedCnt = 0;
    // то что внутри table->tbody
    for (var i = 0; i < allCheckboxes.length; i++) {
        if (allCheckboxes[i].checked == true) {
            checkedCnt++;
            // Получаем индекс удаляемой строки.
            var selectedCheckboxId = allCheckboxes[i].id;
            var selectedRowIndex = parseInt(selectedCheckboxId.substring(
                selectedCheckboxId.lastIndexOf("_") + 1,
                selectedCheckboxId.length));
            var tsRow = document.getElementById("ts_row_" + selectedRowIndex);
            if (tsRow !== null) {
                tsRow.parentNode.removeChild(tsRow);
            }
        }
    }
    recalculateRowNumbers();
    recalculateDuration();
}
//ищет идентификатор первой строки
function GetFirstIdDescription() {
    var tsTable = dojo.byId("time_sheet_table");
    var tsRows = dojo.query(".time_sheet_row");
    var firstRow = tsRows[0];
    var firstRowId = dojo.attr(firstRow, "id");
    var lastRow = parseInt(firstRowId.substring(firstRowId.lastIndexOf("_") + 1, firstRowId.length));
    return lastRow;
}

/* Производит пересчёт номеров строк табличной части отчёта. */
function recalculateRowNumbers() {
    var rows = dojo.query(".row_number");
    var amount_rows = rows.length;
    for (var i = 0; i < rows.length; i++) {
        rows[i].innerHTML = i + 1;
    }

    //дизейблим копирование в комментарий если нет в таблице строк
    if (amount_rows == 0) {
        dojo.attr("add_in_comments", {
            disabled:"disabled"
        });
    }
    else {
        var lastRowIndex = GetFirstIdDescription();
        if (dojo.attr("description_id_" + lastRowIndex, "disabled")) {
            dojo.attr("add_in_comments", {
                disabled:"disabled"
            });
        }
        else {
            dojo.removeAttr("add_in_comments", "disabled");
        }
    }
}

/* Отображает диалог подтверждения отправки отчёта. */
function confirmSendReport() {
    var totalDuration = recalculateDuration();
    var vacation = dojo.byId("long_vacation");
    var illness = dojo.byId("long_illness");
    var actTypes = dojo.query(".activityType");
    var reportDate = dijit.byId("calDate").value;
    var oob = false; //Отпуск или отгул или болезнь
    for (var i = 0; i < actTypes.length; i++) {
        if (actTypes[i].value == "17"
            || actTypes[i].value == "15" || actTypes[i].value == "24"
            || actTypes[i].value == "16" || actTypes[i].value == "18") {
            oob = true;
        }
    }
    if ((totalDuration < 8 || totalDuration > 8)
        && (vacation.checked != true && illness.checked != true)
        && !oob) {
        return confirm("Количество отработанных часов отлично от 8. Вы действительно хотите отправить отчет?");
    }
    else if (reportDate !== null) {
        if (reportDate !== undefined) {
            if (dateNotBetweenMonth(reportDate))
                return confirm("Указанная дата отличается от текущей более чем на 27 дней. Вы уверены, что хотите отправить отчет?");
        }
        else
            return confirm("Указанная дата некорректна. Вы уверены, что хотите отправить отчет?");
    }
    return confirm("Вы действительно хотите отправить отчет?");
}

function dateNotBetweenMonth(value) {
    var maxDate = new Date();
    maxDate.setDate(maxDate.getDate() + 27);
    var minDate = new Date();
    minDate.setDate(minDate.getDate() - 27);
    return value > maxDate || value < minDate;
}

function invalidReportDate(value) {
    var maxDate = new Date();
    maxDate.setDate(maxDate.getDate() + 27);
    var minDate = new Date();
    minDate.setDate(minDate.getDate() - 27);
    if (value > maxDate) return 1;
    if (value < minDate) return -1;
    return 0;
}

/* Отображает диалог подтверждения создания нового отчёта. */
function confirmCreateNewReport() {
    return confirm("Вы уверены?");
}

/*
 * Заполняет список "Категория активности" доступными значениями
 * в соответствии с определённой логикой.
 */
function fillAvailableActivityCategoryList(rowIndex) {
    var actTypeSelect = dojo.byId("activity_type_id_" + rowIndex);
    var projectRoleSelect = dojo.byId("project_role_id_" + rowIndex);
    var actCatSelect = dojo.byId("activity_category_id_" + rowIndex);
    var actType = dojo.attr(actTypeSelect, "value");
    var projectRole = dojo.attr(projectRoleSelect, "value");
    actCatSelect.options.length = 0;
    insertEmptyOption(actCatSelect);
    for (var i = 0; i < availableActCategoryList.length; i++) {
        if (actType == availableActCategoryList[i].actType && projectRole != 0 && projectRole == availableActCategoryList[i].projRole) {
            for (var j = 0; j < availableActCategoryList[i].avActCats.length; j++) {
                var actCatOption = dojo.doc.createElement("option");
                dojo.attr(actCatOption, {
                    value:availableActCategoryList[i].avActCats[j]
                });
                for (var k = 0; k < actCategoryList.length; k++) {
                    if (availableActCategoryList[i].avActCats[j] == actCategoryList[k].id) {
                        actCatOption.title = actCategoryList[k].value;
                        actCatOption.innerHTML = actCategoryList[k].value;
                        actCatSelect.appendChild(actCatOption);
                    }
                }
            }
        }
    }
    sortSelectOptions(actCatSelect);
}

/*
 * Срабатывает при смене значения в списке "Проектная роль"
 * Влияет на доступные категории активности.
 */
function projectRoleChange(obj) {
    var select = null;
    if (obj.target == null) {
        select = obj;
    }
    else {
        select = obj.target;
    }
    var selectId = dojo.attr(select, "id");
    var rowIndex = selectId.substring(selectId.lastIndexOf("_") + 1, selectId.length);
    fillAvailableActivityCategoryList(rowIndex);
}

/* Сортирует по алфавиту содержимое выпадающих списков. */
function sortSelectOptions(select) {
    var tmpArray = [];
    for (var i = 0; i < select.options.length; i++) {
        tmpArray.push(select.options[i]);
    }
    tmpArray.sort(function (a, b) {
        return (a.text < b.text) ? -1 : 1;
    });
    select.options.length = 0;
    for (var i = 0; i < tmpArray.length; i++) {
        select.options[i] = tmpArray[i];
    }
}

/*
 * Превращает timestamp строку (yyyy-mm-dd) в строку для
 * displayValue DateTextBoxА (dd.mm.yyyy) 
 */
function timestampStrToDisplayStr(str) {
    if (str != "") {
        var splittedStr = str.split("-");
        return splittedStr[2] + "." + splittedStr[1] + "." + splittedStr[0];
    } else {
        return str;
    }
}
/*Проверяет, какой тип проблемы выбран. Если выбран тип "Меня нет в списках",
 то делает списки подразделений и сотрудников неактивными*/
function feedbackTypeChange(obj) {
    var select = obj;
    if (select.value == "Меня нет в списке") {
        dojo.attr("divisionId", {disabled:"disabled", value:"0" });
        dojo.attr("employeeId", {disabled:"disabled", value:"0"});

        dojo.removeAttr("name", "disabled");
        dojo.removeAttr("email", "disabled");
        name_email.style.display = 'block';

    } else {
        dojo.removeAttr("divisionId", "disabled");
        dojo.removeAttr("employeeId", "disabled");

        dojo.attr("name", {disabled:"disabled"});
        dojo.attr("email", {disabled:"disabled"});
        name_email.style.display = 'none';
    }
}
function confirmCancelWindow() {
    return confirm("Вы действительно хотите закрыть окно?");
}
function confirmClearWindow() {
    return confirm("Вы действительно хотите очистить окно?");
}
function confirmTimeSheetCloseWindow() {
    if (tablePartNotEmpty() || planBoxNotEmpty())
        return "Отчет не был отправлен.";
    /*else
     {
     event.returnValue = false;
     return false;
     } */
}
function setIDs() {
    divId = dojo.byId("divisionID").value;
    empId = dojo.byId("empIdID").value;
}

function openViewReportsWindow() {
    var employeeId = dojo.byId("employeeId").value;
    var divisionId = dojo.byId("divisionId").value;
    if (employeeId != 0) {
        var date = new Date();
        window.open('viewreports/' + divisionId + '/' + employeeId + '/' + date.getFullYear() + '/' + (date.getMonth() + 1), 'reports_window' + employeeId);
    }
}
;

function maskBody() {
    dojo.query('#maskDiv').addClass("masked");

}

/* объект подсказки */
var tooltip = function () {
    var id = 'tt';
    var top = 3;
    var left = 3;
    var maxw = 300;
    var speed = 10;
    var timer = 20;
    var endalpha = 95;
    var alpha = 0;
    var tt, t, c, b, h;
    var ie = document.all ? true : false;
    return{
        show:function (v, w) {
            if (tt == null) {
                tt = document.createElement('div');
                tt.setAttribute('id', id);
                t = document.createElement('div');
                t.setAttribute('id', id + 'top');
                c = document.createElement('div');
                c.setAttribute('id', id + 'cont');
                b = document.createElement('div');
                b.setAttribute('id', id + 'bot');
                tt.appendChild(t);
                tt.appendChild(c);
                tt.appendChild(b);
                document.body.appendChild(tt);
                tt.style.opacity = 0;
                tt.style.filter = 'alpha(opacity=0)';
                document.onmousemove = this.pos;
            }
            tt.style.display = 'block';
            c.innerHTML = v;
            tt.style.width = w ? w + 'px' : 'auto';
            if (!w && ie) {
                t.style.display = 'none';
                b.style.display = 'none';
                tt.style.width = tt.offsetWidth;
                t.style.display = 'block';
                b.style.display = 'block';
            }
            if (tt.offsetWidth > maxw) {
                tt.style.width = maxw + 'px'
            }
            h = parseInt(tt.offsetHeight) + top;
            clearInterval(tt.timer);
            tt.timer = setInterval(function () {
                tooltip.fade(1)
            }, timer);
        },
        pos:function (e) {
            var u = ie ? event.clientY + document.documentElement.scrollTop : e.pageY;
            var l = ie ? event.clientX + document.documentElement.scrollLeft : e.pageX;
            tt.style.top = (u - h) + 'px';
            tt.style.left = (l + left) + 'px';
        },
        fade:function (d) {
            var a = alpha;
            if ((a != endalpha && d == 1) || (a != 0 && d == -1)) {
                var i = speed;
                if (endalpha - a < speed && d == 1) {
                    i = endalpha - a;
                } else if (alpha < speed && d == -1) {
                    i = a;
                }
                alpha = a + (i * d);
                tt.style.opacity = alpha * .01;
                tt.style.filter = 'alpha(opacity=' + alpha + ')';
            } else {
                clearInterval(tt.timer);
                if (d == -1) {
                    tt.style.display = 'none'
                }
            }
        },
        hide:function () {
            clearInterval(tt.timer);
            tt.timer = setInterval(function () {
                tooltip.fade(-1)
            }, timer);
        }
    };
}();

/* Заполняет список доступных проектов/пресейлов */
//function fillProjectListByDivision(division) {
function fillProjectListByDivision(division) {

    var checkBox = dojo.byId("filterProjects");

    if (division == null) {
        division = dojo.byId("divisionId");

        if ((checkBox.checked) && (division.value == 0))
            division.value = defaultDivision;
    }

    var divisionId = division.value;

    var projectSelect = dojo.byId("projectId");

    projectSelect.options.length = 0;

    if (checkBox.checked) {

        dojo.removeAttr("divisionId", "disabled");

        if (divisionId != 0) {

            for (var i = 0; i < projectList.length; i++) {
                if ((divisionId == projectList[i].divId) || (!checkBox.checked)) {
                    insertEmptyOption(projectSelect);
                    for (var j = 0; j < projectList[i].divProjs.length; j++) {
                        projectOption = dojo.doc.createElement("option");
                        dojo.attr(projectOption, {
                            value:projectList[i].divProjs[j].id
                        });
                        projectOption.title = projectList[i].divProjs[j].value;
                        projectOption.innerHTML = projectList[i].divProjs[j].value;
                        projectSelect.appendChild(projectOption);
                    }
                }
            }
        } else {
            insertEmptyOption(projectSelect);
        }
    }
    else {

        division.value = 0;

        dojo.attr("divisionId", {disabled:"disabled"});

        insertEmptyOption(projectSelect);
        for (var i = 0; i < fullProjectList.length; i++) {
            projectOption = dojo.doc.createElement("option");
            dojo.attr(projectOption, {
                value:fullProjectList[i].id
            });
            projectOption.title = fullProjectList[i].value;
            projectOption.innerHTML = fullProjectList[i].value;
            projectSelect.appendChild(projectOption);
        }
    }
    sortSelectOptions(projectSelect);
}

function fillEmployeeListByDivision(division) {
    var employeeSelect = dojo.byId("employeeId");
    var employeeOption = null;

    var divisionId = division.value;
    //Очищаем список сотрудников.
    employeeSelect.options.length = 0;
    for (var i = 0; i < employeeList.length; i++) {
        if (divisionId == employeeList[i].divId) {
            insertEmptyOption(employeeSelect);
            for (var j = 0; j < employeeList[i].divEmps.length; j++) {
                if (employeeList[i].divEmps[j].id != 0 && employeeList[i].divEmps[j].id != 27) {
                    employeeOption = dojo.doc.createElement("option");
                    dojo.attr(employeeOption, {
                        value:employeeList[i].divEmps[j].id
                    });
                    employeeOption.title = employeeList[i].divEmps[j].value;
                    employeeOption.innerHTML = employeeList[i].divEmps[j].value;
                    employeeSelect.appendChild(employeeOption);
                }
            }
        }
    }
    sortSelectOptions(employeeSelect);
    if (divisionId == 0) {
        insertEmptyOption(employeeSelect);
    }
    if (divisionId == 1) {
        var employeeOption = dojo.doc.createElement("option");
        dojo.attr(employeeOption, {
            value:'27'
        });
        employeeOption.innerHTML = 'Тестовый';
        employeeSelect.appendChild(employeeOption);
    }
    var rows = dojo.query(".row_number");
    for (var i = 0; i < rows.length; i++) {
        fillProjectList(i, dojo.byId("activity_type_id_" + i).value);
    }
}
