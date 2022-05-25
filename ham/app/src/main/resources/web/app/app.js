// noinspection JSUnusedGlobalSymbols

if (Function.prototype.name === undefined) {
    // Add a custom property to all function values
    // that actually invokes a method to get the value
    Object.defineProperty(Function.prototype, 'name', {
        get: function () {
            return /function ([^(]*)/.exec(this + "")[1];
        }
    });
}

const getUrlParameter = function (sParam, defaultVal) {
    if (defaultVal === undefined) defaultVal = false;
    const sPageURL = window.location.search.substring(1),
        sURLVariables = sPageURL.split('&');
    let sParameterName,
        i;

    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return typeof sParameterName[1] === undefined ? true : decodeURIComponent(sParameterName[1]);
        }
    }
    return defaultVal;
};

///////////////////////     SIMPLE GRID

class SimpleGrid {
    data;
    tableId;
    idField;
    fields;
    deleteFunction;
    saveFunction;
    objType;
    loadFunction;
    showSearch;
    addbutton;
    editFunction;

    constructor(objType, name, idField, fields, loadFunction, editFunction, deleteFunction, saveFunction, showSearch = []) {
        this.objType = objType;
        this.tableId = name;
        this.editFunction = editFunction;
        this.deleteFunction = deleteFunction;
        this.saveFunction = saveFunction;
        this.loadFunction = loadFunction;
        this.data = [];
        this.idField = idField;
        this.fields = fields;
        this.showSearch = showSearch;
        this.addbutton = null;
        this.buildSearch(showSearch);
    }

    filterRow(inputData) {
        if (this.isNotSearchEnabled()) return true;

        for (let i = 0; i < this.showSearch.length; i++) {
            const str = this.showSearch[i];
            const content = this.retrieveFieldComplexContent(inputData, str.id);
            const realId =  str.id.replaceAll("\.","_") + `_` + str.type;
            const valueToCompareWith = $("#" + this.tableId + " #"+realId).val();
            if(valueToCompareWith=="" || valueToCompareWith===undefined)continue;
            if (str.type == "string") {
                if(!this.compareString(valueToCompareWith,content))return false;
            } else if (str.type == "number") {
                if(!this.compareNumber(valueToCompareWith,content))return false;
            }else if (str.type == "bool") {
                if(!this.compareBool(valueToCompareWith,content))return false;

            }
        }
        return true;
    }

    compareString(valueToCompareWith, content) {
        //NOT
        if(valueToCompareWith.substring(0,2)=="!!"){
            return !(content.indexOf(valueToCompareWith.substring(2))>=0);
        }
        //CONTAINS
        return (content.indexOf(valueToCompareWith)>=0);
    }

    compareNumber(valueToCompareWith, content) {
        if(parseInt(valueToCompareWith)==parseInt(content)) return true;
        return false;
    }

    stringToBoolean(string){
        switch(string.toLowerCase().trim()){
            case "true":
            case "yes":
            case "1":
                return true;

            case "false":
            case "no":
            case "0":
            case null:
                return false;

            default:
                return false;
        }
    }

    compareBool(valueToCompareWith, content) {
        if(this.stringToBoolean(valueToCompareWith)==this.stringToBoolean(content)) return true;
        return false;
    }

    isNotSearchEnabled() {
        return this.showSearch == null || this.showSearch.length == 0;
    }

    load() {
        this.loadFunction(this);
        return this;
    }

    clearTable(callback) {
        var self = this;
        this.data.forEach(function (row, i) {
            let id = row[self.idField]+"";
            let selectedItem = $("#" + self.tableId + " #" + self.tableId + "-" + id.replaceAll("\.","_"));
            if (selectedItem) {
                selectedItem.remove();
            }
        });
        if (callback != null) {
            callback();
        }

    }

    deleteFromTable(id, callback) {
        const action = confirm("Are you sure you want to delete this " + this.objType + "?");
        const msg = this.objType + " deleted successfully!";
        const msgError = this.objType + " cannot be deleted!";
        const self = this;
        this.data.forEach(function (row, i) {
            if (row[self.idField].replaceAll("_",".") == id.replaceAll("_",".") && action != false) {
                if (callback == null || callback === undefined) {
                    self.data.splice(i, 1);
                    $("#" + self.tableId + " #" + self.tableId + "-" + id).remove();
                    flashMessage(msg);
                } else {
                    callback(id, function () {
                        self.data.splice(i, 1);
                        $("#" + self.tableId + " #" + self.tableId + "-" + id).remove();
                        flashMessage(msg);
                    }, function () {
                        flashMessage(msgError);
                    });
                }
            }
        });
    }

