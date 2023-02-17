<template>
  <div>
    <h3>Query Matcher</h3><br>
    <p>SQL, and db Name are mandatory</p>
    <p>The data can be set directly with body type, copying from a real resultset obtained from logs or recording</p>
    <div class="form-group">
      <label htmlFor="dbName">dbName</label>
      <input class="form-control" required type="text" name="dbName" id="dbName" v-model="matcher.dbName"/>
    </div>
    <div class="form-group">
      <label for="sql">sql</label><br>
      <!--prism-live language-javascript-->
      <textarea spellcheck="false"
                class="form-control"
                rows="6" cols="50" name="sql" id="sql" v-model="matcher.sql"></textarea>
    </div>
  </div>
</template>
<script>
module.exports = {
  name: "querymatcher",
  props: {
    matcher: Object
  },
  methods:{
    isValid:function(){
      return !isUndefined(this.matcher.dbName);
    }
  }
}
</script>