package com.app.legend.dms.hooks;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import android.support.annotation.WorkerThread;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.app.legend.dms.adapter.HideAdapter;
import com.app.legend.dms.model.HideComic;
import com.app.legend.dms.utils.Conf;
import com.app.legend.dms.utils.FileUtil;
import com.app.legend.dms.utils.JsonUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

import java.io.InputStreamReader;

import java.lang.reflect.Method;

import java.util.List;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 最邪恶之hook——鸠占鹊巢
 */
public class HideFragmentHook extends BaseHook implements IXposedHookLoadPackage {

    private static final String CLASS="com.dmzj.manhua.g.a";

    private static final String CLASS2="com.dmzj.manhua.ui.MainSceneCartoonActivity$1";//hook OnPageChangeListener

    private boolean isHook=false;//标记是否被hook，用于判断是否占有这个class

    private ViewGroup decorView;//获取整个Fragment的viewGroup

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private List<HideComic> hideComicList;
    private HideAdapter adapter;
    private ClassLoader loader;
    private SwipeRefreshLayout swipeRefreshLayout;


    private Activity activity;//上帝对象context

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals(Conf.PACKAGE)){
            return;
        }


        /*hook布局，一开始将布局给替换*/
        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, "b", LayoutInflater.class, ViewGroup.class, Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                decorView= (ViewGroup) param.getResult();//获取返回的view

                XposedBridge.log("info---->>>>"+decorView);

                XposedBridge.log("view----->>>"+param.getResult());

//                clean();
//                initRecyclerView();
                resume();

            }
        });


        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, "a", Object.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {

                XposedBridge.log("obj----->>>>"+param.args[0]);

                XposedBridge.log("instance---->>>>"+param.thisObject);

//                int s= (int) param.args[0];
//
//                if (param.args[0] instanceof Integer)

                if (param.args[0] instanceof String){//占巢成功，开始清空
                    isHook=true;

                    XposedBridge.log("isHook---->>>hook");


                    return null;
                }else {

                    isHook=false;

                    return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);

                }

            }
        });

        /**/
        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, "a", boolean.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {


                if (isHook) {
                    return null;
                }else {
                    return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                }
            }
        });

        /*获取上帝对象*/
        XposedHelpers.findAndHookMethod("com.dmzj.manhua.ui.MainSceneCartoonActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                activity= (Activity) param.thisObject;
            }
        });

        /*监听滑动，在需要的地方切换isHook*/
        XposedHelpers.findAndHookMethod(CLASS2, lpparam.classLoader, "onPageSelected", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);

                int i= (int) param.args[0];

                if (i==3){//不hook
                    isHook=false;

                    resume();

                    XposedBridge.log("ishook--->>>"+isHook);

                }else {
                    isHook=true;
                }

                if (i==1){
                    initRecyclerView();

                    loader=lpparam.classLoader;

                }

            }
        });



    }

    /*清除里面所有view*/
    private void clean(){

        if (this.decorView!=null&&isHook){

            decorView.removeAllViews();//清除所有view

        }else {


        }

    }

    private void initRecyclerView(){

        if (isHook&&swipeRefreshLayout!=null&&swipeRefreshLayout.getVisibility()==View.GONE){
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        }else if (activity!=null&&decorView!=null&&isHook){

            swipeRefreshLayout=new SwipeRefreshLayout(activity);
            RelativeLayout.LayoutParams params1= new
                    RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            swipeRefreshLayout.setLayoutParams(params1);


            this.recyclerView=new RecyclerView(activity);
            recyclerView.setBackgroundColor(Color.WHITE);

//            decorView.removeAllViews();
            this.decorView.addView(swipeRefreshLayout);//添加到里面来

            swipeRefreshLayout.addView(recyclerView);

            RelativeLayout.LayoutParams params= new
                    RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            recyclerView.setLayoutParams(params);

            linearLayoutManager=new LinearLayoutManager(activity);
            adapter=new HideAdapter();
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(adapter);

            /*item的点击事件，将title与id传到漫画详情页面，从而达到打开漫画的目的*/
            adapter.setListener(comic -> {

                openComicActivity(comic.getId(),comic.getTitle());


            });

            swipeRefreshLayout.setColorSchemeResources(
                    android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);



            swipeRefreshLayout.setOnRefreshListener(this::refreshData);
        }

        if (isHook) {

            initData();//初始化数据
        }

    }

    /**
     * 恢复
     */
    private void resume(){

        if (decorView!=null&&!isHook&&recyclerView!=null){

//            decorView.removeView(recyclerView);//恢复

            swipeRefreshLayout.setVisibility(View.GONE);

        }

    }

    /**
     * 获取数据
     */
    private void initData(){

        new Thread(){

            @Override
            public void run() {
                super.run();
                openFileAndGetDate();

            }
        }.start();

    }

    @WorkerThread
    private void openFileAndGetDate(){

        try {

            File file=new File(activity.getFilesDir(),"comic");

            StringBuilder builder=new StringBuilder();

            if (file.exists()) {//文件存在，解析当前文件

                FileInputStream inputStream = activity.openFileInput("comic");

                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader buffReader = new BufferedReader(reader);
                String strTmp = "";
                while((strTmp = buffReader.readLine())!=null){
//                    System.out.println(strTmp);
                    builder.append(strTmp);
                }

                String json=builder.toString();//获取json信息

                XposedBridge.log("json---->>>"+json);

                List<HideComic> hideComics= JsonUtil.getList(json);

//                handler.obtainMessage(100,hideComics).sendToTarget();

                setData(hideComics);


            }else {//文件不存在，下载后继续

                FileUtil.createFile(activity,"comic",true);
                FileUtil.downloadFileByOne(activity);
                openFileAndGetDate();
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void setData(List<HideComic> list){

        if (activity==null){
            return;
        }

        Runnable runnable= () -> {
            hideComicList=list;
            adapter.setHideComicList(hideComicList);
            swipeRefreshLayout.setRefreshing(false);
        };

        activity.runOnUiThread(runnable);



    }



    /*打开漫画详情页面*/
    private void openComicActivity(String id,String title){

        try {
            Class<?> c = loader.loadClass("com.dmzj.manhua.beanv2.AppBeanUtils");

            Method method = c.getDeclaredMethod("b", Activity.class,String.class,String.class);
            method.invoke(null,activity,id,title);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 刷新数据
     * 如果没网就不动
     */
    private void refreshData(){


        FileUtil.deleteFile(activity,"comic");

        initData();


    }


}
