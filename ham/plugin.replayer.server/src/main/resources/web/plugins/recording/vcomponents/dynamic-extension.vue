<template>
  <component :is="component" v-if="component"
             :value="value" @componentevent="onComponentEvent"/>
</template>
<script>

module.exports = {
  name: 'dynamic-extension',
  props: ['template', 'path', 'default', 'value'],
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
      return httpVueLoader(this.path + "/ext" + this.template + ".vue");
    },
  },
  watch: {
    template: function (val, oldVal) {
      if (this.template) {
        this.reload();
      }
    }
  },
  methods: {
    reload: function () {
      this.loader()
          .then(() => {
            this.component = () => this.loader()
          })
          .catch(() => {
            this.component = httpVueLoader(this.path + "/ext" + this.default + '.vue')
          })
    },
    onComponentEvent: function (evt) {
      this.$emit("componentevent", evt);
    }
  },
  mounted() {
    this.reload();
  },
}
</script>