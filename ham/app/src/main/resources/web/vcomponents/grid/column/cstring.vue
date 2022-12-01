<template>
  <span>{{data}}</span>
</template>
<script>
module.exports = {
  props: {
    data: String,
    entry: Object,
    entrykey: String,
    properties: Object
  },
  name: 'cstring'
}
</script>