package cn.edu.ustc.software.hanyizhao.encryptiontool;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cn.edu.ustc.software.hanyizhao.encryptiontool.service.StaticData;

/**
 * Created by HanYizhao on 2015/12/29.
 * 处理Activity在内存不足重新唤醒之后，可能一些静态数据已经丢失，这时候需要重新登录
 */
public class HandleSavedDataActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!StaticData.getInstance().isHasReset() && savedInstanceState != null) {
            String a = savedInstanceState.getString("about");
            StaticData.getInstance().setRealPassword(a);
            StaticData.getInstance().resetData();
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("about", StaticData.getInstance().getRealPassword());
        super.onSaveInstanceState(outState);
    }
}
