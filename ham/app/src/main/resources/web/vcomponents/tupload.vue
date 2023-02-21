<template>
  <div>
    <div class="form-group">
      <button type="button" id="btLoadFile" name="btLoadFile" class="bi bi-folder2-open" v-on:click="openFile" title="Open File"></button>
      <button type="button" id="btUploadScript" name="btUploadScript" class="bi bi-cloud-plus" v-on:click="upload" title="Upload"></button>
      <input ref="uploadScript" type="file" style="width:0;height: 0;opacity:0;"
             id="uploadScript" name="uploadScript"
             @change="loadFile"
      >
    </div>
    <div class="form-group">
      <input class="form-control" type="text" name="uploadScriptShow" id="uploadScriptShow"
             v-model="uploadScriptShow"/>
    </div>
  </div>
</template>
<script>
module.exports = {
  props: {
    useFormData:Boolean,
    contentType:String,
    path:String
  },
  name: 'ham-upload',
  data:function(){
    return {
      fileContent:null,
      uploadScriptShow:""
    };
  },
  methods:{
    openFile:function(){
      this.fileContent = null;
      this.$refs.uploadScript.click();
    },
    loadFile:function(){
      var th = this;
      this.uploadAsyncFile(this.$refs.uploadScript.files,function(data){
        th.fileContent = data[0];
        th.uploadScriptShow = data[0].name;
      },function(exception){
        this.$emit('error', {
          files : [],
          error : exception
        });
      });
    },
    upload:function(){
      var contentType=this.contentType;
      if(this.contentType==null||typeof this.contentType =="undefined"||this.contentType==""){
        contentType="application/json";
      }
      if(this.useFormData==null||typeof this.useFormData == "undefined"||this.useFormData==false) {
        var toUpload = JSON.stringify(this.fileContent);
        const headers = {'Content-Type': contentType};
        var th=this;
        axiosHandle(axios.post(this.path, toUpload, {headers}),(res) => {
          var data = {
            response:res,
            name:th.uploadScriptShow,
            length:toUpload.length
          };
          axiosOk();
          th.$emit('success',data)
        });
      }else {
        const formData = new FormData();
        formData.append('file', this.fileContent);
        const headers = { 'Content-Type': 'multipart/form-data' };
        axiosHandle(axios.post(this.path, formData, { headers }),(res) => {
          axiosOk();
          th.$emit('success',{
            response:res,
            name:th.uploadScriptShow,
            length:toUpload.length
          });
        });
      }
    },
    splitOnFirst : function (str, sep) {
      const index = str.indexOf(sep);
      return index < 0 ? [str] : [str.slice(0, index), str.slice(index + sep.length)];
    },
    uploadAsyncFile : async function (files, callback, callbackError) {

      let filesLoadedEvent = [];
      let filesLoaded = [];
      if (files && files.length) {
        try {
          for (let i = 0; i < files.length; i++) {
            const uploadedImageBase64 = await this.convertFileToBase64(files[i]);
            filesLoaded.push({
              data: this.splitOnFirst(uploadedImageBase64, ",")[1],
              name: files[i].name,
              type: files[i].type
            });
            filesLoadedEvent.push({
              name: files[i].name,
              type: files[i].type
            });
          }

          callback(filesLoaded)
        } catch (exception) {
          this.$emit('error', {
            files : filesLoadedEvent,
            error : exception
          });
        }
      } else {
        this.$emit('error', {
          files:[],
          error:"No files to upldad"
        });
      }
    },
    convertFileToBase64 : function (file) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => resolve(reader.result);
        reader.onerror = reject;
      });
    }
  }
}
</script>