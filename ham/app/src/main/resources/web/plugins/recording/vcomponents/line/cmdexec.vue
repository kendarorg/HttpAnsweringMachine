<template>

  <div v-if="value.type=='org.kendar.janus.cmd.Exec'">
    <simple-modal v-if="modalShow"
                  :modal-data="modalData"
                  @close="modalShow = false">
      <span slot="header">Set Exec Param</span>
      <span slot="body">
        <div class="form-group">
          <label htmlFor="typ">Method type</label>
          <input class="form-control" type="text" name="typ" id="typ" v-model="modalData.data.type"/>
        </div>
        <div class="form-group">
          <label htmlFor="typl">Value Type</label>
          <input class="form-control" type="text" name="typl" id="typl" v-model="modalData.data.typevalue"/>
        </div>
        <div class="form-group">
          <label htmlFor="val">Value</label>
          <input class="form-control" type="text" name="val" id="val" v-model="modalData.data.value"/>
        </div>

      </span>
    </simple-modal>

    <h3>{{value.type}}</h3><br>
    <div class="form-group">
      <label htmlFor="method">Method</label>
      <input class="form-control" type="text" name="method" id="method" v-model="value.children[0].value"/>
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
        <th>Type</th>
        <th>Value</th>
      </tr>
      <tr v-for="(item,index) in gridData">
        <td>
          <button class="bi bi-pen-fill" @click="doUpdate(index)" title="Edit"></button>
          <button class="bi bi-trash" @click="doDelete(index)" title="Delete"></button>
        </td>
        <td>{{ item.value }}</td>
        <td>{{ value.children[2].children[index].value }}</td>
      </tr>
    </table>
  </div>
</template>
<script>
module.exports = {
  name: "cmdexec",
  props: {
    value: Object
  },
  computed:{
    gridData:function(){
      //this.triggerChange;
      return this.value.children[1].children;
    }
  },
  methods: {
    doUpdate: function (index) {
      var shouldEdit = index >= 0;
      var item = {};
      if (shouldEdit) {
        item.index = index;
        item.type = this.value.children[1].children[index].value;
        item.typevalue = this.value.children[2].children[index].type;
        item.value = this.value.children[2].children[index].value;
      }
      this.modalData = {
        data: item,
        edit: shouldEdit,
        save: this.save
      };
      this.modalShow = true;
    },
    doDelete: function (item, index) {
      addMessage("doDelete","error")
    },
    save: function () {
      var item = this.modalData.data;
      if (this.modalData.edit) {
        this.value.children[1].children[item.index].value = item.type;
        this.value.children[2].children[item.index].value = item.value;
        this.value.children[2].children[item.index].type = item.typevalue;
      } else {
        this.value.children[1].children.push({
          name:"_",
          type:"java.lang.Class",
          value:item.type
        });
        this.value.children[2].children.push({
          name:"_",
          type:item.typevalue,
          value:item.value
        });
      }
      this.$emit("componentevent",{
        id:"changed"
      })
      this.modalData={}
      this.modalShow = false;
      //.triggerChange = !this.triggerChange;
    }
  },
  data: function () {
    return {
      //triggerChange:false,
      modalData: null,
      modalShow: false,
      // columns: [
      //   {id: "id", template: "string", index: true,size:20,sortable:true},
      //   {id: "type", template: "string",label:"Type",size:20,sortable:true}
      //     ]
    }
  },
  components: {
    'simple-modal': httpVueLoader('/vcomponents/tmodal.vue'),
  }
}
</script>