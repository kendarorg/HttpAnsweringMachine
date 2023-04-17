const downloadFile = function (urlToSend, proposedName) {
    const req = new XMLHttpRequest();
    req.open("GET", urlToSend, true);
    req.responseType = "blob";
    req.onload = function (event) {
        const blob = req.response;
        let fileName = req.getResponseHeader("fileName"); //if you have the fileName header available
        if (proposedName != "" && typeof proposedName != "undefined") {
            fileName = proposedName;
        }
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = fileName;
        link.click();
    };

    req.send();
};

const downloadFilePost = function (urlToSend, proposedName, jsonPost) {
    const req = new XMLHttpRequest();
    req.open("POST", urlToSend, true);
    req.setRequestHeader("Content-Type", "application/json");
    req.send(JSON.stringify(jsonPost));
    req.responseType = "blob";
    req.onload = function (event) {
        const blob = req.response;
        let fileName = req.getResponseHeader("fileName"); //if you have the fileName header available
        if (proposedName != "" && typeof proposedName != "undefined") {
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

const isUndefined = function (variable) {
    if (typeof variable == "undefined" || variable == null) return true;
    if (variable.constructor === String && variable == "") return true;
    return false;
}

function getTag(value) {
    if (value == null) {
        return value === undefined ? '[object Undefined]' : '[object Null]'
    }
    return toString.call(value)
}

function isObjectLike(value) {
    return typeof value === 'object' && value !== null
}

function isBoolean(value) {
    return value === true || value === false ||
        (isObjectLike(value) && getTag(value) == '[object Boolean]')
}

const isTrue = function (variable) {
    if (variable.constructor === String && variable.toUpperCase() == "TRUE") return true;
    else if (isBoolean(variable)) return variable;
    return false;
}

const waitForAvailableVariableTimes = function (variable, timeout, func, times) {
    times--;
    if (times < 0) return;
    if (isUndefined(variable())) {
        setTimeout(function () {
            if (isUndefined(variable())) {
                waitForAvailableVariableTimes(variable, timeout, func, times);
            } else {
                func(variable());
            }
        }, timeout);
    } else {
        func(variable());
    }
}

const waitForAvailableVariable = function (variable, timeout, func) {
    waitForAvailableVariableTimes(variable, timeout, func, 1)
}


const isAPrimitiveValue = function (value) {
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

const isAnArray = function (input) {
    return Array.isArray(input)//(!isAnObject(input) && !isAPrimitiveValue(input))
}

// Check if input is not primitive value, therefore object:
const isAnObject = function (input) {
    if (isAPrimitiveValue(input) || isAnArray(input)) {
        return false;
    }
    return true;
};


const jsonStringifyRecursive = function (obj) {
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

const evalInContext = function (Context, toEval) {
    return eval(`(function Main() {
        ${toEval}
    })`).call(Context);
}

var a_table = "00000000 77073096 EE0E612C 990951BA 076DC419 706AF48F E963A535 9E6495A3 0EDB8832 79DCB8A4 E0D5E91E 97D2D988 09B64C2B 7EB17CBD E7B82D07 90BF1D91 1DB71064 6AB020F2 F3B97148 84BE41DE 1ADAD47D 6DDDE4EB F4D4B551 83D385C7 136C9856 646BA8C0 FD62F97A 8A65C9EC 14015C4F 63066CD9 FA0F3D63 8D080DF5 3B6E20C8 4C69105E D56041E4 A2677172 3C03E4D1 4B04D447 D20D85FD A50AB56B 35B5A8FA 42B2986C DBBBC9D6 ACBCF940 32D86CE3 45DF5C75 DCD60DCF ABD13D59 26D930AC 51DE003A C8D75180 BFD06116 21B4F4B5 56B3C423 CFBA9599 B8BDA50F 2802B89E 5F058808 C60CD9B2 B10BE924 2F6F7C87 58684C11 C1611DAB B6662D3D 76DC4190 01DB7106 98D220BC EFD5102A 71B18589 06B6B51F 9FBFE4A5 E8B8D433 7807C9A2 0F00F934 9609A88E E10E9818 7F6A0DBB 086D3D2D 91646C97 E6635C01 6B6B51F4 1C6C6162 856530D8 F262004E 6C0695ED 1B01A57B 8208F4C1 F50FC457 65B0D9C6 12B7E950 8BBEB8EA FCB9887C 62DD1DDF 15DA2D49 8CD37CF3 FBD44C65 4DB26158 3AB551CE A3BC0074 D4BB30E2 4ADFA541 3DD895D7 A4D1C46D D3D6F4FB 4369E96A 346ED9FC AD678846 DA60B8D0 44042D73 33031DE5 AA0A4C5F DD0D7CC9 5005713C 270241AA BE0B1010 C90C2086 5768B525 206F85B3 B966D409 CE61E49F 5EDEF90E 29D9C998 B0D09822 C7D7A8B4 59B33D17 2EB40D81 B7BD5C3B C0BA6CAD EDB88320 9ABFB3B6 03B6E20C 74B1D29A EAD54739 9DD277AF 04DB2615 73DC1683 E3630B12 94643B84 0D6D6A3E 7A6A5AA8 E40ECF0B 9309FF9D 0A00AE27 7D079EB1 F00F9344 8708A3D2 1E01F268 6906C2FE F762575D 806567CB 196C3671 6E6B06E7 FED41B76 89D32BE0 10DA7A5A 67DD4ACC F9B9DF6F 8EBEEFF9 17B7BE43 60B08ED5 D6D6A3E8 A1D1937E 38D8C2C4 4FDFF252 D1BB67F1 A6BC5767 3FB506DD 48B2364B D80D2BDA AF0A1B4C 36034AF6 41047A60 DF60EFC3 A867DF55 316E8EEF 4669BE79 CB61B38C BC66831A 256FD2A0 5268E236 CC0C7795 BB0B4703 220216B9 5505262F C5BA3BBE B2BD0B28 2BB45A92 5CB36A04 C2D7FFA7 B5D0CF31 2CD99E8B 5BDEAE1D 9B64C2B0 EC63F226 756AA39C 026D930A 9C0906A9 EB0E363F 72076785 05005713 95BF4A82 E2B87A14 7BB12BAE 0CB61B38 92D28E9B E5D5BE0D 7CDCEFB7 0BDBDF21 86D3D2D4 F1D4E242 68DDB3F8 1FDA836E 81BE16CD F6B9265B 6FB077E1 18B74777 88085AE6 FF0F6A70 66063BCA 11010B5C 8F659EFF F862AE69 616BFFD3 166CCF45 A00AE278 D70DD2EE 4E048354 3903B3C2 A7672661 D06016F7 4969474D 3E6E77DB AED16A4A D9D65ADC 40DF0B66 37D83BF0 A9BCAE53 DEBB9EC5 47B2CF7F 30B5FFE9 BDBDF21C CABAC28A 53B39330 24B4A3A6 BAD03605 CDD70693 54DE5729 23D967BF B3667A2E C4614AB8 5D681B02 2A6F2B94 B40BBE37 C30C8EA1 5A05DF1B 2D02EF8D";
var b_table = a_table.split(' ').map(function (s) {
    return parseInt(s, 16)
});
const b_crc32 = function (str) {
    var crc = -1;
    for (var i = 0, iTop = str.length; i < iTop; i++) {
        crc = (crc >>> 8) ^ b_table[(crc ^ str.charCodeAt(i)) & 0xFF];
    }
    return (crc ^ (-1)) >>> 0;
};


const clearArray = function (array) {
    while (array.length > 0) {
        array.pop();
    }
}

const showSpinner = function (toggle, text) {
    var x = document.getElementById("spinnerObject");
    if (x.style.display === "none" && toggle) {
        x.style.display = "block";
        if (!isUndefined(text)) {
            x.childNodes[3].innerText = text;
        } else {
            x.childNodes[3].innerText = "";
        }
    } else if (!toggle) {
        x.style.display = "none";
        x.childNodes[3].innerText = "";
    }
}

const generateUUID = function () { // Public Domain/MIT
    var d = new Date().getTime();//Timestamp
    var d2 = ((typeof performance !== 'undefined') && performance.now && (performance.now() * 1000)) || 0;//Time in microseconds since page-load or 0 if unsupported
    return 'IDxxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = Math.random() * 16;//random number between 0 and 16
        if (d > 0) {//Use timestamp until depleted
            r = (d + r) % 16 | 0;
            d = Math.floor(d / 16);
        } else {//Use microseconds since page-load if supported
            r = (d2 + r) % 16 | 0;
            d2 = Math.floor(d2 / 16);
        }
        return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
}


const addError = function (message) {
    addMessage(message, "error")
}
window.addError = addError;
/*
.catch(function (error) {
        addError("Invalid data");
      });
 */
const addWarning = function (message) {
    addMessage(message, "warn")
}
window.addWarning = addWarning;
const addMessage = function (message, type) {

    var color = "#b3e5a1";
    var border = "#6cee3a";
    var to = 15;
    console.log("[" + type + "] " + message);
    if (isUndefined(type) || type == "ok") {
        color = "#b3e5a1";
        border = "#6cee3a";
        to = 15;
    } else if (type == "warn") {
        color = "#deec73";
        border = "#e3d119";
        to = 30;
    } else if (type == "error") {
        color = "#e85e6d";
        border = "#a41725";
        to = 60;
    } else {
        return;
    }

    var x = document.getElementById("messageObject");
    var newdiv = document.createElement('div');
    newdiv.id = generateUUID();
    newdiv.innerHTML = "<p style='margin:2px 2px 2px 2px '><b>" + message + "</b></p>";

    newdiv.style.background = color;
    newdiv.style.border = "1px solid " + border;
    newdiv.style.borderRadius = "5px";
    newdiv.style.width = "80%";
    newdiv.style.marginLeft = "5px";
    newdiv.style.marginTop = "5px";
    newdiv.style.marginBottom = "5px";
    newdiv.style.marginRight = "5px";
    //newdiv.style.height="200px";
    //newdiv.style.position="fixed";
    newdiv.style.top = "20px";
    x.appendChild(newdiv);

    setTimeout(function () {
        var d = document.getElementById(newdiv.id);
        d.parentNode.removeChild(d);
    }, to * 1000);
}


window.addMessage = addMessage;

const axiosOk = function () {
    addMessage("Ok")
}

const axiosHandle = async function (axiosCall, thenFunc, errorFunc) {
    var part = axiosCall;
    if (!isUndefined(thenFunc)) {
        part = part.then((r) => {
            thenFunc(r);
        });
    }
    if (!isUndefined(errorFunc)) {
        part = part.catch((r) => {
            console.log("[error] " + r);
            errorFunc(r);
        });
    } else {
        part = part.catch((r) => {
            console.log("[error] " + r);
            addError(r.message);
        });
    }
    return part;
}