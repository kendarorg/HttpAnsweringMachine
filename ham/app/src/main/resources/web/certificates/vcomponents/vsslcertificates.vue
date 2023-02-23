<template>
<div>
  <simple-grid id="ssl01"
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
    'simple-grid': httpVueLoader('/vcomponents/testgrid.vue')
  },
  name: 'ssl-certificates',
  data: function () {
    return {
      modalData: null,
      modalShow: false,
      columns: [
        {id: "id", template: "string", index: true,searchable:false,sortable:false}
      ],
      extraColumns: [
        {
          id: "_download", template: "iconbutton", default: false, searchable: false, sortable: false, properties: {
            name: "Download", style: "bi bi-download"
          }
        }
      ]
    }
  },
  methods: {
    retrieveData: async function () {
      var result = await axiosHandle(axios.get("/api/certificates"));
      var realData = [];
      for(var i=0;i<result.data.length;i++){
        var newRow={};
        newRow.id=result.data[i];
        realData.push(newRow);
      }
      result.data = realData;
      return result;
    },
    gridClicked: async function (evt) {
      var row = this.$refs.grid.getById(evt.index);

      if (evt.buttonid == "_download") {
        downloadFile("/api/certificates/"+row.id,row.id+".zip");
      }
    }
  }
}
</script>