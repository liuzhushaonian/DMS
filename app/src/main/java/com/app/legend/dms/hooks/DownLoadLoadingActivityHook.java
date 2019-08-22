package com.app.legend.dms.hooks;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.app.legend.dms.model.HideComic;
import com.app.legend.dms.utils.Conf;
import com.app.legend.dms.utils.JsonUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 废弃该class
 * 该class原本作用好像是记录下载的漫画并显示，但大妈封杀后连资源都不允许下载，该class失去了原本的作用，外加上会影响原本的下载，所以去掉
 */

@Deprecated
public class DownLoadLoadingActivityHook extends BaseHook implements IXposedHookLoadPackage {

    private static final String CLASS = "com.dmzj.manhua.download.DownLoadLoadingActivity";

    private Activity activity;

    private SQLiteDatabase sqLiteDatabase;

    private List<HideComic> hideComicList;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals(Conf.PACKAGE)) {
            return;
        }

        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, "initData", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                activity = (Activity) param.thisObject;

                initList();

                Intent intent=activity.getIntent();

                String id=intent.getStringExtra("intent_extra_commic_id");

                insertData(id);

            }
        });


        /**
         * 获取sqLiteDatabase实例，操作数据库
         */
        XposedHelpers.findAndHookConstructor("com.dmzj.manhua.dbabst.db.DownLoadWrapperTable",
                lpparam.classLoader, "com.dmzj.manhua.dbabst.AbstractDBHelper", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                        Object object = param.args[0];

                        sqLiteDatabase = (SQLiteDatabase) XposedHelpers.callMethod(object, "getWritableDatabase");


                    }
                });


    }


    private void insertData(String id){

        if (sqLiteDatabase==null){
            return;
        }

        String select="select remoteid from bookinfo where remoteid = '"+id+"' limit 1";

        Cursor cursor=sqLiteDatabase.rawQuery(select,null);

        if (cursor!=null){

            int count=cursor.getCount();

            if (count==0){//表示无记录


                HideComic comic=isDelete(id);

                if (comic!=null) {

                    String sql = "insert into bookinfo (islong,remoteid,title,cover,authors,description) values ('2','"+id+"','"+comic.getTitle()+"','"+comic.getBookLink()+"','"+comic.getAuthor()+"','1')";

                    sqLiteDatabase.execSQL(sql);

                }

            }

            cursor.close();
        }


    }

    private HideComic isDelete(String id){

        if (hideComicList==null){
            return null;
        }

        for (HideComic comic:hideComicList){

            if (comic.getId().equals(id)&&comic.getDelete()==1){
                return comic;
            }

        }

        return null;


    }

    private void initList(){

        if (hideComicList!=null){
            return;
        }

        File file = new File(activity.getFilesDir(), "comic");

        StringBuilder builder = new StringBuilder();

        try {
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

                hideComicList= JsonUtil.getList(json);//获取id_list

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
