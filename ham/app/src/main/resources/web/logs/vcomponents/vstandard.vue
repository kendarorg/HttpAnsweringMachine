<template>
  <div>
    <simple-modal v-if="modalShow"
                  :modal-data="modalData"
                  @close="modalShow = false">
      <span slot="header">Standard logger</span>
      <span slot="body"><change-logger :data="modalData.data"/></span>
    </simple-modal>
    <button v-on:click="reload()" class="bi bi-arrow-clockwise" title="Reload"></button>
    <button v-on:click="addNew(false,[])" class="bi bi-plus-square" title="Add new"></button>
    <br><br>
    <simple-grid id="standard14"
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
    'change-logger': httpVueLoader('/logs/vcomponents/veditlogger.vue')
  },
  name: 'standard-loggers',
  data: function () {
    return {
      modalData: null,
      modalShow: false,
      columns: [
        {id: "key", template: "string", index: true},
        {id: "value", template: "string"}
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
      var result = await axiosHandle(axios.get("/api/log/logger"));
      return result;
    },
    gridClicked: async function (evt) {
      var row = this.$refs.grid.getById(evt.index);

      if (evt.buttonid == "_edit") {
        this.addNew(true, evt.index)
      } else if (evt.buttonid == "_delete") {
        await axiosHandle(axios.delete("/api/log/logger/" + row['key'] + "?level=" + row['value']), axiosOk)
        this.reload();
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
          key: '',
          value: 'DEBUG'
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
      await axiosHandle(axios.post('/api/log/logger/' +
          this.modalData.data.key + "?level=" +
          this.modalData.data.value, this.modalData.data), () => {
        this.modalShow = false;
        this.reload();
        axiosOk()
      });
    }
  }
}
</script>