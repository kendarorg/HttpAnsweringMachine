<template>
  <div>
  <label for="name">Address</label>
  <input class="form-control" type="text" name="address"  id="address"  v-model="data.address"/>
    <label for="resolved">Resolved</label>
    <input class="form-control" type="text" name="resolved"  id="resolved"  v-model="data.resolved" disabled="true"/>
  <div class="form-check">
    <input class="form-check-input" type="checkbox" value="" id="enabled" name="enabled" v-model="data.enabled">
    <label class="form-check-label" for="enabled">
      Running
    </label>
  </div>

    <label for="id">Id</label>
    <input class="form-control" type="text" name="id"  id="id"  v-model="data.id" disabled="true"/>
  </div>
</template>
<script>
module.exports = {
  name: 'change-dns-server',
  props:{
    data:Object
  }
}
</script>