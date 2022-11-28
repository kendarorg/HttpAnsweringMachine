<template>
    <button v-on:click="count++">You clicked me {{ count }} times.</button>
</template>
<script>
module.exports = {
    data: function() {
        return {
            count: 0
        }
    }
}
</script>
<style>
</style>