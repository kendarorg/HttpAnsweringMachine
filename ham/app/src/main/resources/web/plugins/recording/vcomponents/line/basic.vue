<template>
  <div>
    <h3>{{value.type}}</h3><br>
    <span v-if="value.name"><b>Name:</b> {{value.name}}<br></span>
    <span v-if="value.name"><b>Value:</b> {{value.value}}<br></span>
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