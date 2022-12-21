
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

const isUndefined=function(variable){
    if(typeof variable=="undefined" || variable==null)return true;
    if (variable.constructor === String && variable=="")return true;
    return false;
}

const waitForAvailableVariableTimes=function(variable,timeout,func,times){
    times--;
    if(times<0)return;
    if(isUndefined(variable())){
        setTimeout(function () {
            if(isUndefined(variable())){
                waitForAvailableVariableTimes(variable,timeout,func,times);
            }else {
                func(variable());
            }
        }, timeout);
    }else{
        func(variable());
    }
}

const waitForAvailableVariable=function(variable,timeout,func){
    waitForAvailableVariableTimes(variable,timeout,func,1)
}


const  isAPrimitiveValue=function(value) {
    return (
        typeof value === "symbol" ||
        typeof value === "string" ||
        typeof value === "number" ||
        typeof value === "boolean" ||
        typeof value === "undefined" ||
        value === null ||
        typeof value === "bigint"
    );
};

const isAnArray = function(input){
    return Array.isArray(input)//(!isAnObject(input) && !isAPrimitiveValue(input))
}

// Check if input is not primitive value, therefore object:
const isAnObject = function (input) {
    if (isAPrimitiveValue(input) ||isAnArray(input)) {
        return false;
    }
    return true;
};


const jsonStringifyRecursive =function (obj) {
    const cache = new Set();
    return JSON.stringify(obj, (key, value) => {
        if (typeof value === 'object' && value !== null) {
            if (cache.has(value)) {
                // Circular reference found, discard key
                return "#ref";
            }
            // Store value in our collection
            cache.add(value);
        }
        return value;
    }, 4);
}