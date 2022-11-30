<template>
  <component :is="component" :data="data" :def="def" v-if="component"
             :entry="entry" :entrykey="entrykey"/>
</template>
<script>
module.exports = {
  name: 'dynamic-template',
  props: ["data","type","def","entry","entrykey"]
      /*{
    data:String,
    type: String,
    def:String,
    callback: Function
  }*/,
  data() {
    return {
      component: null,
    }
  },
  computed: {
    loader() {
      if (!this.type) {
        return null
      }
      return httpVueLoader(`vcomponents/grid/t${this.type}.vue`);
    },
  },
  mounted() {
    this.loader()
        .then(() => {
          this.component = () => this.loader()
        })
        .catch(() => {
          this.component = httpVueLoader('vcomponents/grid/tstring.vue')
        })
  },
}
</script>