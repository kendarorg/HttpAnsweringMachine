<template>
  <component :is="component"  :def="def" v-if="component"
              :entrykey="entrykey" :grid="grid" :properties="properties"/>
</template>
<script>
module.exports = {
  name: 'dynamic-search',
  props: ["type","def","entrykey","grid","properties"]
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
      return httpVueLoader(`vcomponents/grid/search/s${this.type}.vue`);
    },
  },
  mounted() {
    this.loader()
        .then(() => {
          this.component = () => this.loader();
        })
        .catch(() => {
          this.component = httpVueLoader('vcomponents/grid/search/sstring.vue')
        })
  },
  methods:{
    clean :function (){
      if(this.$children.length==0)return;
      this.$children[0].clean();
    }
  }
}
</script>