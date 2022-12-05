<template>
  <component  :is="component"  v-if="component"
              :descriptor="descriptor" />
</template>
<script>
module.exports = {
  name: 'dynamic-search',
  props: /*["data"]*/
      {
        descriptor:Object
  },
  data() {
    return {
      component: null,
    }
  },
  computed: {
    loader() {
      if (!this.descriptor.template) {
        return null
      }
      return httpVueLoader(`/vcomponents/grid/search/s${this.descriptor.template}.vue`);
    },
  },
  mounted() {
    this.loader()
        .then(() => {
          this.component = () => this.loader();
        })
        .catch(() => {
          this.component = httpVueLoader('/vcomponents/grid/search/snothing.vue')
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