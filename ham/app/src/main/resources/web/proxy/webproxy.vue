<template>
  <div>
    <simple-modal v-if="modalShow"
                  :modal-data="modalData"
                  @close="modalShow = false">
      <span slot="header">Url Rewrite</span>
      <span slot="body"><change-url-rewrite :data="modalData.data"/></span>
    </simple-modal>

    <button id="webprx-gird-reload" v-on:click="reload()" class="bi bi-arrow-clockwise" title="Reload"></button>
    <button id="webprx-gird-add" v-on:click="addNew(false,[])" class="bi bi-plus-square" title="Add new"></button>
    <br><br>
    Apply Proxy to file
    <ham-upload id="webprx-upload"
                path="/api/utils/proxiesapply"
                @success="onSuccessApply"
                @error="onErrorApply"
    ></ham-upload>
    <br><br>
    <simple-grid id="webprx-gird"
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
  name: 'web-proxy',
  components: {
    'change-url-rewrite': httpVueLoader('/proxy/vediturlrewrite.vue'),
    'simple-grid': httpVueLoader('/vcomponents/testgrid.vue'),
    'simple-modal': httpVueLoader('/vcomponents/tmodal.vue'),
    'ham-upload': httpVueLoader('/vcomponents/tupload.vue')
  },
  data: function () {
    return {
      modalData: null,
      modalShow: false,
      columns: [
        {id: "id", template: "string", index: true},
        {id: "when", template: "string"},
        {id: "where", template: "string"},
        {id: "test", template: "string"},
        {id: "running", template: "bool"},
        {id: "force", template: "bool"}
      ],
      extraColumns: [
        {
          id: "_edit",
          template: "iconbutton",
          default: false,
          searchable: false,
          sortable: false,
          properties: {
            name: "Edit", style: "bi bi-pen-fill"
          }
        },
        {
          id: "_delete",
          template: "iconbutton",
          default: false,
          searchable: false,
          sortable: false,
          properties: {
            name: "Delete", style: "bi bi-trash"
          }
        }
      ]
    }
  },
  methods: {
    onSuccessApply: function (data) {
      const filename = "applied.txt";
      const blob = new Blob([data.response.data], {type: 'text/plain'});
      if (window.navigator.msSaveOrOpenBlob) {
        window.navigator.msSaveBlob(blob, filename);
      } else {
        const elem = window.document.createElement('a');
        elem.href = window.URL.createObjectURL(blob);
        elem.download = filename;
        document.body.appendChild(elem);
        elem.click();
        document.body.removeChild(elem);
      }
    },
    onErrorApply: function (data) {
      addMessage(data.error, error);
    },
    retrieveData: async function () {
      var result = await axiosHandle(axios.get("/api/proxies"));
      return result;
    },
    gridClicked: async function (evt) {
      var row = this.$refs.grid.getById(evt.index);

      if (evt.buttonid == "_edit") {
        this.addNew(true, evt.index)
      } else if (evt.buttonid == "_delete") {
        await axiosHandle(axios.delete("/api/proxies/" + row['id']), axiosOk);
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
        await axiosHandle(axios.put('/api/proxies/' + this.modalData.data.id, this.modalData.data), () => {
          this.modalShow = false;
          this.reload();
          axiosOk()
        });
      } else {
        await axiosHandle(axios.post('/api/proxies', this.modalData.data), () => {
          this.modalShow = false;
          this.reload();
          axiosOk()
        });
      }
    }
  }
}
</script>