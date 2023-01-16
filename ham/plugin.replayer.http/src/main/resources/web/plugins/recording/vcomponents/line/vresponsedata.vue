<template>
  <div  v-if="typeof this.data!='undefined'" width="800px">

    <ham-upload
        @success="onSuccess"
        @error="onError"
        v-bind:path="address"
        :content-type="contentType"
    ></ham-upload>

    <button v-if="data.responseHash!='0'" v-on:click="downloadData()" class="bi bi-download" title="Download Data"></button>
    <button  v-if="!data.response.binaryResponse" type="button" class="bi bi-floppy" v-on:click="updateContent()" title="Save changes"></button>
    <div v-if="!data.response.binaryResponse">
      <br>
      <div class="form-group">
        <label for="free_content">Value</label>
        <textarea class="form-control" rows="6" cols="50"
                  name="free_content" id="free_content"
                  v-model="data.response.responseText"></textarea>
      </div>
    </div>
  </div>

</template>
<script>
module.exports = {
  name: "response-data",
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
      if(this.data.response.binaryResponse){
        return "application/octet-stream";
      }else{
        return "text/plain";
      }
    }
  },
  methods:{
    updateContent:function(){
      var fileContent = {
        data:btoa(this.data.response.responseText),
        name:"data.txt",
        type:"text/plain"
      };
      var toUpload = JSON.stringify(fileContent);
      const headers = {'Content-Type': 'text/plain'};
      axios.post(this.getAddress(), toUpload, {headers}).then((res) => {

      });
    },
    onSuccess:function(data){
      console.log("onSuccess")
    },
    onError:function(data){
      console.log(data.error);
    },
    getAddress:function(){
      return "/api/plugins/replayer/recording/" + this.data.recordingId + '/line/' + this.data.id + '/response';
    },
    hasData:function(){
      return typeof this.data != 'undefined' && this.data!=null;
    },
    downloadData:function(){
      var extension = this.data.response.binaryResponse?"bin":"txt";
      downloadFile("/api/plugins/replayer/recording/" + this.data.recordingId + "/line/" + this.data.id + "/response",
        "data_"+this.data.recordingId+"_"+this.data.id);
    }
  }

}
</script>