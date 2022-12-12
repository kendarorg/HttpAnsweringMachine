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
        REQUEST DATA
      </vtab>
      <vtab name="RESPONSE">
        <br>
        RESPONSE
      </vtab>
      <vtab name="RESDATA">
        <br>
        RESPONSE DATA
      </vtab>
      <vtab name="SCRIPT">
        <br>
        SCRIPT
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
      data:undefined
    }
  },
  components: {
    // 'simple-grid': httpVueLoader('/vcomponents/testgrid.vue'),
    // 'recording-list': httpVueLoader('/plugins/recording/vcomponents/vlist.vue'),
    'global-line': httpVueLoader('/plugins/recording/vcomponents/line/vglobal.vue'),
    'request-line': httpVueLoader('/plugins/recording/vcomponents/line/vrequest.vue'),
    'vtab': httpVueLoader('/vcomponents/tab/vtab.vue'),
    'vtabs': httpVueLoader('/vcomponents/tab/vtabs.vue')
  },
  watch: {
    currentRow: function (val, oldVal) {
      var th=this;
      axios.get("/api/plugins/replayer/recording/"+getUrlParameter("id")+"/line/" + val)
          .then(function(result){
            th.data=result.data;
            console.log(th.data)
          })
    }
  },
  methods:{
    hasData:function(){
      return typeof this.data != 'undefined' && this.data!=null;
    }
  }
}
</script>
<style scoped >
.tabs-width {
  width: 800px;
}
</style>