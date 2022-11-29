<template>
  <table>
    <thead>
    <tr>
      <th v-for="key in columns"
          @click="sortBy(key.id)"
          :class="{ active: sortKey == key.id }">
        {{ key.id | capitalize }}
        <span class="arrow" :class="sortOrders[key] > 0 ? 'asc' : 'dsc'">
              </span>
      </th>
    </tr>
    </thead>
    <tbody>
    <tr v-for="entry in filteredHeroes">
      <td v-for="key in columns">
        <!--{{ entry[key.id] }}-->
        <dynamic-template :data="entry[key.id]" :type="key.template" />
      </td>
    </tr>
    </tbody>
  </table>
</template>
<script>
module.exports = {
  props: {
    columns: Array,
    filterKey: String,
    address: String
  },
  created: function () {
  },
  async mounted() {
    let response = await axios.get("http://localhost:63342/ham/app/web/" + this.address);
    this.heroes = response.data;
  },
  data: function () {
    var sortOrders = {};
    this.columns.forEach(function (key) {
      sortOrders[key.id] = 1;
    });
    return {
      heroes: [],
      sortKey: "",
      sortOrders: sortOrders
    };
  },
  components:{
    'dynamic-template': httpVueLoader('vcomponents/dynamictemplate.vue'),
    'tstring': httpVueLoader('vcomponents/tstring.vue')
  },
  computed: {
    filteredHeroes: function () {

      var sortKey = this.sortKey;
      var filterKey = this.filterKey && this.filterKey.toLowerCase();
      var order = this.sortOrders[sortKey] || 1;
      var heroes = this.heroes;
      if (filterKey) {
        heroes = heroes.filter(function (row) {
          return Object.keys(row).some(function (key) {
            return (
                String(row[key])
                    .toLowerCase()
                    .indexOf(filterKey) > -1
            );
          });
        });
      }
      if (sortKey) {
        heroes = heroes.slice().sort(function (a, b) {
          a = a[sortKey];
          b = b[sortKey];
          return (a === b ? 0 : a > b ? 1 : -1) * order;
        });
      }
      return heroes;

    }
  },
  filters: {
    capitalize: function (str) {
      return str.charAt(0).toUpperCase() + str.slice(1);
    }
  },
  methods: {
    sortBy: function (key) {
      this.sortKey = key;
      this.sortOrders[key] = this.sortOrders[key] * -1;
    }
  }
}
</script>
<style>
table {
  border: 2px solid #42b983;
  border-radius: 3px;
  background-color: #fff;
}

th {
  background-color: #42b983;
  color: rgba(255, 255, 255, 0.66);
  cursor: pointer;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
}

td {
  background-color: #f9f9f9;
}

th,
td {
  min-width: 120px;
  padding: 10px 20px;
}

th.active {
  color: #fff;
}

th.active .arrow {
  opacity: 1;
}

.arrow {
  display: inline-block;
  vertical-align: middle;
  width: 0;
  height: 0;
  margin-left: 5px;
  opacity: 0.66;
}

.arrow.asc {
  border-left: 4px solid transparent;
  border-right: 4px solid transparent;
  border-bottom: 4px solid #fff;
}

.arrow.dsc {
  border-left: 4px solid transparent;
  border-right: 4px solid transparent;
  border-top: 4px solid #fff;
}
</style>