<template>
  <li>
    <div
        :class="{bold: isFolder}"
        @click="toggle"
        @dblclick="makeFolder">
      <b>Name:</b>{{ item.name }}  <b>Type:</b>{{item.type}}  <span v-if="typeof item.value!='undefined'&&item.value!=null"><b>Value:</b>{{item.value}}</span>
      <span v-if="isFolder">[{{ isOpen ? '-' : '+' }}]</span>
    </div>
    <ul v-show="isOpen" v-if="isFolder">
      <simple-tree
          class="item"
          v-for="(child, index) in item.children"
          :key="index"
          :item="child"
          @make-folder="$emit('make-folder', $event)"
          @add-item="$emit('add-item', $event)"
      ></simple-tree>
      <li class="add" @click="$emit('add-item', item)">+</li>
    </ul>
  </li>
</template>
<script>
module.exports = {
  name: 'simple-tree',
  props: {
    item: Object
  },
  data: function() {
    return {
      isOpen: false
    };
  },
  computed: {
    isFolder: function() {
      if(typeof this.item.children =="undefined") return false;
      return this.item.children && this.item.children.length;
    }
  },
  methods: {
    toggle: function() {
      if (this.isFolder) {
        this.isOpen = !this.isOpen;
      }
    },
    makeFolder: function() {
      if (!this.isFolder) {
        this.$emit("make-folder", this.item);
        this.isOpen = true;
      }
    }
  }
}
</script>