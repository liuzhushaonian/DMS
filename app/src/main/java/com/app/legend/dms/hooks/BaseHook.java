package com.app.legend.dms.hooks;

import android.content.Context;

import com.app.legend.dms.utils.Conf;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class BaseHook {

    protected ClassLoader classLoader;


//    @Override
//    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
//
//        if (lpparam.packageName.equals(Conf.PACKAGE)){
//
//
//            XposedHelpers.findAndHookMethod("com.stub.StubApp", lpparam.classLoader, "attachBaseContext", Context.class, new XC_MethodHook() {
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    super.afterHookedMethod(param);
//
//                    Context context= (Context) param.args[0];
//
//                    classLoader=context.getClassLoader();
//
//                    XposedBridge.log("class--->>>获取成功");
//
//                    init(classLoader);
//
//                }
//            });
//
//        }
//
//    }

    protected void init(ClassLoader classLoader){


    }

}
