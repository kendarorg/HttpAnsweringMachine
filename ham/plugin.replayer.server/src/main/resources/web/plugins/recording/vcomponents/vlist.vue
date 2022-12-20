<template>
<div>
  <simple-modal v-if="modalShow"
                :modal-data="modalData"
                @close="modalShow = false">
    <span slot="header">Multiple Scripts</span>
    <span slot="body"><global-script :data="modalData"/></span>
<!--    <span slot="footer">-->
<!--              <button type="button" class="bi bi-x-square" @click="$emit('close')" title="Close"></button></span>-->
  </simple-modal>
  <button v-on:click="selectAll()" class="btn btn-default" title="Check All">Check all</button>
  <button v-on:click="toggleSelect()" class="btn btn-default" title="Toggle">Toggle Selected</button>
  <button v-on:click="selectedToStimulator()" class="btn btn-default" title="Toggle">Selected Stimulator</button>
  <button v-on:click="selectedToStimulated()" class="btn btn-default" title="Toggle">Selected Stimulated</button>
  <button v-on:click="selectedToPact()" class="btn btn-default" title="Toggle">Selected Pact</button>
  <Br><br>
  <button v-on:click="generateDns()" class="btn btn-default" title="Toggle">Gnerate DNS for selected</button>
  <button v-on:click="generateSSL()" class="btn btn-default" title="Toggle">Gnerate SSL for selected</button>
  <button v-on:click="setScript()" class="btn btn-default" title="Toggle">Set SCript for selected</button>
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
    'simple-grid': httpVueLoader('/vcomponents/testgrid.vue'),
    'global-script': httpVueLoader('/plugins/recording/vcomponents/line/vscript.vue'),
    'simple-modal': httpVueLoader('/vcomponents/tmodal.vue'),
  },
  data:function (){
    return {
      modalData: null,
      modalShow: false,
      data:[],
      columns: [
        {id: "id", template: "long", index: true,size:4,sortable:true},
        {id: "type", template: "string",label:"Type",size:4,sortable:true},
        {id: "pactTest", template: "boolw",label:"Pact",sortable:true},
        {id: "stimulatorTest", template: "boolw",label:"Stimulator",sortable:true},
        {id: "stimulatedTest", template: "boolw",label:"Stimultated",sortable:true},
        {id: "requestMethod", template: "string",label:"Method",size:7,sortable:true},
        {id: "requestHost", template: "string",label:"Host",sortable:true,size:15},
        {id: "requestPath", template: "string",label:"Path",sortable:true,size:15},
        {id: "queryCalc", template: "string",label:"Query",sortable:true,size:15},
        {id: "responseStatusCode", template: "long",label:"Status",size:4,sortable:true},
        {id: "requestHashCalc", template: "bool",label:"Req Body",sortable:true},
        {id: "responseHashCalc", template: "bool",label:"Res Body",sortable:true},
        {id: "preScript", template: "bool",label:"Pre",sortable:true},
        {id: "script", template: "bool",label:"Post",sortable:true},
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
          row['pactTest']=true;
        }
      })
    },
    toggleSelect: function () {
      this.$refs.grid.toggleSelect("select");
    },
    selectAll: function () {
      this.$refs.grid.selectAll("select");
    },
    generateDns:function (){
      var allHosts = [];
      this.$refs.grid.onSelected(function(row){
        allHosts.push(row['requestHost']);
      })
      var toUpload = JSON.stringify(allHosts);
      const headers = {'Content-Type': 'application/json'};
      axios.post('/api/dns/mappings', toUpload, {headers}).then((res) => {

      });
    },
    setScript:function (){
      var allHosts = [];
      this.$refs.grid.onSelected(function(row){
        allHosts.push(row['id']);
      })
      var ids = allHosts.join(",");
      this.modalData ={
        method:null,
        host:null,
        path:null,
        id:ids,
        pre:"",
        post:"",
        save:this.savePrePosts
      };
      this.modalShow = true;
    },
    savePrePosts: async function(){
      var pathId = this.modalData.id;
      var realId = this.modalData.id;
      if(realId.indexOf(",")>0){
        realId = "-1";
      }
      var toPost = {
        id:realId,
        method:"",
        host:"",
        path:"",
        pre :this.modalData.pre,
        post :this.modalData.post
      }


      var toUpload = JSON.stringify(toPost);
      const headers = {'Content-Type': 'application/json'};
      await axios.put('/api/plugins/replayer/recording/'+getUrlParameter("id")+'/script/'+pathId,toUpload, {headers}).then(() => {
        this.modalShow = false;
        location.reload();
      });
    },
    generateSSL:function (){
      var allHosts = [];
      this.$refs.grid.onSelected(function(row){
        allHosts.push(row['requestHost']);
      })
      var toUpload = JSON.stringify(allHosts);
      const headers = {'Content-Type': 'application/json'};
      axios.post('/api/ssl', toUpload, {headers}).then((res) => {

      });
    }
  }
}
</script>