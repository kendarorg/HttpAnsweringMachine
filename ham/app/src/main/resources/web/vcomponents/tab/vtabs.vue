<!--suppress ALL -->
<template>
  <div class="col-md-8">
    <ul class="nav nav-tabs tabs-width">
      <li v-for="(tab,index) in tabs" class="nav-item">
        <a :id="prefix+'tab_'+index" class="nav-link" :href="tab.href" :class="{ 'really-active': tab.isActive }" @click="selectTab(tab)">
          {{ tab.compTitle }}
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
  props: {
    prefix:{
      type: String,
      required: false,
      default:""
    },
    reactToHashBang: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  data() {
    return {tabs: []};
  },
  components: {
    'vtab': httpVueLoader('/vcomponents/tab/vtab.vue')
  },
  created() {
    this.tabs = this.$children;
  },
  watch: {
    tabs: function (val, oldVal) {

        if (val.length > 0) {
          if(this.prefix=="") {
            val[0].isActive = true;
          }else{
            var selected = sessionStorage.getItem("tab_"+this.prefix);
            if(selected==null){
              val[0].isActive = true;
              sessionStorage.setItem("tab_"+this.prefix, "0");
            }else{
              val[parseInt(selected)].isActive = true;
            }
          }
        }

    }
  },
  methods: {
    selectTab(selectedTab) {
      var toSelect = "";
      if (typeof selectedTab === 'string') {
        toSelect = selectedTab;
      } else {
        toSelect = selectedTab.name;
      }
      this.tabs.forEach(tab => {
        if (tab.isActive == (tab.name == toSelect)) return;
        tab.isActive = (tab.name == toSelect);
      });
      for(var i=0;i<this.tabs.length;i++){
        if(this.tabs[i].isActive){
          sessionStorage.setItem("tab_"+this.prefix, i+"");
        }
      }
    }
  }
}
</script>
<style>
.really-active {
  border: 1px solid;
  border-top-left-radius: 0.25rem;
  border-top-right-radius: 0.25rem;
  color: #495057;
  background-color: #EEEEEE;
  border-color: black black black;
}
</style>
<!--<style scoped>
  border-color: #dee2e6 #dee2e6 #fff;
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

</style>-->