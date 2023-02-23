<template>
  <div v-if="typeof this.data!='undefined'" width="800px">
    <kvp-modal v-if="showModal"
               :modal-data="modalData"
               @close="showModal = false">
    </kvp-modal>
    <div width="800px" >
      <div class="form-check" width="800px">
        <input class="form-check-input" type="checkbox" value="" id="request_static" name="request_static"
               v-model="data.static">
        <label class="form-check-label" for="request_static" >
          Is Static
        </label>
        &nbsp;
        <input class="form-check-input" type="checkbox" value="" id="request_soap" name="request_soap"
               v-model="data.soap">
        <label class="form-check-label" for="request_soap">
          Is Soap
        </label>
        &nbsp;
        <input class="form-check-input" type="checkbox" value="" id="request_binary" name="request_binary"
               v-model="data.binaryRequest">
        <label class="form-check-label" for="request_binary">
          Is Binary
        </label>
      </div>
    </div>
    <div  width="800px" >
      <h4>REQUEST QUERY</h4>
    </div>
    <div  width="800px">
      <button v-on:click="addNewQuery(false,[])" class="bi bi-plus-square" title="Add new"></button><br/><br/>
      <simple-grid id="rep18"
          :is-object="true"
          :extra="extraColumns"
          v-on:gridclicked="queryGridClicked"
          ref="queryGrid"
      />
    </div>
    <div  width="800px">
      <h4>REQUEST POST PARAMETERS</h4>
    </div>
    <div  width="800px">
      <button v-on:click="addNewPost(false,[])" class="bi bi-plus-square" title="Add new"></button><br/><br/>
      <simple-grid id="rep19"
          :is-object="true"
          :extra="extraColumns"
          v-on:gridclicked="postGridClicked"
          ref="postGrid"
      />
    </div>
    <div  width="800px">
      <h4>REQUEST HEADERS</h4>
    </div>
    <div  width="800px">
      <button v-on:click="addNewHeader(false,[])" class="bi bi-plus-square" title="Add new"></button><br/><br/>
      <simple-grid id="rep20"
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
  name: "request-line",
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
    data:function(val,old){
      var th=this;


      waitForAvailableVariableTimes(
          ()=>th.$refs.headersGrid,
          100,
          function(){
            th.$refs.headersGrid.reload(th.data.headers)
            th.$refs.queryGrid.reload(th.data.query)
            th.$refs.postGrid.reload(th.data.postParameters)
          },10
      );
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
        this.$refs.headersGrid.delete(evt.index,this.data.headers);
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
      this.$refs.headersGrid.update(realData,this.data.headers);
      this.showModal=false;
    },
    queryGridClicked: async function (evt) {
      if(evt.buttonid=="_edit"){
        this.addNewQuery(true,evt.index)
      }else if(evt.buttonid=="_delete"){
        this.$refs.queryGrid.delete(evt.index,this.data.query);
      }
    },
    addNewQuery: function (shouldEdit, rowId) {
      var row = null;
      if (shouldEdit) {
        row = this.$refs.queryGrid.getById(rowId);
      }
      this.modalData={
        valDesc:"Value",
        valValue:row==null?"":row['value'],
        keyDesc:"Key",
        keyValue:row==null?"":row['key'],
        title:"Query parameter",
        edit:shouldEdit,
        save:this.saveQuery
      };
      this.showModal=true;
    },
    saveQuery:function(data){
      var realData = {
        key:this.modalData.keyValue,
        value:this.modalData.valValue
      };
      this.$refs.queryGrid.update(realData,this.data.query);
      this.showModal=false;
    },
    postGridClicked: async function (evt) {
      if(evt.buttonid=="_edit"){
        this.addNewPost(true,evt.index)
      }else if(evt.buttonid=="_delete"){
        this.$refs.postGrid.delete(evt.index,this.data.postParameters);
      }
    },
    addNewPost: function (shouldEdit, rowId) {
      var row = null;
      if (shouldEdit) {
        row = this.$refs.postGrid.getById(rowId);
      }
      this.modalData={
        valDesc:"Value",
        valValue:row==null?"":row['value'],
        keyDesc:"Key",
        keyValue:row==null?"":row['key'],
        title:"Post parameter",
        edit:shouldEdit,
        save:this.savePost
      };
      this.showModal=true;
    },
    savePost:function(data){
      var realData = {
        key:this.modalData.keyValue,
        value:this.modalData.valValue
      };
      this.$refs.postGrid.update(realData,this.data.postParameters);
      this.showModal=false;
    },
    reload:function(val){


    },
    updated: function () {

    },
  }
}
</script>