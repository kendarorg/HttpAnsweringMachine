<template>
  <div>
    <h3>{{value.type}}</h3><br>
    <div class="form-group">
      <label htmlFor="method">Query</label>
      <textarea style="width:1000px" wrap="soft" class="form-control" rows="6" cols="60"
                name="free_content" id="free_content"
                v-model="sql"></textarea>
    </div>
    <br>
    <b>Parameters</b>
    <table>
      <tr>
        <th>
          <button
              style="border:none;background-color: #4CAF50;"
              class="bi bi-plus-square" @click="doUpdate(-1)" title="Add"></button>
        </th>
        <th>Index</th>
        <th>Name</th>
        <th>Value</th>
      </tr>
      <tr v-for="(item,index) in parameters">
        <td>
          <button class="bi bi-pen-fill" @click="doUpdate(index)" title="Edit"></button>
          <button class="bi bi-trash" @click="doDelete(index)" title="Delete"></button>
        </td>
        <td>{{ findChildItemWithType(item.value,'columnIndex').value }}</td>
        <td>{{ findChildItemWithType(item.value,'columnName').value }}</td>
        <td>{{ findChildItemWithType(item.value,'value').value }}</td>
      </tr>
    </table>
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
        return findChildItemWithType(this.value,'sql').value;
      },
      set: function(newValue) {
        findChildItemWithType(this.value,'sql').value=newValue;
        this.$emit("componentevent",{
          id:"changed"
        })
      }
    },
    parameters:{
      get: function() {
        return findChildItemWithType(this.value,'parameters').value;
      },
      set: function(newValue) {
        findChildItemWithType(this.value,'parameters').value=newValue;
        this.$emit("componentevent",{
          id:"changed"
        })
      }
    }
  }
}
</script>