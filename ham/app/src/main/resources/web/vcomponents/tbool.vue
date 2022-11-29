<template>
  <input type="checkbox" id="checkbox" v-model="data" disabled="true"/>
</template>
<script>
module.exports = {
  props: {
    data: Boolean
  },
  name: 'tstring'
}
</script>