<template>
  <div>
    <label for="role">Role</label>
    <input class="form-control" type="text" name="role" id="role" v-model="data.role"/>

    <label for="dns">Name</label>
    <input class="form-control" type="text" name="name" id="name" v-model="data.name"/>
    <label for="id">Id</label>
    <input class="form-control" type="text" name="id" id="id" v-model="data.id" disabled="true"/>
  </div>
</template>
<script>
module.exports = {
  name: 'edit-employee',
  props: {
    data: Object
  }
}
</script>