<template>
  <div>

    <button v-on:click="selectAll()" class="btn btn-default" title="Check All">Check all</button>
    <button v-on:click="toggleSelect()" class="btn btn-default" title="Toggle">Toggle Selected</button>
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
        {id:"select",template:"boolw",default:false},
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
      this.$refs.grid.toggleSelect("select");
    },
    selectAll: function () {
      this.$refs.grid.selectAll("select");
    },
    generateSSLMappings: async function () {
      var data = [];
      this.$refs.grid.onSelected(function(row){
          data.push(row['name']);
      });
      var toUpload = JSON.stringify(data);
      const headers = {'Content-Type': 'application/json'};
      axios.post('/api/ssl', toUpload, {headers}).then((res) => {

      });
    },
    generateDNSMappings: async function () {
      var data = [];
      this.$refs.grid.onSelected(function(row){
        data.push(row['name']);
      });
      var toUpload = JSON.stringify(data);
      const headers = {'Content-Type': 'application/json'};
      axios.post('/api/dns/mappings', toUpload, {headers}).then((res) => {

      });
    }
  }
}
</script>