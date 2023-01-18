<template>
  <div>
    RESULTSET
    <div class="form-group">
      <label htmlFor="val">Value</label>
      <input class="form-control" type="text" name="val" id="val" v-model="columndescriptorslength"/>
    </div>
    <table>
      <tr v-for="(row,index) in rows">
        <td v-for="(field,fieldindex) in row">{{ field }}</td>
      </tr>
    </table>

  </div>
</template>
<script>
module.exports = {
  name: "jdbcresultset",
  props: {
    value: Object
  },
  computed:{
    columndescriptorslength:{
      get: function() {
        console.log("columndescriptorslength")
        return findChildItemWithType(this.value,'columndescriptors').children.length;
      }
    },

    rows:{
      get: function() {
        console.log("rows")
        var rows = findChildItemWithType(this.value,'rows').children;
        var realRows=[];
        rows.forEach(function(row){
          var realRow =[];
          row.children.forEach(function(col){
            var value = col.value;
            realRow.push(value);
          })
          realRows.push(realRow);

        })
        return realRows;
      },
      // set: function(newValue) {
      //   findChildItemWithType(this.value,'sql').value=newValue;
      //   this.$emit("componentevent",{
      //     id:"changed"
      //   })
      // }
    }
  }
}
</script>