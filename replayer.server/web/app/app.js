var scripts = [];
$.ajax({
    url: "/api/recording",
    type: 'GET',
    success: function(res) {
        $.each(res, function(i, script) {
            appendToScriptTable(script);
            appendToScriptTable("TEST");
        });
    }
});



function appendToScriptTable(script) {
    scripts.push(script);
    $("#scriptTable > tbody:last-child").append(`
        <tr id="script-${script}">
            <td class="userData" name="name">${script}</td>
            '<td align="center">
                <button class="btn btn-success form-control" onClick="editScript('${script}')">EDIT</button>
            </td>
            <td align="center">
                <button class="btn btn-danger form-control" onClick="deleteScript('${script}')">DELETE</button>
            </td>
        </tr>
    `);
}

function editScript(id) {
    location.href = "script.html?id"+id;
}

function deleteScript(id) {
    var action = confirm("Are you sure you want to delete this script?");
    $.ajax({
        url: "/api/recording/"+id,
        type: 'DELETE',
        success: function(res) {
            var msg = "Script deleted successfully!";
            scripts.forEach(function(user, i) {
                if (scripts == id && action != false) {
                    scripts.splice(i, 1);
                    $("#scriptTable #script-" + script).remove();
                    flashMessage(msg);
                }
            });
        }
    });

}



$("form").submit(function(e) {
    e.preventDefault();
});
