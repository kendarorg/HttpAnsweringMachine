<template>
  <div class="col-md-8">
    <vtabs>
      <vtab name="GLOBAL">
        GLOBAL
      </vtab>
      <vtab name="REQUEST">
        REQUEST
      </vtab>
      <vtab name="RESPONSE">
        RESPONSE
      </vtab>
      <vtab name="SCRIPT">
        SCRIPT
      </vtab>
    </vtabs>
  </div>
</template>
<script>
module.exports = {
  name: "current-line",
  props:{
    selectedRow:Number
  },
  data:function(){
    return {
      data:{}
    }
  },
  components: {
    // 'simple-grid': httpVueLoader('/vcomponents/testgrid.vue'),
    // 'recording-list': httpVueLoader('/plugins/recording/vcomponents/vlist.vue'),
    // 'current-line': httpVueLoader('/plugins/recording/vcomponents/vcurrent.vue'),
    'vtab': httpVueLoader('/vcomponents/tab/vtab.vue'),
    'vtabs': httpVueLoader('/vcomponents/tab/vtabs.vue')
  },
  watch: {
    selectedRow: function (val, oldVal) {
      var th=this;
      axios.get("/api/plugins/replayer/recording/"+getUrlParameter("id")+"/line/" + val)
          .then(function(result){
            th.data=result.data;
          })
    }
  }
}
</script>