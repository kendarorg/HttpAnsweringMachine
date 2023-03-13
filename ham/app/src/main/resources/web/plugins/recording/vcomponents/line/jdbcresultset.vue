<template>
  <div>
    <h3>{{ value.type }}</h3><br>
    <button id="jdbcres-apply" type="button" class="bi bi-floppy" v-on:click="saveChanges()"
            title="Save changes">Apply Changes
    </button>

    <div class="form-group">
      <label htmlFor="val">Cols count</label>
      <input class="form-control" readonly type="text" name="val" id="val" v-model="columndescriptorslength"/>
    </div>

    <button id="jdbcres-addrow" v-on:click="addRow()" class="bi bi-plus-square" title="Add new"></button>
    <table class="rounded-top">
      <tr>
        <th></th>
        <th></th>
        <th v-for="(col,index) in cols">
          {{ col }}
        </th>
      </tr>
      <tr v-if="newitemrow">
        <td>
          <button id="jdbcres-delrow" v-on:click="deleteRow(-1)" class="bi bi-trash" title="Delete"></button>
        </td>
        <td>
          <button id="jdbcres-confirmrow" v-on:click="confirmRow(-1)" class="bi bi-pen-fill" title="Confirm"></button>
        </td>
        <td v-for="(col,index) in cols">
          <input :id="'jdbcres-col-'+index" class="form-control"
                 v-bind:size="getWidthNewRow(index)"
                 v-bind:style="'width:'+(getWidthNewRow(index)*10+40)+'px;'"
                 type="text" name="val" id="val" v-model="newRow[index]"/>
        </td>
      </tr>

      <tr v-for="(row,index) in shownRows">
        <td>
          <button :id="'jdbcres-row-delete-'+index" v-if="rowstatus!=index" v-on:click="deleteRow(index)"
                  class="bi bi-trash" title="Delete"></button>
        </td>
        <td>
          <div v-if="rowstatus!=index">
            <button :id="'jdbcres-row-modify-'+index" v-on:click="modifyRow(index)" class="bi bi-pen-fill"
                    title="Edit"></button>
          </div>
          <div v-else>
            <button id="jdbcres-row-confirm-{{index}}" v-on:click="confirmRow(index)" class="bi bi-floppy"
                    title="Confirm"></button>
          </div>
        </td>
        <td v-for="(field,fieldindex) in row">
          <div v-if="rowstatus!=index">
            {{ field }}
          </div>
          <div v-else>
            <input class="form-control"
                   v-bind:size="getWidth(index,fieldindex)"
                   v-bind:style="'width:'+(getWidth(index,fieldindex)*10+40)+'px;'"
                   type="text" name="val" id="val" v-model="rows[index][fieldindex]"/>
          </div>
        </td>
      </tr>
    </table>

  </div>
</template>
<script>
module.exports = {
  name: "jdbcresultset",
  props: {
    value: Object
  },
  data: function () {
    return {
      blockChange: false,
      rowsChanged: false,
      newRow: [],
      newitemrow: false,
      rows: [],
      rowstatus: -1,
    }
  },
  computed: {
    columndescriptorslength: {
      get: function () {
        return findChildItemWithType(this.value, 'columndescriptors').children.length;
      }
    },
    shownRows: {
      get: function () {
        var rows = findChildItemWithType(this.value, 'columndescriptors').children;
        if (this.rowsChanged) {
          this.rowsChanged = false;
          return this.rows;
        } else {
          if (this.blockChange) {
            return this.rows;
          }
          this.value;
          return this.retrievePassedRows();
        }
      }
    },
    cols: {
      get: function () {
        var rows = findChildItemWithType(this.value, 'columndescriptors').children;
        var realRows = [];
        rows.forEach(function (row) {
          realRows.push(findChildItemWithType(row, 'name').value)
        })

        return realRows;
      }
    }
  },
  methods: {
    retrievePassedRows: function () {
      var result = [];
      var th = this;
      var rows = findChildItemWithType(this.value, 'rows').children;

      rows.forEach(function (row) {
        var realRow = [];
        row.children.forEach(function (col) {
          if (!isUndefined(col)) {
            var value = col.value;
            if (typeof value == "undefined") value = null;
            realRow.push(value);
          } else {
            realRow.push(null);

          }
        });
        if (realRow.length > 0) {
          result.push(realRow);
        }
      })
      this.rows = result;
      return result;
    },
    reloadObject: function (originalResultset, setitem) {
      var data = {
        jsonResultSet: originalResultset,
        data: JSON.stringify(this.rows)
      }
      axiosHandle(axios.post('/api/jdbcproxies/utils/modifyresultset', data), (result) => {
        setitem(JSON.stringify(result.data));
      });
    },
    saveChanges: function () {
      this.$emit("componentevent", {
        id: "reloadcallback",
        callback: this.reloadObject,
      })
    },
    getWidth: function (index, fieldindex) {
      if (isUndefined(this.rows[index][fieldindex])) return 2;
      return (this.rows[index][fieldindex] + "").length;
    },
    getWidthNewRow: function (fieldindex) {
      if (isUndefined(this.newRow[fieldindex])) return 2;
      return (this.newRow[fieldindex] + "").length;
    },
    confirmRow: function (index) {
      this.rowsChanged = true;
      this.blockChange = false;
      if (this.newitemrow == true) {
        this.newitemrow = false;
        var newInserted = [];
        for (var i = 0; i < this.columndescriptorslength; i++) {
          newInserted.push(this.newRow[i]);
        }
        this.rows.push(newInserted);
      }
      this.rowstatus = -1;
      this.saveChanges();
    },
    modifyRow: function (index) {
      this.blockChange = true;
      this.rowstatus = index;
    },
    deleteRow: function (index) {
      this.rowsChanged = true;
      if (this.newitemrow == true) {
        this.newitemrow = false;
        return;
      }
      this.rows.splice(index, 1);
    },
    addRow: function () {
      clearArray(this.newRow);
      for (var i = 0; i < this.columndescriptorslength; i++) {
        this.newRow.push("");
      }
      this.newitemrow = true;
    }
  }
}
</script>