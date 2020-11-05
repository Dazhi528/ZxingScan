package com.dazhi.sample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.alibaba.android.arouter.launcher.ARouter;
import com.dazhi.libroot.root.RootSimpActivity;
import com.dazhi.libroot.util.RtCmn;
import com.dazhi.scan.util.UtScan;
import androidx.annotation.Nullable;

/**
 * 功能：
 * 描述：
 * 作者：WangZezhi
 * 邮箱：wangzezhi528@163.com
 * 日期：20-9-9 下午6:36
 */
public class MainActivity extends RootSimpActivity {
    EditText editText;

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
        editText = findViewById(R.id.etInput);
        ImageView imageView = findViewById(R.id.ivQrCode);
        // ======= 批量扫描
        Button btBatch = findViewById(R.id.btBatch);
        btBatch.setOnClickListener(v -> {
            UtScan.addBatchScanCallback(scanCode -> {
                if(scanCode==null || scanCode.isEmpty()) {
                    return;
                }
                RtCmn.toastShort(scanCode);
            });
            //
            ARouter.getInstance()
                    .build("/scan/ScanActivity")
                    .withBoolean("BOO_BATCH", true) // 批量扫码设true
                    .navigation(MainActivity.this, 66);
        });
        // ======= 单笔扫描
        Button btTest = findViewById(R.id.btGotoScan);
        btTest.setOnClickListener(view -> {
            // 正式代码如果没有添加过批量扫描，可不移除
            UtScan.addBatchScanCallback(null);
            //
            ARouter.getInstance()
                    .build("/scan/ScanActivity")
                    .navigation(MainActivity.this, 66);
        });
        // ======== 生成二维码按钮部分
        Button btQrcode=findViewById(R.id.btCreateQrCode);
        btQrcode.setOnClickListener(view -> {
            Bitmap mBitmap = UtScan.createQRCode(editText.getText().toString(),
                    400, 400,
                    BitmapFactory.decodeResource(getResources(), R.drawable.ico_libscan_hand));
            imageView.setImageBitmap(mBitmap);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==66 && data!=null) {
            String scanRet = data.getStringExtra(UtScan.RESULT_CODE);
            if(!TextUtils.isEmpty(scanRet)) {
                editText.setText("扫描的内容为："+scanRet);
            }
        }
    }

}
