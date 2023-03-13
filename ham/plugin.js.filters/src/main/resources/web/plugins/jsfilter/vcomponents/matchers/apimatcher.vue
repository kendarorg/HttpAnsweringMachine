<template>
  <div>
    <h3>API Matcher</h3><br>
    <p>At least one between path/host address/regexp must be set</p>
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
  </div>
</template>
<script>
module.exports = {
  name: "apimatcher",
  props: {
    matcher: Object
  },
  methods: {
    isValid: function () {

      return !isUndefined(this.matcher.pathPattern) || !isUndefined(this.matcher.pathAddress) ||
          !isUndefined(this.matcher.hostAddress) || !isUndefined(this.matcher.hostPattern);
    }
  }
}
</script>