<template>
  <div>
    <div class="row">
      <div class="col-md-8">
        <h3>UPLOAD SCRIPT</h3>
        <ham-upload id="upload-create-filter"
                    path="/api/plugins/replayer/recording"
                    @success="onSuccess"
                    @error="onError"
        ></ham-upload>
      </div>
      <div class="col-md-8">
        <h3>CREATE SCRIPT</h3>
        <form action="" id="createScript" method="POST">
          <div class="form-group">
            <input class="form-control" type="text" name="createScriptName" id="createScriptName" v-model="scriptName"/>
          </div>
          <div class="form-group">
            <button type="button" id="createScriptBt" name="createScriptBt" class="btn btn-default" v-on:click="create">
              Create
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
<script>
module.exports = {
  name: "create-js",
  props: {
    data: Object
  },
  data: function () {
    return {
      scriptName: ""
    }
  },
  components: {
    'ham-upload': httpVueLoader('/vcomponents/tupload.vue')
  },
  computed: {
    realPath: function () {
      return '/api/plugins/jsfilter/filters/' + this.scriptName;
    }
  },
  methods: {
    onSuccess: function (data) {
      //location.href = "script.html?id="+data.response.data;
    },
    onError: function (data) {
      addMessage(data.error, "error");
    },
    create: function () {
      var data = {
        name: this.scriptName,
        phase: "NONE",
        type: "script",
        blocking: false,
        matchers: {
          apimatcher: JSON.stringify({
            hostAddress: "www.change.me",
            pathAddress: "/changeme",
            method: "GET",
          }),
        },
        source:
            "//Here you can examine the request and produce a response\n" +
            "response.setResponseText('To implement');\n" +
            "response.setStatusCode(404);\n" +
            "//If return is false and blocking is false the response will be sent immediatly\n" +
            "return false;"

      }
      const headers = {'Content-Type': 'application/json'};
      axiosHandle(axios.post('/api/plugins/jsfilter/filters', JSON.stringify(data), {headers}), (res) => {
        location.href = "index.html?id=" + name;
      });
    }
  }
}
</script>