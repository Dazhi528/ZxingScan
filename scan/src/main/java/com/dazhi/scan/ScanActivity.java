package com.dazhi.scan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.dazhi.libroot.root.RootSimpActivity;
import com.dazhi.libroot.util.RtCmn;
import com.dazhi.scan.util.UtScanCode;
import com.dazhi.scan.util.UtScanDisplay;

/**
 * 功能：定制化扫描界面
 * 描述：
 * 作者：WangZezhi
 * 邮箱：wangzezhi528@163.com
 * 创建日期：2018/5/18 10:33
 * 修改日期：2018/5/18 10:33
 * 注意要加： ZXingLibrary.initDisplayOpinion(this);
 */
@Route(path = "/scan/ScanActivity")
public class ScanActivity extends RootSimpActivity implements View.OnClickListener {
    @Autowired(name = "BOO_BATCH")
    public boolean booBatch = false; // 默认不开批量扫描
    private Button btLibScanLight;
    private boolean booLight = false; //默认闪光灯是关闭的

    @Override
    protected int getLayoutId() {
        return R.layout.libscan_activity;
    }

    @Override
    protected void initConfig(TextView tvToolTitle) {
        UtScanDisplay.initDisplayOpinion(this);
        ARouter.getInstance().inject(this);
        permissionCamera();
    }

    @Override
    protected void initViewAndDataAndEvent() {
        //初始化view
        Button btLibScanEsc = findViewById(R.id.btLibScanEsc);
        btLibScanEsc.setOnClickListener(this);
        btLibScanLight = findViewById(R.id.btLibScanLight);
        btLibScanLight.setOnClickListener(this);
        //
        initLibScan();
    }

    @Override
    public void onClick(View v) {
        int intId = v.getId();
        if (intId == R.id.btLibScanEsc) {
            finish();
            return;
        }
        if (intId == R.id.btLibScanLight) {
            booLight = !booLight; //取反
            if (booLight) {
                //开灯
                UtScanCode.isLightEnable(true);
                btLibScanLight.setText(R.string.libscan_lightoff);
            } else {
                //关灯
                UtScanCode.isLightEnable(false);
                btLibScanLight.setText(R.string.libscan_lighton);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //关闭闪光灯
        UtScanCode.isLightEnable(false);
    }

    private void initLibScan() {
        //执行扫面Fragment的初始化操作
        final ScanFragment scanFragment = new ScanFragment();
        //为二维码扫描界面设置定制化界面
        UtScanCode.setFragmentArgs(scanFragment, R.layout.libscan_frame);
        scanFragment.setAnalyzeCallback(new UtScanCode.AnalyzeCallback() {
            @Override
            public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                if (booBatch) {
                    // 批量扫描
                    UtScanDisplay.BatchScanCallback mTemp = UtScanDisplay.getBatchScanCallback();
                    if(mTemp!=null) {
                        mTemp.call(result);
                    }
                    // 重新开始扫描
                    scanFragment.getHandler().sendEmptyMessageDelayed(R.id.restart_preview, 1000);
                } else {
                    // 单笔扫描
                    Intent resultIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putInt(UtScanCode.RESULT_TYPE, UtScanCode.RESULT_SUCCESS);
                    bundle.putString(UtScanCode.RESULT_STRING, result);
                    resultIntent.putExtras(bundle);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }

            @Override
            public void onAnalyzeFailed() {
                if (booBatch) {
                    // 批量扫描
                    RtCmn.toastLong(R.string.libscan_fail);
                } else {
                    // 单笔扫描
                    Intent resultIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putInt(UtScanCode.RESULT_TYPE, UtScanCode.RESULT_FAILED);
                    bundle.putString(UtScanCode.RESULT_STRING, "");
                    resultIntent.putExtras(bundle);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });
        //替换我们的扫描控件
        getSupportFragmentManager().beginTransaction().replace(R.id.flLibScanContainer, scanFragment).commit();
    }


}
