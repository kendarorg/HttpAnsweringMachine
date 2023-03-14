<template>
  <button :class="descriptor.properties['style']" @click="onClick">{{ descriptor.properties['name'] }}</button>
</template>
<script>
module.exports = {
  props: ['descriptor', 'index', 'value'],
  name: 'cbutton',
  methods: {
    clean: function () {
    },
    onClick: function () {
      let eventData = {
        index: this.index,
        buttonid: this.descriptor.id
      }
      this.$parent.$emit("gridclicked", eventData);
    },
    forceUpdate: function () {
    }
  }
}
</script>