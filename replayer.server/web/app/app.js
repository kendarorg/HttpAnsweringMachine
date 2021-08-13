
var getUrlParameter = function(sParam) {
    var sPageURL = window.location.search.substring(1),
        sURLVariables = sPageURL.split('&'),
        sParameterName,
        i;

    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return typeof sParameterName[1] === undefined ? true : decodeURIComponent(sParameterName[1]);
        }
    }
    return false;
};


var appendToTable = function(tSource,tableId,idField,inputData,fields) {

    var idContent=inputData[idField];
    for(var i=0;i<tSource.length;i++){
        var line = tSource[i];
        if(line[idField]==inputData[idField]){
            tSource[i]=inputData;
            for(var v=0;v<fields.length;v++){
                var index = fields[v];
                var content = inputData[index];
                $("#" + tableId + " #" + tableId + "-" + idContent+" #"+index).innerHTML = content;
            }
            return;
        }
    }
    tSource.push(inputData);
    var data = `
        <tr id="${tableId}-${idContent}">`;

    /*for(var i=0;i<fields.length;i++){
        var index = fields[i];
        var content = inputData[index];
        data+=`<td class="userData" name="${index}">${content}</td>`;
    }*/

    for(var i=0;i<fields.length;i++){
        var index = fields[i];
        var allIndex = fields[i].split(".");
        var content = inputData;
        for(var s=0;s<allIndex.length;s++){
            content = content[allIndex[s]];
        }
        data+=`<td class="userData" name="${index}">${content}</td>`;
    }


    data+=`<td align="center">
                <button class="btn btn-success form-control" onClick="edit${tableId}('${idContent}')">EDIT</button>
            </td>
            <td align="center">
                <button class="btn btn-danger form-control" onClick="delete${tableId}('${idContent}')">DELETE</button>
            </td>
        </tr>
    `;
    $("#"+tableId+" > tbody:last-child").append(data);
}


var deleteFromTable = function(tSource,tableId,idField,id,objType,callback) {
    var action = confirm("Are you sure you want to delete this "+objType+"?");
    var msg = objType+" deleted successfully!";
    var msgError = objType+" cannot be deleted!";
    tSource.forEach(function(row, i) {
        if (row[idField] == id && action != false) {
            callback(id,function(){
                tSource.splice(i, 1);
                $("#" + tableId + " #" + tableId + "-" + id).remove();
                flashMessage(msg);
            },function(){
                flashMessage(msgError);
            });
        }
    });
}

var flashMessage = function(msg) {
    $(".flashMsg").remove();
    $(".row").prepend(`
        <div class="col-sm-12"><div class="flashMsg alert alert-success alert-dismissible fade in" role="alert"> <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">×</span></button> <strong>${msg}</strong></div></div>
    `);
}


