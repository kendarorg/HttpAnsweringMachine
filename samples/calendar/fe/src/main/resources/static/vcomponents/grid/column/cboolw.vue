<template>
  <input type="checkbox" id="checkbox" v-model="realData"/>
</template>
<script>
module.exports = {
  props: {
    value: Object,
    descriptor: Object,
    index: Array
  },
  data: function () {
    return {forceUpdateMark: 0}
  },
  name: 'cboolw',
  computed: {
    realData: {
      get: function () {
        this.forceUpdateMark;
        return this.value[this.descriptor.id];
      },
      set: function (newValue) {
        if (this.value[this.descriptor.id] != newValue) {
          this.$parent.setField(this.descriptor.id, this.index, newValue);
        }
      }
    }
  },
  methods: {
    clean: function () {
    },
    forceUpdate: function () {
      this.forceUpdateMark++;
    }
  }
}
</script>