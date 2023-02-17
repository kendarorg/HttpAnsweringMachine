<template>
  <div>
    <h3>Script Matcher</h3><br>
    <p>At least one between host address/regexp must be set, with the script</p>
    <div class="form-group">
      <label htmlFor="hostAddress">hostAddress</label>
      <input class="form-control" type="text" required name="hostAddress" id="hostAddress" v-model="matcher.hostAddress"/>
    </div>
    <div class="form-group">
      <label htmlFor="pathAddress">pathAddress</label>
      <input class="form-control" type="text" required name="pathAddress" id="pathAddress" v-model="matcher.pathAddress"/>
    </div>
    <div class="form-group">
      <label for="script">script</label><br>
      <!--prism-live language-javascript-->
      <textarea spellcheck="false"
                class="form-control"
                rows="6" cols="50" name="script" id="script" v-model="matcher.script"></textarea>
    </div>
  </div>
</template>
<script>
module.exports = {
  name: "scriptmatcher",
  props: {
    matcher: Object
  },
  methods:{
    isValid:function(){
      return ( !isUndefined(this.matcher.hostAddress)||!isUndefined(this.matcher.pathAddress) ) &&
          !isUndefined(this.matcher.script);
    }
  }
}
</script>