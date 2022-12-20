<template>
  <div v-if="typeof this.data!='undefined'" width="800px">
    <div class="form-group">
      <label for="free_content">Value</label>
      <textarea class="form-control" rows="6" cols="50"
                name="free_content" id="free_content"
                v-model="shown"></textarea>
    </div>
    <ul >
      <simple-tree
          class="item"
          :item="treeData"
          @make-folder="makeFolder"
          @add-item="addItem"
      ></simple-tree>
    </ul>
  </div>
</template>
<script>
module.exports = {
  name: "serializable-object",
  props:{
    data:String
  },
  components: {
    'simple-tree': httpVueLoader('/vcomponents/vtree.vue')
  },
  data:function(){

    return {
      treeData:{
        name: "My Tree",
        type: "ROOT",
        value: null,
        children:[]
      },
      actual:{},
      visualization:null
    }
  },
  watch:{
    data:function (val,oldVal){
      this.treeData.children = convertToNodes(JSON.parse(val));
      this.visualization=this.val;
    },
  },
  created: function () {
    this.treeData.children = convertToNodes(JSON.parse(this.data));
  },
  computed:{
    shown:{
      get: function () {
        if(this.visualization==null){
          this.visualization=this.data;
        }
        return this.visualization
      },
      // setter
      set: function (newValue) {
        this.$emit("changed",newValue);
        this.visualization=newValue;
        this.treeData.children = convertToNodes(JSON.parse(this.visualization));
        this.treeData.children = convertToNodes(JSON.parse(this.visualization));
      }
    }
  },
  methods: {
    makeFolder: function(item) {
      Vue.set(item, "children", []);
      this.addItem(item);
    },
    addItem: function(item) {
      item.children.push({
        name: "new stuff"
      });
    }
  }
}
</script>