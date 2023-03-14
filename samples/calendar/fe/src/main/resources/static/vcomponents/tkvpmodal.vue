<template>
  <transition name="modal">
    <div class="modal-mask">
      <div class="modal-dialog">
        <div class="modal-content">

          <div class="modal-header">
            <table style="width:100%">
              <tr>
                <td>
                  <h2>{{ modalData.title }}</h2>
                </td>
                <td>
                  <button id="tkvp-close" type="button" class="close" @click="$emit('close')" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                  </button>
                </td>
              </tr>
            </table>

          </div>

          <div class="modal-body">
            <div class="form-group">
              <label for="key">{{ modalData.keyDesc }}</label>
              <input type="string" class="form-control" id="key" v-model="modalData.keyValue"
                     :readonly="modalData.edit">
            </div>
            <div class="form-group">
              <label for="val">{{ modalData.valDesc }}</label>
              <input type="string" class="form-control" id="val" v-model="modalData.valValue">
            </div>
          </div>

          <div class="modal-footer">
            <button id="tkvp-closes"  type="button" class="bi bi-x-square icon-black" @click="$emit('close')" title="Close"></button>
            <button id="tkvp-save"  type="button" class="bi bi-save-fill icon-black" v-on:click="saveTheModal" title="Save changes"></button>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>
<script>
module.exports = {
  name: 'kvp-modal',
  props: {
    modalData: Object
  },
  data: function () {
    return {
      showModal: false
    }
  },
  methods: {
    saveTheModal: function () {
      this.modalData.save();
    }
  }
}
</script>
<style>
.modal-mask {
  position: fixed;
  z-index: 9998;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: table;
  transition: opacity 0.3s ease;
}

</style>