<template>
  <div class="col-md-8">
      <ul class=" v-effect-link">
        <li  v-for="tab in tabs" >
          <a  :href="tab.href"  @click="selectTab(tab)">
            <span :class="{ 'is-active': tab.isActive }"><h3>[{{ tab.name }}]</h3></span>
          </a>
        </li>
      </ul>

    <div class="tabs-details">
      <slot></slot>
    </div>
  </div>
</template>
<script>
module.exports = {
  name: 'vtabs',
  data() {
    return {tabs: [] };
  },
  components: {
    'vtab': httpVueLoader('/vcomponents/tab/vtab.vue')
  },
  created() {
    this.tabs = this.$children;
  },
  watch:{
    tabs:function(val,oldVal){
      console.log("tabs changed")
      if(val.length>0){
        val[0].isActive=true;
      }
    }
  },
  methods: {
    selectTab(selectedTab) {
      this.tabs.forEach(tab => {
        tab.isActive = (tab.name == selectedTab.name);
      });
    }
  }
}
</script>
<style scoped>
.v-effect-link {
  list-style-type: none;
  margin: 0;
  padding: 0;
  text-align: center;
}
.v-effect-link li {
  display: inline-block;
  min-width: 5em;
  margin: 0 0.5em;
}
.v-effect-link a {
  text-decoration: none;
  display: block;
  position: relative;
  color: black;
  padding:.5em 0
}

.is-active {
  color: #42b983;
}

.is-active :before{
  content: "";
  position: absolute;
  width: 0;
  height: .5px;
  background-color: #42b983;
  bottom: calc(-1px);
  right: 0;
}

.v-effect-link a:hover {
  color: #42b983;
}

.v-effect-link a:hover:before {
  left: 0;
  width: 100%;
}
.v-effect-link a:before {
  content: "";
  position: absolute;
  width: 0;
  height: .5px;
  background-color: #42b983;
  bottom: calc(-1px);
  right: 0;
  transition: all 0.3s cubic-bezier(0.785, 0.135, 0.15, 0.86);
}

</style>