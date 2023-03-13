<template>
  <table style="width:1000px"  >
    <tr>
      <th>
        <button
            id="lop-doupdate"
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
        <button :id="'lop-doupdate-'+index" class="bi bi-pen-fill" @click="doUpdate(index)" title="Edit"></button>
        <button :id="'lop-dodelete-'+index" class="bi bi-trash" @click="doDelete(index)" title="Delete"></button>
      </td>
      <td>{{ item.columnIndex }}</td>
      <td>{{ item.columnName }}</td>
      <td>{{ item.value }}</td>
      <td>{{ item.type }}</td>
    </tr>
  </table>
</template>
<script>
module.exports = {
  name: "listofparams",
  props: {
    value: Object,
    field: String
  },
  computed:{
    parameters:{
      get: function() {
        var result = [];
        try {
          var partial = findChildItemWithType(this.value, this.field).children;
          for (var i = 0; i < partial.length; i++) {
            var sub = partial[i];
            var toAdd = {
              type: sub.type.replace("org.kendar.janus.cmd.preparedstatement.parameters.", ""),
              columnIndex: findChildItemWithType(sub, 'columnIndex').value,
              columnName: findChildItemWithType(sub, 'columnName').value,
              value: findChildItemWithType(sub, 'value').value,
            }
            result.push(toAdd);
          }
        }catch (e) {
          
        }
        return result;
      }
    }
  }
}
</script>