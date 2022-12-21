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
        <dynamic-component v-if="selectedComponentItem!=null"
                           :path="'/plugins/recording/vcomponents/line'"
                           :default="'basic'"
                           :template="selectedComponentType|normalize"
                           :value="selectedComponentItem"
                           @componentevent="onComponentEvent"/>
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
    //'basiccomponent': httpVueLoader('/plugins/recording/vcomponents/line/vbasic.vue'),
    //'orgkendarjanuscmdexec': httpVueLoader('/plugins/recording/vcomponents/line/orgkendarjanuscmdexec.vue'),
    'root': httpVueLoader('/plugins/recording/vcomponents/line/root.vue'),
    'dynamic-component': httpVueLoader('/plugins/recording/vcomponents/dynamic-component.vue'),
  },
  data: function () {

    return {
      selectedComponentItem: null,
      selectedComponentType: "basic",
      treeData: {
        name: "My Tree",
        type: "ROOT",
        value: null,
        children: [],
        parent: null
      },
      actual: {},
      visualization: null
    }
  },
  watch: {
    data: function (val, oldVal) {
      this.selectedComponentItem=null;
      this.selectedComponentType = "basic";
      this.prepareTree(val);
      this.visualization = val;
    },
  },
  created: function () {
    this.prepareTree(this.data);
  },
  filters: {
    normalize: function (str) {
      if (typeof str == "undefined") return str;
      return str.replaceAll(".", "").toLowerCase();
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
    onComponentEvent:function(evt){
      if(evt.id=="changed"){
        this.visualization =JSON.stringify(convertToStructure(this.treeData.children))
      }
    },
    prepareTree: function (newData) {
      this.treeData.children = convertToNodes(JSON.parse(newData));
      var th = this;
      this.treeData.children.forEach(function (child) {
        child.parent = this.treeData;
      })
    },
    showContent: function (item) {
      if (!item) {
        this.selectedComponentItem = null;
        this.selectedComponentType = "basic";
        return;
      }
      this.selectedComponentItem = item;
      var type = item.type.replaceAll(".", "").toLowerCase();
      this.selectedComponentType = item.type;
      /*
      let componentExists = type in this.$options.components
      if (componentExists) {

      } else {

        this.selectedComponentType = "basic";
      }*/
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