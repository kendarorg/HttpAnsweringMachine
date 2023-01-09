<template>

    <input name="query" v-model="searchQuery" v-bind:size="inputLength"/>

</template>
<script>
module.exports = {
  props: {
    descriptor: Object
  },
  name: 'sstring',
  data:function(){
    return {
      searchQuery: ""
    };
  },
  computed: {
    inputLength: function () {
      if(typeof this.descriptor.size =="undefined")return 30;
      return this.descriptor.size;
    }
  },
  watch:{
    searchQuery : function(val,oldVal){
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