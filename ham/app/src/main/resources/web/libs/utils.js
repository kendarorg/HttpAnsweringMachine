
const downloadFile = function (urlToSend,proposedName) {
    const req = new XMLHttpRequest();
    req.open("GET", urlToSend, true);
    req.responseType = "blob";
    req.onload = function (event) {
        const blob = req.response;
        let fileName = req.getResponseHeader("fileName"); //if you have the fileName header available
        if(proposedName != "" && typeof proposedName != "undefined"){
            fileName = proposedName;
        }
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = fileName;
        link.click();
    };

    req.send();
};