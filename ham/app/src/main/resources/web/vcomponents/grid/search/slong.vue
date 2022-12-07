<template>
  <form id="search">
    <input name="query" v-model="searchQuery" />
  </form>
</template>
<script>
module.exports = {
  props: {
    descriptor: Object,
  },
  name: 'slong',
  data:function(){
    return {
      searchQuery: ""
    };
  },
  watch:{
    searchQuery:function(val,oldVal){
      this.$parent.setSearchField(this.descriptor.id,val,this.descriptor.template);
    }
  },
  methods:{
    clean:function(){
      this.searchQuery = "";
    }
  }
}
</script>