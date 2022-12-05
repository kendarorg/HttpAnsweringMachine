<template>
  <a :href="value">{{descriptor.name}}</a>
</template>
<script>
module.exports = {
  props: {
    value: String,
    descriptor:Object,
    index: Array
  },
  name: 'cstring',
  methods:{
    clean:function(){}
  }
}
</script>