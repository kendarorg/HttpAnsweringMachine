
/*

[
    {
        "name": "command",
        "type": "org.kendar.janus.cmd.Exec",
        "size": null,
        "children": [
            {
                "name": "name",
                "type": "java.lang.String",
                "size": null,
                "value": "isValid",
                "children": [],
                "parent": "#ref"
            },
            {
                "name": "paramtype",
                "type": "[Ljava.lang.Class;",
                "size": 1,
                "children": [
                    {
                        "name": "_",
                        "type": "java.lang.Class",
                        "size": null,
                        "value": "inta",
                        "children": [],
                        "parent": "#ref"
                    }
                ],
                "parent": "#ref"
            },
            {
                "name": "parameters",
                "type": "[Ljava.lang.Object;",
                "size": 1,
                "children": [
                    {
                        "name": "_",
                        "type": "java.lang.Integera",
                        "size": null,
                        "value": "200",
                        "children": [],
                        "parent": "#ref"
                    }
                ],
                "parent": "#ref"
            }
        ]
    }
]
 */

/*
{
	"command": {
		"name": "isValid",
		":name:": "java.lang.String",
		"paramtype": [{
			"_": "int",
			":_:": "java.lang.Class"
		}],
		":paramtype:": "[Ljava.lang.Class;",
		"[paramtype]": "1",
		"parameters": [{
			"_": 20,
			":_:": "java.lang.Integer"
		}],
		":parameters:": "[Ljava.lang.Object;",
		"[parameters]": "1"
	},
	":command:": "org.kendar.janus.cmd.Exec"
}
 */
const convertToStructure = function(nodes){
    var result = {};
    nodes.forEach(function(node){
        result[":"+node.name+":"]=node.type;

        if(node.children &&
            ((node.size!=null && typeof node.size!="undefined" && node.size>=0)||
                node.type=="java.util.ArrayList"
            )){
            if(node.size!=null && typeof node.size!="undefined"){
                result["["+node.name+"]"]=node.size;
            }
            var temp=[];
            node.children.forEach(function(ch){
                var str = convertToStructure([ch]);
                temp.push(str);
            })
            result[node.name]=temp;

            //Contains an array

        }else if(typeof node.value!="undefined"){
                //Contains an object
                result[node.name] = node.value
        }else{
            if(node.children && node.children.length>0 && node.children[0].type=="_MapEntry") {
                var finalResult = [];
                node.children.forEach(function(mapEntry){
                    finalResult.push(convertToStructure(mapEntry.children));
                });
                result[node.name] = finalResult;
            }else{
                result[node.name]=convertToStructure(node.children);
            }
        }
        })
    return result;
}
const convertToNodes = function(serializedObject){
    var ob = serializedObject;
    var keys = Object.keys(ob);
    //console.log(keys);
    var typeValues = [];
    var valValues = [];
    var valChildrens = [];
    var valSizes = [];
    var allKeys = [];
    keys.forEach(function(id){

        var size=0;
        valSizes[id]=null;
        valChildrens[id]=[];
        if(id.startsWith("[")){
            valSizes[id.substring(1,id.length-1)]=parseInt(ob[id]);
        }else if(id.startsWith(":")){
            typeValues[id.substring(1,id.length-1)]=ob[id];
        }else{
            allKeys.push(id);
            var subVal = ob[id];
            if(isAnObject(subVal)){
                if(isAnArray(subVal))debugger;
                var nodes = convertToNodes(subVal);
                nodes.forEach(function(v){
                    valChildrens[id].push(v);
                })

            }else if(isAnArray(subVal)){
                var tmp = [];
                subVal.forEach(function(v){
                    var nodes = convertToNodes(v);
                    if(nodes.length>1){
                        var mapEntry = {};
                        mapEntry.name = "_MapEntry";
                        mapEntry.type = "_MapEntry";
                        mapEntry.size = null;
                        mapEntry.value = null;
                        mapEntry.children = nodes;
                        tmp.push(mapEntry)
                    }else{
                        tmp.push(nodes[0])
                    }
                    size++;
                });
                valChildrens[id]=tmp;
            }else{
                if(isAnArray(subVal))debugger;
                valValues[id]=subVal;
            }

        }
    })
    // console.log(typeValues);
    // console.log(valValues);
    // console.log(allKeys);
    var nodes = [];
    allKeys.forEach(function(id){
        var node = {};
        node.name = id;
        node.type = typeValues[id];
        node.size = valSizes[id];
        node.value = valValues[id];
        node.children = valChildrens[id];
        if(typeof node.children=="undefined")debugger;
        if(node.children){
            node.children.forEach(function(child){
                if(typeof child=="undefined")return;

                if( child.type=="_MapEntry"){
                    child.children.forEach(function(mapCh){
                        mapCh.parent=child;
                    })
                };
                child.parent=node;
            })
        }
        nodes.push(node);
    });
    return nodes;
}