<template>
  <div>
    <table class="rounded-top">
      <thead>
      <tr >
        <th v-for="key in extra" >
          <span v-if="key.id.indexOf('_')!=0" class="btn btn-kendar btn-sm">
          {{ key.id | capitalize }}
            </span>
        </th>
        <th v-for="key in columns"
            @click="sortBy(key.id)"
            >
          <span v-if="key.id.indexOf('_')!=0" class="btn btn-kendar btn-sm" :class="{ active: sortKey == key.id }">
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
          <dynamic-search v-if="key.id.indexOf('_')!=0"
                          :ref="'search'+key.id"
                          :descriptor="key"/>
        </td>
        <td v-for="key in columns">
          <dynamic-search v-if="key.id.indexOf('_')!=0"
                          :ref="'search'+key.id"
                          :descriptor="key"/>
        </td>
      </tr>
      <tr v-for="entry in filteredData">
        <td v-for="key in extra">
          <dynamic-column :descriptor="key"
                          :value="entry[key.id]"
                          :index="buildId(entry)" />
        </td>
        <td v-for="key in columns">
          <dynamic-column :descriptor="key"
                          :value="entry[key.id]"
                          :index="buildId(entry)"/>
        </td>
      </tr>
      </tbody>
    </table>
  </div>
</template>
<script>
module.exports = {
  props: {
    columns: Array,
    extra: Array,
    address: String,
    retrieveData: Function
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
    return {
      filterKeys:{},
      extrasCalculated: false,
      data: [],
      sortKey: "",
      sortOrders: sortOrders,
      forceUpdate:0
    };
  },
  components:{
    'dynamic-column': httpVueLoader('vcomponents/grid/dynamiccolumn.vue'),
    'dynamic-search': httpVueLoader('vcomponents/grid/dynamicsearch.vue')
  },
  computed: {
    filteredData: function () {
      if(this.data == null || typeof this.data =="undefined"){
        return [];
      }
      this.forceUpdate;
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
    getById: function (indexArray) {
      for(var row of this.data){
        var tempId = this.buildId(row);
        var tempIdMatch = true;
        for(var i=0;i<indexArray.length;i++){
          if(indexArray[i]!=tempId[i]){
            tempIdMatch = false;
            break;
          }
        }
        if(tempIdMatch) return row;
      }
      return null;
    },
    getByRow: function (otherRow) {
      var indexArray = this.buildId(otherRow);
      return this.getById(indexArray);
    },
    setField: function(id,indexArray,newValue){
      var row = this.getById(indexArray);
      row[id]=newValue;
      this.forceUpdate++;
    },
    buildId: function (row){
      var idList = [];
      for(const key of this.columns){
        if(key.hasOwnProperty("index") && key.index){
          idList.push(row[key.id]);
        }
      }

      return idList;
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
    asyncRequestsRunner: function(asyncFunc){
      let promises = [asyncFunc()]
      //let promises = [async function(){ return await asyncFunc();}]
      let result={
        data:[],
        err:null
      };

      Promise.all(promises)
          .then(results=>{
            result.data=results[0];
            console.log("SOLVED")
          })
          .catch(err => {
            result.err=err
          });
      if(result.err!=null){
        throw result.err;
      }
      console.log(JSON.stringify(result.data))
      return result.data;
    },
    reload: function () {
      var th= this;
      this.retrieveData().then(function(result){
        th.extrasCalculated=false;
        th.data =  result.data;
        th.extra.forEach(function (ex) {
          if(th.$refs.hasOwnProperty('search'+ex.id)) {
            if(th.$refs['search' + ex.id].length>0) {
              th.$refs['search' + ex.id][0].clean();
            }
          }
        });
        th.columns.forEach(function (ex) {
          if(th.$refs.hasOwnProperty('search'+ex.id)) {
            if(th.$refs['search' + ex.id].length>0) {
              th.$refs['search' + ex.id][0].clean();
            }
          }
        });
        th.calculateExtra();
      });

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
    },
    toggleSelect : function(selectField){
      var th = this;
      this.filteredData.forEach(function(toSel){
        var index =th.buildId(toSel);
        th.setField(selectField,index,!toSel.select);
      });
    },
    selectAll : function(selectField){
      var th = this;
      this.filteredData.forEach(function(toSel){
        var index =th.buildId(toSel);
        th.setField(selectField,index,true);
      });
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
  min-width: 10px;
  padding: 5px 5px;
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