    buildSearch(showSearchFields) {
        if (this.isNotSearchEnabled()) return;
        let idContent = "search";
        let toWrite = `
        <tr id="${this.tableId}-${idContent}">`;

        for (let v = 0; v < this.fields.length; v++) {
            const index = this.fields[v];
            let founded = _.find(showSearchFields, function (ssf) {
                return ssf.id == index;
            });
            if (founded === undefined) {
                toWrite += `<td></td>`;
                continue;
            }
            const foundedType = founded.type;
            const id = index.replaceAll("\.","_") + `_` + foundedType;
            toWrite += `<td><div class="form-group">
                <input class="form-control" type="text" name="${id}" id="${id}" />
            </div></td>`;
        }
        const buttonId = this.tableId + "-" + idContent + "-search";
        toWrite += `<td><button id="${buttonId}" name="${buttonId}" 
        type="button"  class="btn btn-default" >
        <i class="bi bi-search"></i>Search</button></td>`;
        toWrite += `</tr>`;

        const self = this;
        $("#" + this.tableId + " > tbody:last-child").append(toWrite);
        $("#" + buttonId).click(function () {
            self.clearTable();
            for (let i = 0; i < self.data.length; i++) {
                const line = self.data[i];
                if (self.filterRow(line)) {
                    let idContent = line[self.idField];
                    if (typeof idContent === 'string') {
                        idContent = idContent.replaceAll(".", "_");
                    }
                    self.writeSingleRow(idContent, line);

                }
            }
        });
    }

    retrieveFieldComplexContent(inputData, id) {
        const allIndex = id.split(".");
        let content = inputData;
        for (let s = 0; s < allIndex.length; s++) {
            content = content[allIndex[s]];
        }
        if (content == undefined) content = "";
        return content;
    }


    appendToTable(inputData, addbutton = null) {
        if (this.addbutton == null) {
            if (addbutton != null) {
                this.addbutton = addbutton;
            } else {
                this.addbutton = true;
            }
        }
        let idContent = inputData[this.idField];
        if (typeof idContent === 'string') {
            idContent = idContent.replaceAll(".", "_");
        }
        for (let i = 0; i < this.data.length; i++) {
            const line = this.data[i];
            if (line[this.idField] == inputData[this.idField]) {
                this.data[i] = inputData;
                for (let v = 0; v < this.fields.length; v++) {
                    const index = this.fields[v];
                    let content = inputData[index];
                    if (content == undefined) content = "";
                    if (!(index.lastIndexOf("_", 0) === 0)) {
                        if (content.length > 60) content = content.substr(0, 60);
                    }

                    let selectedItem = $("#" + this.tableId +
                        " #" + this.tableId + "-" + idContent +
                        " #" + index);
                    if (selectedItem) {
                        selectedItem.innerHTML = content;
                    }
                }
                return;
            }
        }
        this.data.push(inputData);
        //if(this.filterRow(inputData))

        this.writeSingleRow(idContent, inputData);

    }

    writeSingleRow(idContent, inputData) {
        if (this.showSearch != null) {
            if (!this.filterRow(inputData)) {
                return;
            }
        }
        let toWrite = `
        <tr id="${this.tableId}-${idContent}">`;

        /*for(var i=0;i<fields.length;i++){
            var index = fields[i];
            var content = inputData[index];
            data+=`<td class="userData" name="${index}">${content}</td>`;
        }*/

        for (let i = 0; i < this.fields.length; i++) {
            const index = this.fields[i];
            /*var allIndex = this.fields[i].split(".");
            var content = inputData;
            for (var s = 0; s < allIndex.length; s++) {
                content = content[allIndex[s]];
            }
            if (content == undefined) content = "";*/
            let content = this.retrieveFieldComplexContent(inputData, index);
            if (!(this.fields[i].lastIndexOf("_", 0) === 0)) {
                if (content.length > 60) content = content.substr(0, 60);
            }
            toWrite += `<td class="userData" name="${index}" id="${index}">${content}</td>`;
        }

        if (this.addbutton == true) {
            toWrite += `<td align="center">
                <button class="btn btn-success form-control" id="${this.tableId}-${idContent}-edit">EDIT</button>
            </td>
            <td align="center">
                <button class="btn btn-danger form-control" id="${this.tableId}-${idContent}-delete">DELETE</button>
            </td>`;
        }

        toWrite += `</tr>`;

        const self = this;
        $("#" + this.tableId + " > tbody:last-child").append(toWrite);
        if (this.addbutton == true) {
            $("#" + this.tableId + "-" + idContent + "-edit").click(function () {
                self.editFunction(self, idContent);
            });
            $("#" + this.tableId + "-" + idContent + "-delete").click(function () {
                self.deleteFunction(self, idContent);
            });
        }
    }
}

