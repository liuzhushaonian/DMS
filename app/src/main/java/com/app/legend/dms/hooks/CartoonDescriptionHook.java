package com.app.legend.dms.hooks;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CartoonDescriptionHook extends BaseHook implements IXposedHookLoadPackage {

    private static final String CLASS="com.dmzj.manhua.beanv2.CartoonDescription";

    private static final String METHOD="getDescription";


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("com.dmzj.manhua")){
            return;
        }

        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, METHOD, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                String title= (String) XposedHelpers.getObjectField(param.thisObject,"title");

                String copyright= (String) XposedHelpers.getObjectField(param.thisObject,"copyright");

                int is_dmzj=XposedHelpers.getIntField(param.thisObject,"is_dmzj");

                XposedBridge.log("dms---title--->>>"+title);
                XposedBridge.log("dms---copyright--->>>"+copyright);
                XposedBridge.log("dms---is_dmzj--->>>"+is_dmzj);

            }
        });



    }
}
