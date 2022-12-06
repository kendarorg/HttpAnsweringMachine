<template>
  <div>
    <label for="address">DNS or IP</label>
    <input class="form-control" type="text" name="address"  id="address"  v-model="data.address"/>
    <label for="id">Id</label>
    <input class="form-control" type="text" name="id"  id="id"  v-model="data.id" disabled="true"/>
  </div>
</template>
<script>
module.exports = {
  name: 'change-ssl-website',
  props:{
    data:Object
  }
}
</script>