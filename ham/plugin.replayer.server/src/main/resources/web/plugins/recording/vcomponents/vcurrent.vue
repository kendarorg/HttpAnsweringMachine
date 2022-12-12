<template>
    <vtabs width="800px" >
      <vtab name="GLOBAL">
        <br>
        <global-line :data="data" v-if="hasData" width="800px">

        </global-line>
      </vtab>
      <vtab name="REQUEST">
        <br>
        <request-line :data="data" v-if="hasData" width="800px">

        </request-line>
      </vtab>
      <vtab name="REQDATA">
        <br>
        <request-data :data="data" v-if="hasData" width="800px">

        </request-data>
      </vtab>
      <vtab name="RESPONSE">
        <br>
        <response-line :data="data" v-if="hasData" width="800px">

        </response-line>
      </vtab>
      <vtab name="RESDATA">
        <br>
        <response-data :data="data" v-if="hasData" width="800px">

        </response-data>
      </vtab>
      <vtab name="SCRIPT">
        <br>
        <global-script :data="script" v-if="hasScript" width="800px">

        </global-script>
      </vtab>
    </vtabs>
</template>
<script>
module.exports = {
  name: "current-line",
  props:{
    currentRow:Number
  },
  data:function(){
    return {
      data:undefined,
      script:undefined
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
    hasData:function(){
      return typeof this.data != 'undefined' && this.data!=null;
    },
    hasScript:function(){
      return typeof this.script != 'undefined' && this.script!=null;
    }
  }
}
</script>
<style scoped >
.tabs-width {
  width: 800px;
}
</style>