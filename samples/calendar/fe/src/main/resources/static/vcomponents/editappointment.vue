<template>
  <div>
    <label for="status">status</label>
    <input class="form-control" readonly type="text" name="status"  id="status" v-model="data.status"/>
    <label for="employeeId">employeeId</label>
    <input class="form-control" readonly type="text" name="employeeId"  id="employeeId" v-model="data.employeeId"/>
    <label for="date">date</label>
    <input class="form-control" type="text" name="date"  id="date" v-model="data.date"/>
    <label for="description">description</label>
    <input class="form-control" type="text" name="description"  id="description" v-model="data.description"/>
  </div>
</template>
<script>
module.exports = {
  name: 'edit-appointment',
  props: {
    data: Object
  }
}
</script>