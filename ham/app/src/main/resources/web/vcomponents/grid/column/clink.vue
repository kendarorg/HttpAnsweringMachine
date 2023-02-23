<template>
  <a :href="shown">{{name}}</a>
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
    name: function() {
      return (typeof this.descriptor.funcname =="function")?
          this.descriptor.funcname(this.value):
          this.descriptor.name;
    },
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