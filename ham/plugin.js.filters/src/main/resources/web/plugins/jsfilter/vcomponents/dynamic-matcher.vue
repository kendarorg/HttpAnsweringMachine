<template>
  <component  :is="component"  v-if="component"
               :matcher="value" ref="subcomp" @componentevent="onComponentEvent"/>
</template>
<script>

module.exports = {
  name: 'dynamic-matcher',
  props:['template','path','default','value'],
  data() {
    return {
      component: null,
    }
  },
  computed: {
    loader() {
      if (!this.template) {
        return null
      }
      return httpVueLoader(this.path + "/" + this.template + ".vue");
    },
  },
  watch:{
    template:function(val,oldVal){
      if (this.template) {
        this.reload();
      }
    }
  },
  methods: {
    isValid:function(){
      return this.$refs.subcomp.isValid();
    },
    reload:function(){
      this.loader()
          .then(() => {
            this.component = () => this.loader()
          })
          .catch(() => {
            this.component = httpVueLoader(this.path+"/"+this.default+'.vue')
          })
    },
    onComponentEvent:function(evt){
      this.$emit("componentevent",evt);
    }
  },
  mounted() {
    this.reload();
  },
}
</script>