<template>
  <div v-show="isActive">
    <slot></slot>
  </div>
</template>
<script>
module.exports = {
  name: 'vtab',
  props: {
    title: String,
    name: {required: true},
    selected: {default: false}
  },
  data() {
    return {
      isActive: false
    };
  },
  computed: {
    compTitle: function () {
      if (isUndefined(this.title)) {
        return this.name;
      }
      return this.title;
    },
    href: function () {
      return '#' + this.name.toLowerCase().replace(/ /g, '-');
    }
  },
  mounted() {
    this.isActive = this.selected;
  }
}
</script>