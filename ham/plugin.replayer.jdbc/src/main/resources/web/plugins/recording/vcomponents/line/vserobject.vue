<template>
  <div v-if="typeof this.data!='undefined'" width="1000px">
    <vtabs prefix="vcoline" width="1000px">
      <vtab name="COMPONENT" width="1000px">
        <br>
        <dynamic-component id="vser-selected-comp" width="1000px" v-if="selectedComponentItem!=null"
                           :path="'/plugins/recording/vcomponents/line'"
                           :default="'basic'"
                           :template="selectedComponentType|normalize"
                           :value="selectedComponentItem"
                           @componentevent="onComponentEvent"/>
      </vtab>
      <vtab name="TREE" width="1000px">
        <ul style="width:1000px">
          <simple-tree id="vser-selected-tree" style="width:1000px"
                       :open-item="false"
                       class="item"
                       :item="treeData"
                       @show-content="showContent"
          ></simple-tree>
        </ul>
      </vtab>
      <vtab name="JSON" width="1000px">
        <br>
        <div class="form-group" width="1000px">
          <label for="cmd11_free_content">Value</label>
          <textarea style="width:1000px" class="form-control" rows="12" cols="100"
                    name="cmd11_free_content" id="cmd11_free_content"
                    v-model="shown"></textarea>
        </div>
      </vtab>
    </vtabs>
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
    'root': httpVueLoader('/plugins/recording/vcomponents/line/root.vue'),
    'dynamic-component': httpVueLoader('/plugins/recording/vcomponents/dynamic-component.vue'),
    'vtab': httpVueLoader('/vcomponents/tab/vtab.vue'),
    'vtabs': httpVueLoader('/vcomponents/tab/vtabs.vue')
  },
  data: function () {

    return {
      selectedComponentItem: null,
      selectedComponentType: "basic",
      treeData: {},
      actual: {},
      visualization: null
    }
  },
  watch: {
    data: function (val, oldVal) {
      this.prepareTree(val);
      if (typeof this.treeData == "undefined" || typeof this.treeData.type == "undefined") return;
      this.visualization = val;
      this.selectedComponentItem = this.treeData;
      this.selectedComponentType = this.treeData.type.replaceAll(".", "").toLowerCase();
    },
  },
  created: function () {
    this.prepareTree(this.data);
    if (typeof this.treeData == "undefined" || typeof this.treeData.type == "undefined") return;
    this.selectedComponentItem = this.treeData;
    this.selectedComponentType = this.treeData.type.replaceAll(".", "").toLowerCase();
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
    onComponentEvent: function (evt) {
      if (evt.id == "changed") {
        this.visualization = JSON.stringify(convertToStructure([this.treeData]))
        this.$emit("changed", this.visualization)
      } else if (evt.id == "reloadcallback") {
        var th = this;
        evt.callback(this.visualization, function (toupdate) {
          th.visualization = toupdate;
          th.$emit("changed", th.visualization)
        })
      }
    },
    prepareTree: function (newData) {
      try {
        this.treeData = convertToNodes(JSON.parse(newData))[0];
      } catch (e) {

      }
    },
    showContent: function (item) {
      if (typeof item == "undefined" || item.type == "undefined") {
        this.selectedComponentItem = null;
        this.selectedComponentType = "basic";
        return;
      }
      this.selectedComponentItem = item;
      var type = item.type.replaceAll(".", "").toLowerCase();
      this.selectedComponentType = item.type;
    },
    addItem: function (item) {
      item.children.push({
        name: "new stuff"
      });
    }
  }
}
</script>