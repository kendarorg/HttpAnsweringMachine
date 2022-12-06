<template>
  <button :class="descriptor.properties['style']" @click="onClick" v-bind:title="descriptor.properties['name']"></button>
</template>
<script>
module.exports = {
  props:['descriptor','index','value'],
  name: 'ciconbutton',
  methods:{
    clean:function(){},
    onClick:function(){
      let eventData = {
        index: this.index,
        buttonid :this.descriptor.id
      }
      this.$parent.$parent.$emit("gridclicked",eventData);
    }
  }
}
</script>