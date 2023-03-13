<template>
  <div>
    <simple-modal v-if="modalShow"
                  :modal-data="modalData"
                  @close="modalShow = false">
      <span slot="header">SSL Website</span>
      <span slot="body"><change-ssl-website :data="modalData.data"/></span>
    </simple-modal>
    <button id="ssl-sites-grid-reload" v-on:click="reload()" class="bi bi-arrow-clockwise" title="Reload"></button>
    <button id="ssl-sites-add" v-on:click="addNew(false,[])" class="bi bi-plus-square" title="Add new"></button>
    <br><br>
    <simple-grid id="ssl-sites-grid"
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
    'change-ssl-website': httpVueLoader('/certificates/vcomponents/veditsslwebsite.vue')
  },
  name: 'ssl-websites',
  data: function () {
    return {
      modalData: null,
      modalShow: false,
      columns: [
        {id: "id", template: "string", index: true},
        {id: "address", template: "string"}
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
      var result = await axiosHandle(axios.get("/api/ssl"));
      return result;
    },
    gridClicked: async function (evt) {
      var row = this.$refs.grid.getById(evt.index);

      if (evt.buttonid == "_edit") {
        this.addNew(true, evt.index)
      } else if (evt.buttonid == "_delete") {
        await axiosHandle(axios.delete("/api/ssl/" + row['id']), () => {
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
      showSpinner(true);
      if (this.modalData.edit) {
        await axiosHandle(axios.put('/api/ssl/' + this.modalData.data.id, this.modalData.data), () => {
          this.modalShow = false;
          showSpinner(false);
          addMessage("Modified webiste")
          this.reload();
        });
      } else {
        await axiosHandle(axios.post('/api/ssl', this.modalData.data), () => {
          this.modalShow = false;
          showSpinner(false);
          addMessage("Added webiste")
          this.reload();
        });
      }
    }
  }
}
</script>