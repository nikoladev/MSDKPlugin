package com.tencent.tmgp.luckykat.skychasers;


import android.app.Activity;

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
                    } catch (JSONException e) {
                        PluginResult dataResult = new PluginResult(PluginResult.Status.ERROR);
                        dataResult.setKeepCallback(true);
                        command.sendPluginResult(dataResult);
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
                    PluginResult dataResult = new PluginResult(PluginResult.Status.OK);
                    dataResult.setKeepCallback(true);
                    command.sendPluginResult(dataResult);

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
                        try {
                            boolean scanCodeLogin = args.getBoolean(0);
                            if (scanCodeLogin) {
                                MSDKHelper.wechatScanCodeLogin();
                            } else {
                                MSDKHelper.wechatNormalLogin();
                            }
                        } catch (JSONException e) {
                            // nothing
                        }
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
                    result.put("platformName", getPlatformName(loginRet.platform));

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
                    result.put("flag", "LOGIN_NETWORK_ERROR");
                    result.put("message", loginRet.desc);
                    break;
                case CallbackFlag.eFlag_WX_UserCancel:
                    result.put("flag", "WX_USER_CANCEL");
                    result.put("message", loginRet.desc);
                    break;
                case CallbackFlag.eFlag_WX_NotInstall:
                    result.put("flag", "WX_NOT_INSTALL");
                    result.put("message", loginRet.desc);
                    break;
                case CallbackFlag.eFlag_WX_NotSupportApi:
                    result.put("flag", "WX_NOT_SUPPORT_API");
                    result.put("message", loginRet.desc);
                    break;
                case CallbackFlag.eFlag_WX_LoginFail:
                    result.put("flag", "WX_LOGIN_FAIL");
                    result.put("message", loginRet.desc);
                    break;
                case CallbackFlag.eFlag_QQ_LoginFail:
                    result.put("flag", "QQ_LOGIN_FAIL");
                    result.put("message", loginRet.desc);
                    break;
                case CallbackFlag.eFlag_Local_Invalid:
                    result.put("flag", "LOCAL_INVALID");
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

    private String getPlatformName(int platform) {
        if (platform == EPlatform.ePlatform_QQ.val()) {
            return "QQ";
        } else if (platform == EPlatform.ePlatform_QQHall.val()) {
            return "QQHall";
        } else if (platform == EPlatform.ePlatform_Weixin.val()) {
            return "WX";
        } else {
            return "NONE";
        }
    }

}
