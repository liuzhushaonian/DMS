package com.app.legend.dms.hooks;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AndroidAppHelper;
import android.content.Intent;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.legend.dms.model.Char;
import com.app.legend.dms.model.CharInfo;
import com.app.legend.dms.utils.Conf;
import com.app.legend.dms.utils.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * hook漫画详情页面，在此处可以看漫画
 */
public class CartoonInstructionActivityHook extends BaseHook implements IXposedHookLoadPackage {

    private static final String CLASS="com.dmzj.manhua.ui.CartoonInstructionActivity";

    private static final String METHOD="g";

    private static final String HOST="http://dms.legic.xyz:9006";
    private static final String DEBUG="http://192.168.0.4:9006";

    private String author="";
    private String name;
    private String id;
    private String cover;
    private List<String> stringList;
    private Activity activity;
    private Map<String,JSONObject> objectMap;

    private final static int CONNECT_TIMEOUT =60;
    private final static int READ_TIMEOUT=100;
    private final static int WRITE_TIMEOUT=60;
    String description="";

    String status="";
    String letter="";


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

//                    JSONObject jsonObject=new JSONObject();
//
//                    try {
//                        jsonObject.put("ac",o.toString());
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }



                    //获取chapters数组


                    //获取数组内的对象






//                    if (queryData(id)){//查询是否已经存在，是则不反应
//
//                        Toast.makeText(activity, "别点了，这本漫画已经被收录了", Toast.LENGTH_SHORT).show();
//
//                        return;
//
//                    }

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

                    TextView z= (TextView) XposedHelpers.getObjectField(param.thisObject,"z");

                    if (z!=null){

                        status=z.getText().toString();

                    }
                    description= (String) XposedHelpers.getObjectField(o,"description");

                    letter= (String) XposedHelpers.getObjectField(o,"first_letter");


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

                getLocalChar();

            }
        });


//        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, "c", boolean.class, new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//
//                getData();
//
////                Activity activity= (Activity) param.thisObject;
////
////                Window window=activity.getWindow();
////
////                ViewGroup d= (ViewGroup) window.getDecorView();
////
////
////
////                ViewGroup viewGroup= (ViewGroup) d.getChildAt(0);
////
////                ViewGroup viewGroup1= (ViewGroup) viewGroup.getChildAt(1);
////
////                ViewGroup RelativeLayout= (ViewGroup) viewGroup1.getChildAt(0);
////
////                for (int i=0;i<RelativeLayout.getChildCount();i++){
////
////                    XposedBridge.log("child--->>"+RelativeLayout.getChildAt(i).getVisibility());
////
////                }
//
//
//
//            }
//        });


        //在它之后判断漫画是否被下架，如果是，则替换，如果不是，则不替换
        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, "c", boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                Activity activity= (Activity) param.thisObject;
                Intent intent=activity.getIntent();

                if (intent!=null){

                    id=intent.getStringExtra("intent_extra_cid");
                    name=intent.getStringExtra("intent_extra_cname");

                }

                getLocalChar();

                JSONObject object=getObject(id);



//                XposedBridge.log("map--->>>"+objectMap);

                if (object!=null){//表示已收藏该下架漫画，显示出来


//                    XposedBridge.log("show---->>>"+object.toString());
                    XposedHelpers.callMethod(param.thisObject,"a",object,false);

                }

            }
        });


