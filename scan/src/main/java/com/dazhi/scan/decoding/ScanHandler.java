package com.dazhi.scan.decoding;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.dazhi.scan.R;
import com.dazhi.scan.ScanFragment;
import com.dazhi.scan.camera.CameraManager;
import com.dazhi.scan.view.RealResultPointCallback;
import com.dazhi.scan.view.ViewScanMask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import java.util.Vector;

import androidx.fragment.app.FragmentActivity;

public final class ScanHandler extends Handler {
    private final ScanFragment fragment;
    private final DecodeThread decodeThread;
    private State state;

    private enum State {
        PREVIEW,
        SUCCESS,
        DONE
    }

    public ScanHandler(ScanFragment fragment, Vector<BarcodeFormat> decodeFormats,
                       String characterSet, ViewScanMask viewfinderView) {
        super();
        this.fragment = fragment;
        decodeThread = new DecodeThread(fragment, decodeFormats, characterSet,
                new RealResultPointCallback(viewfinderView));
        decodeThread.start();
        state = State.SUCCESS;
        // Start ourselves capturing previews and decoding.
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        if (message.what == R.id.auto_focus) {
            //Log.d(TAG, "Got auto-focus message");
            // When one auto focus pass finishes, start another. This is the closest thing to
            // continuous AF. It does seem to hunt a bit, but I'm not sure what else to do.
            if (state == State.PREVIEW) {
                CameraManager.self().requestAutoFocus(this, R.id.auto_focus);
            }
        } else if (message.what == R.id.restart_preview) {
            //Log.d(TAG, "Got restart preview message");
            restartPreviewAndDecode();
        } else if (message.what == R.id.decode_succeeded) {
            //Log.d(TAG, "Got decode succeeded message");
            state = State.SUCCESS;
            Bundle bundle = message.getData();
            //
            Bitmap barcode = bundle == null ? null :
                    (Bitmap) bundle.getParcelable(DecodeThread.BARCODE_BITMAP);
            fragment.handleDecode((Result) message.obj, barcode);
        } else if (message.what == R.id.decode_failed) {
            // We're decoding as fast as possible, so when one decode fails, start another.
            state = State.PREVIEW;
            CameraManager.self().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
        } else if (message.what == R.id.return_scan_result) {
            //Log.d(TAG, "Got return scan result message");
            FragmentActivity mActivity=fragment.getActivity();
            if(mActivity!=null) {
                mActivity.setResult(Activity.RESULT_OK, (Intent) message.obj);
                mActivity.finish();
            }
        } else if (message.what == R.id.launch_product_query) {
            //Log.d(TAG, "Got product query message");
            String url = (String) message.obj;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            FragmentActivity mActivity=fragment.getActivity();
            if(mActivity!=null) {
                mActivity.startActivity(intent);
            }
        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        CameraManager.self().stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
        quit.sendToTarget();
        try {
            decodeThread.join();
        } catch (InterruptedException e) {
            // continue
        }
        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
    }

    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            CameraManager.self().startPreview();
            CameraManager.self().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
            CameraManager.self().requestAutoFocus(this, R.id.auto_focus);
        }
    }

}
