<template>
  <div v-if="typeof this.data!='undefined'" width="800px">
    <div class="form-group">
      <label for="free_content">Value</label>
      <textarea class="form-control" rows="6" cols="50"
                name="free_content" id="free_content"
                v-model="shown"></textarea>
    </div>
    <ul width="1000px">
      <simple-tree width="1000px"
          class="item"
          :item="treeData"
          @show-content="showContent"
      ></simple-tree>
      <br><br>
      <div>
        <component v-if="selectedComponentItem!=null" :is="selectedComponentType|normalize"
                   :value="selectedComponentItem"/>
      </div>
      <br><br>

    </ul>
  </div>
</template>
<script>
module.exports = {
  name: "serializable-object",
  props: {
    data: String
  },
  components: {
    'simple-tree': httpVueLoader('/vcomponents/vtree.vue'),
    'basiccomponent': httpVueLoader('/plugins/recording/vcomponents/line/vbasic.vue'),
    'orgkendarjanuscmdexec': httpVueLoader('/plugins/recording/vcomponents/line/orgkendarjanuscmdexec.vue'),
    'root': httpVueLoader('/plugins/recording/vcomponents/line/root.vue'),
  },
  data: function () {

    return {
      selectedComponentItem:null,
      selectedComponentType:"basiccomponent",
      treeData: {
        name: "My Tree",
        type: "ROOT",
        value: null,
        children: [],
        parent:null
      },
      actual: {},
      visualization: null
    }
  },
  watch: {
    data: function (val, oldVal) {
      this.prepareTree(val);
      this.visualization = val;
    },
  },
  created: function () {
    this.prepareTree(this.data);
  },
  filters: {
    normalize: function (str) {
      if(typeof str == "undefined") return str;
      return str.replaceAll(".","").toLowerCase();
    }
  },
  computed: {
    shown: {
      get: function () {
        if (this.visualization == null) {
          this.visualization = this.data;
        }
        return this.visualization
      },
      // setter
      set: function (newValue) {
        this.$emit("changed", newValue);
        this.visualization = newValue;
        this.prepareTree(this.visualization);
      }
    }
  },
  methods: {
    prepareTree:function(newData){
      this.treeData.children = convertToNodes(JSON.parse(newData));
      var th=this;
      this.treeData.children.forEach(function(child){
        child.parent=this.treeData;
      })
    },
    showContent: function (item) {
      if(!item){
        this.selectedComponentItem=null;
        this.selectedComponentType="basiccomponent";
        return;
      }
      this.selectedComponentItem = item;
      var type = item.type.replaceAll(".","").toLowerCase();
      let componentExists = type in this.$options.components
      if(componentExists) {
        this.selectedComponentType = item.type;
      }else{
        console.error("MISSING COMPONENT FOR "+item.type);
        this.selectedComponentType="basiccomponent";
      }
      //Vue.set(item, "children", []);
      //this.addItem(item);
    },
    addItem: function (item) {
      item.children.push({
        name: "new stuff"
      });
    }
  }
}
</script>