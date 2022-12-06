<template>
  <transition name="modal">
    <div class="modal-mask">
      <div class="modal-dialog">
        <div class="modal-content">

          <div class="modal-header">
            <table style="width:100%">
              <tr><td ><slot name="header" >
                default header
              </slot></td><td><button type="button" class="close" @click="$emit('close')" aria-label="Close">
                <span aria-hidden="true">&times;</span>
              </button></td></tr>
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
              <button type="button" class="bi bi-save" v-on:click="saveTheModal" title="Save changes"></button>
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
  props:{
    modalData:Object
  },
  data: function() {
    return {
      showModal: false
    }
  },
  methods:{
    saveTheModal:function(){
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
/*
.modal-wrapper {
  display: table-cell;
  vertical-align: middle;
}

.modal-container {
  width: 300px;
  margin: 0px auto;
  padding: 20px 30px;
  background-color: #fff;
  border-radius: 2px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.33);
  transition: all 0.3s ease;
  font-family: Helvetica, Arial, sans-serif;
}


.modal-header h3 {
  margin-top: 0;
  color: #42b983;
}

.modal-body {
  margin: 20px 0;
}


.modal-default-button {
  float: right;
}*/

/*
 * The following styles are auto-applied to elements with
 * transition="modal" when their visibility is toggled
 * by Vue.js.
 *
 * You can easily play with the modal transition by editing
 * these styles.
 */
/*
.modal-enter {
  opacity: 0;
}

.modal-leave-active {
  opacity: 0;
}

.modal-enter .modal-container,
.modal-leave-active .modal-container {
  -webkit-transform: scale(1.1);
  transform: scale(1.1);
}
*/
 */
</style>