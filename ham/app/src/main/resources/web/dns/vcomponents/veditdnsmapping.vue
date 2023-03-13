<template>
  <div>
    <label for="ip">Ip</label>
    <input class="form-control" type="text" name="ip" id="ip" v-model="data.ip"/>

    <label for="dns">Dns</label>
    <input class="form-control" type="text" name="dns" id="dns" v-model="data.dns"/>
    <label for="id">Id</label>
    <input class="form-control" type="text" name="id" id="id" v-model="data.id" disabled="true"/>
  </div>
</template>
<script>
module.exports = {
  name: 'change-dns-mapping',
  props: {
    data: Object
  }
}
</script>