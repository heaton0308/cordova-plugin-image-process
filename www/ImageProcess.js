var exec = require('cordova/exec');

exports.openCamera = function (savedFilePath, success, error) {
    exec(success, error, "ImageProcess", "openCamera", [savedFilePath]);
};
exports.openAlbum = function (savedFilePath, success, error) {
    exec(success, error, "ImageProcess", "openAlbum", [savedFilePath]);
};
exports.openCrop = function (savedFilePath,selectedFilePath, success, error) {
    exec(success, error, "ImageProcess", "openCrop", [savedFilePath,selectedFilePath]);
};
