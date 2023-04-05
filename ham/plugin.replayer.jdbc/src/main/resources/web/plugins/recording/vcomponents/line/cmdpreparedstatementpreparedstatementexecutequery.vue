<template>
  <div>
    <h3>{{ value.type }}</h3><br>
    <div class="form-group">
      <label htmlFor="method">Query</label>
      <textarea style="width:1000px" wrap="soft" class="form-control" rows="6" cols="60"
                name="cmd6_free_content" id="cmd6_free_content"
                v-model="sql"></textarea>
    </div>
    <br>
    <b>Parameters</b>
    <listofparams id="parameters-list" :value="value" field="parameters"/>
  </div>
</template>
<script>
module.exports = {
  name: "cmdpreparedstatementpreparedstatementexecutequery",
  props: {
    value: Object
  },
  components: {
    'listofparams': httpVueLoader('/plugins/recording/vcomponents/listofparams.vue')
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