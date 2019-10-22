package com.app.legend.dms.hooks;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.AndroidAppHelper;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.app.legend.dms.model.ExportComic;
import com.app.legend.dms.model.LocalComic;
import com.app.legend.dms.utils.Conf;
import com.app.legend.dms.utils.ZipUtils;
import net.lingala.zip4j.ZipFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 从UI获取需要导出的id
 * 从数据库查询id并导出到SD卡上
 */
public class MineCartoonDownActivityHook extends BaseHook implements IXposedHookLoadPackage {

//    private static final String CLASS = "com.dmzj.manhua.ui.MineCartoonDownActivity";

    private static final String CLASS2 = "com.dmzj.manhua.download.DownLoadManageAbstractActivity";


    private Activity activity;
    private SQLiteDatabase sqLiteDatabase;

    private static final String table = "downloadwrapper";//表的名字

    private static final int ZIP = 0x00100;
    private static final int FOLDER = 0x00300;


    private int CORE_COUNT = Runtime.getRuntime().availableProcessors();
    private int CORE_POOL_SIZE = CORE_COUNT + 1;
    private int CORE_POOL_MAX_SIZE = CORE_COUNT * 2 + 1;

    private ThreadPoolExecutor poolExecutor;




    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals(Conf.PACKAGE)) {
            return;
        }

        /**
         * 获取sqLiteDatabase实例，操作数据库
         */
        XposedHelpers.findAndHookConstructor("com.dmzj.manhua.dbabst.db.DownLoadWrapperTable", lpparam.classLoader,
                "com.dmzj.manhua.dbabst.AbstractDBHelper",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                        Object object = param.args[0];

                        sqLiteDatabase = (SQLiteDatabase) XposedHelpers.callMethod(object, "getWritableDatabase");

//                XposedBridge.log("sql---->>>"+param.args[0].toString());

                    }
                });


        /**
         * 获取头部组件并放入自定义按钮
         * findviewbyid
         */
        XposedHelpers.findAndHookMethod(CLASS2, lpparam.classLoader, "findViews", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                RelativeLayout z = (RelativeLayout) XposedHelpers.getObjectField(param.thisObject, "layout_editstatus_oprations");

                TextView textView = new TextView(AndroidAppHelper.currentApplication());

                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);

                int id = AndroidAppHelper.currentApplication().getResources().getIdentifier("txt_select_shower",
                        "id", lpparam.packageName);

                layoutParams.addRule(RelativeLayout.RIGHT_OF, id);

                layoutParams.leftMargin = (int) getDp(16, AndroidAppHelper.currentApplication());
