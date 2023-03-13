<template>
  <div>
    <button v-on:click="reload()" class="bi bi-arrow-clockwise" title="Reload"></button>
    <br><br>
    <simple-grid id="loggers12"
                 ref="grid"
                 :columns="columns"
                 :retrieve-data="retrieveData"
    >
    </simple-grid>
  </div>
</template>
<script>
module.exports = {
  components: {
    'simple-grid': httpVueLoader('/vcomponents/testgrid.vue')
  },
  name: 'special-loggers',
  data: function () {
    return {
      modalData: null,
      modalShow: false,
      columns: [
        {id: "path", template: "string", index: true},
        {id: "level", template: "string"},
        {id: "description", template: "string"}
      ]
    }
  },
  methods: {
    retrieveData: async function () {
      var result = await axiosHandle(axios.get("/api/log/special"));
      return result;
    },
    reload: function () {
      this.$refs.grid.reload();
    }
  }
}
</script>