package com.app.legend.dms.hooks;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import dalvik.system.PathClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * from https://github.com/shuihuadx/XposedHook
 * 测试使用
 * 3Q
 */

public class HookLoader implements IXposedHookLoadPackage {


    //按照实际使用情况修改下面几项的值
    /**
     * 当前Xposed模块的包名,方便寻找apk文件
     */
    private final String modulePackage = "com.app.legend.dms";
    /**
     * 宿主程序的包名(允许多个),过滤无意义的包名,防止无意义的apk文件加载
     */
    private static List<String> hostAppPackages = new ArrayList<>();


    /**
     *
     *
     *
     */


    private static List<String> handleHookClasses=new ArrayList<>();

    private static List<String> findAndHookConstructorList=new ArrayList<>();



    private static void init(String modulePackage){

        handleHookClasses.add(modulePackage);

    }

    private static void addConstruct(String modulePackage){

        findAndHookConstructorList.add(modulePackage);

    }

    static {
        // TODO: Add the package name of application your want to hook!
        hostAppPackages.add("com.dmzj.manhua");

        init(CartoonInstructionActivityHook.class.getName());

        init(SplashAdHook.class.getName());

        init(MainSceneCartoonActivityHook.class.getName());
        init(HideFragmentHook.class.getName());

        init(MainSceneMineEnActivityHook.class.getName());
//        init(CartoonDescriptionCommentPackHook.class.getName());

        init(MineCartoonDownActivityHook .class.getName());

//        init(DownLoadLoadingActivityHook.class.getName());

        init(BrowseActivityAncestorsHook.class.getName());

        init(CartoonInstrctionListActivityHook.class.getName());

//        init(HttpHostHook.class.getName());
//        init();

//        addConstruct(HttpHostHook.class.getName());
    }

    /**
     * 实际hook逻辑处理类
     */


    /**
     * 实际hook逻辑处理类的入口方法
     */
    private final String handleHookMethod = "handleLoadPackage";

    private final String handelConstructorMethod="findAndHookConstructor";//hook构造方法的类

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (hostAppPackages.contains(loadPackageParam.packageName)) {
            //将loadPackageParam的classloader替换为宿主程序Application的classloader,解决宿主程序存在多个.dex文件时,有时候ClassNotFound的问题
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Context context=(Context) param.args[0];
                    loadPackageParam.classLoader = context.getClassLoader();

                    //普通的hook方法的
                    for (String handleHookClass:handleHookClasses) {

                        invokeHandleHookMethod(context, modulePackage, handleHookClass, handleHookMethod, loadPackageParam);
                    }

                    //hook构造方法的
//                    for (String s:findAndHookConstructorList){
//
//                        invokeHandleHookMethod(context, modulePackage,s, handelConstructorMethod, loadPackageParam);
//                    }

                }
            });
        }
    }

    /**
     * 安装app以后，系统会在/data/app/下备份了一份.apk文件，通过动态加载这个apk文件，调用相应的方法
     * 这样就可以实现，只需要第一次重启，以后修改hook代码就不用重启了
     * @param context context参数
     * @param modulePackageName 当前模块的packageName
     * @param handleHookClass   指定由哪一个类处理相关的hook逻辑
     * @param loadPackageParam  传入XC_LoadPackage.LoadPackageParam参数
     * @throws Throwable 抛出各种异常,包括具体hook逻辑的异常,寻找apk文件异常,反射加载Class异常等
     */
    private void invokeHandleHookMethod(Context context,String modulePackageName, String handleHookClass, String handleHookMethod, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
//        File apkFile = findApkFileBySDK(modulePackageName);//会受其它Xposed模块hook 当前宿主程序的SDK_INT的影响
//        File apkFile = findApkFile(modulePackageName);
        //原来的两种方式不是很好,改用这种新的方式
        File apkFile=findApkFile(context,modulePackageName);
        if (apkFile==null){
            throw new RuntimeException("寻找模块apk失败");
        }
        //加载指定的hook逻辑处理类，并调用它的handleHook方法
        PathClassLoader pathClassLoader = new PathClassLoader(apkFile.getAbsolutePath(), ClassLoader.getSystemClassLoader());
        Class<?> cls = Class.forName(handleHookClass, true, pathClassLoader);
        Object instance = cls.newInstance();
        Method method = cls.getDeclaredMethod(handleHookMethod, XC_LoadPackage.LoadPackageParam.class);
        method.invoke(instance, loadPackageParam);
    }

    /**
     * 根据包名构建目标Context,并调用getPackageCodePath()来定位apk
     * @param context context参数
     * @param modulePackageName 当前模块包名
     * @return return apk file
     */
    private File findApkFile(Context context,String modulePackageName){
        if (context==null){
            return null;
        }
        try {
            Context moudleContext = context.createPackageContext(modulePackageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            String apkPath=moudleContext.getPackageCodePath();
            return new File(apkPath);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}