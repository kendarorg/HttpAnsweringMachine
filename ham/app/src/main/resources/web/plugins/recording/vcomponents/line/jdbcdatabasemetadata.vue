<template>

  <div v-if="value.type=='org.kendar.janus.JdbcDatabaseMetaData'">
    <h3>{{value.type}}</h3><br>
    <table>
      <tr>
        <th>Item</th>
        <th>Value</th>
      </tr>
      <tr v-for="(item,index) in gridData">
        <td>{{ item.name }}</td>
        <td>{{ item.value }}</td>
      </tr>
    </table>
  </div>
</template>
<script>
module.exports = {
  name: "jdbcdatabasemetadata",
  props: {
    value: Object
  },
  computed:{
    gridData:function(){
      //this.triggerChange;
      return this.value.children;
    }
  }
}
</script>