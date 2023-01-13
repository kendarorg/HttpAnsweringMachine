<template>
  <div v-if="active" class="loader-wrapper overlay">
    <div class="loader">
      <div></div>
      <div></div>
      <div></div>
      <div></div>
    </div>
    <p>{{ text }}</p>
  </div>
</template>
<script>
module.exports = {
  name: 'spinner',
  props: {
    active: Boolean,
    text: String
  }
}
</script>
<style scoped>
p {
  font-size: 0.8em;
  font-weight: 300;
  margin-top: 5px;
  letter-spacing: 1px;
  color: rgb(82, 82, 82);
}

.loader-wrapper {
  text-align: center;
}

.loader {
  display: inline-block;
  position: relative;
  width: 80px;
  height: 80px;
}
.loader div {
  box-sizing: border-box;
  display: block;
  position: absolute;
  width: 64px;
  height: 64px;
  margin: 8px;
  border: 8px solid #6916a0;
  border-radius: 50%;
  animation: loader 1.2s cubic-bezier(0.5, 0, 0.5, 1) infinite;
  border-color: #6916a0 transparent transparent transparent;
}
.loader div:nth-child(1) {
  animation-delay: -0.45s;
}
.loader div:nth-child(2) {
  animation-delay: -0.3s;
}
.loader div:nth-child(3) {
  animation-delay: -0.15s;
}
@keyframes loader {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.dimScreen
{
  position:fixed;
  padding:0;
  margin:0;

  top:0;
  left:0;

  width: 100%;
  height: 100%;
  background:rgba(204, 204, 204, 0.75);
}

.overlay{
  opacity:0.8;
  background-color: rgba(204, 204, 204, 0.75);
  position:fixed;
  width:100%;
  height:100%;
  top:0px;
  left:0px;
  z-index:1000;
}
</style>