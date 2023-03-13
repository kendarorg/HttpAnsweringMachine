<template>
  <div>
    <h3>{{ value.type }}</h3><br>
    <div class="form-group">
      <label htmlFor="method">Query</label>
      <textarea style="width:1000px" wrap="soft" class="form-control" rows="6" cols="60"
                name="free_content" id="free_content"
                v-model="sql"></textarea>
    </div>
  </div>
</template>
<script>
module.exports = {
  name: "cmdconnectionconnectionpreparestatement",
  props: {
    value: Object
  },
  computed: {
    sql: {
      get: function () {
        return findChildItemWithType(this.value, 'sql').value;
      },
      set: function (newValue) {
        findChildItemWithType(this.value, 'sql').value = newValue;
        this.$emit("componentevent", {
          id: "changed"
        })
      }
    }
  }
}
</script>