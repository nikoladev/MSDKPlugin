package com.techstorm;

import com.tencent.msdk.api.LoginRet;
import com.tencent.msdk.api.MsdkBaseInfo;
import com.tencent.msdk.api.WGPlatform;
import com.tencent.msdk.api.WGPlatformObserver;
import com.tencent.msdk.api.WGQZonePermissions;
import com.tencent.msdk.consts.CallbackFlag;
import com.tencent.msdk.consts.EPlatform;

import android.app.Activity;

public class MSDKHelper {

    public static LoginRet getLoginRecord() {
        LoginRet ret = new LoginRet();
        WGPlatform.WGGetLoginRecord(ret);
        return ret;
    }

    public static EPlatform getPlatform() {
        LoginRet ret = new LoginRet();
        WGPlatform.WGGetLoginRecord(ret);
        if (ret.flag == CallbackFlag.eFlag_Succ) {
            return EPlatform.getEnum(ret.platform);
        }
        return EPlatform.ePlatform_None;
    }

    public static void qqLogin() {
        WGPlatform.WGLogin(EPlatform.ePlatform_QQ);
    }

    public static void wechatNormalLogin() {
        WGPlatform.WGLogin(EPlatform.ePlatform_Weixin);
    }

    public static void wechatScanCodeLogin() {
        WGPlatform.WGQrCodeLogin(EPlatform.ePlatform_Weixin);
    }

    public static void logout() {
        WGPlatform.WGLogout();
    }

    public static void initMSDK(MsdkBaseInfo baseInfo, Activity activity, WGPlatformObserver wgplatformObserver) {
// TODO GAME 初始化MSDK
        /***********************************************************
         *  TODO GAME 接入必须要看， baseInfo值因游戏而异，填写请注意以下说明:
         *  	baseInfo值游戏填写错误将导致 QQ、微信的分享，登录失败 ，切记 ！！！
         * 		只接单一平台的游戏请勿随意填写其余平台的信息，否则会导致公告获取失败
         *      offerId 为必填，一般为手QAppId
         ***********************************************************/


        // 注意：传入Initialized的activity即this，在游戏运行期间不能被销毁，否则会产生Crash
        WGPlatform.Initialized(activity, baseInfo);
        // 设置拉起QQ时候需要用户授权的项
        WGPlatform.WGSetPermission(WGQZonePermissions.eOPEN_ALL);

        // 设置java层或c++层回调,如果两层都设置了则会只回调到java层
        WGPlatform.WGSetObserver(wgplatformObserver);



        // TODO GAME 处理游戏被拉起的情况
        // launchActivity的onCreat()和onNewIntent()中必须调用
        // WGPlatform.handleCallback()。否则会造成微信登录无回调
        if (WGPlatform.wakeUpFromHall(activity.getIntent())) {
            // 拉起平台为大厅
            System.out.println("LoginPlatform is Hall");
            System.out.println(activity.getIntent());
        } else {
            // 拉起平台不是大厅
            System.out.println("LoginPlatform is not Hall");
            System.out.println(activity.getIntent());
            WGPlatform.handleCallback(activity.getIntent());
        }
    }

}
