<template>
  <div>
    <b>Name:</b> {{value.name}}<br>
    <b>Type:</b> {{value.type}}<br>
    <b>Value:</b> {{value.value}}<br>
  </div>
</template>
<script>
module.exports = {
  name: "basiccomponent",
  props:{
    value:Object
  }
}
</script>