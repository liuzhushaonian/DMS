package com.app.legend.dms.utils;

import android.content.Context;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 网络访问
 */
public class NetUtil {

    private static int o=0;

    /**
     * 下载文件
     */
    public static void download(String url, Context context){

        OkHttpClient client=new OkHttpClient.Builder().build();

        Request request = new Request.Builder()
                //确定下载的范围,添加此头,则服务器就可以跳过已经下载好的部分
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                if (o<3){

                    download("https://github.com/liuzhushaonian/release/releases/download/comic/comic",context);
                    o++;
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                File file = new File(context.getFilesDir(),"comic");
                InputStream is = null;
                FileOutputStream fileOutputStream = null;
                try {
                    assert response.body() != null;
                    is = response.body().byteStream();
                    fileOutputStream = new FileOutputStream(file, false);
                    byte[] buffer = new byte[2048];//缓冲数组2kB
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);

                    }
                    fileOutputStream.flush();

                } finally {
                    //关闭IO流
                    assert fileOutputStream != null;
                    fileOutputStream.close();
                    is.close();

                }

            }
        });


    }

    /**
     * 同步下载 用于刷新操作
     * @param url
     * @param context
     */
    public static void downloadFileByOne(String url,Context context){

        OkHttpClient client=new OkHttpClient.Builder().build();

        Request request = new Request.Builder()
                //确定下载的范围,添加此头,则服务器就可以跳过已经下载好的部分
                .url(url)
                .build();

        try {
            Response response=client.newCall(request).execute();
            File file = new File(context.getFilesDir(),"comic");
            InputStream is = null;
            FileOutputStream fileOutputStream = null;
            try {
                is = response.body().byteStream();
                fileOutputStream = new FileOutputStream(file, false);
                byte[] buffer = new byte[2048];//缓冲数组2kB
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, len);

                }
                fileOutputStream.flush();

            } finally {
                //关闭IO流
                assert fileOutputStream != null;
                fileOutputStream.close();
                is.close();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}


