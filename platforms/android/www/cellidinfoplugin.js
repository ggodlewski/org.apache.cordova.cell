var exec = require('cordova/exec');

module.exports = {
    getCellInfo: function(successCallback, failure){
        exec(successCallback, failure, "CellId", "getCellInfo", []);
    }
}
