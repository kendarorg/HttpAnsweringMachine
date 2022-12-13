<template>
  <span>{{value}}</span>
</template>
<script>
module.exports = {
  props: {
    value: Number,
    descriptor:Object,
    index: Array
  },
  name: 'calong',
  methods:{
    clean:function(){}
  }
}
</script>