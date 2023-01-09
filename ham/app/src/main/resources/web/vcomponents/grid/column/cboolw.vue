<template>
  <input type="checkbox" id="checkbox" v-model="realData" />
</template>
<script>
module.exports = {
  props: {
    value: Object,
    descriptor:Object,
    index: Array
  },
  name: 'cboolw',
  computed: {
    realData:  {
      get: function() {
        return this.value[this.descriptor.id];
      },
      set: function(newValue) {
        this.$parent.setField(this.descriptor.id,this.index,newValue);
      }
    }
  },
  methods:{
    clean:function(){}
  }
}
</script>