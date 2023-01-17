<template>
<div>
  <div class="row">
    <div class="col-md-8">
      <h3>UPLOAD SCRIPT</h3>
        <ham-upload
            path="/api/plugins/replayer/recording"
            @success="onSuccess"
            @error="onError"
        ></ham-upload>
      <!--<form action="" id="addUser"  method="POST"  >
        <div class="form-group">
          <button type="button" id="btLoadFile" name="btLoadFile" class="btn btn-default" v-on:click="openFile">Open file</button>
          <input ref="uploadScript" type="file" style="width:0;height: 0;opacity:0;"
                 id="uploadScript" name="uploadScript"
                 @change="loadFile"
          >
        </div>
        <div class="form-group">
          <input class="form-control" type="text" name="uploadScriptShow" id="uploadScriptShow"
            v-model="uploadScriptShow"/>
        </div>
        <div class="form-group">
          <button type="button" id="btUploadScript" name="btUploadScript" class="btn btn-default" v-on:click="upload">Upload</button>
        </div>
      </form>-->
    </div>
    <div class="col-md-8">
      <h3>CREATE SCRIPT</h3>
      <form action="" id="createScript"  method="POST"  >
        <div class="form-group">
          <input class="form-control" type="text" name="createScriptName" id="createScriptName" v-model="scriptName" />
        </div>
        <div class="form-group">
          <button type="button" id="createScriptBt" name="createScriptBt" class="btn btn-default" v-on:click="create">Create</button>
        </div>
      </form>
    </div>
  </div>
</div>
</template>
<script>
module.exports = {
  name:"create-js",
  props:{
    data:Object
  },
  data: function() {
    return {
      scriptName:""
    }
  },
  components: {
    'ham-upload': httpVueLoader('/vcomponents/tupload.vue')
  },
  computed:{
    realPath:function(){
      return '/api/plugins/jsfilter/filters/'+this.scriptName;
    }
  },
  methods:{
    onSuccess:function(data){
      location.href = "script.html?id="+data.response.data;
    },
    onError:function(data){
      alert(data.error);
    },
    create:function(){
      var data = {
            name: this.scriptName,
            phase: "NONE",
            matchers:{
              apimatcher:JSON.stringify({
                hostAddress: "www.change.me",
                pathAddress: "changeme",
                method: "GET",
              })
          },
        source:
          "//Here you can examine the request and produce a response\n"+
          "response.setResponseText('To implement');\n"+
          "response.setStatusCode(404);\n"+
          "//If return is false and blocking is false the response will be sent immediatly\n"+
          "return false;"

      }
      const headers = {'Content-Type': 'application/json'};
      axios.post('/api/plugins/jsfilter/filters', JSON.stringify(data), {headers}).then((res) => {
        location.href = "script.html?id="+name;
      });
    }
  }
}
</script>