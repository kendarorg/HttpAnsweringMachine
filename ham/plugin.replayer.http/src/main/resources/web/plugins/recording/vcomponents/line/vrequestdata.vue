<template>
  <div  v-if="typeof this.data!='undefined'" width="800px">

    <ham-upload
        @success="onSuccess"
        @error="onError"
        v-bind:path="address"
        :content-type="contentType"
    ></ham-upload>

    <button v-if="data.requestHash!='0'" v-on:click="downloadData()" class="bi bi-download" title="Download Data"></button>
    <button v-if="!data.request.binaryRequest" type="button" class="bi bi-floppy" v-on:click="updateContent()" title="Save changes">Save request data changes</button>
    <div v-if="!data.request.binaryRequest">
      <br>
      <div class="form-group">
        <label for="free_content">Value</label>
        <textarea class="form-control" rows="6" cols="50"
                  name="free_content" id="free_content"
                  v-model="data.request.requestText"></textarea>
      </div>
    </div>
  </div>

</template>
<script>
module.exports = {
  name: "request-data",
  props:{
    data:Object
  },
  data:function(){
    return {

    }
  },
  components: {
    'ham-upload': httpVueLoader('/vcomponents/tupload.vue')
  },
  computed:{
    address:function(){
      return this.getAddress();
    },
    contentType:function(){
      if(this.data.request.binaryRequest){
        return "application/octet-stream";
      }else{
        return "text/plain";
      }
    }
  },
  methods:{
    updateContent:function(){
      var fileContent = {
        data:btoa(this.data.request.requestText),
        name:"data.txt",
        type:"text/plain"
      };
      var toUpload = JSON.stringify(fileContent);
      const headers = {'Content-Type': 'text/plain'};
      axiosHandle(axios.post(this.getAddress(), toUpload, {headers}),axiosOk);
    },
    onSuccess:function(data){
      console.log("onSuccess")
    },
    onError:function(data){
      console.log(data.error);
    },
    getAddress:function(){
      return "/api/plugins/replayer/recording/" + this.data.recordingId + '/line/' + this.data.id + '/request';
    },
    hasData:function(){
      return typeof this.data != 'undefined' && this.data!=null;
    },
    downloadData:function(){
      var extension = this.data.request.binaryRequest?"bin":"txt";
      downloadFile("/api/plugins/replayer/recording/" + this.data.recordingId + "/line/" + this.data.id + "/request",
        "data_"+this.data.recordingId+"_"+this.data.id+"."+extension);
    }
  }

}
</script>