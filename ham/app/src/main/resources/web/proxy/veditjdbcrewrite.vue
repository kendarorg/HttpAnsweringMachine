<template>
  <div>
  <label for="driver">Driver</label>
  <input class="form-control" type="text" name="driver"  id="driver"  v-model="data.driver"/>

    <div class="card">
      <b>REMOTE</b>
      <label for="connectionString">Connection string</label>
      <input class="form-control" type="text" name="connectionString"  id="connectionString"  v-model="data.remote.connectionString" />
      <label for="login">Login</label>
      <input class="form-control" type="text" name="login"  id="login"  v-model="data.remote.login" />
      <label for="password">Password</label>
      <input class="form-control" type="password" name="password"  id="password"  v-model="data.remote.password" />
    </div>
    <br><br>
    <div class="card">
      <b>LOCAL</b>
      <br><b>Connection String: </b> jdbc:janus:http://THISHOST/api/db/{{data.exposed.connectionString}}
      <br><b>Driver: </b> org.kendar.janus.JdbcDriver
      <br>
      <label for="connectionString">Name</label>
      <input class="form-control" type="text" name="connectionString"  id="connectionString"  v-model="data.exposed.connectionString" />
      <label for="login">Login</label>
      <input class="form-control" type="text" name="login"  id="login"  v-model="data.exposed.login" />
      <label for="password">Password</label>
      <input class="form-control" type="password" name="password"  id="password"  v-model="data.exposed.password" />
    </div>
  </div>
</template>
<script>
module.exports = {
  name: 'change-jdbc-rewrite',
  props:{
    data:Object
  }
}
</script>