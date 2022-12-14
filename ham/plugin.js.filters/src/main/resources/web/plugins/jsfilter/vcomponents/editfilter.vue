<template>
<div>
  <div class="col-md-8">
    <br>
    <button id="save" name="save" type="button"  class="btn btn-default" onClick="save();">Save</button>

  </div>

  <div class="col-md-8">
    <div class="form-group">
      <label htmlFor="id">Id</label>
      <input class="form-control" readOnly type="text" name="id" id="id"/>
    </div>
    <div class="form-group">
      <label htmlFor="method">method</label>
      <select class="form-control" name="method" id="method" >
        <option>GET</option>
        <option>POST</option>
        <option>PUT</option>
        <option>DELETE</option>
        <option>OPTIONS</option>
        <option>PATCH</option>
      </select>
    </div>
    <div class="form-group">
      <label htmlFor="hostAddress">hostAddress</label>
      <input class="form-control" type="text" name="hostAddress" id="hostAddress"/>
    </div>
    <div class="form-group">
      <label htmlFor="hostRegexp">hostRegexp</label>
      <input class="form-control" type="text" name="hostRegexp" id="hostRegexp"/>
    </div>
    <div class="form-group">
      <label htmlFor="pathAddress">pathAddress</label>
      <input class="form-control" type="text" name="pathAddress" id="pathAddress"/>
    </div>
    <div class="form-group">
      <label htmlFor="pathRegexp">pathRegexp</label>
      <input class="form-control" type="text" name="pathRegexp" id="pathRegexp"/>
    </div>
    <div class="form-group">
      <label htmlFor="phase">phase</label>
      <select class="form-control" name="phase" id="phase" >
        <option>NONE</option>
        <option>PRE_RENDER</option>
        <option>API</option>
        <option>STATIC</option>
        <option>PRE_CALL</option>
        <option>POST_CALL</option>
        <option>POST_RENDER</option>
      </select>
    </div>
    <div class="form-group">
      <label htmlFor="priority">priority (number)</label>
      <input class="form-control" readOnly type="text" name="priority" id="priority"/>
    </div>
    <div class="form-check">
      <input class="form-check-input" type="checkbox" value="" id="blocking" name="blocking">
      <label class="form-check-label" for="blocking">
        blocking
      </label>
    </div>
  </div>
  <div class="form-group">
    <label for="source">source</label><br>
    <!--prism-live language-javascript-->
    <textarea spellcheck="false"
              class="form-control"
              rows="6" cols="50"  name="source" id="source"></textarea>
  </div>

  <div class="col-md-8">
    <h4>REQUIRES</h4>
  </div>
  <div class="col-md-8">
    <table class="table table-striped" id="requires_list">
      <tr>
        <th>Id</th>
        <th colspan="2">
          <button class="btn btn-danger form-control"
                  onClick="addRequires();">Add</button>
        </th>
      </tr>
    </table>
  </div>
</div>
</template>
<script>
module.exports = {
  name: "edit-js"
}
</script>