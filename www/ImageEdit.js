var exec = require("cordova/exec");
var ImageEdit = {};
ImageEdit.imageprocess = function (successCallback, errorCallback, args) {
    // var args = ["path"]
    exec(successCallback, errorCallback, "ImageProcess", "openCamera", args);
};
module.exports = ImageEdit;
