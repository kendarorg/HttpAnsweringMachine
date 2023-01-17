<template>
<div>
  <div class="col-md-8">
    <br>

    <button type="button" class="bi bi-floppy" v-on:click="updateContent()"
            :disabled="typeof data.id=='undefined'" title="Save changes"></button>
    <br>
    <br>

  </div>

  <div class="col-md-8">
    <div class="form-group">
      <label htmlFor="id">Id</label>
      <input class="form-control" readOnly type="text" name="id" id="id" v-model="data.id"/>
    </div>
    <div class="form-group">
      <label htmlFor="name">name</label>
      <input class="form-control" type="text" name="name" id="name" v-model="data.name"/>
    </div>
    <div class="form-group">
      <label htmlFor="method">method</label>
      <select class="form-control" name="method" id="method"v-model="matcher.method" >
        <option>GET</option>
        <option>POST</option>
        <option>PUT</option>
        <option>DELETE</option>
        <option>OPTIONS</option>
        <option>PATCH</option>
      </select>
    </div>
    <div class="form-group">
      <label htmlFor="hostAddress">hostAddress</label>
      <input class="form-control" type="text" name="hostAddress" id="hostAddress" v-model="matcher.hostAddress"/>
    </div>
    <div class="form-group">
      <label htmlFor="hostRegexp">hostRegexp</label>
      <input class="form-control" type="text" name="hostRegexp" id="hostRegexp" v-model="matcher.hostRegexp"/>
    </div>
    <div class="form-group">
      <label htmlFor="pathAddress">pathAddress</label>
      <input class="form-control" type="text" name="pathAddress" id="pathAddress" v-model="matcher.pathAddress"/>
    </div>
    <div class="form-group">
      <label htmlFor="pathRegexp">pathRegexp</label>
      <input class="form-control" type="text" name="pathRegexp" id="pathRegexp"  v-model="matcher.pathRegexp"/>
    </div>
    <div class="form-group">
      <label htmlFor="phase">phase</label>
      <select class="form-control" name="phase" id="phase" v-model="data.phase" >
        <option>NONE</option>
        <option>PRE_RENDER</option>
        <option>API</option>
        <option>STATIC</option>
        <option>PRE_CALL</option>
        <option>POST_CALL</option>
        <option>POST_RENDER</option>
      </select>
    </div>
    <div class="form-group">
      <label htmlFor="type">Script type</label>
      <select class="form-control" name="type" id="type" v-model="data.type" >
        <option>script</option>
        <option>body</option>
      </select>
    </div>
    <div class="form-group">
      <label htmlFor="priority">priority (number)</label>
      <input class="form-control" readOnly type="text" name="priority" id="priority" v-model="data.priority"/>
    </div>
    <div class="form-check">
      <input class="form-check-input" type="checkbox" value="" id="blocking" name="blocking" v-model="data.blocking">
      <label class="form-check-label" for="blocking">
        blocking
      </label>
    </div>
  </div>
  <div class="form-group">
    <label for="source">source</label><br>
    <!--prism-live language-javascript-->
    <textarea spellcheck="false"
              class="form-control"
              rows="6" cols="50"  name="source" id="source" v-model="source"></textarea>
  </div>

  <!--<div class="col-md-8">
    <h4>REQUIRES</h4>
  </div>
  <div class="col-md-8">
    <br>
    <button v-on:click="reload()" class="bi bi-arrow-clockwise" title="Reload"></button>
    <button v-on:click="addNew(false,[])" class="bi bi-plus-square" title="Add new"></button>
    <br><br>
    <simple-grid
        v-on:gridclicked="gridClicked"
        ref="grid"
        :columns="columns"
        :extra="extraColumns"
        :retrieve-data="retrieveData"
    >
    </simple-grid>
  </div>-->
</div>
</template>
<script>
module.exports = {
  name: "edit-js",
  props:{
    selectedRow:Number
  },
  data:function(){
    return {
      data:{},
      matcher:{},
      source:"",
      columns: [
        {id: "id", template: "string", index: true},
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
  components: {
    'simple-grid': httpVueLoader('/vcomponents/testgrid.vue'),
    'simple-modal': httpVueLoader('/vcomponents/tmodal.vue'),
  },
  watch: {
    selectedRow: function (val, oldVal) {
      if(isUndefined(val))return;
      var th=this;
      axios.get("/api/plugins/jsfilter/filters/"+val)
          .then(function(result){
            th.matcher = JSON.parse(result.data.matchers['apimatcher']);
            th.data=result.data;
            th.source = th.data.source;
          });
    },
    data:function(val,old){
      // var th=this;
      // waitForAvailableVariableTimes(
      //     ()=>th.$refs.grid,
      //     100,
      //     function(){
      //       th.$nextTick(function () {
      //
      //         th.$refs.grid.reload(th.data.headers)
      //         th.$refs.queryGrid.reload(th.data.query)
      //         th.$refs.postGrid.reload(th.data.postParameters)
      //       })
      //     },10
      // );
    }
  },
  methods:{
    fromArray : function(arlist){
      var result = "";
      for(var i = 0;i<arlist.length;i++){
        result+=arlist[i]+"\r\n";
      }
      return result;
    },
    updateContent:function (){
      this.data.source = this.source
      this.data.matchers['apimatcher']=JSON.stringify(this.matcher);
      axios.put("/api/plugins/jsfilter/filters/"+this.data.id,this.data)
          .then(function(result){
            //th.data=result.data;
          });
    },
    // gridClicked: async function (evt) {
    //   var row = this.$refs.grid.getById(evt.index);
    //
    //   if (evt.buttonid == "_edit") {
    //     location.href="text.html?id="+getUrlParameter("id")+
    //         "&file="+value["value"]+"&action=edit";
    //   } else if (evt.buttonid == "_delete") {
    //     alert("DELETE TO BE IMPLEMENTED"); //TODO
    //     this.$refs.grid.delete(evt.index);
    //   }
    // },
    // retrieveData: async function () {
    //
    //   return {
    //     data:[]
    //   };
    // },
  }
}
</script>