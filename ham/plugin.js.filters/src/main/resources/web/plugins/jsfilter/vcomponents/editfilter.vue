<template>
  <div>
    <div class="col-md-12">
      <br/>

      <button type="button" class="bi bi-floppy" v-on:click="updateContent()"
              title="Save changes"></button>
      &nbsp;
      <button type="button" class="bi bi-download" v-on:click="download()"
              title="Download"></button>
      <br/>
      <br/>

    </div>

    <div class="col-md-12">
      <div class="form-group">
        <label htmlFor="id">Id</label>
        <input class="form-control" readOnly type="text" name="id" id="id" v-model="data.id"/>
      </div>
      <div class="form-group">
        <label htmlFor="name">name</label>
        <input class="form-control" type="text" name="name" id="name" v-model="data.name"/>
      </div>

      <div class="form-group">
        <label htmlFor="type">Script type</label>
        <select class="form-control" name="type" id="type" v-model="data.type">
          <option>script</option>
          <option>body</option>
        </select>
      </div>

      <div class="form-group">
        <label htmlFor="matcher">matcher</label>
        <select class="form-control" name="matcher" id="matcher" v-model="matchersSelected" @change="onChangeMatcher()">
          <option v-for="item in matchers"
                  v-bind:value="item"
          >{{ item }}
          </option>
        </select>
      </div>
      <div class="form-group">
        <label htmlFor="phase">phase</label>
        <select class="form-control" name="phase" id="phase" v-model="data.phase">
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
        <input class="form-control" type="text" name="priority" id="priority" v-model="data.priority"/>
      </div>
      <div class="form-check">
        <input class="form-check-input" type="checkbox" value="" id="blocking" name="blocking" v-model="data.blocking">
        <label class="form-check-label" for="blocking">
          blocking
        </label>
      </div>
    </div>
    <div class="boxed col-md-12">
      <br>
      <dynamic-matcher ref="dynmatch" width="1000px"
                       :path="'/plugins/jsfilter/vcomponents/matchers'"
                       :default="'apimatcher'"
                       :template="matchersSelected"
                       :value="matcher"
                       @componentevent="onMatcherEvent"/>
      <br>
    </div>
    <!--<div>

      <div class="form-group">
        <label htmlFor="method">method</label>
        <select class="form-control" name="method" id="method" v-model="matcher.method">
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
        <label htmlFor="hostPattern">hostPattern</label>
        <input class="form-control" type="text" name="hostPattern" id="hostPattern" v-model="matcher.hostPattern"/>
      </div>
      <div class="form-group">
        <label htmlFor="pathAddress">pathAddress</label>
        <input class="form-control" type="text" name="pathAddress" id="pathAddress" v-model="matcher.pathAddress"/>
      </div>
      <div class="form-group">
        <label htmlFor="pathPattern">pathPattern</label>
        <input class="form-control" type="text" name="pathPattern" id="pathPattern" v-model="matcher.pathPattern"/>
      </div>

    </div>-->
    <br>
    <div class="boxed col-md-12">
      <div class="form-group">
        <label for="source">source</label><br>
        <!--prism-live language-javascript-->
        <textarea spellcheck="false"
                  class="form-control"
                  rows="6" cols="50" name="source" id="source" v-model="source"></textarea>
      </div>
    </div>

    <!--<div class="col-md-12">
      <h4>REQUIRES</h4>
    </div>
    <div class="col-md-12">
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
  props: {
    selectedRow: Number
  },
  watch: {},
  data: function () {
    return {
      data: {},
      matcher: {},
      matchersSelected: "apimatcher",
      matchers: [],
      source: "",
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
    'dynamic-matcher': httpVueLoader('/plugins/jsfilter/vcomponents/dynamic-matcher.vue'),
  },
  computed: {},
  watch: {
    selectedRow: function (val, oldVal) {
      if (isUndefined(val)) return;
      var th = this;
      axiosHandle(axios.get("/api/matchers")
          , (matchers) => {
            matchers.data.forEach(function (f) {
              console.log(f);
              th.matchers.push(f);
            });
            axiosHandle(axios.get("/api/plugins/jsfilter/filters/" + val)
                , (result) => {
                  var matcherIndex = "apimatcher";
                  for (const [key, value] of Object.entries(result.data.matchers)) {
                    matcherIndex = key;
                  }
                  console.log("MATCHER SELECTED")
                  th.matchersSelected = matcherIndex;
                  th.matcher = JSON.parse(result.data.matchers[matcherIndex]);
                  if (isUndefined(th.matcher)) {
                    th.matcher = {};
                  }
                  th.data = result.data;
                  th.source = th.data.source;
                  axiosOk();
                });
          });

    },
    data: function (val, old) {

    }
  },
  methods: {
    download: function () {
      downloadFile("/api/plugins/jsfilter/filters/" + this.data.id + "?full=true", "script_" + this.data.id);
    },
    onChangeMatcher: function () {
      this.matcher = {};
    },
    onMatcherEvent: function (evt) {

    },
    fromArray: function (arlist) {
      var result = "";
      for (var i = 0; i < arlist.length; i++) {
        result += arlist[i] + "\r\n";
      }
      return result;
    },
    updateContent: function () {
      if (!this.$refs.dynmatch.isValid()) {
        addMessage("Fill required fields!", "error");
        return;
      }
      this.data.source = this.source
      this.data.matchers = {};
      this.data.matchers[this.matchersSelected] = JSON.stringify(this.matcher);
      axiosHandle(axios.put("/api/plugins/jsfilter/filters/" + this.data.id, this.data), axiosOk);
    },
  }
}
</script>