//        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, "a", Object.class, boolean.class, new XC_MethodHook() {
//
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//
//
//
//                boolean f= (boolean) param.args[1];//获取第二个参数，判断是否有本地缓存，如果是false，则不替换
//
//                if (!f){//替换
//
//                    JSONObject object=new JSONObject(json);
//
//                    param.args[0]=object;
//
//                }
//
//            }
//        });


    }


    /**
     * 展示窗口，表明是否上传漫画信息
     * @param activity
     */
    private void showDialog(Activity activity){

        AlertDialog.Builder builder=new AlertDialog.Builder(activity);

        builder.setTitle("上传说明").setMessage("嘿，兄dei，恭喜你发现了新天地。这个封面在点击过后，可以上传这本漫画信息到共享库里，" +
                "共享库里存储着大量被大妈封印的漫画，如果你发现了一本被封印的漫画，欢迎你将这本漫画的信息上传到共享库内，让更多的人能够阅读到这本漫画。" +
                "当然，我并不会获取你的个人信息，上传的信息仅仅是这本漫画的id，名字以及作者，共享库也在GitHub上随时可以查阅。你可以选择上传普通信息或是上传章节信息，上传章节信息会将普通信息也" +
                "一并上传。但是这种上传只限已下架的漫画，如果你发现某一本漫画被下架了，就可以通过这种方式共享已有的章节信息，让被大妈下架的漫画重新被人看到！")
                .setPositiveButton("上传普通信息",(dialog, which) -> {

                    JSONObject jsonObject=new JSONObject();

                    try {
                        jsonObject.put(Conf.COMIC_NAME,name);
                        jsonObject.put(Conf.COMIC_ID,id);
                        jsonObject.put(Conf.AUTHOR,author);
                        jsonObject.put(Conf.COMIC_BOOK,cover);
                        jsonObject.put("first_letter",letter);
                        jsonObject.put("status",status);
                        jsonObject.put("description",description);

                        uploadAndSave(activity,jsonObject);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


        }).setNeutralButton("上传章节信息",(dialog, which) -> {

            try {
                getCharInfo(activity);
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }).setNegativeButton("取消",(dialog, which) -> {

            Toast.makeText(activity, "打扰了，如果以后发现被隐藏的漫画，还可以继续点击封面进行上传哦~", Toast.LENGTH_LONG).show();

        }).show();
    }


    private void uploadAndSave(Activity activity, JSONObject jsonObject){

        OkHttpClient client= new OkHttpClient.Builder()
                .readTimeout(READ_TIMEOUT,TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)//设置写的超时时间
                .connectTimeout(CONNECT_TIMEOUT,TimeUnit.SECONDS)//设置连接超时时间
                .build();

        String url=HOST+"/v1/save-comic-info";


        RequestBody body=RequestBody.create(MediaType.parse("application/json;charset=utf-8"),jsonObject.toString());

        XposedBridge.log(url);

        Request request=new Request.Builder().url(url).post(body).build();

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
//                    String msg = response.body().string();

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

    /**
     * 获取章节信息
     * @param k 传入本hook对象
     */
    private void getCharInfo(Object k) throws JSONException {

        Object o=XposedHelpers.getObjectField(k,"ac");//获取ac

        List list= (List) XposedHelpers.getObjectField(o,"chapters");//获取漫画的大章节信息，一般是连载，单行本，其他或是原作之类的

        JSONObject jsonObject=new JSONObject();//总object

//        List<CharInfo> infos=new ArrayList<>();
//
//        List<Char> charList=new ArrayList<>();

        JSONArray array=new JSONArray();//大章节数组

        JSONArray chars=new JSONArray();//章节数组

        JSONArray authors=new JSONArray();//作者数组

        List authorList= (List) XposedHelpers.getObjectField(o,"authors");//获取作者数组

        for (int l=0;l<authorList.size();l++){

            Object a= authorList.get(l);

            JSONObject author=new JSONObject();

            int id= XposedHelpers.getIntField(a,"tag_id");

            String name= (String) XposedHelpers.getObjectField(a,"tag_name");

            author.put("tag_id",id);
            author.put("tag_name",name);

            authors.put(author);//放入数组


        }

        jsonObject.put("authors",authors);



        for (int i=0;i<list.size();i++){


            Object o1=list.get(i);

            String infoTitle= (String) XposedHelpers.getObjectField(o1,"title");//获取名字

//            CharInfo info=new CharInfo();
//
//            info.setTitle(infoTitle);
//            info.setDmId(id);

            JSONObject in=new JSONObject();

            in.put("title",infoTitle);
            in.put("dmId",id);

            array.put(in);


//          posedBridge.log("jsonObject----->>>>"+o1.toString());

            List l= (List) XposedHelpers.getObjectField(o1,"data");//获取具体章节

//            XposedBridge.log("size--->>>"+l.size());

            for (int p=0;p<l.size();p++){

                Object q=l.get(p);

                String chapter_title= (String) XposedHelpers.getObjectField(q,"chapter_title");//章节名字

//                String title= (String) XposedHelpers.getObjectField(q,"title");

                String charId= (String) XposedHelpers.getObjectField(q,"chapter_id");//章节id

//                XposedBridge.log("charId--->>>"+charId);

                int order=XposedHelpers.getIntField(q,"chapter_order");//章节序号


//                Char c=new Char();
//                c.setChapterOrder(order);//顺序
//                c.setCharId(charId);//章节id
//                c.setDmId(id);//漫画id
//                c.setCharInfoTitle(infoTitle);//大章节名字
//                c.setTitle(chapter_title);

                JSONObject m=new JSONObject();

                m.put("charId",charId);
                m.put("dmId",id);
                m.put("title",chapter_title);
                m.put("charInfoTitle",infoTitle);
                m.put("chapterOrder",order);

                chars.put(m);


//                XposedBridge.log("chapter_title--->>>"+chapter_title);

            }

        }

        //循环完成，将所有信息封装到jsonObject



        jsonObject.put("chars",chars);

        jsonObject.put("charsInfo",array);

        //放入普通信息

        jsonObject.put(Conf.COMIC_NAME,name);
        jsonObject.put(Conf.COMIC_ID,id);
        jsonObject.put(Conf.AUTHOR,author);
        jsonObject.put(Conf.COMIC_BOOK,cover);//

        jsonObject.put("status",status);
        jsonObject.put("description",description);
        jsonObject.put("first_letter",letter);


//        uploadAndSave(activity,jsonObject);//上传

        new Thread(){

            @Override
            public void run() {
                super.run();
                uploadJsonChapterFile(jsonObject);

            }
        }.start();



//        XposedBridge.log("json--->>>"+jsonObject.toString());


    }

    /**
     * 获取本地的章节信息
     *
     */
    private void getLocalChar(){

        if (this.objectMap==null) {


            try {
                File file = new File(AndroidAppHelper.currentApplication().getFilesDir(), "chapter");

                StringBuilder builder = new StringBuilder();


                if (file.exists()) {//文件存在，解析当前文件

                    FileInputStream inputStream = AndroidAppHelper.currentApplication().openFileInput("chapter");

                    InputStreamReader reader = new InputStreamReader(inputStream);
                    BufferedReader buffReader = new BufferedReader(reader);
                    String strTmp = "";
                    while ((strTmp = buffReader.readLine()) != null) {
//                    System.out.println(strTmp);
                        builder.append(strTmp);
                    }

                    String json = builder.toString();//获取json信息
                    this.objectMap = JsonUtil.getJsonObjectList(json);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * 获取本地章节信息
     * @param id 传入id
     * @return 返回json对象
     */
    private JSONObject getObject(String id){

        if (this.objectMap==null||id.equals("0")){

            return null;

        }

        return objectMap.get(id);
    }


    private void uploadJsonChapterFile(JSONObject object){

        File file=new File(AndroidAppHelper.currentApplication().getFilesDir(),"ccc");//生成文件

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(object.toString()); // \r\n即为换行
            out.flush(); // 把缓存区内容压入文件
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        MediaType mediaType = MediaType.parse("text/x-markdown; charset=utf-8");
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(READ_TIMEOUT,TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)//设置写的超时时间
                .connectTimeout(CONNECT_TIMEOUT,TimeUnit.SECONDS)//设置连接超时时间
                .build();

        String url=HOST+"/v1/upload-chapter";

//        XposedBridge.log("url--->>>"+url);

        RequestBody requestBody = RequestBody.create(MediaType.parse("text/x-markdown"), file);


        MultipartBody multipartBody = new MultipartBody.Builder()
                // 设置type为"multipart/form-data"，不然无法上传参数
                .setType(MultipartBody.FORM)
                .addFormDataPart("json", "cc", requestBody)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(multipartBody)
                .build();


        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                e.printStackTrace();

                XposedBridge.log("e---->>>>"+e.toString());

                Runnable runnable= () -> Toast.makeText(activity, "发生了点意外，可能是后端服务器崩了~", Toast.LENGTH_SHORT).show();

                activity.runOnUiThread(runnable);

                file.delete();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {


                Runnable runnable= () -> Toast.makeText(activity, "上传成功", Toast.LENGTH_SHORT).show();

                activity.runOnUiThread(runnable);

                file.delete();

            }
        });
    }


}
