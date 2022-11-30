<template>
  <form id="search">
    <input name="query" v-model="searchQuery" />
  </form>
</template>
<script>
module.exports = {
  props: {
    data: Boolean,
    grid: Function,
    entrykey: String
  },
  name: 'sboolw',
  data:function(){
    return {
      searchQuery: ""
    };
  },
  watch:{
    searchQuery:function(val,oldVal){
      if(val==null|| typeof val == "undefined"){
        this.grid(this.entrykey,null,"string");
      }else{
        if((val.toLowerCase() === 'true')||val==="1"){
          this.grid(this.entrykey,true,"string");
        }else if((val.toLowerCase() === 'false')||val==="1"){
          this.grid(this.entrykey,false,"string");
        }else{
          this.grid(this.entrykey,null,"string");
        }
      }
    }
  },
  methods:{
    clean:function(){
      this.searchQuery = "";
    }
  }
}
</script>