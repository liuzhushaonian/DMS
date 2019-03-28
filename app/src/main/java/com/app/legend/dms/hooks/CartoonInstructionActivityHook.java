package com.app.legend.dms.hooks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.app.legend.dms.utils.Conf;
import com.app.legend.dms.utils.JsonUtil;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * hook漫画详情页面，在此处可以看漫画
 */
public class CartoonInstructionActivityHook extends BaseHook implements IXposedHookLoadPackage {

    private static final String CLASS="com.dmzj.manhua.ui.CartoonInstructionActivity";

    private static final String METHOD="g";

    private static final String HOST="http://dms.legic.xyz:9006";

    private String author="";
    private String name;
    private String id;
    private String cover;
    private List<String> stringList;
    private Activity activity;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals(Conf.PACKAGE)){
            return;
        }

        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, METHOD, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                Activity activity= (Activity) param.thisObject;
                Intent intent=activity.getIntent();

                if (intent!=null){

                    id=intent.getStringExtra("intent_extra_cid");
                    name=intent.getStringExtra("intent_extra_cname");

                }

                ImageView book= (ImageView) XposedHelpers.getObjectField(param.thisObject,"u");//获取封面imageview

                book.setOnClickListener(v -> {

                    if (activity==null){
                        return;
                    }

                    if (queryData(id)){//查询是否已经存在，是则不反应

                        Toast.makeText(activity, "别点了，这本漫画以及被收录了", Toast.LENGTH_SHORT).show();

                        return;

                    }

                    //避免id为0
                    if (id.equals("0")){
                        Toast.makeText(activity, "id不可以为0哦~", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Object o=XposedHelpers.getObjectField(param.thisObject,"ac");//获取ac

                    if (o!=null) {
                        cover = (String) XposedHelpers.getObjectField(o, "cover");
                        name= (String) XposedHelpers.getObjectField(o,"title");

                    }

                    TextView t = (TextView) XposedHelpers.getObjectField(param.thisObject,"v");

                    if (t!=null) {
                        author = t.getText().toString();
                    }

                    showDialog(activity);

                });


            }
        });

        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, "onResume", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                activity= (Activity) param.thisObject;

                initList();//实例化

            }
        });


    }


    /**
     * 展示窗口，表明是否上传漫画信息
     * @param activity
     */
    private void showDialog(Activity activity){

        AlertDialog.Builder builder=new AlertDialog.Builder(activity);



        builder.setTitle("上传说明").setMessage("嘿，兄dei，恭喜你发现了新天地。这个封面在点击过后，可以上传这本漫画信息到共享库里，" +
                "共享库里存储着大量被大妈封印的漫画，如果你发现了一本被封印的漫画，欢迎你将这本漫画的信息上传到共享库内，让更多的人能够阅读到这本漫画。" +
                "当然，我并不会获取你的个人信息，上传的信息仅仅是这本漫画的id，名字以及作者，共享库也在GitHub上随时可以查阅。\n" +
                "id：" +
                id +"\n"+
                "漫画名称：" +
                name +"\n" +
                "作者：" +
                author+"\n" +
                "封面链接：" +
                cover)
                .setPositiveButton("确认无误，上传吧",(dialog, which) -> {

                    JSONObject jsonObject=new JSONObject();

                    try {
                        jsonObject.put(Conf.COMIC_NAME,name);
                        jsonObject.put(Conf.COMIC_ID,id);
                        jsonObject.put(Conf.AUTHOR,author);
                        jsonObject.put(Conf.COMIC_BOOK,cover);

                        uploadAndSave(activity,jsonObject);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


        }).setNegativeButton("取消",(dialog, which) -> {

            Toast.makeText(activity, "打扰了，如果以后发现被隐藏的漫画，还可以继续点击封面进行上传哦~", Toast.LENGTH_LONG).show();

        }).show();
    }


    private void uploadAndSave(Activity activity, JSONObject jsonObject){

        OkHttpClient client=new OkHttpClient();

        String url=HOST+"/v1/save-comic-info?data="+jsonObject.toString();

        Request request=new Request.Builder().url(url).method("GET",null).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

//                Toast.makeText(activity, "发生了点意外，可能是后端服务器崩了~", Toast.LENGTH_SHORT).show();

                e.printStackTrace();

                Runnable runnable= () -> Toast.makeText(activity, "发生了点意外，可能是后端服务器崩了~", Toast.LENGTH_SHORT).show();

                activity.runOnUiThread(runnable);

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {


                if (response.body()!=null) {
                    String msg = response.body().string();

                    Runnable runnable = () -> Toast.makeText(activity, "上传成功，感谢你的分享~", Toast.LENGTH_SHORT).show();

                    activity.runOnUiThread(runnable);

//                Toast.makeText(activity, ""+msg, Toast.LENGTH_SHORT).show();
                }

            }
        });


    }





    /**
     * 查询该漫画是否已经存在
     */
    private boolean queryData(String id){

        boolean result=false;

        if (this.stringList!=null){

            for (String s:stringList){

                if (s.equals(id)){
                    result=true;
                    break;
                }

            }


        }

        return result;

    }

    /**
     *
     */
    private void initList() {
        if (stringList == null) {
            stringList = new ArrayList<>();

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

                    stringList=JsonUtil.getIdList(json);//获取id_list

                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }


}
