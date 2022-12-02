<template>
  <component  :is="component"  v-if="component"
              :descriptor="descriptor" :index="index" :value="value"/>
</template>
<script>
module.exports = {
  name: 'dynamic-column',
  props:['descriptor','index','value'],
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
      return httpVueLoader(`vcomponents/grid/column/c${this.descriptor.template}.vue`);
    },
  },
  methods:{
  },
  mounted() {
    this.loader()
        .then(() => {
          this.component = () => this.loader()
        })
        .catch(() => {
          this.component = httpVueLoader('vcomponents/grid/column/cstring.vue')
        })
  },
}
</script>