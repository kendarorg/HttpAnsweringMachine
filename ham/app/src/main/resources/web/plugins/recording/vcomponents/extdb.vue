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
      recordVoidDbCalls:false
    }
  },
  methods:{
    changeRecordVoidDbCalls:function (){
      var evt = {
        value:this.recordVoidDbCalls,
        id:"recordVoidDbCalls"
      }
      console.log("DYNCOMP0")
      this.$emit("componentevent",evt);
    },
    changeRecordDbCalls:function(){
      var evt = {
        value:this.recordDbCalls,
        id:"recordDbCalls"
      }
      console.log("DYNCOMP1")
      this.$emit("componentevent",evt);
    }
  }
}
</script>