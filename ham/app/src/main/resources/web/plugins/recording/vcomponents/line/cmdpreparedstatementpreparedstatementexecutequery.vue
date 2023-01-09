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
    <table style="width:1000px"  >
      <tr>
        <th>
          <button
              style="border:none;background-color: #4CAF50;"
              class="bi bi-plus-square" @click="doUpdate(-1)" title="Add"></button>
        </th>
        <th>Index</th>
        <th>Name</th>
        <th>Value</th>
        <th>Type</th>
      </tr>
      <tr v-for="(item,index) in parameters">
        <td>
          <button class="bi bi-pen-fill" @click="doUpdate(index)" title="Edit"></button>
          <button class="bi bi-trash" @click="doDelete(index)" title="Delete"></button>
        </td>
        <td>{{ item.columnIndex }}</td>
        <td>{{ item.columnName }}</td>
        <td>{{ item.value }}</td>
        <td>{{ item.type }}</td>
      </tr>
    </table>
  </div>
</template>
<script>
module.exports = {
  name: "cmdpreparedstatementpreparedstatementexecutequery",
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
        var result = [];
        var partial = findChildItemWithType(this.value,'parameters').children;
        for(var i=0;i<partial.length;i++){
          var sub = partial[i];
          var toAdd={
            type:sub.type.replace("org.kendar.janus.cmd.preparedstatement.parameters.",""),
            columnIndex:findChildItemWithType(sub,'columnIndex').value,
            columnName:findChildItemWithType(sub,'columnName').value,
            value:findChildItemWithType(sub,'value').value,
          }
          result.push(toAdd);
        }
        return result;
      }
    }
  }
}
</script>