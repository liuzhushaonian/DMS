package com.app.legend.dms.hooks;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.legend.dms.utils.Conf;
import com.app.legend.dms.utils.ZipUtils;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class BrowseActivityAncestorsHook extends BaseHook implements IXposedHookLoadPackage {

    private static final String CLASS="com.dmzj.manhua.ui.BrowseActivityAncestors";

    private Activity activity;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (lpparam.packageName.equals(Conf.PACKAGE)){


            XposedHelpers.findAndHookMethod("com.stub.StubApp", lpparam.classLoader, "attachBaseContext", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    Context context= (Context) param.args[0];

                    classLoader=context.getClassLoader();

                    XposedBridge.log("class--->>>获取成功");

                    init(classLoader);

                }
            });

        }

    }


    @Override
    protected void init(ClassLoader classLoader) {
        /**
         * 偏移底部信息，避免曲屏圆角而无法看到
         */
        XposedHelpers.findAndHookMethod(CLASS, classLoader, "publicFindViews", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                activity= (Activity) param.thisObject;

                TextView textView= (TextView) XposedHelpers.getObjectField(param.thisObject,"page_navigation");

                initOffset(textView);

                textView.setOnLongClickListener(v -> {

                    offsetBottomText(textView);

                    return true;
                });


            }
        });

        XposedHelpers.findAndHookMethod("com.dmzj.manhua.ui.ShareActivity", classLoader,
                "img_share_save_album", new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {

                        if (activity==null){

                            XposedBridge.log("release--->>the activity is null");

                            return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                        }


                        Object currentModel=XposedHelpers.getObjectField(activity,"currentModel");

                        if (currentModel!=null){

                            Object local=XposedHelpers.getObjectField(currentModel,"localWrapper");

                            if (local!=null){//只要有本地图片，则不运行官方方法，避免FC

                                String file= (String) XposedHelpers.getObjectField(local,"file");//zip文件

                                int offsetLocal=XposedHelpers.getIntField(currentModel,"offset_local");

                                String oneName=offsetLocal+".jpg";

                                String name="dmzj-"+System.currentTimeMillis()+".jpg";

                                String savePath= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()+"/"+name;

                                XposedBridge.log("save---->>"+savePath);


                                boolean r= ZipUtils.fixSaveLocalImage(file,oneName,savePath,AndroidAppHelper.currentApplication());

                                if (r){

                                    Toast.makeText(activity, "release修复，图片已保存-->"+savePath, Toast.LENGTH_SHORT).show();

                                }else {

                                    Toast.makeText(activity, "图片没能保存成功，但是release已阻止大妈闪退，请在日志里查找不能保存成功的原因", Toast.LENGTH_SHORT).show();

                                }

                                return null;

                            }

                        }


                        return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                    }
                });
    }


    private float getDip(int d, Context activity){

        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, d,
                activity.getResources().getDisplayMetrics());

    }

    private void offsetBottomText(TextView textView){

        if (textView==null){
            XposedBridge.log("the textview is null!!");
            return;
        }

        SharedPreferences sharedPreferences=AndroidAppHelper.currentApplication().getSharedPreferences(Conf.SHARED,Context.MODE_PRIVATE);

        int type=sharedPreferences.getInt("ttt",10);


        switch (type){

            case 10://原位，偏移

                FrameLayout.LayoutParams layoutParams= (FrameLayout.LayoutParams) textView.getLayoutParams();

                layoutParams.rightMargin= (int) getDip(30, AndroidAppHelper.currentApplication());//向左偏移30dp，避免曲屏看不全

                textView.setLayoutParams(layoutParams);

                sharedPreferences.edit().putInt("ttt",20).apply();

//                Toast.makeText(activity, "长按可恢复原位", Toast.LENGTH_SHORT).show();

                break;


            case 20://偏移，复原

                FrameLayout.LayoutParams layoutParams1= (FrameLayout.LayoutParams) textView.getLayoutParams();

                layoutParams1.rightMargin= 0;//恢复

                textView.setLayoutParams(layoutParams1);

                sharedPreferences.edit().putInt("ttt",10).apply();

//                Toast.makeText(activity, "长按可偏移", Toast.LENGTH_SHORT).show();

                break;

        }


    }

    /**
     * 初始化
     * @param textView
     */
    private void initOffset(TextView textView){

        SharedPreferences sharedPreferences=AndroidAppHelper.currentApplication().getSharedPreferences(Conf.SHARED,Context.MODE_PRIVATE);

        int type=sharedPreferences.getInt("ttt",10);

        if (type==20){//需要偏移

            FrameLayout.LayoutParams layoutParams= (FrameLayout.LayoutParams) textView.getLayoutParams();

            layoutParams.rightMargin= (int) getDip(30, AndroidAppHelper.currentApplication());//向左偏移30dp，避免曲屏看不全

            textView.setLayoutParams(layoutParams);

        }

    }

}
