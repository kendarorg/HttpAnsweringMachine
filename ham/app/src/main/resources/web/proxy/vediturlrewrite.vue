<template>
  <div>
  <label for="when">When</label>
  <input class="form-control" type="text" name="when"  id="when"  v-model="data.when"/>
    <label for="where">Where</label>
    <input class="form-control" type="text" name="where"  id="where"  v-model="data.where" />

    <label for="test">Test</label>
    <input class="form-control" type="text" name="test"  id="test"  v-model="data.test" />
    <label for="id">Id</label>
    <input class="form-control" type="text" name="id"  id="id"  v-model="data.id" disabled="true"/>
  </div>
</template>
<script>
module.exports = {
  name: 'change-url-rewrite',
  props:{
    data:Object
  }
}
</script>