<template>
  <div class="row boxed">
    <div class="col-md-8">Http Filters
    </div>
    <div class="col-md-8">
        <label style="font-size: 1em;" for="exthttpname">Hosts</label>
        <input @change="changeHosts" class="form-control" type="text" name="exthttpname" id="exthttpname" v-model="hosts"/>
      </div>
  </div>
</template>
<script>
module.exports = {
  name: 'exthttp',
  props: {
    value: Object
  },
  data: function () {
    return {
      hosts: "*"
    }
  },
  watch: {
    $props: {
      handler(val, oldVal) {
        if (val.value.hosts) this.hosts = val.value.hosts;
        this.changeHosts();
      },
      deep: true,
      immediate: true,
    }
  },
  methods: {
    changeHosts: function () {
      var evt = {
        value: this.hosts,
        id: "hosts"
      }
      this.$emit("componentevent", evt);
    }
  }
}
</script>