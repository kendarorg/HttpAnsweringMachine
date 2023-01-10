<template>
  <span>{{shown}}</span>
</template>
<script>
module.exports = {
  props: {
    value: Object,
    descriptor:Object,
    index: Array
  },
  name: 'cstring',
  computed:{
    shown: function() {
      return (typeof this.descriptor.func =="function")?
          this.descriptor.func(this.value):
          this.value[this.descriptor.id];
    }
  },
  methods:{
    clean:function(){},
    forceUpdate:function(){}
  }
}
</script>