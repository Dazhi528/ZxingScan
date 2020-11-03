package com.dazhi.sample;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.alibaba.android.arouter.launcher.ARouter;
import com.dazhi.libroot.root.RootSimpActivity;
import com.dazhi.libroot.util.RtCmn;
import com.dazhi.libroot.util.RtCode;
import com.dazhi.scan.util.UtScanCode;
import com.dazhi.scan.util.UtScanDisplay;

/**
 * 功能：
 * 描述：
 * 作者：WangZezhi
 * 邮箱：wangzezhi528@163.com
 * 日期：20-9-9 下午6:36
 */
public class MainActivity extends RootSimpActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initConfig(TextView tvToolTitle) {
        tvToolTitle.setText("扫描库");
    }

    @Override
    protected void initViewAndDataAndEvent() {
        boolean booBATCH = true; // 设置true体验批量扫描
        Button btTest = findViewById(R.id.btTest);
        btTest.setOnClickListener(view -> {
            ARouter.getInstance()
                    .build("/scan/ScanActivity")
                    .withBoolean("BOO_BATCH", booBATCH) // 需批量扫码时添加
                    .navigation(MainActivity.this, 66);
        });
        // 批量扫描回调监听
        if(booBATCH) {
            UtScanDisplay.addBatchScanCallback(scanCode -> {
                if(scanCode==null || scanCode.isEmpty()) {
                    return;
                }
                RtCmn.toastShort(scanCode);
            });
        }else { // 非批量扫描时，移除批量扫描测试时添加的回调
            // 正式代码如果没有添加过批量扫描，可不移除
            UtScanDisplay.addBatchScanCallback(null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==66 && data!=null) {
            String scanRet = data.getStringExtra(UtScanCode.RESULT_CODE);
            if(!TextUtils.isEmpty(scanRet)) {
                RtCmn.toastShort(scanRet);
            }
        }
    }

}
