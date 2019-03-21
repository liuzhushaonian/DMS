package com.app.legend.dms.hooks;

import java.lang.reflect.Method;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 去除启动页广告
 * 启动页广告有两处method，统一替换然后直接执行启动
 * 我特么的发现居然还有其他地方是显示启动页广告的，不按套路出牌啊，鸡蛋不能全放在一个篮子里的意思？
 */
public class SplashAdHook extends BaseHook implements IXposedHookLoadPackage {

    private static final String CLASS = "com.lt.adv.b.b";
    private static final String METHOD1 = "b";
    private static final String METHOD2 = "d";

    private static final String CLASS2 = "com.lt.adv.a";


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("com.dmzj.manhua")) {
            return;
        }

        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, METHOD1, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {

                Class<?> c = lpparam.classLoader.loadClass(CLASS2);

                Method a=c.getDeclaredMethod("a");

                Object o=a.invoke(null);//运行a静态方法，获取实例

                Method method = c.getDeclaredMethod("b", int.class, String.class);
                method.invoke(o, -1, "Inter onADClosed");




                return null;
            }
        });

        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, METHOD2, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {

                Class<?> c = lpparam.classLoader.loadClass(CLASS2);

                Method a=c.getDeclaredMethod("a");

                Object o=a.invoke(null);//运行a静态方法，获取实例

                Method method = c.getDeclaredMethod("b", int.class, String.class);
                method.invoke(o, -1, "Inter onADClosed");



                return null;
            }
        });


        XposedHelpers.findAndHookMethod("com.lt.adv.b.a", lpparam.classLoader, "e", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {

                Class<?> c = lpparam.classLoader.loadClass(CLASS2);

                Method a=c.getDeclaredMethod("a");

                Object o=a.invoke(null);//运行a静态方法，获取实例

                Method method = c.getDeclaredMethod("b", int.class, String.class);
                method.invoke(o, -1, "Inter onADClosed");


                return null;
            }
        });


    }



}
