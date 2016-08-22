var MSDK = function() {};

MSDK.prototype.initMSDK = function(qqAppId, qqAppKey, wxAppId, msdkKey, offerId, appVersionName, appVersionCode, success, fail) {
    cordova.exec(success, fail, "MSDKPlugin", "initMSDK", [qqAppId, qqAppKey, wxAppId, msdkKey, offerId, appVersionName, appVersionCode]);
};

MSDK.prototype.qqLogin = function(success, fail) {
    cordova.exec(success, fail, "MSDKPlugin", "qqLogin", []);
};

MSDK.prototype.wechatLogin = function(success, fail) {
    cordova.exec(success, fail, "MSDKPlugin", "wechatLogin", []);
};

MSDK.prototype.logout = function(success, fail) {
    cordova.exec(success, fail, "MSDKPlugin", "logout", []);
};

var msdk = new MSDK();
module.exports = msdk;