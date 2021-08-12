import Vue from 'vue'
import App from './app'

new Vue({
    components: { App },
    el: "#demo",
    data: {
        searchQuery: "",
        gridColumns: ["name", "power"],
        gridData: [
            { name: "Chuck Norris", power: Infinity },
            { name: "Bruce Lee", power: 9000 },
            { name: "Jackie Chan", power: 7000 },
            { name: "Jet Li", power: 8000 }
        ]
    }
});