package com.app.legend.dms;


import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

public class MainActivity extends AppCompatActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView=findViewById(R.id.webview);

        webView.loadUrl("https://github.com/liuzhushaonian/DMS/blob/master/README.md");

    }

    @Override
    public void finish() {
        PackageManager p = getPackageManager();
        p.setComponentEnabledSetting(getAliseComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        super.finish();
    }

    private ComponentName getAliseComponentName(){
        return new ComponentName(MainActivity.this, "com.app.legend.dms.MainActivityAlias");
        // 在AndroidManifest.xml中为MainActivity定义了一个别名为MainActivity-Alias的activity，是默认启动activity、是点击桌面图标后默认程序入口
    }
}
