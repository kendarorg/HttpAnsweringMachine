<template>
  <div>
    TODO
    <div class="form-group">
      <label htmlFor="dbName">dbName</label>
      <input class="form-control" required type="text" name="dbName" id="dbName" v-model="matcher.dbName"/>
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
      return !isUndefined(matcher.dbName);
    }
  }
}
</script>