///////////////////////     KVP


const buildKvpModalDialog = function(modal, table, value, idField, valueField, randomId) {
    let bodyContent = "";
    if (value[valueField].length > 60) {
        bodyContent += `
                    <label for="value">Value</label>
                    <textarea class="form-control" rows="6" cols="50" name="value" id="value" ></textarea>
                `;
    } else {
        bodyContent += `
                    <label for="value">Value</label>
                    <input class="form-control" type="text" name="value"  id="value" />
                `;
    }
    let openAsEdit = true;
    if (value[idField] == '' || value[idField] === undefined) {
        openAsEdit = false;
    }
    buildGenericModal(modal, table, value, idField, bodyContent, randomId, openAsEdit);
}

const addKvp = function (modal, table, idField, valueField) {
    const randomId = "BUTTON" + Math.floor(Math.random() * 999999999);
    const value = {};
    value[idField] = '';
    value[valueField] = '';
    buildKvpModalDialog(modal, table, value, idField, valueField, randomId);
    const localTable = table;
    $(modal).find("#" + randomId).click(function () {
        value[idField] = $(modal).find("#key").val();
        value[valueField] = $(modal).find("#value").val();
        localTable.saveFunction(localTable, value, true);
    });
    $(modal).find("#value").val(value[valueField]);
};


const editKvp = function (modal, table, id, idField, valueField) {
    const randomId = "BUTTON" + Math.floor(Math.random() * 999999999);
    const localTable = table;
    localTable.data.forEach(function (value, i) {
        if (value[idField].replaceAll(".", "_") == id) {
            buildKvpModalDialog(modal, table, value, idField, valueField, randomId);
            $(modal).find("#" + randomId).click(function () {
                localTable.saveFunction(localTable, id, false);
            });
            $(modal).find("#value").val(value[valueField]);
        }
    });
};

const getKvpData = function (table, idField, valueField) {
    const result = {};
    for (let i = 0; i < table.data.length; i++) {
        result[table.data[i][idField]] = table.data[i][valueField];
    }
    return result;
};

const updateKvp = function (modal, table, id, idField, valueField,callback) {
    const msg = table.objType + " updated successfully!";

    if (id[idField] == undefined) {
        const user = {};
        user[idField] = id;
        table.data.forEach(function (user, i) {
            if (user[idField].replaceAll(".", "_") == id) {
                $(modal).find("#editKvp").children("input").each(function () {
                    const value = $(this).val();
                    const attr = $(this).attr("name");
                    if (attr == idField) {
                        user[idField] = value;
                    } else if (attr == valueField) {
                        user[valueField] = value;
                    }
                });
                $(modal).find("#editKvp").children("textarea").each(function () {
                    const value = $(this).val();
                    const attr = $(this).attr("name");
                    if (attr == idField) {
                        user[idField] = value;
                    } else if (attr == valueField) {
                        user[valueField] = value;
                    }
                });

                if(undefined !== callback){
                    callback(user[idField],user[valueField],false);
                }
                table.data.splice(i, 1);
                table.data.splice(user[idField] - 1, 0, user);

                let userIdField = user[idField];
                if (typeof userIdField === 'string') {
                    userIdField = userIdField.replaceAll(".", "_");
                }
                $("#" + table.tableId + " #" + table.tableId + "-" + userIdField).children(".userData").each(function () {
                    const attr = $(this).attr("name");
                    if (attr == idField) {
                        $(this).text(user[idField]);
                    } else if (attr == valueField) {
                        let content = user[valueField];
                        if (content == undefined) content = "";
                        if (content.length > 60) content = content.substr(0, 60);
                        $(this).text(content);
                    }
                });
                $(modal).modal("toggle");
                flashMessage(msg);
            }
        });
    } else {
        const line = {key: id[idField], value: id[valueField]};
        if(undefined !== callback){
            callback(id[idField],id[valueField],true);
        }
        table.appendToTable(line);
        $(modal).modal("toggle");
        flashMessage(msg);
    }
};


