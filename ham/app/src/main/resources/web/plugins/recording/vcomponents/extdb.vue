<template>
  <div>
    <input class="form-check-input" type="checkbox" value="" id="recordVoidDbCalls" name="recordVoidDbCalls"
           v-model="recordVoidDbCalls" @change="changeRecordVoidDbCalls">
    <label class="form-check-label" for="recordVoidDbCalls" >
      Record void db calls
    </label>
    <input class="form-check-input" type="checkbox" value="" id="recordDbCalls" name="recordDbCalls"
           v-model="recordDbCalls" @change="changeRecordDbCalls">
    <label class="form-check-label" for="recordDbCalls" >
      Record db calls
    </label>
    <input class="form-check-input" type="checkbox" value="" id="useSimEngine" name="useSimEngine"
           v-model="useSimEngine" @change="changeUseSimEngine">
    <label class="form-check-label" for="useSimEngine" >
      Use simulated engine (not suitable for metadata)
    </label>
    <br>
  </div>
</template>
<script>
module.exports = {
  name: 'extdb',
  props:{
    value:Object
  },
  data:function(){
    return {
      recordDbCalls:false,
      recordVoidDbCalls:false,
      useSimEngine:false
    }
  },
  methods:{
    changeUseSimEngine:function (){
      var evt = {
        value:this.useSimEngine,
        id:"useSimEngine"
      }
      this.$emit("componentevent",evt);
    },
    changeRecordVoidDbCalls:function (){
      var evt = {
        value:this.recordVoidDbCalls,
        id:"recordVoidDbCalls"
      }
      this.$emit("componentevent",evt);
    },
    changeRecordDbCalls:function(){
      var evt = {
        value:this.recordDbCalls,
        id:"recordDbCalls"
      }
      this.$emit("componentevent",evt);
    }
  }
}
</script>