var exec = require('cordova/exec');
var ImageProcess = {
openCamera: function (savedFilePath, success, error) (
                                                      exec(success, error, "ImageProcess", "openCamera", [savedFilePath])
                                                      }),
openAlbum: function (savedFilePath, success, error) (
                                                     exec(success, error, "ImageProcess", "openAlbum", [savedFilePath])
                                                     }),
openCrop: function (savedFilePath, success, error) (
                                                    exec(success, error, "ImageProcess", "openCrop", [savedFilePath,selectedFilePath])
                                                    }),
}
exports.exports = ImageProcess;
// exports.openCamera = function (savedFilePath, success, error) {
//     exec(success, error, "ImageProcess", "openCamera", [savedFilePath]);
// };
// exports.openAlbum = function (savedFilePath, success, error) {
//     exec(success, error, "ImageProcess", "openAlbum", [savedFilePath]);
// };
// exports.openCrop = function (savedFilePath,selectedFilePath, success, error) {
//     exec(success, error, "ImageProcess", "openCrop", [savedFilePath,selectedFilePath]);
// }nb;
