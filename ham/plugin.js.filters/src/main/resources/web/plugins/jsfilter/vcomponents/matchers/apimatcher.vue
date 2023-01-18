<template>
  <div>
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
      <label htmlFor="hostRegexp">hostRegexp</label>
      <input class="form-control" type="text" name="hostRegexp" id="hostRegexp" v-model="matcher.hostRegexp"/>
    </div>
    <div class="form-group">
      <label htmlFor="pathAddress">pathAddress</label>
      <input class="form-control" type="text" name="pathAddress" id="pathAddress" v-model="matcher.pathAddress"/>
    </div>
    <div class="form-group">
      <label htmlFor="pathRegexp">pathRegexp</label>
      <input class="form-control" type="text" name="pathRegexp" id="pathRegexp" v-model="matcher.pathRegexp"/>
    </div>
  </div>
</template>
<script>
module.exports = {
  name: "apimatcher",
  props: {
    matcher: Object
  },
  methods:{
    isValid:function(){

      return !isUndefined(matcher.pathRegexp)||!isUndefined(matcher.pathAddress)||
          !isUndefined(matcher.hostAddress)||!isUndefined(matcher.hostRegexp);
    }
  }
}
</script>