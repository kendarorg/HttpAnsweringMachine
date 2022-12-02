<template>
  <input type="checkbox" id="checkbox" v-model="value" disabled="true"/>
</template>
<script>
module.exports = {
  props: {
    value: Boolean,
    descriptor:Object,
    index: Array
  },
  name: 'cbool',
  methods:{
    clean:function(){}
  }
}
</script>