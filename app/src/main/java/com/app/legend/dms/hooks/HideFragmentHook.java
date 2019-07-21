package com.app.legend.dms.hooks;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.WorkerThread;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
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

    private static final String CLASS = "com.dmzj.manhua.ui.uifragment.CartoonClassifyFragment";

    private static final String CLASS2 = "com.dmzj.manhua.ui.MainSceneCartoonActivity$1";//hook OnPageChangeListener

    private boolean isHook = false;//标记是否被hook，用于判断是否占有这个class

    private ViewGroup decorView;//获取整个Fragment的viewGroup

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private List<HideComic> hideComicList;
    private HideAdapter adapter;
    private ClassLoader loader;
    private SwipeRefreshLayout swipeRefreshLayout;

    private EditText editText;//搜索框

    private LinearLayout linearLayout;


    private boolean refresh = false;

    private Activity activity;//上帝对象context

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals(Conf.PACKAGE)) {
            return;
        }


        /*hook布局，一开始将布局给替换*/
        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, "createContent",
                LayoutInflater.class, ViewGroup.class, Bundle.class,
                new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                decorView = (ViewGroup) param.getResult();//获取返回的view



            }
        });


        /**
         * 占巢
         * 接收对象开始
         */
        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, "analysisData", Object.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {


                if (param.args[0] instanceof String) {//占巢成功，开始清空
                    isHook = true;

                    return null;
                } else {

                    isHook = false;

                    return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);

                }

            }
        });

        /**
         *
         */
        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, "loadData", boolean.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {


                if (isHook) {
                    return null;
                } else {
                    return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
                }
            }
        });

        /*获取上帝对象*/
        XposedHelpers.findAndHookMethod("com.dmzj.manhua.ui.MainSceneCartoonActivity",
                lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                activity = (Activity) param.thisObject;
            }
        });

        /*监听滑动，在需要的地方切换isHook*/
        XposedHelpers.findAndHookMethod(CLASS2, lpparam.classLoader, "onPageSelected",
                int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);

                int i = (int) param.args[0];

                if (i == 3) {//不hook
                    isHook = false;

                    resume();

//                    XposedBridge.log("ishook--->>>"+isHook);

                } else {
                    isHook = true;
                }

                if (i == 1) {
//                    initRecyclerView();

                    initView();//初始化view

                    loader = lpparam.classLoader;

                }
            }
        });


    }

    /**
     * 初始化view
     */
    private void initView() {


        decorView.removeView(linearLayout);

        linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);//垂直布局

        linearLayout.setBackgroundColor(Color.WHITE);//放置白色背景避免看到底部

        this.decorView.removeView(linearLayout);
        this.decorView.addView(linearLayout);

        initEditText();//初始化搜索框
        initRecyclerView();//初始化列表


//        }

    }

    private void initRecyclerView() {


        if (activity != null && decorView != null && isHook) {

            swipeRefreshLayout = new SwipeRefreshLayout(activity);
            RelativeLayout.LayoutParams params1 = new
                    RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

//            params1.topMargin= (int) getDip(48,activity);//顶部有个72dp的margin

            swipeRefreshLayout.setLayoutParams(params1);


            this.recyclerView = new RecyclerView(activity);
            recyclerView.setBackgroundColor(Color.WHITE);

//            decorView.removeAllViews();
            this.linearLayout.addView(swipeRefreshLayout);//添加到里面来

            swipeRefreshLayout.addView(recyclerView);

            RelativeLayout.LayoutParams params = new
                    RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            recyclerView.setLayoutParams(params);

            linearLayoutManager = new LinearLayoutManager(activity);
            adapter = new HideAdapter();
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(adapter);

            /*item的点击事件，将title与id传到漫画详情页面，从而达到打开漫画的目的*/
            adapter.setListener(comic -> {

                openComicActivity(comic.getId(), comic.getTitle());


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
    private void resume() {

        if (linearLayout != null) {

//            decorView.removeView(recyclerView);//恢复

            linearLayout.setVisibility(View.GONE);

        }

    }

    /**
     * 获取数据
     */
    private void initData() {

        new Thread() {

            @Override
            public void run() {
                super.run();
                openFileAndGetDate();

            }
        }.start();

    }

    @WorkerThread
    private void openFileAndGetDate() {

        try {

            File file = new File(activity.getFilesDir(), "comic");

            StringBuilder builder = new StringBuilder();

            if (file.exists()) {//文件存在，解析当前文件

                FileInputStream inputStream = activity.openFileInput("comic");

                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader buffReader = new BufferedReader(reader);
                String strTmp = "";
                while ((strTmp = buffReader.readLine()) != null) {
//                    System.out.println(strTmp);
                    builder.append(strTmp);
                }

                String json = builder.toString();//获取json信息

//                XposedBridge.log("json---->>>"+json);

                List<HideComic> hideComics = JsonUtil.getList(json);

//                handler.obtainMessage(100,hideComics).sendToTarget();

                setData(hideComics);


            } else {//文件不存在，下载后继续

                FileUtil.createFile(activity, "comic", true);
                FileUtil.downloadFileByOne(activity);
                openFileAndGetDate();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void setData(List<HideComic> list) {

        if (activity == null) {
            return;
        }

        Runnable runnable = () -> {
            hideComicList = list;
            adapter.setHideComicList(hideComicList);
            swipeRefreshLayout.setRefreshing(false);

            String hint = "" + hideComicList.size() + "本隐藏漫画|搜索漫画&作者";

            if (editText != null) {
                editText.setHint(hint);
            }

            if (refresh) {
                Toast.makeText(activity, "已刷新最新数据", Toast.LENGTH_SHORT).show();
                refresh = false;
            }

        };

        activity.runOnUiThread(runnable);

    }


    /*打开漫画详情页面*/
    private void openComicActivity(String id, String title) {

        try {
            Class<?> c = loader.loadClass("com.dmzj.manhua.utils.ActManager");

            Method method = c.getDeclaredMethod("startCartoonDescriptionActivity", Activity.class, String.class, String.class);
            method.invoke(null, activity, id, title);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 刷新数据
     * 如果没网就不动
     */
    private void refreshData() {


        refresh = true;

        FileUtil.deleteFile(activity, "comic");
        FileUtil.deleteFile(activity, "chapter");

        initData();


    }

    private void initEditText() {

        if (activity != null) {

            editText = new EditText(activity);
            RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getDip(48, activity));

            editText.setLayoutParams(params);
            linearLayout.removeView(editText);
            linearLayout.addView(editText);

            editText.setHint("搜索漫画&作者");

            /*监听输入*/
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    if (TextUtils.isEmpty(s) && adapter != null) {

                        adapter.resume();

                    } else if (adapter != null && activity != null) {

                        adapter.query(s.toString(), activity);

                    }

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            // editText.setVisibility(View.GONE);

        }

    }

    private float getDip(int d, Context activity) {

        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, d,
                activity.getResources().getDisplayMetrics());

    }

}
