<template>
  <div>
    <div class="form-group">
      <label for="key">Class or Package</label>
      <search-auto-complete :items="autocompletItems" @input="data.key= $event" v-bind:startvalue="data.key"/>
    </div>
    <div class="form-group">
      <label for="value">Level</label>
      <search-auto-complete :items="autocompletLevels" @input="data.value= $event" v-bind:startvalue="data.value"/>
    </div>
  </div>
</template>
<script>
module.exports = {
  name: 'change-logger',
  props: {
    data: Object
  },
  data: function () {
    return {
      autocompletItems: [],
      autocompletLevels: ['ALL', 'TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR', 'FATAL', 'OFF']
    }
  },
  mounted: function () {
    var th = this;

    axios.get("/api/log/special").then(function (data) {
      var appdata = [];
      for (var i = 0; i < data.data.length; i++) {
        appdata.push(data.data[i]['path']);
      }
      th.autocompletItems = appdata;
    });
  },
  components: {
    'search-auto-complete': httpVueLoader('/vcomponents/searchautocomplete.vue')
  }
}
</script>