<template>
  <div>
    <button type="button" id="genTLSSSL" name="genTLSSSL" class="btn btn-default" v-on:click="generateTLSSSL()">Generate
      TLSSSL
    </button>

    <div class="form-group">
      <label htmlFor="cn">CN</label>
      <input class="form-control" type="text" name="cn" id="cn" placeholder="CN=main" v-model="cn"/>
    </div>
    <!--<div class="form-group">
      <label htmlFor="validity">Validity</label>
      <input class="form-control" type="text" name="validity" id="validity" placeholder="2040-12-25" v-model="validity"/>
    </div>-->
    <div class="form-group">
      <label htmlFor="name">File name</label>
      <input class="form-control" type="text" name="name" id="name" placeholder="my-certificate" v-model="name"/>
    </div>
    <div class="form-group">
      <label for="extraDomains">Domains</label>
      <textarea placeholder="www.main.com" class="form-control" rows="6" cols="50" name="extraDomains" id="extraDomains"
                v-model="extraDomains"></textarea>
    </div>
  </div>
</template>
<script>
module.exports = {
  name: "ssl-root-generate",
  data: function () {
    return {
      cn: "",
      extraDomains: "",
      validity: "",
      name: ""
    }
  },
  methods: {
    generateTLSSSL: function () {
      if (this.name == "" || typeof this.name == "undefined") {
        this.name = "my-certificate";
      }
      var th = this;
      var data = {
        cn: this.cn,
        extraDomains: this.extraDomains.split(/\r?\n/)
      };

      axiosHandle(axios({
        url: '/api/sslgen', //your url
        method: 'POST',
        responseType: 'blob', // important
        data: data
      }), (response) => {
        // create file link in browser's memory
        const href = URL.createObjectURL(response.data);

        // create "a" HTML element with href to file & click
        const link = document.createElement('a');
        link.href = href;
        var filename = th.name + ".cer";
        link.setAttribute('download', filename); //or any other extension
        document.body.appendChild(link);
        link.click();

        // clean up "a" element & remove ObjectURL
        document.body.removeChild(link);
        URL.revokeObjectURL(href);
      });
    }
  }
}
</script>