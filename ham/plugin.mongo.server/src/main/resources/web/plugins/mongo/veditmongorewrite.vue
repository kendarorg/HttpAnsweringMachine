<template>
  <div>
    <input class="form-check-input" type="checkbox" value="" id="active" name="active" v-model="data.active">
    <label class="form-check-label" for="active">
      Active
    </label>
    <div class="card">
      <b>REMOTE</b>
      <label for="connectionStringR">Connection string</label>
      <input class="form-control" type="text" name="connectionStringR" id="connectionStringR"
             v-model="data.remote.connectionString"/>
      <label for="loginR">Login</label>
      <input class="form-control" type="text" name="loginR" id="loginR" v-model="data.remote.login"/>
      <label for="passwordR">Password</label>
      <input class="form-control" type="password" name="passwordR" id="passwordR" v-model="data.remote.password"/>
    </div>
    <br><br>
    <div class="card">
      <b>LOCAL</b>
      <br><b>Connection String: </b>
      mongodb://THISHOST:{{ data.exposedPort }}
      <br>
      <label for="exposedPort">Exposed Port</label>
      <input class="form-control" type="text" name="exposedPort" id="exposedPort"
             v-model="data.exposedPort"/>
    </div>
  </div>
</template>
<script>
module.exports = {
  name: 'change-mongo-rewrite',
  props: {
    data: Object
  }
}
</script>