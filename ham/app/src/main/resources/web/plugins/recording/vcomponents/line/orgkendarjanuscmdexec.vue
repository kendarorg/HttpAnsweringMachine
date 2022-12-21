<template>

  <div>
    <b>{{value.type}}</b><br>
    <div class="form-group">
      <label htmlFor="method">Method</label>
      <input class="form-control" readOnly type="text" name="method" id="method" v-model="value.children[0].value"/>
    </div>
    <br>
    <b>Parameters</b>
    <ul>
      <li v-for="(item,index) in value.children[1].children">
        {{item.value}} = {{value.children[2].children[index].value}}
      </li>
    </ul>
  </div>
</template>
<script>
module.exports = {
  name: "orgkendarjanuscmdexec",
  props:{
    value:Object
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