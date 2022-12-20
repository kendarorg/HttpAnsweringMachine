<template>
  <div v-if="typeof this.data!='undefined'" width="800px">
    <div class="form-group">
      <label for="free_content">Value</label>
      <textarea class="form-control" rows="6" cols="50"
                name="free_content" id="free_content"
                v-model="shown"></textarea>
    </div>
    <table class="rounded-top">
      <tr v-for="key in fields" >
        <td  >
          {{key}}
        </td>
      </tr>
    </table>
  </div>
</template>
<script>
module.exports = {
  name: "serializable-object",
  props:{
    data:String
  },
  data:function(){

    return {
      fields:[],
      actual:{},
      visualization:null
    }
  },
  watch:{
    data:function (val,oldVal){
      console.log(val)
      this.actual = JSON.parse(val);
      //this.fields = ;
    },
  },
  created: function () {
    this.actual = JSON.parse(this.data);
    this.fields = Object.keys(this.actual);
  },
  computed:{
    shown:{
      get: function () {
        if(this.visualization==null){
          this.visualization=this.data;
        }
        return this.visualization
      },
      // setter
      set: function (newValue) {
        this.$emit("changed",newValue);
        this.visualization=newValue;
        this.actual = JSON.parse(this.visualization);
        this.fields = Object.keys(this.actual);
        console.log("WHAT")
      }
    }
  }
}
</script>