<template>
  <div>
    <div v-if="pageSize!=null">
      <br>
      <button v-bind:disabled="index<=0" class="bi bi bi-skip-backward" @click="goStart" title="Start"></button>
      <button v-bind:disabled="index<=0" class="bi bi-skip-start" @click="goPrev" title="Prev"> </button>
      <button v-bind:disabled="!canGoNext()" class="bi bi-skip-end" @click="goNext" title="Next"></button>
      <button v-bind:disabled="!canGoNext()" class="bi bi-skip-forward" @click="goEnd" title="End"></button>
      <br><br>
    </div>
    <table class="rounded-top">
      <thead>
      <tr >
        <th v-for="key in extra" >
          <span v-if="getBoolVal(key.sortable,true)" class="btn btn-kendar btn-sm">
          {{ key.id | cleanUp }}
          </span>
        </th>
        <th v-for="key in columns"
            @click="sortBy(key.id)"
            >
          <span v-if="getBoolVal(key.sortable,true)" class="btn btn-kendar btn-sm" :class="{ active: sortKey == key.id }">
                {{ key.id | cleanUp }}
                <span :class="(sortOrders[key] > 0 ? 'arrow asc' : 'arrow dsc')">
                </span>
          </span>
          <span v-else class="btn btn-kendar btn-sm">
                {{ key.id | cleanUp }}
          </span>
        </th>
      </tr>
      </thead>
      <tbody>
      <tr>
        <td v-for="key in extra">
          <dynamic-search v-if="getBoolVal(key.searchable,true)"
                          :ref="'search'+key.id"
                          :descriptor="key"/>
        </td>
        <td v-for="key in columns">
          <dynamic-search v-if="getBoolVal(key.searchable,true)"
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
    retrieveData: Function,
    pageSize:Number,
    serverPagination:Boolean
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
      index:0,
      filterKeys:{},
      extrasCalculated: false,
      data: [],
      length:0,
      sortKey: "",
      sortOrders: sortOrders,
      forceUpdate:0
    };
  },
  components:{
    'dynamic-column': httpVueLoader('/vcomponents/grid/dynamiccolumn.vue'),
    'dynamic-search': httpVueLoader('/vcomponents/grid/dynamicsearch.vue')
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
    cleanUp: function (str) {
      if(str.charAt(0)=="_"){
        str = str.substring(1);
      }
      return str.charAt(0).toUpperCase() + str.slice(1);
    }
  },
  methods: {
    canGoNext:function(){
      return this.length>(this.index+1)*this.pageSize
    },
    goStart:function(){
      this.index=0;
      this.reload();
    },
    goNext:function(){
      this.index++;
      this.reload();
    },
    goEnd:function(){
      var supposedIndex = Math.floor(this.length/ this.pageSize);
      var remainder = this.length % this.pageSize;
      if(remainder==0){
        supposedIndex--;
      }
      if(supposedIndex<0)supposedIndex=0;
      this.index  = supposedIndex;
      this.reload();
    },
    goPrev:function(){
      this.index--;
      this.reload();
    },
    retrieve: function(entry,key){
      return {
        entry:entry,
        key:key
      };
    },
    getBoolVal: function(val,defVal){

      if(typeof val == "undefined") {
        return defVal;
      }
      if(val===false || val ===true) {
        return val;
      }
      return defVal;
    },
    sortBy: function (inputKey) {

      for(const key of this.columns){
        if(key.id.toUpperCase()==inputKey.toUpperCase() && !key.sortable)return;
      }
      for(const key of this.extra){
        if(key.id.toUpperCase()==inputKey.toUpperCase() && !key.sortable)return;
      }
      this.sortKey = inputKey;
      this.sortOrders[inputKey] = this.sortOrders[inputKey] * -1;
    },
    getData: function () {
      return this.data;
    },
    cleanRow:function(inrow){
      var realData = {};
      for(const key of this.columns){
        realData[key.id]=inrow[key.id];
      }
      return realData;
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

        if(tempIdMatch) {
          return this.cleanRow(row);
        }
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
          })
          .catch(err => {
            result.err=err
          });
      if(result.err!=null){
        throw result.err;
      }
      return result.data;
    },
    reload: function () {
      var th= this;
      this.retrieveData(this.index,this.pageSize).then(function(result){
        th.extrasCalculated=false;
        if(th.pageSize!=null && typeof th.pageSize !="undefined" && th.pageSize !=0 &&
            (th.serverPagination==null || typeof th.serverPagination =="undefined" || th.serverPagination ===false)) {
          var newData = [];
          th.length = result.data.length;
          var start = th.index * th.pageSize;
          var end = start+ th.pageSize;
          for (var i=start;i<end && i<result.data.length;i++){
            newData.push(result.data[i]);
          }
          th.data = newData;
        }else if(th.serverPagination!=null && typeof th.serverPagination !="undefined" && th.serverPagination ===true){
          var newData = [];
          var realIndex = th.index;
          if(realIndex<0)realIndex=0;
          th.length=Math.max(th.length,th.pageSize*realIndex+result.data.length);
          for (var i=0;i<th.pageSize && i<result.data.length;i++){
            newData.push(result.data[i]);
          }
          th.data = newData;
        }else{
          debugger;
          th.data = result.data;
        }
        if(th.extra) {
          th.extra.forEach(function (ex) {
            if (th.$refs.hasOwnProperty('search' + ex.id)) {
              if (th.$refs['search' + ex.id].length > 0) {
                th.$refs['search' + ex.id][0].clean();
              }
            }
          });
        }
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

      if(!this.extrasCalculated && typeof this.data !="undefined" && this.data.length >0
      && this.extra) {
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