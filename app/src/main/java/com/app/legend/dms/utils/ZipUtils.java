package com.app.legend.dms.utils;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;

import com.app.legend.dms.model.ExportComic;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

/**
 * 解压缩工具
 */
public class ZipUtils {

    private static final String PATH= Environment.getExternalStorageDirectory().getAbsolutePath()+"/release";//保存路径


    /**
     * 生成zip包
     * @param name 名字
     *
     */
    public static ZipFile createZip(String name) {

        name=name.replace("！","");
        name=name.replace(" ","");
        if (name.startsWith("+")){
            name=name.replace("+","");
        }

        File file=new File(PATH,name+".zip");

        if (!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }

        return new ZipFile(file);

    }

    /**
     * 添加文件到zip包里
     * @param zipFile 主zip包
     *
     */
    public static void addZipFile(ZipFile zipFile, List<ExportComic> exportComicList){

        if (zipFile==null){
            Log.d("zipFile--->>>","the file is null");
            return;
        }

        ZipParameters parameters=new ZipParameters();

        CompressionMethod compressionMethod=CompressionMethod.STORE;

        parameters.setCompressionMethod(compressionMethod);
        parameters.setCompressionLevel(CompressionLevel.FAST);


//        parameters.setSourceExternalStream(true);//开启流方式添加

        for (ExportComic comic:exportComicList){

            String path= comic.getPath();

            if (path!=null&&path.endsWith(".zip")) {

                try {

                    FileInputStream fileInputStream = new FileInputStream(path);

                    String name=comic.getBigTitle()+"_"+comic.getCharTitle()+".zip";

//                    File c=new File(path);
//
//                    Log.d("ff---->>>",c.getAbsolutePath());

//                    parameters.setFileNameInZip(name);
//
//                    zipFile.addFile(c,parameters);
//
                    parameters.setFileNameInZip(name);

                    zipFile.addStream(fileInputStream, parameters);

                } catch (ZipException | FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 先获取章节zip
     * 在release下建立相应文件夹，与章节名一致
     * 解压到该文件夹下
     * 获取该文件夹并加入zip包
     * 删除该文件夹
     * @param zipFile 漫画zip
     * @param exportComicList 章节列表
     *
     */
    public static void addFolderFile(ZipFile zipFile,List<ExportComic> exportComicList){

        if (zipFile==null){
            return;
        }

        ZipParameters parameters=new ZipParameters();

        parameters.setCompressionMethod(CompressionMethod.STORE);
        parameters.setCompressionLevel(CompressionLevel.FAST);
//        parameters.setSourceExternalStream(true);//开启流方式添加


        for (ExportComic comic:exportComicList){

            String path=comic.getPath();

            if (path!=null&&path.endsWith(".zip")) {

                String n=zipFile.getFile().getName().replace(".zip","");

                String folder=PATH+"/"+n+"/"+comic.getBigTitle()+"_"+comic.getCharTitle();

                File file=new File(folder);

                if (!file.getParentFile().exists()){
                    file.mkdirs();//建立文件夹
                }

                try {

                    ZipFile z=new ZipFile(path);//获取路径下的zip包
                    z.extractAll(file.getAbsolutePath());//解压到文件夹下
                    String name=comic.getBigTitle()+"_"+comic.getCharTitle();
                    parameters.setFileNameInZip(name);
                    zipFile.addFolder(file);

//                    deleteDirWihtFile(file);

                } catch (ZipException e) {
                    e.printStackTrace();
                }finally {

                    deleteDirWihtFile(file.getParentFile());

//                    System.out.println("最后删除文件夹");

                }


            }

        }

    }


    private static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }


    /**
     * 修复官方保存本地图片FC，哎，我终究沦落到给官方修bug了吗？
     * 
     * 
     * @param zipFile zip文件路径
     * @param oneName 需要保存的图片在zip中的名字，0.jpg之类的
     * @param savePath 保存的路径，默认不改，且需要将其插入媒体数据库内
     */
    public static boolean fixSaveLocalImage(String zipFile, String oneName, String savePath, Context context){

        try {
            ZipFile zipFile1=new ZipFile(zipFile);

            FileHeader header=zipFile1.getFileHeader(oneName);

            ZipInputStream zipInputStream=zipFile1.getInputStream(header);

            OutputStream outputStream=new FileOutputStream(savePath);

            Bitmap bitmap=BitmapFactory.decodeStream(zipInputStream);

            boolean r= bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);

            if (r) {//如果保存成功，则插入数据库

                MediaScannerConnection.scanFile(context, new String[]{savePath}, null,
                        (path, uri1) -> {
                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            mediaScanIntent.setData(uri1);
                            context.sendBroadcast(mediaScanIntent);
                        });
            }

            outputStream.close();
            zipInputStream.close();

            return r;

        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }

    }


}
