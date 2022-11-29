<template>
  <component :is="component" :data="data" v-if="component" />
</template>
<script>
module.exports = {
  name: 'dynamic-template',
  props: ['data', 'type'],
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
      return httpVueLoader(`vcomponents/t${this.type}.vue`);
    },
  },
  mounted() {
    this.loader()
        .then(() => {
          this.component = () => this.loader()
        })
        .catch(() => {
          this.component = httpVueLoader('vcomponents/tstring.vue')
        })
  },
}
</script>