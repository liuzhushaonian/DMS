package com.app.legend.dms.utils;

import android.os.Environment;

import com.app.legend.dms.model.ExportComic;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.UnzipParameters;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedBridge;

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

        File file=new File(PATH,name+".zip");

        if (!file.getParentFile().exists()){
            file.mkdirs();
        }

        try {
            return new ZipFile(file);
        } catch (ZipException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * 添加文件到zip包里
     * @param zipFile 主zip包
     *
     */
    public static void addZipFile(ZipFile zipFile, List<ExportComic> exportComicList){

        if (zipFile==null){
            return;
        }

        ZipParameters parameters=new ZipParameters();

        parameters.setCompressionMethod(Zip4jConstants.COMP_STORE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FASTEST);
        parameters.setSourceExternalStream(true);//开启流方式添加

        for (ExportComic comic:exportComicList){

            String path=comic.getPath();

            if (path!=null&&path.endsWith("zip")) {

                try {

                    FileInputStream fileInputStream = new FileInputStream(path);

                    String name=comic.getBigTitle()+"_"+comic.getCharTitle();

                    parameters.setFileNameInZip(name);

                    zipFile.addStream(fileInputStream, parameters);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (ZipException e) {
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

        parameters.setCompressionMethod(Zip4jConstants.COMP_STORE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FASTEST);
//        parameters.setSourceExternalStream(true);//开启流方式添加


        for (ExportComic comic:exportComicList){

            String path=comic.getPath();

            if (path!=null&&path.endsWith("zip")) {

                String folder=PATH+"/"+comic.getBigTitle()+"_"+comic.getCharTitle();

                File file=new File(folder);

                if (!file.exists()){
                    file.mkdirs();//建立文件夹
                }

                try {

                    ZipFile z=new ZipFile(path);//获取路径下的zip包

                    z.extractAll(file.getAbsolutePath());//解压到文件夹下

                    String name=comic.getBigTitle()+"_"+comic.getCharTitle();

                    parameters.setFileNameInZip(name);

                    zipFile.addFolder(file.getAbsolutePath(),parameters);

//                    deleteDirWihtFile(file);

                } catch (ZipException e) {
                    e.printStackTrace();
                }finally {

                    deleteDirWihtFile(file);

                    System.out.println("最后删除文件夹");

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






}
