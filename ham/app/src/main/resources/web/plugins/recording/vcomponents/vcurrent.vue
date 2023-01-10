<template>
  <div width="800px">
    <div width="800px" v-if="data.type=='http'" >
      <button type="button" :disabled="data.id ==-1"  class="bi bi-floppy" v-on:click="updateContent()" title="Save changes"></button>
      <button type="button" :disabled="prev<0"  class="bi bi-floppy" v-on:click="prevRow()" title="Save changes">Prev</button>
      <button type="button" :disabled="next<0"  class="bi bi-floppy" v-on:click="nextRow()" title="Save changes">Next</button>
      <br>
      <br>
      <vtabs width="800px" >
        <vtab name="GLOBAL">
          <br>
          <global-line :data="data" width="800px">

          </global-line>
        </vtab>
        <vtab name="REQUEST">
          <br>
          <request-line :data="data.request" ref="drq" width="800px">

          </request-line>
        </vtab>
        <vtab name="REQDATA">
          <br>
          <request-data :data="data"  width="800px">

          </request-data>
        </vtab>
        <vtab name="RESPONSE">
          <br>
          <response-line   :data="data.response" ref="drs"  width="800px">

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
    <div width="800px" v-if="data.type=='db'" >
      <button type="button" :disabled="data.id ==-1"  class="bi bi-floppy" v-on:click="updateContent()" title="Save changes"></button>
      <button type="button" :disabled="prev<0"  class="bi bi-floppy" v-on:click="prevRow()" title="Save changes">Prev</button>
      <button type="button" :disabled="next<0"  class="bi bi-floppy" v-on:click="nextRow()" title="Save changes">Next</button>
      <br><br>
      <h3>{{data.request.path}}</h3>
      <vtabs width="800px" >
        <vtab name="REQDATA">
          <br>
          <serializable-object :data="data.request.requestText"  @changed="changedAt" width="800px">

          </serializable-object>
        </vtab>
        <vtab name="RESDATA">
          <br>
          <serializable-object :data="data.response.responseText" @changed="changedRt" width="800px">

          </serializable-object>
        </vtab>
      </vtabs>
    </div>
  </div>
</template>
<script>
module.exports = {
  name: "current-line",
  props:{
    currentRow:Number,
  },
  data:function(){
    return {

      prev:-1,
      next:-1,
      script:{},
      data:{
        type:'none',
        id:-1,
        request:{path:'',requestText:''},
        response:{responseText:''}
      }
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
    'serializable-object': httpVueLoader('/plugins/recording/vcomponents/line/vserobject.vue'),
    'vtab': httpVueLoader('/vcomponents/tab/vtab.vue'),
    'vtabs': httpVueLoader('/vcomponents/tab/vtabs.vue')
  },
  watch: {
    currentRow: function (val, oldVal) {
      var th=this;
      axios.get("/api/plugins/replayer/recording/" + getUrlParameter("id") + "/line/" + val)
          .then(function (result) {

            th.next = parseInt(result.headers.get("X-NEXT"));
            th.prev = parseInt(result.headers.get("X-PREV"));
            th.data = result.data;

            axios.get("/api/plugins/replayer/recording/" + getUrlParameter("id") + "/script/" + val)
                .then(function (results) {
                  th.script = results.data;

                })
          })
    }

  },
  methods:{
    changedAt:function(val){
      this.data.request.requestText=val;
    },
    changedRt:function(val){
      this.data.response.responseText=val;
    },
    prevRow:function(newRow){
      this.$emit('rowchanged', this.prev);
    },
    nextRow:function(newRow){
      this.$emit('rowchanged', this.next);
    },
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