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
import DemoGrid from './demoGrid'

export default {
  name: 'App',
  components: {
    DemoGrid
  },
  el: "#demo",
  data: {
    searchQuery: "",
    gridColumns: ["name", "power"],
    gridData: [
      { name: "Chuck Norris", power: Infinity },
      { name: "Bruce Lee", power: 9000 },
      { name: "Jackie Chan", power: 7000 },
      { name: "Jet Li", power: 8000 }
    ]
  }
}
</script>