<template>
  <li v-if="item">
    <div
        :class="{bold: isFolder}"
        @click="showContent"
        @dblclick="showContent">
      <b>Name:</b>{{ item.name }}  <b>Type:</b>{{item.type}}  <span v-if="typeof item.value!='undefined'&&item.value!=null"><b>Value:</b>{{item.value}}</span>
      <span  @click="toggle" v-if="isFolder">[{{ isOpen ? '-' : '+' }}]</span>
    </div>
    <ul v-show="isOpen" v-if="isFolder">
      <simple-tree
          class="item"
          v-for="(child, index) in item.children"
          :key="index"
          :item="child"
          @show-content="$emit('show-content', $event)"
      ></simple-tree>
      <!--<li class="add" @click="$emit('add-item', item)">+</li>-->
    </ul>
  </li>
</template>
<script>
module.exports = {
  name: 'simple-tree',
  props: {
    item: Object,
    openItem:{
      type:Boolean,
      default:false
    }
  },
  data: function() {
    return {
      isOpen: this.openItem
    };
  },
  computed: {
    isFolder: function() {
      return this.item.children && this.item.children.length;
    }
  },
  methods: {
    toggle: function() {
      if (this.isFolder) {
        this.isOpen = !this.isOpen;
      }
    },
    showContent: function() {
      this.$emit("show-content", this.item);
    }
  }
}
</script>