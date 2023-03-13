<template>
  <div>
    <simple-modal v-if="modalShow"
                  :modal-data="modalData"
                  @close="modalShow = false">
      <span slot="header">Dns Server</span>
      <span slot="body"><change-dns-server :data="modalData.data"/></span>
    </simple-modal>

    <button id="dns-servers-reload-grid" v-on:click="reload()" class="bi bi-arrow-clockwise" title="Reload"></button>
    <button id="dns-servers-add" v-on:click="addNew(false,[])" class="bi bi-plus-square" title="Add new"></button>
    <br/>
    <br><br>
    <simple-grid id="dns-servers-grid"
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
    'change-dns-server': httpVueLoader('/dns/vcomponents/veditdnsserver.vue')
  },
  name: 'dns-servers',
  data: function () {
    return {
      modalData: null,
      modalShow: false,
      columns: [
        {id: "id", template: "string", index: true},
        {id: "address", template: "string"},
        {id: "resolved", template: "string"},
        {id: "enabled", template: "bool"}
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
      var result = await axiosHandle(axios.get("/api/dns/servers"));
      return result;
    },
    gridClicked: async function (evt) {
      var row = this.$refs.grid.getById(evt.index);

      if (evt.buttonid == "_edit") {
        this.addNew(true, evt.index)
      } else if (evt.buttonid == "_delete") {
        await axiosHandle(axios.delete("/api/dns/servers/" + row['id']), () => {
          axiosOk();
          this.reload()
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
          id: URL.createObjectURL(new Blob([])).substr(-36)
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
        await axiosHandle(axios.put('/api/dns/servers/' + this.modalData.data.id, this.modalData.data), () => {
          this.modalShow = false;
          axiosOk()
          this.reload();
        });
      } else {
        await axiosHandle(axios.post('/api/dns/servers', this.modalData.data), () => {
          this.modalShow = false;
          axiosOk()
          this.reload();
        });
      }
    }
  }
}
</script>