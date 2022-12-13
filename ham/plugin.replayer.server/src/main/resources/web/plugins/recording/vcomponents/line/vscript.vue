<template>
  <div  v-if="typeof this.data!='undefined'" width="800px">
    <button type="button" class="bi bi-floppy" v-on:click="updateContent()" title="Save changes"></button>
    <br><br>
    <div class="form-group">
      <label htmlFor="request_id">Id</label>
      <input class="form-control" type="text" name="request_id" id="request_id" v-model="data.id"/>
    </div>
    <div class="form-group">
      <label htmlFor="request_method">Method</label>
      <input class="form-control" type="text" name="request_method" id="request_method" v-model="data.method"/>
    </div>
    <div class="form-group">
      <label htmlFor="request_host">Host</label>
      <input class="form-control" type="text" name="request_host" id="request_host" v-model="data.host"/>
    </div>
    <div class="form-group">
      <label htmlFor="request_path">Path</label>
      <input class="form-control" type="text" name="request_path" id="request_path" v-model="data.path"/>
    </div>
      <div class="form-group">
        <label for="jsScriptPre">PRE</label>
        <textarea class="form-control" rows="6" cols="50" name="jsScriptPre" id="jsScriptPre" v-model="data.pre"></textarea>
      </div>
      <div class="form-group">
        <label for="jsScriptPost">POST</label>
        <textarea class="form-control" rows="6" cols="50" name="jsScriptPost" id="jsScriptPost" v-model="data.post"></textarea>
      </div>
  </div>
</template>
<script>
module.exports = {
  name: "global-script",
  props:{
    data:Object
  },
  methods:{
    hasData:function(){
      return typeof this.data != 'undefined' && this.data!=null;
    },
    updateContent:function(){
      var data = {
        id:this.data.id,
        method:this.data.method,
        host:this.data.host,
        path:this.data.path,
        pre:this.data.pre,
        post:this.data.post
      };
      const headers = {'Content-Type': 'application/json'};
      axios.put('/api/plugins/replayer/recording/'+getUrlParameter("id")+'/script/'+this.data.id,
          data, {headers}).then((res) => {

      });
    }
  }

}
</script>