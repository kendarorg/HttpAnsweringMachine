<template>
  <form id="search">
    <input name="query" v-model="searchQuery" />
  </form>
</template>
<script>
module.exports = {
  props: {
    data: String,
    grid: Function,
    entrykey: String
  },
  name: 'sstring',
  data:function(){
    return {
      searchQuery: ""
    };
  },
  watch:{
    searchQuery:function(val,oldVal){
      this.grid(this.entrykey,val,"string");
    }
  },
  methods:{
    clean:function(){
      this.searchQuery = "";
    }
  }
}
</script>