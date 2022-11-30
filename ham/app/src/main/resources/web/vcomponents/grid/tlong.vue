<template>
  <span>{{data}}</span>
</template>
<script>
module.exports = {
  props: {
    data: Number,
    entry: Object,
    entrykey: String
  },
  name: 'tlong'
}
</script>