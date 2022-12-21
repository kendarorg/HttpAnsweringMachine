<template>

  <div v-if="value.type=='org.kendar.janus.cmd.Exec'">
    <b>{{value.type}}</b><br>
    <div class="form-group">
      <label htmlFor="method">Method</label>
      <input class="form-control" readOnly type="text" name="method" id="method" v-model="value.children[0].value"/>
    </div>
    <br>
    <b>Parameters</b>
    <table>
      <tr>
        <th><button class="bi bi-plus-square" @click="doAdd()" title="Add"></button></th><th>Type</th><th>Value</th>
      </tr>
      <tr v-for="(item,index) in value.children[1].children">
        <td>
          <button class="bi bi-pen-fill" @click="doEdit(item,index)" title="Edit"></button>
          <button class="bi bi-trash" @click="doDelete(item,index)" title="Delete"></button>
        </td>
        <td>{{item.value}}</td>
        <td>{{value.children[2].children[index].value}}</td>
      </tr>
    </table>
  </div>
</template>
<script>
module.exports = {
  name: "orgkendarjanuscmdexec",
  props:{
    value:Object
  },
  methods:{
    doAdd:function(){
      alert("doAdd")
    },
    doDelete:function(item,index){
      alert("doDelete")
    },
    doEdit:function(item,index){
      alert("doEdit")
    }
  },
  data:function () {
    return {
      // columns: [
      //   {id: "id", template: "string", index: true,size:20,sortable:true},
      //   {id: "type", template: "string",label:"Type",size:20,sortable:true}
      //     ]
    }
  },
  components:{
    //'simple-grid': httpVueLoader('/vcomponents/testgrid.vue'),
  }
}
</script>