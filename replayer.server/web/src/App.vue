<template>
  <form id="search">
    Search <input name="query" v-model="searchQuery" />
  </form>
  <demo-grid
      :heroes="gridData"
      :columns="gridColumns"
      :filter-key="searchQuery"
  >
  </demo-grid>
</template>
<script>
import DemoGrid from '../components/demoGrid'

export default {
  name: 'App',
  components: {
    DemoGrid
  }
}
</script>