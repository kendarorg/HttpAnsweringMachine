<template>
  <table class="rounded-top">
    <thead>
    <tr >
      <th v-for="key in extra" >
        <span class="btn btn-kendar btn-sm">
        {{ key.id | capitalize }}
          </span>
      </th>
      <th v-for="key in columns"
          @click="sortBy(key.id)"
          >
        <span class="btn btn-kendar btn-sm" :class="{ active: sortKey == key.id }">
        {{ key.id | capitalize }}
        <span :class="(sortOrders[key] > 0 ? 'arrow asc' : 'arrow dsc')">
              </span>
          </span>
      </th>
    </tr>
    </thead>
    <tbody>
    <tr>
      <td v-for="key in extra">
        <dynamic-search :ref="'search'+key.id" :type="key.template" :def="key.default"
                        :grid="setSearchField" :entrykey="key.id" />
      </td>
      <td v-for="key in columns">
        <dynamic-search :ref="'search'+key.id" :type="key.template" :def="key.default"
                         :grid="setSearchField" :entrykey="key.id" />
      </td>
    </tr>
    <tr v-for="entry in filteredData">
      <td v-for="key in extra">
        <dynamic-column :data="entry[key.id]" :type="key.template" :def="key.default"
                          :entry="entry" :entrykey="key.id" />
      </td>
      <td v-for="key in columns">
        <!--{{ entry[key.id] }}-->
        <dynamic-column :data="entry[key.id]" :type="key.template"
                          :entry="entry" :entrykey="key.id"/>
      </td>
    </tr>
    </tbody>
  </table>
</template>
<script>
module.exports = {
  props: {
    columns: Array,
    extra: Array,
    address: String
  },
  created: function () {
    this.extrasCalculated = false;
  },
  async mounted() {
    await this.reload();
  },
  data: function () {
    var sortOrders = {};
    this.columns.forEach(function (key) {
      sortOrders[key.id] = 1;
    });
    console.log("data");
    return {
      filterKeys:{},
      extrasCalculated: false,
      data: [],
      sortKey: "",
      sortOrders: sortOrders
    };
  },
  components:{
    'dynamic-column': httpVueLoader('vcomponents/grid/dynamiccolumn.vue'),
    'dynamic-search': httpVueLoader('vcomponents/grid/dynamicsearch.vue')
  },
  computed: {
    filteredData: function () {
      var filterKeys = this.filterKeys;

      var sortKey = this.sortKey;
      var order = this.sortOrders[sortKey] || 1;
      var data = this.data;
      if (filterKeys) {


        data = data.filter(function (row) {
          let allGood=true;
          for (const [key, value] of Object.entries(filterKeys)) {
            if(value==null|| typeof value=="undefined")continue;
            allGood = String(row[key])
                .toLowerCase()
                .indexOf(String(value)
                    .toLowerCase()) > -1;
            if(!allGood)break;
          }
          return allGood;
        });
      }
      if (sortKey) {
        data = data.slice().sort(function (a, b) {
          a = a[sortKey];
          b = b[sortKey];
          return (a === b ? 0 : a > b ? 1 : -1) * order;
        });
      }
      return data;

    }
  },
  filters: {
    capitalize: function (str) {
      return str.charAt(0).toUpperCase() + str.slice(1);
    }
  },
  methods: {
    retrieve: function(entry,key){
      return {
        entry:entry,
        key:key
      };
    },
    sortBy: function (key) {
      this.sortKey = key;
      this.sortOrders[key] = this.sortOrders[key] * -1;
    },
    getData: function () {
      return this.data;
    },
    setSearchField: function (keyNew,valueNew,type) {
      let temp = {};

      var th = this;
      for (const [key, value] of Object.entries(this.filterKeys)) {
        temp[key]=value;
      };

      if(valueNew===false || valueNew===true){
        temp[keyNew] = valueNew;
      }else if(valueNew==null || typeof valueNew == "undefined" || valueNew==""){
        delete temp[keyNew];
      }else {
        temp[keyNew] = valueNew;
      }

      this.filterKeys=temp;
    },
    reload: async function () {
      this.extrasCalculated=false;
      let response = await axios.get("http://localhost:63342/ham/app/web/" + this.address);
      this.data = response.data;
      var th = this;
      this.extra.forEach(function (ex) {
        th.$refs['search'+ex.id][0].clean();
      });
      this.columns.forEach(function (ex) {
        th.$refs['search'+ex.id][0].clean();
      });
      this.calculateExtra();
    },
    calculateExtra: function () {
      if(!this.extrasCalculated && typeof this.data !="undefined" && this.data.length >0) {
        this.extrasCalculated = true;
        var th = this;
        this.extra.forEach(function (ex) {
          var eex = ex;
          th.data.forEach(function (item) {

            item[eex.id] = eex.default;

          })
        })
      }
    }
  }
}
</script>
<style>
table {
  border-collapse:separate;
  border: 2px solid #42b983;
  border-radius: 5px;
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