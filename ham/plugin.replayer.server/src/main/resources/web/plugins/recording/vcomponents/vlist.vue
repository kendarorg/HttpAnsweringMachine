<template>
<div>
  <button v-on:click="selectAll()" class="btn btn-default" title="Check All">Check all</button>
  <button v-on:click="toggleSelect()" class="btn btn-default" title="Toggle">Toggle Selected</button>
  <button v-on:click="selectedToStimulator()" class="btn btn-default" title="Toggle">Selected Stimulator</button>
  <button v-on:click="selectedToStimulated()" class="btn btn-default" title="Toggle">Selected Stimulated</button>
  <button v-on:click="selectedToPact()" class="btn btn-default" title="Toggle">Selected Pact</button>
  <Br><br>
  <button v-on:click="reload()" class="bi bi-arrow-clockwise" title="Reload"></button>
  <!--<button v-on:click="addNew(false,[])" class="bi bi-plus-square" title="Add new"></button>-->
  <button v-on:click="download()" class="bi bi-download" title="Download Hosts File"></button>
  <br><br>
  <simple-grid
      v-on:gridclicked="gridClicked"
      ref="grid"
      :columns="columns"
      :extra="extraColumns"
  >
  </simple-grid>
</div>
</template>
<script>
module.exports = {
  name: "recording-list",
  components: {
    'simple-grid': httpVueLoader('/vcomponents/testgrid.vue')
  },
  data:function (){
    return {
      data:[],
      columns: [
        {id: "id", template: "long", index: true,size:4},
        {id: "pactTest", template: "boolw",label:"Pact"},
        {id: "stimulatorTest", template: "boolw",label:"Stimulator"},
        {id: "stimulatedTest", template: "boolw",label:"Stimultated"},
        {id: "requestMethod", template: "string",label:"Method",size:7},
        {id: "requestHost", template: "string",label:"Host"},
        {id: "requestPath", template: "string",label:"Path"},
        {id: "queryCalc", template: "string",label:"Query"},
        {id: "responseStatusCode", template: "long",label:"Status",size:4},
        {id: "requestHashCalc", template: "bool",label:"Req Body"},
        {id: "responseHashCalc", template: "bool",label:"Res Body"},
        {id: "preScript", template: "bool",label:"Pre"},
        {id: "script", template: "bool",label:"Post"},
      ],
      extraColumns: [
        {id:"select",template:"boolw",default:false},
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
  methods:{
    reload(externalData){
      console.log("RELOAD VLIST")
      this.$refs.grid.reload(externalData);
    },
    download:function(){
      this.$emit("download");
    },
    gridClicked: async function (evt) {
      var row = this.$refs.grid.getById(evt.index);
      var th = this;
      if (evt.buttonid == "_edit") {
        th.$emit("selectrow",row['id']);
        //location.href = "line.html?line="+row['id']+"&id="+getUrlParameter("id");
      } else if (evt.buttonid == "_delete") {
        await axios.delete("/api/plugins/replayer/recording/"+getUrlParameter("id")+"/lineindex/" + row['id'])
            .then(function(){
              th.$emit("reload");
            })
      }
    },
    selectedToStimulator:function(){
      this.$refs.grid.onSelected(function(row){
        if(row['select']){
          row['stimulatorTest']=true;
        }
      })
    },
    selectedToStimulated:function(){
      this.$refs.grid.onSelected(function(row){
        if(row['select']){
          row['stimulatedTest']=true;
        }
      })
    },
    selectedToPact:function(){

      this.$refs.grid.onSelected(function(row){
        if(row['select']){
          row['pacttests']=true;
        }
      })
    },
    toggleSelect: function () {
      this.$refs.grid.toggleSelect("select");
    },
    selectAll: function () {
      this.$refs.grid.selectAll("select");
    }
    /*,
    addNew: function (shouldEdit, rowId) {
      var row = null;
      if (shouldEdit) {
        row = this.$refs.grid.getById(rowId);
      } else {
        row = {

        }
      }
      this.modalData = {
        data: row,
        edit: shouldEdit,
        save: this.save
      };
      this.modalShow = true;
    }*/
  }
}
</script>