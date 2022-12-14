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
      <label htmlFor="method">method</label>
      <select class="form-control" name="method" id="method"v-model="data.method" >
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
      <input class="form-control" type="text" name="hostAddress" id="hostAddress" v-model="data.hostAddress"/>
    </div>
    <div class="form-group">
      <label htmlFor="hostRegexp">hostRegexp</label>
      <input class="form-control" type="text" name="hostRegexp" id="hostRegexp" v-model="data.hostRegexp"/>
    </div>
    <div class="form-group">
      <label htmlFor="pathAddress">pathAddress</label>
      <input class="form-control" type="text" name="pathAddress" id="pathAddress" v-model="data.pathAddress"/>
    </div>
    <div class="form-group">
      <label htmlFor="pathRegexp">pathRegexp</label>
      <input class="form-control" type="text" name="pathRegexp" id="pathRegexp"  v-model="data.pathRegexp"/>
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

  <div class="col-md-8">
    <h4>REQUIRES</h4>
  </div>
  <div class="col-md-8">
    <table class="table table-striped" id="requires_list">
      <tr>
        <th>Id</th>
        <th colspan="2">
          <button class="btn btn-danger form-control"
                  onClick="addRequires();">Add</button>
        </th>
      </tr>
    </table>
  </div>
</div>
</template>
<script>
module.exports = {
  name: "edit-js",
  props:{
    selectedRow:String
  },
  data:function(){
    return {
      data:{},
      source:""
    }
  },

  watch: {
    selectedRow: function (val, oldVal) {
      if(isUndefined(val))return;
      var th=this;
      axios.get("/api/plugins/jsfilter/filters/"+val)
          .then(function(result){
            th.data=result.data;
            th.source = th.fromArray(th.data.source);
          });
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
      this.data.source = this.source.split(/\r?\n/)
      axios.post("/api/plugins/jsfilter/filters/"+this.data.id,this.data)
          .then(function(result){
            //th.data=result.data;
          });
    }
  }
}
</script>