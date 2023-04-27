<template>
  <div>
    <simple-modal v-if="modalShow"
                  :modal-data="modalData"
                  @close="modalShow = false">
      <span slot="header">JDBC Proxy</span>
      <span slot="body"><change-jdbc-rewrite :data="modalData.data"/></span>
    </simple-modal>

    <button id="jdbcprx-grid-reload" v-on:click="reload()" class="bi bi-arrow-clockwise" title="Reload"></button>
    <button id="jdbcprx-grid-addnew" v-on:click="addNew(false,[])" class="bi bi-plus-square" title="Add new"></button>
    <br><br>
    <simple-grid id="jdbcprx-grid"
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
  name: 'jdbc-proxy',
  components: {
    'change-jdbc-rewrite': httpVueLoader('/proxy/veditjdbcrewrite.vue'),
    'simple-grid': httpVueLoader('/vcomponents/testgrid.vue'),
    'simple-modal': httpVueLoader('/vcomponents/tmodal.vue')
  },
  data: function () {
    return {
      modalData: null,
      modalShow: false,
      columns: [
        {id: "id", template: "string", index: true, visible:false},
        {id: "active", template: "bool", size:4},
        {id: "driver", template: "string"},
        {
          id: "remote", template: "string", func: function (e) {
            return e.remote.connectionString;
          }
        },
        {
          id: "exposed", template: "string", func: function (e) {
            return e.exposed.connectionString;
          }
        },
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
        }, {
          id: "_test",
          template: "iconbutton",
          default: false,
          searchable: false,
          sortable: false,
          properties: {
            name: "Edit", style: "bi bi-file-check"
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
    retrieveData: async function () {
      var result = await axiosHandle(axios.get("/api/jdbcproxies/proxies"));
      return result;
    },
    gridClicked: async function (evt) {
      var row = this.$refs.grid.getById(evt.index);

      if (evt.buttonid == "_edit") {
        this.addNew(true, evt.index)
      } else if (evt.buttonid == "_delete") {
        await axiosHandle(axios.delete("/api/jdbcproxies/proxies/" + row['id']), axiosOk);
        this.reload();
      } else if (evt.buttonid == "_test") {
        await axiosHandle(
            axios.get("/api/jdbcproxies/proxies/" + row['id'] + "?test=true"), (data) => {
              if (data.status == 200) addMessage("OK");
              else addMessage("ERROR CONNECTING!", "error")
            }, (error) => {
              addMessage(error.response.data, "error");
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
          remote: {},
          exposed: {}
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
        await axiosHandle(axios.put('/api/jdbcproxies/proxies/' + this.modalData.data.id, this.modalData.data),
            () => {
              axiosOk();
              this.modalShow = false;
              this.reload();
            });
      } else {
        await axiosHandle(axios.post('/api/jdbcproxies/proxies', this.modalData.data), () => {
          this.modalShow = false;
          axiosOk();
          this.reload();
        });
      }
    }
  }
}
</script>