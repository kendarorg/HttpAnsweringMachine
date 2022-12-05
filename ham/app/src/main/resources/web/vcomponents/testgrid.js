function SimpleGridUtils(selectRow){
    this.selectRow = selectRow;
    this.toggleSelect = function(gridInstance){
        var th = this;

        gridInstance.filteredData.forEach(function(toSel){
            var index =gridInstance.buildId(toSel);
            gridInstance.setField(th.selectRow,index,!toSel.select);
        });
    }
    this.selectAll = function(gridInstance){
        var th = this;
        gridInstance.filteredData.forEach(function(toSel){
            var index =gridInstance.buildId(toSel);
            gridInstance.setField(th.selectRow,index,true);
        });
    }
}