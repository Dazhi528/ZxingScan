package com.dazhi.scan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import com.dazhi.scan.camera.CameraManager;
import com.dazhi.scan.decoding.ScanHandler;
import com.dazhi.scan.decoding.InactivityTimer;
import com.dazhi.scan.util.UtScan;
import com.dazhi.scan.view.ViewScanMask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.Vector;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

/**
 * 自定义实现的扫描Fragment
 */
public class ScanFragment extends Fragment implements SurfaceHolder.Callback {
    private ScanHandler handler;
    private ViewScanMask viewScanMask;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private SurfaceHolder surfaceHolder;
    private UtScan.AnalyzeCallback analyzeCallback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this.getActivity());
    }

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        View view = null;
        if (bundle != null) {
            int layoutId = bundle.getInt(UtScan.LAYOUT_ID);
            if (layoutId != -1) {
                view = inflater.inflate(layoutId, null);
            }
        }
        if (view == null) {
            view = inflater.inflate(R.layout.libscan_fragment, null);
        }
        SurfaceView surfaceView = view.findViewById(R.id.preview_view);
        viewScanMask = view.findViewById(R.id.view_scan_mask);
        surfaceHolder = surfaceView.getHolder();
        return view;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onResume() {
        super.onResume();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;
        playBeep = true;
        FragmentActivity mActivity = getActivity();
        if(mActivity!=null) {
            AudioManager audioService = (AudioManager) mActivity
                    .getSystemService(Context.AUDIO_SERVICE);
            if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                playBeep = false;
            }
            initBeepSound();
            vibrate = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        viewScanMask.setCameraRuning(false);
        CameraManager.self().closeDriver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        inactivityTimer.shutdown();
    }

    /**
     * 作者：WangZezhi  (2020/11/3  16:39)
     * 功能：SurfaceHolder.Callback 接口实现部分
     * 描述：
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 执行closeDriver()会调用相机释放，释放时销毁表层执行此方法
        hasSurface = false;
    }
    
    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.self().openDriver(getActivity(), surfaceHolder);
        } catch (Exception e) {
            return;
        }
        viewScanMask.setCameraRuning(true);
        viewScanMask.postInvalidate();
        if (handler == null) {
            handler = new ScanHandler(this, decodeFormats,
                    characterSet, viewScanMask);
        }
    }
    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            FragmentActivity mActivity=getActivity();
            if(mActivity==null) {
                return;
            }
            mActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);
            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }
    // 提示音播放完毕后，快退以将其排队
    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer
            .OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    public Handler getHandler() {
        return handler;
    }
    public void setAnalyzeCallback(UtScan.AnalyzeCallback analyzeCallback) {
        this.analyzeCallback = analyzeCallback;
    }

    /**
     * 作者：WangZezhi  (2020/11/3  16:41)
     * 功能：扫描结果处理部分
     * 描述：
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        if (analyzeCallback == null) {
            return;
        }
        if (result == null || TextUtils.isEmpty(result.getText())) {
            analyzeCallback.onAnalyzeFailed();
        } else {
            analyzeCallback.onAnalyzeSuccess(barcode, result.getText());
        }
    }
    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            FragmentActivity mActivity = getActivity();
            if(mActivity==null) {
                return;
            }
            Vibrator vibrator = (Vibrator) mActivity
                    .getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(200);
        }
    }

}
