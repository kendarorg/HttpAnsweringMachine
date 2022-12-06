<template>
  <div>

    <button v-on:click="selectAll()" class="btn btn-default" title="Check All">Check all</button>
    <button v-on:click="toogleSelect()" class="btn btn-default" title="Toggle">Toggle Selected</button>
    <button v-on:click="generateSSLMappings()" class="btn btn-default" title="Generate SSL Mappings">Generate SSL
      Mappings
    </button>
    <button v-on:click="generateDNSMappings()" class="btn btn-default" title="Generate DNS Mappings">Generate DNS
      Mappings
    </button>
    <br><br>
    <button v-on:click="reload()" class="bi bi-arrow-clockwise" title="Reload"></button>
    <br><br>
    <simple-grid
        ref="grid"
        :columns="columns"
        :extra="extraColumns"
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
  name: 'dns-resolved',
  data: function () {
    return {
      modalData: null,
      modalShow: false,
      columns: [
        {id: "name", template: "string", index: true},
        {id: "resolved", template: "string"}
      ],
      extraColumns: [
        {id: "_select", template: "boolw", default: false}
      ]
    }
  },
  methods: {
    retrieveData: async function () {
      var result = await axios.get("/api/dns/list");
      return result;
    },
    reload: function () {
      this.$refs.grid.reload();
    },
    toggleSelect: function () {
      this.$refs.grid.toggleSelect("_select");
    },
    selectAll: function () {
      this.$refs.grid.selectAll("_select");
    },
    generateSSLMappings: async function () {
      var data = [];
      this.$refs.grid.getData().forEach(function (row, i) {
        if (row['_select'] === true) {
          data.push(this.$refs.grid.cleanRow(row));
        }
      });
      await axios.post('/api/ssl', data).then(() => {
        alert("SSL Certificates Generated!");
      });
    },
    generateDNSMappings: async function () {
      var data = [];
      this.$refs.grid.getData().forEach(function (row, i) {
        if (row['_select'] === true) {
          data.push(row['name']);
        }
      });
      await axios.post('/api/dns/mappings', data).then(() => {
        alert("DNS Mappings Generated!");
      });
    }
  }
}
</script>