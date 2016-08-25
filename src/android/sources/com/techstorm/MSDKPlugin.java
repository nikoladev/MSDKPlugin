package com.techstorm;


import android.app.Activity;
import android.util.Log;

import com.tencent.msdk.api.CardRet;
import com.tencent.msdk.api.LocationRet;
import com.tencent.msdk.api.LoginRet;
import com.tencent.msdk.api.MsdkBaseInfo;
import com.tencent.msdk.api.ShareRet;
import com.tencent.msdk.api.TokenRet;
import com.tencent.msdk.api.WGPlatformObserver;
import com.tencent.msdk.api.WakeupRet;
import com.tencent.msdk.consts.CallbackFlag;
import com.tencent.msdk.consts.EPlatform;
import com.tencent.msdk.consts.TokenType;
import com.tencent.msdk.remote.api.RelationRet;
import com.tencent.msdk.tools.Logger;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MSDKPlugin extends CordovaPlugin {

    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext command) throws JSONException {
        final Activity activity = this.cordova.getActivity();
        this.cordova.getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (action.equals("initMSDK")) {
                    MsdkBaseInfo baseInfo = new MsdkBaseInfo();
                    try {
                        baseInfo.qqAppId = args.getString(0);
                        baseInfo.qqAppKey = args.getString(1);
                        baseInfo.wxAppId = args.getString(2);
                        baseInfo.msdkKey = args.getString(3);
                        baseInfo.offerId = args.getString(4);

                        baseInfo.appVersionName = args.getString(5);
                        baseInfo.appVersionCode = args.getInt(6);
                        Log.d("TRY", "using provided parameters");
                    } catch (JSONException e) {
                        //default
                        baseInfo.qqAppId = "100703379";
                        baseInfo.qqAppKey = "4578e54fb3a1bd18e0681bc1c734514e";
                        baseInfo.wxAppId = "wxcde873f99466f74a";
                        baseInfo.msdkKey = "5d1467a4d2866771c3b289965db335f4";
                        baseInfo.offerId = "100703379";
                        // TODO GAME 自2.7.1a开始游戏可在初始化msdk时动态设置版本号，灯塔和bugly的版本号由msdk统一设置
                        // 1、版本号组成 = versionName + versionCode
                        // 2、游戏如果不赋值给appVersionName（或者填为""）和appVersionCode(或者填为-1)，
                        // msdk默认读取AndroidManifest.xml中android:versionCode="51"及android:versionName="2.7.1"
                        // 3、游戏如果在此传入了appVersionName（非空）和appVersionCode（正整数）如下，则灯塔和bugly上获取的版本号为2.7.1.271
                        baseInfo.appVersionName = "2.14.4";
                        baseInfo.appVersionCode = 68903;
                        Log.d("CATCH", "using default parameters");
                    }




                    MSDKHelper.initMSDK(baseInfo, activity, new WGPlatformObserver() {
                        @Override
                        public void OnLoginNotify(LoginRet loginRet) {
                            JSONObject result = loginReturnResponse(loginRet);

                            PluginResult dataResult = new PluginResult(PluginResult.Status.OK, result);
                            dataResult.setKeepCallback(true);
                            command.sendPluginResult(dataResult);
                        }

                        @Override
                        public void OnShareNotify(ShareRet shareRet) {

                        }

                        @Override
                        public void OnWakeupNotify(WakeupRet wakeupRet) {

                        }

                        @Override
                        public void OnAddWXCardNotify(CardRet cardRet) {

                        }

                        @Override
                        public void OnRelationNotify(RelationRet relationRet) {
                            int aa = 33;
                        }

                        @Override
                        public void OnLocationNotify(RelationRet relationRet) {

                        }

                        @Override
                        public void OnLocationGotNotify(LocationRet locationRet) {

                        }

                        @Override
                        public void OnFeedbackNotify(int i, String s) {

                        }

                        @Override
                        public String OnCrashExtMessageNotify() {
                            return null;
                        }

                        @Override
                        public byte[] OnCrashExtDataNotify() {
                            return new byte[0];
                        }
                    });
                } else if (action.equals("qqLogin")) {
                    if (MSDKHelper.getPlatform() == EPlatform.ePlatform_QQ) {
                        command.success(loginReturnResponse(MSDKHelper.getLoginRecord()));
                    } else if (MSDKHelper.getPlatform() == EPlatform.ePlatform_None) {
                        MSDKHelper.qqLogin();
                    }
                } else if (action.equals("wechatLogin")) {
                    if (MSDKHelper.getPlatform() == EPlatform.ePlatform_Weixin) {
                        command.success(loginReturnResponse(MSDKHelper.getLoginRecord()));
                    } else {
//                        try {
//                            boolean scanCodeLogin = args.getBoolean(0);
//                            if (scanCodeLogin) {
//                                MSDKHelper.wechatScanCodeLogin();
//                            } else {
                                MSDKHelper.wechatNormalLogin();
//                            }
//                        } catch (JSONException e) {
//                            // nothing
//                        }
                    }
                    command.success();
                } else if (action.equals("logout")) {
                    MSDKHelper.logout();
                    command.success();
                }
            }
        });

        return true;
    }

    private JSONObject loginReturnResponse(LoginRet loginRet) {
        Logger.d("called");
        Logger.d("ret.flag" + loginRet.flag);
        JSONObject result = new JSONObject();
        try {
            switch (loginRet.flag) {
                case CallbackFlag.eFlag_Succ:
                    result.put("flag", "LOGIN_SUCCESS");
                    // 登陆成功, 读取各种票据
                    result.put("openId", loginRet.open_id);
                    result.put("pf", loginRet.pf);
                    result.put("pfKey", loginRet.pf_key);
                    result.put("platform", loginRet.platform);

                    JSONArray array = new JSONArray();
                    for (TokenRet tr : loginRet.token) {
                        JSONObject objectJson = new JSONObject();
                        switch (tr.type) {
                            case TokenType.eToken_WX_Access:
                                objectJson.put("type", "ACCESS");
                                objectJson.put("accessToken", tr.value);
                                objectJson.put("accessTokenExpire", tr.expiration);
                                break;
                            case TokenType.eToken_WX_Refresh:
                                objectJson.put("type", "REFRESH");
                                objectJson.put("refreshToken", tr.value);
                                objectJson.put("refreshTokenExpire", tr.expiration);
                                break;
                            default:
                                break;
                        }
                        array.put(objectJson);
                    }
                    result.put("token", array);
                    break;
                // 游戏逻辑，对登陆失败情况分别进行处理
                case CallbackFlag.eFlag_NotInWhiteList:
                    result.put("flag", "NOT_IN_WHITE_LIST");
                    break;
                case CallbackFlag.eFlag_Need_Realname_Auth:
                    result.put("flag", "NEED_REALNAME_AUTH");
                    result.put("message", "Require real-name authentication");
                    break;
                case CallbackFlag.eFlag_Login_NetworkErr:
                case CallbackFlag.eFlag_WX_UserCancel:
                case CallbackFlag.eFlag_WX_NotInstall:
                case CallbackFlag.eFlag_WX_NotSupportApi:
                case CallbackFlag.eFlag_WX_LoginFail:
                case CallbackFlag.eFlag_QQ_LoginFail:
                case CallbackFlag.eFlag_Local_Invalid:
                    result.put("flag", "LOGIN_FAILED");
                    result.put("message", loginRet.desc);
                default:
                    break;
            }

            result.put("event", "LOGIN_RETURN");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

}
