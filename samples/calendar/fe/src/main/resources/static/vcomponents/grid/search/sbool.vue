<template>

  <input name="query" style="width:40px" v-model="searchQuery" size="5"/>

</template>
<script>
module.exports = {
  props: {
    descriptor: Object
  },
  name: 'sbool',
  data: function () {
    return {
      searchQuery: ""
    };
  },
  watch: {
    searchQuery: function (val, oldVal) {
      if (!(val == null || typeof val == "undefined")) {
        if ((val.toLowerCase() === 'true') || val === "1") {
          this.$parent.setSearchField(this.descriptor.id, true, this.descriptor.template);
          return;
        } else if ((val.toLowerCase() === 'false') || val === "0") {
          this.$parent.setSearchField(this.descriptor.id, false, this.descriptor.template);
          return;
        }
      }
      this.$parent.setSearchField(this.descriptor.id, null, this.descriptor.template);
    }
  },
  methods: {
    clean: function () {
      this.searchQuery = "";
    }
  }
}
</script>