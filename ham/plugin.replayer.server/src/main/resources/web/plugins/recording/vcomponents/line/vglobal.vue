<template>
  <div  v-if="typeof this.data!='undefined'" width="800px">
    <div class="form-group">
      <label htmlFor="id">Id</label>
      <input class="form-control" readOnly type="text" name="id" id="id" v-model="data.id"/>
    </div>
    <div class="form-group">
      <label htmlFor="timestamp">Timestamp</label>
      <input class="form-control" readOnly type="text" name="timestamp" id="timestamp" v-model="data.timestamp"/>
    </div>
    <div width="800px">
      <input class="form-check-input" type="checkbox" value="" id="stimulatedTest" name="stimulatedTest" v-model="data.stimulatedTest">
      <label class="form-check-label" for="stimulatedTest">
        Stimulated Test
      </label>
      &nbsp;
      <input class="form-check-input" type="checkbox" value="" id="stimulatorTest" name="stimulatorTest">
      <label class="form-check-label" for="stimulatorTest">
        Stimulator Test
      </label>
      &nbsp;
      <input class="form-check-input" type="checkbox" value="" id="pactTest" name="pactTest">
      <label class="form-check-label" for="pactTest">
        PACT Test
      </label>
    </div>
    <div class="form-group">
      <label htmlFor="request_method">Method</label>
      <input class="form-control" type="text" name="request_method" id="request_method" v-model="data.request.method"/>
    </div>
    <div class="form-group">
      <label htmlFor="request_protocol">Protocol</label>
      <input class="form-control" type="text" name="request_protocol" id="request_protocol" v-model="data.request.protocol"/>
    </div>
    <div class="form-group">
      <label htmlFor="request_port">Port</label>
      <input class="form-control" type="text" name="request_port" id="request_port" v-model="data.request.port"/>
    </div>
    <div class="form-group">
      <label htmlFor="request_host">Host</label>
      <input class="form-control" type="text" name="request_host" id="request_host" v-model="data.request.host"/>
    </div>
    <div class="form-group">
      <label htmlFor="request_path">Path</label>
      <input class="form-control" type="text" name="request_path" id="request_path" v-model="data.request.path"/>
    </div>
  </div>
</template>
<script>
module.exports = {
  name: "global-line",
  props:{
    data:Object
  },
  methods:{
    hasData:function(){
      return typeof this.data != 'undefined' && this.data!=null;
    }
  }

}
</script>