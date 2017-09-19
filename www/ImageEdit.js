var exec = require("cordova/exec");
var ImageEdit = {};
ImageEdit.imageedit = function(successCallback, errorCallback, args) {
  exec(successCallback, errorCallback, "ImageEdit", "imageedit", args);
};
module.exports = ImageEdit;
