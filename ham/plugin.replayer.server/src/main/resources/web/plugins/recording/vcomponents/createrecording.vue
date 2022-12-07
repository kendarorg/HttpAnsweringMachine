<template>
<div>
  <div class="row">
    <div class="col-md-8">
      <h3>UPLOAD SCRIPT</h3>
      <form action="" id="addUser"  method="POST"  >
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
      </form>
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
      scriptName: "",
      fileContent:null,
      uploadScriptShow:""
    }
  },
  methods:{
    openFile:function(){
      this.fileContent = null;
      this.$refs.uploadScript.click();
    },
    loadFile:function(){
      var th = this;
      uploadAsyncFile(this.$refs.uploadScript.files,function(data){
        th.fileContent = data[0];
        th.uploadScriptShow = data[0].name;
      },function(exception){
        alert(exception);
      });
    },
    upload:function(){
      var toUpload = JSON.stringify(this.fileContent);
      var name = this.uploadScriptShow.substr(0,this.uploadScriptShow.length-5);
      const headers = { 'Content-Type': 'application/json' };
      axios.post('/api/plugins/replayer/recording', toUpload, { headers }).
      then((res) => {
        location.href = "script.html?id="+response;
      });
      /*const formData = new FormData();
      formData.append('file', this.fileContent);
      const headers = { 'Content-Type': 'multipart/form-data' };
      axios.post('/api/plugins/replayer/recording', formData, { headers }).
      then((res) => {
        console.log(res)
        res.data.files; // binary representation of the file
        res.status; // HTTP status
      });*/

    },
    create:function(){}
  }
}
</script>