//                layoutParams.topMargin= (int) getDp(24,AndroidAppHelper.currentApplication());

                textView.setLayoutParams(layoutParams);

                textView.setText("导出");
                textView.setTextColor(Color.RED);
                textView.setTextSize(16);
                textView.setGravity(Gravity.CENTER);
                z.addView(textView, 1);

                activity = (Activity) param.thisObject;

                textView.setOnClickListener(v -> {

                    List o = (List) XposedHelpers.getObjectField(param.thisObject, "manageWrappers");

                    show(o);
                });

                initThreadPoolExecutor();//初始化线程池

            }
        });


    }

    private void show(List list) {

        if (list==null){
            return;
        }

        showDialog(list);

    }

    /**
     * 单zip导出
     *
     * @param list
     */
    private void startExportZip(List list) {

        new Thread() {
            @Override
            public void run() {
                super.run();

                getSelectObject(list, ZIP);

            }
        }.start();

    }

    private void startExportFolder(List list) {

        new Thread() {
            @Override
            public void run() {
                super.run();

                getSelectObject(list, FOLDER);

            }
        }.start();


    }

    private void getSelectObject(List list, int type) {

        List<Object> list1 = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {//遍历所有的

            Object s = list.get(i);
            Map map = (Map) XposedHelpers.getObjectField(s, "mTags");//获取map


            for (Object val : map.values()) {

                if ((boolean) val) {//被选中的

                    list1.add(s);//将选中的放入list内，之后提供下一个方法进行遍历
                }
            }

        }

        getLocalBean(list1, type);


    }


    /**
     * 遍历被选中的list
     *
     * @param list
     */
    private void getLocalBean(List list, int type) {

        List<LocalComic> localComicList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {

            Object s = list.get(i);

            String id = (String) XposedHelpers.getObjectField(s, "id");

            String name = (String) XposedHelpers.getObjectField(s, "name");

            LocalComic localComic = new LocalComic();

            localComic.setId(id);
            localComic.setTitle(name);

            localComic.setExportComicList(queryData(id));//将章节信息填入其中

            localComicList.add(localComic);

            //之后打开Service进行导出
        }

        //遍历完成，封装完成，传入Service下载

        startExport(localComicList, type);


    }


    private List<ExportComic> queryData(String id) {

        if (this.sqLiteDatabase == null) {

            XposedBridge.log("数据库没有实例化");

            return null;
        }

        List<ExportComic> exportComicList = new ArrayList<>();

        String sql = "select localpath,chapterid,title,chapter_title from " + table + " where commic_id = '" + id + "'";

        Cursor cursor = this.sqLiteDatabase.rawQuery(sql, null);

        String p=AndroidAppHelper.currentApplication().getExternalFilesDir(null).getAbsolutePath();

        if (cursor != null) {

            if (cursor.moveToFirst()) {

                do {

                    String path = cursor.getString(cursor.getColumnIndex("localpath"));

                    String chapterId = cursor.getString(cursor.getColumnIndex("chapterid"));
                    String chapter_title = cursor.getString(cursor.getColumnIndex("chapter_title"));
                    String title = cursor.getString(cursor.getColumnIndex("title"));

                    ExportComic comic = new ExportComic();
                    comic.setBigTitle(title);
                    comic.setCharId(chapterId);
                    comic.setCharTitle(chapter_title);
                    comic.setPath(path);

                    exportComicList.add(comic);//添加到里面

                } while (cursor.moveToNext());


            }

            cursor.close();

        }

        return exportComicList;//返回查询结果


    }

    int s=0;


    /**
     * 导出章节并打包为zip
     *
     * @param localComicList 所选的漫画
     */
    private void startExport(List<LocalComic> localComicList, int type) {

        s=0;

        for (LocalComic comic : localComicList) {

            Runnable runnable= () -> {
                threadExport(comic,type,localComicList.size());
            };

            poolExecutor.execute(runnable);
        }

//        Runnable runnable = () -> Toast.makeText(AndroidAppHelper.currentApplication(), "导出完成，本次导出" + localComicList.size() + "本漫画", Toast.LENGTH_LONG).show();
//
//        activity.runOnUiThread(runnable);

//        XposedBridge.log("size---->>" + localComicList.size());

    }

    private void threadExport(LocalComic comic,int type,int size){

        ZipFile zipFile = ZipUtils.createZip(comic.getTitle());

        switch (type) {

            case ZIP:

//                        Log.d("pp---->>>",AndroidAppHelper.currentApplication().getExternalFilesDir(null).getAbsolutePath());

                ZipUtils.addZipFile(zipFile, comic.getExportComicList());
                break;

            case FOLDER:

                ZipUtils.addFolderFile(zipFile, comic.getExportComicList());
                break;
        }

//        XposedBridge.log("export----->>>>"+comic.getTitle()+"thread--->>>"+Thread.currentThread().getName());

        s++;

        if (s>=size){

            Runnable r= () -> {
                Toast.makeText(activity, "导出完成，本次导出"+size+"本漫画", Toast.LENGTH_SHORT).show();
            };

            activity.runOnUiThread(r);

            s=0;//复位
        }

    }




    /**
     * 显示弹窗
     */
    private void showDialog(List list) {

        if (activity == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle("导出漫画")
                .setMessage("导出漫画功能仅能导出已下载到本地的漫画，" +
                        "release提供单话打包zip导出以及单话解压为文件夹两种方式导出，" +
                        "最后都会打包到漫画的zip包下，这个漫画zip包在sdcard/release文件夹下\n" +
                        "建议单话zip导出，因为单话文件夹导出需要多一份的解压以及删除操作，会消耗更长的时间")
                .setPositiveButton("zip导出", (dialog, which) -> {

                    Toast.makeText(activity, "正在导出，请稍等~", Toast.LENGTH_SHORT).show();

                    startExportZip(list);
                })
                .setNegativeButton("取消", (dialog, which) -> {



                })
                .setNeutralButton("文件夹导出", (dialog, which) -> {

                    Toast.makeText(activity, "正在导出，请稍等~", Toast.LENGTH_SHORT).show();
                    startExportFolder(list);

                }).show();

    }


    private float getSp(int d, Context activity) {

        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, d,
                activity.getResources().getDisplayMetrics());

    }


    private float getDp(int d, Context context) {

        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, d,
                context.getResources().getDisplayMetrics());

    }

    private void initThreadPoolExecutor(){

        BlockingQueue<Runnable> runnableBlockingQueue = new LinkedBlockingQueue<>();
        int KEEP_ALIVE = 10;
        poolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, CORE_POOL_MAX_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, runnableBlockingQueue);

    }

}