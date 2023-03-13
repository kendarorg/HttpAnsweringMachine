<template>
<div>
  <div class="row">
    <div class="col-md-8">
      <h3>UPLOAD SCRIPT</h3>
        <ham-upload id="upload-recording-script"
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
      <h3>CREATE RECORDING</h3>
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
  name:"create-recording",
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
  methods:{
    onSuccess:function(data){
      location.href = "script.html?id="+data.response.data;
    },
    onError:function(data){
      addMessage(data.error,"error");
    },
    create:function(){
      var data = {
        name:this.scriptName+".json",
        data:btoa("{}"),
        type:"application/json"
      };
      const headers = {'Content-Type': this.contentType};
      axiosHandle(axios.post("/api/plugins/replayer/recording", data, {headers}),(res) => {
        location.href = "script.html?id=" + res.data;
      });
    }
  }
}
</script>