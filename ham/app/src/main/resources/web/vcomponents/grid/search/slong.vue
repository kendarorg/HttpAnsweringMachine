<template>
  <form id="search">
    <input name="query" v-model="searchQuery" />
  </form>
</template>
<script>
module.exports = {
  props: {
    data: Number,
    grid: Function,
    entrykey: String
  },
  name: 'slong',
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