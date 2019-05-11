package com.app.legend.dms.hooks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.app.legend.dms.utils.Conf;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainSceneMineEnActivityHook extends BaseHook implements IXposedHookLoadPackage {

    private static final String CLASS="com.dmzj.manhua.ui.mine.activity.MainSceneMineEnActivity";



    private Activity activity;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals(Conf.PACKAGE)){
            return;
        }

        /**
         * findviewbyid，初始化控件的时候
         */
        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, "e", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                activity= (Activity) param.thisObject;

                ViewGroup viewGroup= (ViewGroup) activity.getWindow().getDecorView();


                ViewGroup viewGroup1= (ViewGroup) viewGroup.getChildAt(0);

//                XposedBridge.log("view---->>>"+viewGroup1.getChildAt(1).toString());

                ViewGroup content= (ViewGroup) viewGroup1.getChildAt(1);

                TextView textView=new TextView(activity);

                textView.setGravity(Gravity.CENTER);

                textView.setTextSize(18);

                textView.setText("点击许愿漫画");

                int color=Color.parseColor("#e91e63");

                textView.setTextColor(color);

                content.addView(textView);

                FrameLayout.LayoutParams params=
                        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity= Gravity.BOTTOM;

                int d= (int) getDip(16,activity);

                params.setMargins(d,0,d,d);

                textView.setLayoutParams(params);


                textView.setOnClickListener(v -> {

                    showLog(activity);

                });


            }
        });


    }


    private void showLog(Activity activity){

        AlertDialog.Builder builder=new AlertDialog.Builder(activity);

        builder.setTitle("漫画许愿")
        .setMessage("这里是请求漫画的地方，如果你实在是找不到某一本漫画，可以从这里申请漫画查询，如果release找到了该漫画，将会在下一个周期进行更新，如果找不到，则表示该漫画并不存在。所以请一定将漫画名填写正确！");

        EditText editText=new EditText(activity);
        editText.setHint("漫画名");

        builder.setView(editText).setPositiveButton("上传",(dialog, which) -> {

            String title=editText.getText().toString();
            if (TextUtils.isEmpty(title)){

                Toast.makeText(activity, "不能上传空的内容哦，会给大家带来困扰的", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject object=new JSONObject();
            try {
                object.put("title",title);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String url="/v1/wish?data="+object.toString();

            upload(activity,url);



        }).setNegativeButton("取消",(dialog, which) -> {

            Toast.makeText(activity, "以后有希望看的隐藏漫画可以来这里试试哦~", Toast.LENGTH_SHORT).show();

        }).show();

    }

    private void upload(Activity activity,String url){

        url="http://dms.legic.xyz:9006"+url;

        OkHttpClient client=new OkHttpClient.Builder().build();

        Request request = new Request.Builder()
                //确定下载的范围,添加此头,则服务器就可以跳过已经下载好的部分
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Runnable runnable= () -> {
                    Toast.makeText(activity, "上传失败，也许是服务器崩溃了呢", Toast.LENGTH_SHORT).show();
                };

                activity.runOnUiThread(runnable);

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String s="上传成功";

                try {
                    JSONObject r=new JSONObject(response.body().string());

                    if (r.has(Conf.MSG)) {
                        s = r.getString(Conf.MSG);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String finalS = s;
                Runnable runnable= () -> {
                    Toast.makeText(activity, finalS, Toast.LENGTH_SHORT).show();
                };

                activity.runOnUiThread(runnable);

            }
        });


    }


    private float getDip(int d, Context activity){

        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, d,
                activity.getResources().getDisplayMetrics());

    }
}
