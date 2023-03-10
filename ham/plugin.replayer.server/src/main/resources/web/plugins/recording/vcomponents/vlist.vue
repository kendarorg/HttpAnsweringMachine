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
  <button v-on:click="deleteSelected()" class="bi bi-trash" title="Toggle">Delete Selected</button>
  <button v-on:click="selectedToStimulator()" class="btn btn-default" title="Toggle">Selected Stimulator</button>
  <Br><br>
  <button v-on:click="generateDns()" class="btn btn-default" title="Toggle">Gnerate DNS for selected</button>
  <button v-on:click="generateSSL()" class="btn btn-default" title="Toggle">Gnerate SSL for selected</button>
  <button v-on:click="setScript()" class="btn btn-default" title="Toggle">Set SCript for selected</button>
  <Br><br>
  <button v-on:click="reload()" class="bi bi-arrow-clockwise" title="Reload"></button>
  <!--<button v-on:click="addNew(false,[])" class="bi bi-plus-square" title="Add new"></button>-->
  <button v-on:click="download()" class="bi bi-download" title="Download Hosts File"></button>
  <br><br>
  <div class="form-group">
    <label for="searchStr">Search content</label>
    <button v-on:click="searchForStr()" class="bi bi-arrow-clockwise" title="Reload"></button>
    <input class="form-control" type="text" name="searchStr" id="searchStr" v-model="searchStr"/>
  </div>
  <simple-grid id="res23"
      v-on:gridrowclicked="recordingListClicked"
      v-on:gridclicked="gridClicked"
      ref="grid"
      :selectedindex="selectedindex"
      :columns="columns"
      :extra="extraColumns"
  >
  </simple-grid>
</div>
</template>
<script>
module.exports = {
  name: "recording-list",
  props: {
    selectedindex: {
      type:Number,
      optional:true,
      default:-1
    }
  },
  components: {
    'simple-grid': httpVueLoader('/vcomponents/testgrid.vue'),
    'global-script': httpVueLoader('/plugins/recording/vcomponents/line/vscript.vue'),
    'simple-modal': httpVueLoader('/vcomponents/tmodal.vue'),
  },
  data:function (){
    return {
      searchStr:"",
      modalData: null,
      modalShow: false,
      data:[],
      columns: [
        {id: "id", template: "long", index: true,size:4,sortable:true},
        {id: "type", template: "string",label:"Type",size:4,sortable:true},
        {id: "stimulatorTest", template: "boolw",label:"Stimulator",sortable:true,visible:false},
        {id: "requestMethod", template: "string",label:"Method",size:7,sortable:true},
        {id: "requestHost", template: "string",label:"Host",sortable:true,size:15,visible:false},
        {id: "requestPath", template: "string",label:"Path",sortable:true,size:15},
        {id: "queryCalc", template: "string",label:"Query",sortable:true,size:15,visible:false},
        {id: "responseStatusCode", template: "long",label:"Status",size:4,sortable:true},
        {id: "requestHashCalc", template: "bool",label:"Req Body",sortable:true,visible:false},
        {id: "responseHashCalc", template: "bool",label:"Res Body",sortable:true,visible:false},
        {id: "preScript", template: "bool",label:"Pre",sortable:true,visible:false},
        {id: "script", template: "bool",label:"Post",sortable:true,visible:false},
        {id: "calls", template: "long",label:"Calls",sortable:true,size:4},
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
    recordingListClicked(evt){
      this.$emit("gridrowclicked",evt);
    },
    searchForStr(){
      this.$emit("reload",this.searchStr);
    },
    reload(externalData){
      if(typeof externalData=="undefined"){

        this.$emit("reload","");
      }else {
        if(!isUndefined(this.$refs.grid) && !isUndefined(this.$refs.grid.reload)){
        this.$refs.grid.reload(externalData);
        }
      }
    },
    download:function(){
      this.$emit("download");
    },
    gridClicked: async function (evt) {
      var row = this.$refs.grid.getById(evt.index);
      var th = this;
      if (evt.buttonid == "_edit") {
        th.$emit("selectrow",row['id'],row['type']);
        //location.href = "line.html?line="+row['id']+"&id="+getUrlParameter("id");
      } else if (evt.buttonid == "_delete") {
        await axiosHandle(axios.delete("/api/plugins/replayer/recording/"+
            getUrlParameter("id")+"/lineindex/" + row['id'])
            ,()=>{
              th.$emit("reload");
              axiosOk();
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
      axiosHandle(axios.post('/api/dns/mappings', toUpload, {headers}),axiosOk);
    },
    deleteSelected:function(){
      //let confirmAction = confirm("Are you sure to delete the lines");
      //if (confirmAction) {
        var data = [];
        this.$refs.grid.onSelected(function(row){
          data.push(row['id']);
        })
        var id = getUrlParameter("id");

        var th=this;
        const headers = {'Content-Type': 'application/json'};
      axiosHandle(axios.post('/api/plugins/replayer/recording/'+id+'/deletelines', data, {headers}),()=>{
          th.$emit("reload");
          axiosOk();
        })
      //}
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
      await axiosHandle(axios.put('/api/plugins/replayer/recording/'+
          getUrlParameter("id")+'/script/'+pathId,toUpload, {headers}),() => {
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
      axiosHandle(axios.post('/api/ssl', toUpload, {headers}),axiosOk);
    }
  }
}
</script>