<template>
  <div>
    <h3>{{value.type}}</h3><br>
    <div class="form-group">
      <label htmlFor="method">Query</label>
      <textarea style="width:1000px" wrap="soft" class="form-control" rows="12" cols="100"
                name="free_content" id="free_content"
                v-model="sql"></textarea>
    </div>
  </div>
</template>
<script>
module.exports = {
  name: "cmdpreparedstatementpreparedstatementexecute",
  props: {
    value: Object
  },
  computed: {
    sql:{
      get: function() {
        return this.value.children[0].value;
      },
      set: function(newValue) {
        this.value.children[0].value=newValue;
        this.$emit("componentevent",{
          id:"changed"
        })
      }
    }
  }
}
</script>