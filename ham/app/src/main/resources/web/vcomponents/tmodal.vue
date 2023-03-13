<template>
  <transition name="modal">
    <div class="modal-mask">
      <div class="modal-dialog">
        <div class="modal-content">

          <div class="modal-header">
            <table style="width:100%">
              <tr>
                <td>
                  <slot name="header">
                    default header
                  </slot>
                </td>
                <td>
                  <button type="button" class="close" @click="$emit('close')" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                  </button>
                </td>
              </tr>
            </table>

          </div>

          <div class="modal-body">
            <slot name="body">
              default body
            </slot>
          </div>

          <div class="modal-footer">
            <slot name="footer">
              <button type="button" class="bi bi-x-square" @click="$emit('close')" title="Close"></button>
              <button type="button" class="bi bi-floppy" v-on:click="saveTheModal" title="Save changes"></button>
            </slot>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>
<script>
module.exports = {
  name: 'simple-modal',
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