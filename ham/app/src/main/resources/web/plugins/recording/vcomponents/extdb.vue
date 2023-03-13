<template>
  <div class="row boxed">
    <div class="col-md-8">
      <input class="form-check-input" type="checkbox" value="" id="recordVoidDbCalls" name="recordVoidDbCalls"
             v-model="recordVoidDbCalls" @change="changeRecordVoidDbCalls">
      <label class="form-check-label" for="recordVoidDbCalls">
        Record void db calls
      </label>
      <input class="form-check-input" type="checkbox" value="" id="recordDbCalls" name="recordDbCalls"
             v-model="recordDbCalls" @change="changeRecordDbCalls">
      <label class="form-check-label" for="recordDbCalls">
        Record db calls
      </label>
    </div>
    <div class="col-md-8">
      <input class="form-check-input" type="checkbox" value="" id="useSimEngine" name="useSimEngine"
             v-model="useSimEngine" @change="changeUseSimEngine">
      <label class="form-check-label" for="useSimEngine">
        Use simulated engine (not suitable for metadata)
      </label>
    </div>
    <div class="col-md-8">
      <div class="form-group">
        <label for="name">DbNames (comma separated)</label>
        <input @change="changeDbName" class="form-control" type="text" name="name" id="name" v-model="dbNames"/>
      </div>
    </div>
  </div>
</template>
<script>
module.exports = {
  name: 'extdb',
  props: {
    value: Object
  },
  data: function () {
    return {
      recordDbCalls: true,
      recordVoidDbCalls: false,
      useSimEngine: true,
      dbNames: "*"
    }
  },
  watch: {
    $props: {
      handler(val, oldVal) {
        if (!isUndefined(val.value)) {
          if (!isUndefined(val.value.recordDbCalls)) this.recordDbCalls = isTrue(val.value.recordDbCalls);
          if (!isUndefined(val.value.recordVoidDbCalls)) this.recordVoidDbCalls = isTrue(val.value.recordVoidDbCalls);
          if (!isUndefined(val.value.useSimEngine)) this.useSimEngine = isTrue(val.value.useSimEngine);
          if (!isUndefined(val.value.dbNames)) this.dbNames = val.value.dbNames;
        }
        this.changeDbName();
        this.changeUseSimEngine();
        this.changeRecordVoidDbCalls();
        this.changeRecordDbCalls();
      },
      deep: true,
      immediate: true,
    }
  },
  methods: {
    changeDbName: function () {
      var evt = {
        value: this.dbNames,
        id: "dbNames"
      }
      this.$emit("componentevent", evt);
    },
    changeUseSimEngine: function () {
      var evt = {
        value: this.useSimEngine,
        id: "useSimEngine"
      }
      this.$emit("componentevent", evt);
    },
    changeRecordVoidDbCalls: function () {
      var evt = {
        value: this.recordVoidDbCalls,
        id: "recordVoidDbCalls"
      }
      this.$emit("componentevent", evt);
    },
    changeRecordDbCalls: function () {
      var evt = {
        value: this.recordDbCalls,
        id: "recordDbCalls"
      }
      this.$emit("componentevent", evt);
    }
  }
}
</script>