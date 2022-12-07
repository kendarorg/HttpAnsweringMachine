<template>
<div>
  <div class="row">
    <div class="col-md-8">
      <h3>UPLOAD SCRIPT</h3>
      <form action="" id="addUser"  method="POST"  >
        <div class="form-group">
          <button type="button" id="btLoadFile" name="btLoadFile" class="btn btn-default" v-on:click="openFile">Open file</button>
          <input type="file" style="width:0;height: 0;opacity:0;" id="uploadScript" name="uploadScript" >
        </div>
        <div class="form-group">
          <input class="form-control" type="text" name="uploadScriptShow" id="uploadScriptShow" v-on:click="uploadScriptShow" />
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
      scriptName: ""
    }
  },
  methods:{
    openFile:function(){},
    uploadScriptShow:function(){},
    upload:function(){},
    create:function(){}
  }
}
</script>