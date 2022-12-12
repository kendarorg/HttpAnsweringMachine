<template>
  <div width="800px">
    <kvp-modal v-if="showModal"
               :modal-data="modalData"
               @close="showModal = false">
    </kvp-modal>
    <div  width="800px">
      <h4>RESPONSE HEADERS</h4>
    </div>
    <div  width="800px">
      <button v-on:click="addNewHeader(false,[])" class="bi bi-plus-square" title="Add new"></button><br/>
      <simple-grid
          :is-object="true"
          :extra="extraColumns"
          v-on:gridclicked="headersGridClicked"
          ref="headersGrid"
      />
    </div>
  </div>
</template>
<script>
module.exports = {
  name: "response-line",
  props:{
    data:Object
  },
  data:function (){
    return {
      showModal:false,
      modalData:{},
      extraColumns: [
        {id: "select", template: "boolw", default: false},
        {
          id: "_edit", template: "button", default: false, properties: {
            name: "Edit", style: "btn btn-success btn-sm"
          }
        },
        {
          id: "_delete", template: "button", default: false, properties: {
            name: "Delete", style: "btn btn-danger btn-sm"
          }
        }
      ]
    }
  },
  watch:{
    data:function(val,oldVal){
      this.$refs.headersGrid.reload(val.response.headers)
    }
  },
  components: {
    'kvp-modal': httpVueLoader('/vcomponents/tkvpmodal.vue'),
    'simple-grid': httpVueLoader('/vcomponents/testgrid.vue')
  },
  methods: {
    headersGridClicked: async function (evt) {
      if(evt.buttonid=="_edit"){
        this.addNewHeader(true,evt.index)
      }else if(evt.buttonid=="_delete"){
        this.$refs.headersGrid.delete(evt.index,this.data.response.headers);
      }
    },
    addNewHeader: function (shouldEdit, rowId) {
      var row = {};
      if (shouldEdit) {
        row = this.$refs.headersGrid.getById(rowId);
      }
      this.modalData={
        valDesc:"Value",
        valValue:row==null?"":row['value'],
        keyDesc:"Key",
        keyValue:row==null?"":row['key'],
        title:"Header",
        edit:shouldEdit,
        save:this.saveHeader
      };
      this.showModal=true;
    },
    saveHeader:function(data){
      var realData = {
        key:this.modalData.keyValue,
        value:this.modalData.valValue
      };
      this.$refs.headersGrid.update(realData,this.data.response.headers);
      this.showModal=false;
    }
  }
}
</script>