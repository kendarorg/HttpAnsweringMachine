<template>
  <div width="800px" v-if="hasData">
  <button type="button" class="bi bi-save" v-on:click="updateContent()" title="Save changes"></button>
  <br>
    <vtabs width="800px" >
      <vtab name="GLOBAL">
        <br>
        <global-line :data="data" width="800px">

        </global-line>
      </vtab>
      <vtab name="REQUEST">
        <br>
        <request-line :data="data.request" width="800px">

        </request-line>
      </vtab>
      <vtab name="REQDATA">
        <br>
        <request-data :data="data"  width="800px">

        </request-data>
      </vtab>
      <vtab name="RESPONSE">
        <br>
        <response-line   :data="data.response" width="800px">

        </response-line>
      </vtab>
      <vtab name="RESDATA">
        <br>
        <response-data :data="data"  width="800px">

        </response-data>
      </vtab>
      <vtab name="SCRIPT">
        <br>
        <global-script :data="script"  width="800px">

        </global-script>
      </vtab>
    </vtabs>
  </div>
</template>
<script>
module.exports = {
  name: "current-line",
  props:{
    currentRow:Number
  },
  data:function(){
    return {
      data:{
        request:{},
        response:{}
      },
      script:{}
    }
  },
  computed:{
    hasData:function(){
      return typeof this.data!="undefined";
    }
  },
  components: {
    // 'simple-grid': httpVueLoader('/vcomponents/testgrid.vue'),
    // 'recording-list': httpVueLoader('/plugins/recording/vcomponents/vlist.vue'),
    'global-line': httpVueLoader('/plugins/recording/vcomponents/line/vglobal.vue'),
    'global-script': httpVueLoader('/plugins/recording/vcomponents/line/vscript.vue'),
    'request-line': httpVueLoader('/plugins/recording/vcomponents/line/vrequest.vue'),
    'response-line': httpVueLoader('/plugins/recording/vcomponents/line/vresponse.vue'),
    'request-data': httpVueLoader('/plugins/recording/vcomponents/line/vrequestdata.vue'),
    'response-data': httpVueLoader('/plugins/recording/vcomponents/line/vresponsedata.vue'),
    'vtab': httpVueLoader('/vcomponents/tab/vtab.vue'),
    'vtabs': httpVueLoader('/vcomponents/tab/vtabs.vue')
  },
  watch: {
    currentRow: function (val, oldVal) {

      var th=this;
      axios.get("/api/plugins/replayer/recording/"+getUrlParameter("id")+"/line/" + val)
          .then(function(result){
            th.data=result.data;
          });
      axios.get("/api/plugins/replayer/recording/"+getUrlParameter("id")+"/script/" + val)
          .then(function(result){
            th.script=result.data;
          })
    }
  },

  methods:{
    // hasData:function(){
    //   return typeof this.data != 'undefined' && this.data!=null;
    // },
    // hasScript:function(){
    //   return typeof this.script != 'undefined' && this.script!=null;
    // },
    updateContent:function (){
      axios.put("/api/plugins/replayer/recording/"+getUrlParameter("id")+"/line/" + this.currentRow,this.data)
          .then(function(result){
            //th.data=result.data;
          });
    }
  }
}
</script>
<style scoped >
.tabs-width {
  width: 800px;
}
</style>