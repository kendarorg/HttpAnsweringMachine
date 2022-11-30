<template>
  <span>{{data}}</span>
</template>
<script>
module.exports = {
  props: {
    data: String,
    entry: Object,
    entrykey: String
  },
  name: 'tstring'
}
</script>