package com.app.legend.dms.hooks;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 测试使用
 */
public class HttpHostHook extends BaseHook implements IXposedHookLoadPackage {

    private static final String CLASS="cz.msebera.android.httpclient.l";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("com.dmzj.manhua")){
            return;
        }

        XposedHelpers.findAndHookConstructor(CLASS, lpparam.classLoader, String.class, int.class, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);

//                String h= (String) param.args[0];
                int p= (int) param.args[1];
//                XposedBridge.log("port---->>>"+p);

                if (p!=-1){//检测到原APP访问网络时会有端口为-1的情况，那个应该是不走代理的，故不做改变
                    param.args[0]="192.168.0.6";
                    param.args[1]=9001;
                }
//                XposedBridge.log("host---->>>"+h);



            }
        });

    }
}
