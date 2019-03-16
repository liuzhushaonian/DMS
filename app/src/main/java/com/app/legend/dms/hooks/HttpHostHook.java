package com.app.legend.dms.hooks;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

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

                String h= (String) param.args[0];
                int p= (int) param.args[1];

                XposedBridge.log("host---->>>"+h);
                XposedBridge.log("port---->>>"+p);


            }
        });

    }
}
