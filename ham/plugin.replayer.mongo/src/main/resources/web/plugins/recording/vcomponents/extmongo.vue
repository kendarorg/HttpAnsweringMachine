<template>
  <div class="row boxed">
    <div class="col-md-8">
      <div class="form-group">
        <label for="exthttpname">Ports</label>
        <input @change="changePorts" class="form-control" type="text" name="exthttpname" id="exthttpname" v-model="ports"/>
      </div>
    </div>
  </div>
</template>
<script>
module.exports = {
  name: 'extmongo',
  props: {
    value: Object
  },
  data: function () {
    return {
      ports: "*"
    }
  },
  watch: {
    $props: {
      handler(val, oldVal) {
        if (val.value.hosts) this.ports = val.value.ports;
        this.changePorts();
      },
      deep: true,
      immediate: true,
    }
  },
  methods: {
    changePorts: function () {
      var evt = {
        value: this.ports,
        id: "ports"
      }
      this.$emit("componentevent", evt);
    }
  }
}
</script>