const deleteKvp = function (modal, table, id, idField, valueField,callback) {

    table.data.forEach(function (user, i) {
        if (user[idField].replaceAll(".", "_") == id) {
            table.deleteFromTable(id, callback);
        }
    });

};


///////////////////////     FLASH MESSAGE

const flashMessage = function (msg) {
    $(".flashMsg").remove();
    $(".row").prepend(`
        <div class="col-sm-12"><div class="flashMsg alert alert-success alert-dismissible fade in" role="alert"> <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">×</span></button> <strong>${msg}</strong></div></div>
    `);
};

const setChecked = function (jqueryObj, checked) {
    if(jqueryObj.is(":hidden"))return;
    if (checked) {
        //jqueryObj.prop("checked","true");
        jqueryObj.attr("value","true");
        jqueryObj.attr('checked', 'checked');
        // jqueryObj.attr('value', "true");
    } else {
        //jqueryObj.prop("checked","false");
        jqueryObj.attr("value","");
        jqueryObj.removeAttr('checked');
        // jqueryObj.attr('value', "false");
    }
    jqueryObj.prop('checked', checked);
    return checked;
};
const toggleCheck = function (jqueryObj) {
    if(jqueryObj.is(":hidden"))return;
    var checked = jqueryObj.prop("checked");
    if(checked===undefined) checked=false;
    if (checked) {
        jqueryObj.removeAttr('checked');
    } else {
        jqueryObj.attr('checked', 'checked');
    }
    jqueryObj.attr('value', !checked);
    return !checked;
};

const downloadFile = function (urlToSend) {
    const req = new XMLHttpRequest();
    req.open("GET", urlToSend, true);
    req.responseType = "blob";
    req.onload = function (event) {
        const blob = req.response;
        const fileName = req.getResponseHeader("fileName"); //if you have the fileName header available
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = fileName;
        link.click();
    };

    req.send();
};

const buildGenericModal = function(modal, table, value, idField, extraContent, randomId, openAsEdit) {
    //var encodedValue = value[valueField].replace('"','\\"');
    if(table!=undefined && table!=null) {
        $(modal).find(".modal-title").empty().append(`${table.objType}`);
    }
    let readonly = "readonly";
    if (!openAsEdit) {
        readonly = "";
    }
    let bodyContent = `
                <form id="editKvp" action="">
                    <label for="key">Key</label>
                    <input class="form-control" type="text" name="key" id="key" ${readonly} value="${value[idField]}"/>
                `;
    bodyContent += extraContent;
    $(modal).find(".modal-body").empty().append(bodyContent);

    $(modal).find(".modal-footer").empty().append(`
                    <button type="button" type="submit" class="btn btn-primary" id="${randomId}" >Save changes</button>
                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                </form>
            `);
    $(modal).modal("toggle");
}

const buildButtonlessModal = function (modal, table, value, idField, extraContent) {
    //var encodedValue = value[valueField].replace('"','\\"');
    $(modal).find(".modal-title").empty().append(`${table.objType}`);
    const readonly = "readonly";

    let bodyContent = `
                <form id="editKvp" action="">
                    <label for="key">Key</label>
                    <input class="form-control" type="text" name="key" id="key" ${readonly} value="${value[idField]}"/>
                `;
    bodyContent += extraContent;
    $(modal).find(".modal-body").empty().append(bodyContent);

    $(modal).find(".modal-footer").empty().append(`
                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                </form>
            `);
    $(modal).modal("toggle");
}

const uuidv4 = function () {
    return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, c =>
        (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
    );
};

const success = function () {
    alert("Ok");
};

const error = function () {
    alert("Error");
};

/**
 *
 * @param str string to split
 * @param sep separator
 * @returns {*[]|*[]}
 */
const splitOnFirst = function (str, sep) {
    const index = str.indexOf(sep);
    return index < 0 ? [str] : [str.slice(0, index), str.slice(index + sep.length)];
};

/**
 *
 * @param files
 * @param callback function(dataArray)
 * @param callbackError function(error)
 * @returns {Promise<void>}
 */
const uploadAsyncFile = async function (files, callback, callbackError) {

    const filesLoaded = [];
    if (files && files.length) {
        try {
            for (let i = 0; i < files.length; i++) {
                const uploadedImageBase64 = await convertFileToBase64(files[i], callback);
                filesLoaded.push({
                    data: splitOnFirst(uploadedImageBase64, ",")[1],
                    name: files[i].name,
                    type: files[i].type
                });
            }

            callback(filesLoaded);
        } catch (exception) {
            callbackError(exception);
        }
    } else {
        callbackError("No files to upload");
    }
};

