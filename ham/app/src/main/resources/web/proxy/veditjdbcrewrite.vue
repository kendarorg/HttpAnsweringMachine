<template>
  <div>
    <label for="driver">Driver</label>
    <input class="form-control" type="text" name="driver" id="driver" v-model="data.driver"/>
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
      jdbc:janus:http://THISHOST/api/db/{{ data.exposed.connectionString }}?loadRsOnExec=true
      <br><b>Driver: </b> org.kendar.janus.JdbcDriver
      <br>
      <label for="connectionStringL">Name</label>
      <input class="form-control" type="text" name="connectionStringL" id="connectionStringL"
             v-model="data.exposed.connectionString"/>
      <label for="loginL">Login</label>
      <input class="form-control" type="text" name="loginL" id="loginL" v-model="data.exposed.login"/>
      <label for="passwordL">Password</label>
      <input class="form-control" type="password" name="passwordL" id="passwordL" v-model="data.exposed.password"/>

    </div>
  </div>
</template>
<script>
module.exports = {
  name: 'change-jdbc-rewrite',
  props: {
    data: Object
  }
}
</script>