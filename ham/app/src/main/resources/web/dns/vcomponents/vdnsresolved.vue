<template>
  <div>

    <button id="dns-resolved-selectall"  v-on:click="selectAll()" class="btn btn-default" title="Check All">Check all</button>
    <button id="dns-resolved-toggleselect"  v-on:click="toggleSelect()" class="btn btn-default" title="Toggle">Toggle Selected</button>
    <button id="dns-resolved-generate-ssl"  v-on:click="generateSSLMappings()" class="btn btn-default" title="Generate SSL Mappings">Generate SSL
      Mappings
    </button>
    <button  id="dns-resolved-generate-dns"  v-on:click="generateDNSMappings()" class="btn btn-default" title="Generate DNS Mappings">Generate DNS
      Mappings
    </button>
    <br><br>
    <button  id="dns-resolved-reload-grid"  v-on:click="reload()" class="bi bi-arrow-clockwise" title="Reload"></button>
    <br><br>
    <simple-grid id="dns-resolved-grid"
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
      var result = await axiosHandle(axios.get("/api/dns/list"));
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
      axiosHandle(axios.post('/api/ssl', toUpload, {headers}),()=>addMessage("Certificates genrated"));
    },
    generateDNSMappings: async function () {
      var data = [];
      this.$refs.grid.onSelected(function(row){
        data.push(row['name']);
      });
      var toUpload = JSON.stringify(data);
      const headers = {'Content-Type': 'application/json'};
      axiosHandle(axios.post('/api/dns/mappings', toUpload, {headers}),axiosOk);
    }
  }
}
</script>