<template>
  <div class="row boxed">
    <div class="col-md-8">
      <div class="form-group">
        <label for="name">Hosts</label>
        <input @change="changeHosts" class="form-control" type="text" name="name" id="name" v-model="hosts"/>
      </div>
    </div>
  </div>
</template>
<script>
module.exports = {
  name: 'exthttp',
  props:{
    value:Object
  },
  data: function () {
    return {
      hosts:"*"
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