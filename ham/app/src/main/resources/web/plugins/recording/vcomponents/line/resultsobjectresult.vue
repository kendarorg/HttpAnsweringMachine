<template>
  <div v-if="value.type=='org.kendar.janus.results.ObjectResult'">

    <h3>{{value.type}}</h3><br>
    <div class="form-group" v-if="otype!=null">
      <label htmlFor="typ">Result Type</label>
      <input class="form-control" type="text" name="typ" id="typ" v-model="otype"/>
    </div>
    <div class="form-group" v-if="ovalue!=null">
      <label htmlFor="val">Value</label>
      <input class="form-control" type="text" name="val" id="val" v-model="ovalue"/>
    </div>
  </div>
</template>
<script>
module.exports = {
  name: "resultsobjectresult",
  props: {
    value: Object
  },
  computed:{
    otype: {
      get: function () {
        try {
          return this.value.children[0].type;
        }catch(e){
          return null;
        }
      },
      // setter
      set: function (newValue) {
        this.value.children[0].type=newValue;
        this.$emit("componentevent",{
          id:"changed"
        })
      }
    },
    ovalue: {
      get: function () {
        try{
          return this.value.children[0].value;
        }catch(e){
          return null;
        }
      },
      // setter
      set: function (newValue) {
        this.value.children[0].value=newValue;
        this.$emit("componentevent",{
          id:"changed"
        })
      }
    }
  }
}
</script>