<template>
  <input type="checkbox" id="checkbox" v-model="realData" />
</template>
<script>
module.exports = {
  props: {
    data: Boolean,
    def:Boolean,
    entry: Object,
    entrykey: String,
    properties: Object
  },
  name: 'cboolw',
  computed: {
    realData:  {
      get: function() {
        // if(typeof  this.data == "undefined"){
        //   return this.def;
        // }
        return this.data;
      },
      set: function(newValue) {
        this.entry[this.entrykey]=newValue;
        //this.data=newValue;
      }
    }
  }
}
</script>