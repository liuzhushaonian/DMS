package com.app.legend.dms.hooks;


import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.legend.dms.utils.Conf;
import com.app.legend.dms.utils.FileUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * hook主界面
 *
 */
public class MainSceneCartoonActivityHook extends BaseHook implements IXposedHookLoadPackage {

    private static final String CLASS="com.dmzj.manhua.ui.MainSceneCartoonActivity$4";
    private static final String METHOD="a";//有个入参，类型是int

    private static final String CLASS2="com.dmzj.manhua.ui.MainSceneCartoonActivity";

    private static final String CLASS3="com.dmzj.manhua.ui.MainSceneCartoonActivity$a";


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(Conf.PACKAGE)){
            return;
        }
        /*添加封印界面*/
        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, METHOD, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);

                String arrays[]=new String[]{"推荐","封印","更新","分类","排行","专题"};

                Class<?> c=lpparam.classLoader.loadClass(CLASS);

                Field f=c.getDeclaredField("a");

                f.setAccessible(true);

                f.set(param.thisObject,arrays);//赋值





            }
        });

        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, "a",new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                param.setResult(6);
            }
        });

        /* 将游戏图标去掉
        * 仔细想过了，还是不去掉，我的目的不是断大妈财路
        *
        * */
        XposedHelpers.findAndHookMethod(CLASS2, lpparam.classLoader, "p", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

//                View s= (View) XposedHelpers.getObjectField(param.thisObject,"s");
//                if (s!=null){
//                    s.setVisibility(View.GONE);
//                }

                Activity activity= (Activity) param.thisObject;

                FileUtil.createFile(activity,"comic",false);
                FileUtil.createFile(activity,"chapter",false);

                FileUtil.downloadFile(activity);

            }
        });


        /*替换原有的获取操作，当然是在入参为1的时候替换，其他时候不动*/
        XposedHelpers.findAndHookMethod(CLASS3, lpparam.classLoader, "getItem", int.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {

                int i= (int) param.args[0];//获取入参

                if (i==1){//第2个页面

                    Object o=null;

                    try {

                        Class<?> clazz=lpparam.classLoader.loadClass("com.dmzj.manhua.g.a");

                        o=clazz.newInstance();
                        Method method=clazz.getDeclaredMethod("a",Object.class);

                        method.setAccessible(true);

                        method.invoke(o,"pp");

                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        System.out.println("此处接收被调用方法内部未被捕获的异常");
                        Throwable t = e.getTargetException();// 获取目标异常
                        t.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }

                    return o;
                }else if (i>1){//从第2个页面之后的页面，入参都要减1

                    param.args[0]=i-1;

                    return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                }else {//第一个页面，不动


                    return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                }
            }
        });

        /*将返回改为6*/
        XposedHelpers.findAndHookMethod(CLASS3, lpparam.classLoader, "getCount", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                param.setResult(6);

            }
        });


    }


}
