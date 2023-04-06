<template>
  <div>
    <simple-modal v-if="modalShow"
                  :modal-data="modalData"
                  @close="modalShow = false">
      <span slot="header">Dns Mapping</span>
      <span slot="body"><change-dns-mapping :data="modalData.data"/></span>
    </simple-modal>
    <button id="dns-mappings-reload" v-on:click="reload()" class="bi bi-arrow-clockwise" title="Reload"></button>
    <button id="dns-mappings-add" v-on:click="addNew(false,[])" class="bi bi-plus-square" title="Add new"></button>
    <button id="dns-mappings-download" v-on:click="downloadHostFiles()" class="bi bi-download"
            title="Download Hosts File"></button>
    <br><br>
    <simple-grid id="dns-mappings-grid"
                 v-on:gridclicked="gridClicked"
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
    'simple-grid': httpVueLoader('/vcomponents/testgrid.vue'),
    'simple-modal': httpVueLoader('/vcomponents/tmodal.vue'),
    'change-dns-mapping': httpVueLoader('/dns/vcomponents/veditdnsmapping.vue')
  },
  name: 'dns-mappings',
  data: function () {
    return {
      modalData: null,
      modalShow: false,
      columns: [
        {id: "id", template: "string", index: true, visible:false},
        {id: "ip", template: "string"},
        {id: "dns", template: "string"}
      ],
      extraColumns: [
        {
          id: "_edit", template: "iconbutton", default: false, searchable: false, sortable: false, properties: {
            name: "Edit", style: "bi bi-pen-fill"
          }
        },
        {
          id: "_delete", template: "iconbutton", default: false, searchable: false, sortable: false, properties: {
            name: "Delete", style: "bi bi-trash"
          }
        }
      ]
    }
  },
  methods: {
    retrieveData: async function () {
      var result = await axiosHandle(axios.get("/api/dns/mappings"));
      return result;
    },
    gridClicked: async function (evt) {
      var row = this.$refs.grid.getById(evt.index);

      if (evt.buttonid == "_edit") {
        this.addNew(true, evt.index)
      } else if (evt.buttonid == "_delete") {
        await axiosHandle(axios.delete("/api/dns/mappings/" + row['id']), () => {
          this.reload()
          addMessage("Deleted")
        });
      }
    },
    reload: function () {
      this.$refs.grid.reload();
    },
    addNew: function (shouldEdit, rowId) {
      var row = null;
      if (shouldEdit) {
        row = this.$refs.grid.getById(rowId);
      } else {
        row = {
          id: URL.createObjectURL(new Blob([])).substr(-36),
          ip: "127.0.0.1"
        }
      }
      this.modalData = {
        data: row,
        edit: shouldEdit,
        save: this.save
      };
      this.modalShow = true;
    },
    save: async function () {
      if (this.modalData.edit) {
        await axiosHandle(axios.put('/api/dns/mappings/' + this.modalData.data.id, this.modalData.data), () => {
          addMessage("Modified")
          this.modalShow = false;
          this.reload();
        });
      } else {
        await axiosHandle(axios.post('/api/dns/mappings', this.modalData.data), () => {
          addMessage("Added")
          this.modalShow = false;
          this.reload();
        });
      }
    },
    downloadHostFiles: function () {
      downloadFile("/api/dns/hosts", "hosts");
    }
  }
}
</script>