const convertFileToBase64 = function (file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => resolve(reader.result);
        reader.onerror = reject;
    });
};

const longPolling = function (milliseconds,callback){
    callback();
    var sleep = time => new Promise(resolve => setTimeout(resolve, time));
    var poll = (promiseFn, time) => promiseFn().then(
        sleep(time).then(() => poll(promiseFn, time)));

    // Greet the World every second
    poll(() => new Promise(() => callback()), milliseconds);
};

const buttonEnabler = function(buttonsMask,status){
    var status = status.toUpperCase();
    for(var i=0;i<buttonsMask.length;i++){
        var bMask = buttonsMask[i];
        var enable = false;
        for(var s=0;s<bMask.states.length;s++){
            var realState = bMask.states[s].toUpperCase();
            if(realState==status || realState=="*"){
                enable=true;
                break;
            }
        }
        if(bMask.callback!==undefined && enable){
            enable = bMask.callback();
        }
        for(var b=0;b<bMask.id.length;b++){
            var butt = bMask.id[b];
            var buttInstance = $("#"+butt);
            if(buttInstance){
                var visible = buttInstance.is(":visible");
                if(visible && !enable){
                    buttInstance.hide();
                }else if(!visible && enable){
                    buttInstance.show();
                }
            }
        }
    }
}


var Base64 = {

    // private property
    _keyStr : "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",

    // public method for encoding
    encode : function (input) {
        var output = "";
        var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
        var i = 0;

        input = Base64._utf8_encode(input);

        while (i < input.length) {

            chr1 = input.charCodeAt(i++);
            chr2 = input.charCodeAt(i++);
            chr3 = input.charCodeAt(i++);

            enc1 = chr1 >> 2;
            enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
            enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
            enc4 = chr3 & 63;

            if (isNaN(chr2)) {
                enc3 = enc4 = 64;
            } else if (isNaN(chr3)) {
                enc4 = 64;
            }

            output = output +
                this._keyStr.charAt(enc1) + this._keyStr.charAt(enc2) +
                this._keyStr.charAt(enc3) + this._keyStr.charAt(enc4);

        }

        return output;
    },

    // public method for decoding
    decode : function (input) {
        var output = "";
        var chr1, chr2, chr3;
        var enc1, enc2, enc3, enc4;
        var i = 0;

        input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");

        while (i < input.length) {

            enc1 = this._keyStr.indexOf(input.charAt(i++));
            enc2 = this._keyStr.indexOf(input.charAt(i++));
            enc3 = this._keyStr.indexOf(input.charAt(i++));
            enc4 = this._keyStr.indexOf(input.charAt(i++));

            chr1 = (enc1 << 2) | (enc2 >> 4);
            chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
            chr3 = ((enc3 & 3) << 6) | enc4;

            output = output + String.fromCharCode(chr1);

            if (enc3 != 64) {
                output = output + String.fromCharCode(chr2);
            }
            if (enc4 != 64) {
                output = output + String.fromCharCode(chr3);
            }

        }

        output = Base64._utf8_decode(output);

        return output;

    },

    // private method for UTF-8 encoding
    _utf8_encode : function (string) {
        string = string.replace(/\r\n/g,"\n");
        var utftext = "";

        for (var n = 0; n < string.length; n++) {

            var c = string.charCodeAt(n);

            if (c < 128) {
                utftext += String.fromCharCode(c);
            }
            else if((c > 127) && (c < 2048)) {
                utftext += String.fromCharCode((c >> 6) | 192);
                utftext += String.fromCharCode((c & 63) | 128);
            }
            else {
                utftext += String.fromCharCode((c >> 12) | 224);
                utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                utftext += String.fromCharCode((c & 63) | 128);
            }

        }

        return utftext;
    },

    // private method for UTF-8 decoding
    _utf8_decode : function (utftext) {
        var string = "";
        var i = 0;
        var c = c1 = c2 = 0;

        while ( i < utftext.length ) {

            c = utftext.charCodeAt(i);

            if (c < 128) {
                string += String.fromCharCode(c);
                i++;
            }
            else if((c > 191) && (c < 224)) {
                c2 = utftext.charCodeAt(i+1);
                string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
                i += 2;
            }
            else {
                c2 = utftext.charCodeAt(i+1);
                c3 = utftext.charCodeAt(i+2);
                string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
                i += 3;
            }

        }

        return string;
    }

}