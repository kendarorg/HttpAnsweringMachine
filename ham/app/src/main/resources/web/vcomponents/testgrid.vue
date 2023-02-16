<template>
  <div>
    <div v-if="pageSize!=null">
      <br>
      <button v-bind:disabled="index<=0" class="bi bi bi-skip-backward" @click="goStart" title="Start"></button>
      <button v-bind:disabled="index<=0" class="bi bi-skip-start" @click="goPrev" title="Prev"> </button>
      <button v-bind:disabled="!canGoNext()" class="bi bi-skip-end" @click="goNext" title="Next"></button>
      <button v-bind:disabled="!canGoNext()" class="bi bi-skip-forward" @click="goEnd" title="End"></button>
      <!--&nbsp;&nbsp;

      <button v-if="serverPagination===true" class="bi bi-binoculars" @click="search()" title="Search"></button>-->
      <br><br>
    </div>
    <table class="rounded-top">
      <thead>
      <tr >
        <th v-for="key in extra" >
          <span v-if="getBoolVal(key.sortable,true)" class="btn btn-kendar btn-sm">
          {{ buildLabel(key) | cleanUp }}
          </span>
        </th>
        <th v-for="key in columns"
            @click="sortBy(key.id)"
            >
          <span v-if="getBoolVal(key.sortable,true)" class="btn btn-kendar btn-sm" :class="{ active: sortKey == key.id }">
                {{ buildLabel(key) | cleanUp }}
                <span :class="(sortOrders[key] > 0 ? 'arrow asc' : 'arrow dsc')">
                </span>
          </span>
          <span v-else class="btn btn-kendar btn-sm">
                {{ buildLabel(key) | cleanUp }}
          </span>
        </th>
      </tr>
      </thead>
      <tbody>
      <tr>
        <td v-for="key in extra">
          <component :is="'s'+key.template" v-if="getBoolVal(key.searchable,true)"
                          :ref="'search'+key.id"
                          :descriptor="key"/>
        </td>
        <td v-for="key in columns">
          <component :is="'s'+key.template" v-if="getBoolVal(key.searchable,true)"
                          :ref="'search'+key.id"
                          :descriptor="key"/>
        </td>
      </tr>
      <tr v-for="(entry,index) in filteredData" @click="onClicked(entry,index)">
        <td v-for="key in extra">
          <component :is="'c'+key.template" :descriptor="key"
                          :value="entry"
                          :ref="buildIndexCrc(entry)+key.id"
                          :index="buildId(entry)" />
        </td>
        <td v-for="key in columns">
          <component  :is="'c'+key.template" :descriptor="key"
                      :value="entry"
                      :ref="buildIndexCrc(entry)+key.id"
                      :index="buildId(entry)"/>
        </td>
      </tr>
      </tbody>
    </table>
    <br>
  </div>
</template>
<script>
module.exports = {
  props: {
    columns: {
      type:Array,
      default: () => [
        {id:"key",template:"string",index:true,sortable:true},
        {id:"value",template:"string",sortable:true}
      ]
    },
    extra: Array,
    address: String,
    retrieveData: {
      type:Function,
      optional:true,
      default:null
    },
    selectedindex: {
      type:Number,
      optional:true,
      default:-1
    },
    pageSize:Number,
    serverPagination:Boolean,
    isObject:{
      type: Boolean,
      default: false
    }
  },
  created: function () {
    this.extrasCalculated = false;
  },
  async mounted() {
    await this.reload();
  },
  watch:{
    selectedindex:function(index,oldVal){

      if(index!=null && index<this.localFilteredData.length && index>=0){
        var prev=null;
        var next=null;
        if(index<0) index=0;
        var entry =this.localFilteredData[index]
        if(index>0){
          prev = this.localFilteredData[index-1];
        }
        if(index<(this.localFilteredData.length-1)){
          next = this.localFilteredData[index+1];
        }
        var evt = {
          index:index,
          prev:prev,
          current:entry,
          next:next,
          data:this.localFilteredData
        }
        this.$emit("gridrowclicked",evt);
      }
    }
  },
  data: function () {
    var sortOrders = {};
    this.columns.forEach(function (key) {
      sortOrders[key.id] = 1;
    });
    return {
      localFilteredData:[],
      index:0,
      columnsKeyMap:null,
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
    'sbool': httpVueLoader('/vcomponents/grid/search/sbool.vue'),
    'sboolw': httpVueLoader('/vcomponents/grid/search/sboolw.vue'),
    'sbutton': httpVueLoader('/vcomponents/grid/search/sbutton.vue'),
    'slink': httpVueLoader('/vcomponents/grid/search/slink.vue'),
    'slong': httpVueLoader('/vcomponents/grid/search/slong.vue'),
    'sstring': httpVueLoader('/vcomponents/grid/search/sstring.vue'),

    'cbool': httpVueLoader('/vcomponents/grid/column/cbool.vue'),
    'cboolw': httpVueLoader('/vcomponents/grid/column/cboolw.vue'),
    'cbutton': httpVueLoader('/vcomponents/grid/column/cbutton.vue'),
    'clink': httpVueLoader('/vcomponents/grid/column/clink.vue'),
    'clong': httpVueLoader('/vcomponents/grid/column/clong.vue'),
    'cstring': httpVueLoader('/vcomponents/grid/column/cstring.vue'),
    'ciconbutton': httpVueLoader('/vcomponents/grid/column/ciconbutton.vue')
  },
  computed: {
    filteredData: function () {
      if(this.data == null || typeof this.data =="undefined"){
        this.localFilteredData=[];
        return [];
      }

      this.forceUpdate;
      var filterKeys = this.filterKeys;

      var sortKey = this.sortKey;
      var order = this.sortOrders[sortKey] || 1;
      var data = this.data;
      var th = this;
      if (filterKeys) {


        data = data.filter(function (row) {

          if(th.columnsKeyMap==null){
            th.columnsKeyMap = {};
            th.columns.forEach(function(col){
              th.columnsKeyMap[col.id]=col;
            })
          }
          let allGood=true;
          for (const [key, value] of Object.entries(filterKeys)) {
            if(value==null|| typeof value=="undefined")continue;
            allGood = String(th.retrieveRowData(row,th.columnsKeyMap[key]))
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
      this.localFilteredData=data;
      return data;

    }
  },
  filters: {
    cleanUp: function (str) {
      if(typeof str == "undefined") return str;
      if(str.charAt(0)=="_"){
        str = str.substring(1);
      }
      return str.charAt(0).toUpperCase() + str.slice(1);
    }
  },
  methods: {
    retrieveRowData:function(entry,key){
      if(typeof key.func=="Function"){
        return key.func(entry);
      }
      return entry[key.id];
    },
    buildLabel: function(key){
      if(typeof key.label=="undefined") return key.id;
      return key.label;
    },
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

      if(typeof val == "undefined" || val==null) {
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
      if(this.extra) {
        for (const key of this.extra) {
          if (key.id.toUpperCase() == inputKey.toUpperCase() && !key.sortable) return;
        }
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
    getByIdFull:function (indexArray) {
      for (var row of this.data) {
        var tempId = this.buildId(row);
        var tempIdMatch = true;
        for (var i = 0; i < indexArray.length; i++) {
          if (indexArray[i] != tempId[i]) {
            tempIdMatch = false;
            break;
          }
        }
        if(tempIdMatch) {return row;}
        }
      return null;

    },
    getIndexByIdFull:function (indexArray) {
      for (var i=0;i<this.data.length;i++) {
        var row = this.data[i];
        var tempId = this.buildId(row);
        var tempIdMatch = true;
        for (var i = 0; i < indexArray.length; i++) {
          if (indexArray[i] != tempId[i]) {
            tempIdMatch = false;
            break;
          }
        }
        if(tempIdMatch) {return i;}
      }
      return -1;

    },
    buildIndexCrc:function (entry){
      var index = this.buildId(entry).join(",");
      return "row"+b_crc32(index);
    },
    getById: function (indexArray) {
      var founded = this.getByIdFull(indexArray);
      if(null == founded) return null;
      return this.cleanRow(founded);
    },
    getByRow: function (otherRow) {
      var indexArray = this.buildId(otherRow);
      return this.getById(indexArray);
    },
    setField: function(id,indexArray,newValue){
      var row = this.getByIdFull(indexArray);
      row[id]=newValue;
      var crc = this.buildIndexCrc(row)+id;
      this.$refs[crc][0].forceUpdate();

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
    loadClientPagination:function(result){
      var th= this;
      var newData = [];
      th.length = result.data.length;
      var start = th.index * th.pageSize;
      var end = start+ th.pageSize;
      for (var i=start;i<end && i<result.data.length;i++){
        newData.push(result.data[i]);
      }
      th.data = newData;
    },
    loadServerPagination:function(result){
      var th= this;
      var newData = [];
      var realIndex = th.index;
      if(realIndex<0)realIndex=0;
      th.length=Math.max(th.length,th.pageSize*realIndex+result.data.length);
      for (var i=0;i<th.pageSize && i<result.data.length;i++){
        newData.push(result.data[i]);
      }
      th.data = newData;
    },
    reloadFieldsAndOrdering:function(){
      var th=this;
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
    },
    reloadDataInternal:function(result){
      var th = this;
      th.extrasCalculated=false;
      var resultData = {
        data:[]
      };
      if(this.isObject){
        var keys = Object.keys(result.data);
        for(var i=0;i<keys.length;i++){
          var key = keys[i];
          var value = result.data[key];
          resultData.data.push({
            key:key,
            value:value
          });
        }
      }else{
        resultData = result;
      }

      if(th.pageSize!=null && typeof th.pageSize !="undefined" && th.pageSize !=0 &&
          (th.serverPagination==null || typeof th.serverPagination =="undefined" || th.serverPagination ===false)) {
        th.loadClientPagination(resultData);
      }else if(th.serverPagination!=null && typeof th.serverPagination !="undefined" && th.serverPagination ===true){
        th.loadServerPagination(resultData);
      }else{
        th.data = resultData.data;
      }
      this.reloadFieldsAndOrdering();

    },
    reload: function (possibleData) {
      if(isUndefined(possibleData)){
        possibleData=[];
      }
      var th= this;
      if(this.retrieveData==null || typeof this.retrieveData =="undefined"){
        if(possibleData!=null && typeof possibleData !="undefined") {
          this.reloadDataInternal({
            data: possibleData
          })
        }else{
          this.reloadFieldsAndOrdering();
        }
      }else {
        this.retrieveData(this.index, this.pageSize).then(function (result) {
          if (typeof result == "undefined") {
            result = {
              data: []
            }
          }
          th.reloadDataInternal(result);
        });
      }

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
        th.setField(selectField,index,!toSel[selectField]);
      });
      this.forceUpdate++;
    },
    onSelected:function(functoApply,selectField){
      if(typeof selectField=="undefined" || selectField==null){
        selectField="select";
      }
      var curse = selectField;
      var th = this;
      this.filteredData.forEach(function(toSel){
        var index =th.buildId(toSel);
        var row = th.getByIdFull(index);
        if(toSel[curse]) {
          functoApply(row);
        }
      });
      this.forceUpdate++;
    },
    selectAll : function(selectField){
      var th = this;
      var toselect = this.filteredData;
      console.log("select all "+selectField)
      toselect.forEach(function(toSel){
        var index =th.buildId(toSel);
        th.setField(selectField,index,true);
      });
      this.forceUpdate++;
    },
    arrayEquals:function(a, b) {
      return Array.isArray(a) &&
          Array.isArray(b) &&
          a.length === b.length &&
          a.every((val, index) => val === b[index]);
    },
    updateInternal(row,dataArray){
      var th=this;
      var index =th.buildId(row);

      if(this.isObject){
        var newArray=[];
        var foundedNew = true;
        var keys = Object.keys(dataArray);
        for(var i=0;i<keys.length;i++){
          var key = keys[i];
          var value = dataArray[key];
          if(row['key'].toUpperCase()==key.toUpperCase()){
            foundedNew=false;
            dataArray[row['key']]=row['value'];
            newArray.push(row);
          }else {
            newArray.push({
              key: key,
              value: value
            });
          }
        }
        if(foundedNew){
          dataArray[row['key']]=row['value'];
          newArray.push(row);
        }
        this.data = newArray;
        return !foundedNew;
      }else{
        var newArray=[];
        var foundedNew=true;
        for (let i = 0; i < dataArray.length; i++){
          var item = dataArray[i];
          newArray.push(item);
          var itemIndex =th.buildId(item);
          if(th.arrayEquals(index,itemIndex)){
            foundedNew = false;
            for(var j=0;j<this.columns.length;j++){
              var col = this.columns[j];
              newArray[col.id]=row[col.id];
            }
          }
        }
        if(foundedNew){
          newArray.push(row);
          this.data = newArray;
          return false;
        }
        this.data = newArray;
        return true;
      }
    },
    update:function(row,realData){
      if(typeof realData!="undefined"){
        return this.updateInternal(row,realData);
      }else{
        return this.updateInternal(row,this.data);
      }

    },
    delete:function(index,realData){
      if(typeof realData!="undefined"){
        return this.deleteInternal(index,realData);
      }else{
        return this.deleteInternal(index,this.data);
      }

    },
    deleteInternal(row,dataArray){
      var th=this;
      var index =th.buildId(row);

      if(this.isObject){
        var newArray=[];
        var keys = Object.keys(dataArray);
        for(var i=0;i<keys.length;i++){
          var key = keys[i];
          var value = dataArray[key];
          if(row[0].toUpperCase()==key.toUpperCase()){
            delete dataArray[row];
          }else {
            newArray.push({
              key: key,
              value: value
            });
          }
        }
        this.data = newArray;
      }else{
        var newArray=[];
        for (let i = 0; i < dataArray.length; i++){
          var item = dataArray[i];
          var itemIndex =th.buildId(item);
          if(!th.arrayEquals(index,itemIndex)){
            newArray.push(item);
          }
        }
        this.data = newArray;
      }
    },
    onClicked:function(entry,index){
      var prev=null;
      var next=null;
      if(index>0){
        prev = this.localFilteredData[index-1];
      }
      if(index<(this.localFilteredData.length-1)){
        next = this.localFilteredData[index+1];
      }
      var evt = {
        index:index,
        prev:prev,
        current:entry,
        next:next,
        data:this.localFilteredData
      }
      this.$emit("gridrowclicked",evt);
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