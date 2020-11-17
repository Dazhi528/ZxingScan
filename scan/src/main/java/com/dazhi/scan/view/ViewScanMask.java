package com.dazhi.scan.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.dazhi.libroot.util.RtLog;
import com.dazhi.scan.R;
import com.dazhi.scan.camera.CameraManager;
import com.dazhi.scan.util.UtScan;
import com.google.zxing.ResultPoint;

import java.util.Collection;
import java.util.HashSet;

/**
 * 自定义组件实现,扫描功能
 */
public final class ViewScanMask extends View {
    private static final long ANIMATION_DELAY = 100L;
    //    private static final int OPAQUE = 0xFF;
    private boolean cameraRuning=false;
    private Collection<ResultPoint> possibleResultPoints;
    private Collection<ResultPoint> lastPossibleResultPoints;
    //
    private final Paint paint;
    private final int maskColor; // 遮罩颜色(扫描框外部)
    // 布局属性
    private int innerFrameColor; // 扫描框边角颜色
    private int innerFrameLength; // 扫描框边角长度
    private int innerFrameWidth; // 扫描框边角宽度
    //    private Bitmap innerLineBitmap; // 扫描线
//    private int lineBitmapTop; // 扫描线移动的y
//    private int innerLineSpeed; // 扫描线移动速度
    private boolean innerResultPointShow; // 是否展示离散结果点
    private int innerResultPointColor; // 离散结果点颜色


    public ViewScanMask(Context context) {
        this(context, null);
    }

    public ViewScanMask(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public ViewScanMask(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        maskColor = getResources().getColor(R.color.libscan_mask);
        innerFrameColor = getResources().getColor(R.color.libscan_frame);
        innerResultPointColor = getResources().getColor(R.color.libscan_result_point);
        possibleResultPoints = new HashSet<>(5);
        // 初始化内部框参数
        initInnerRect(context, attrs);
    }

    // 初始化内部框参数
    @SuppressLint("ResourceAsColor")
    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private void initInnerRect(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ViewScanMask);
        // 扫描框的宽度
        CameraManager.FRAME_WIDTH = (int) ta.getDimension(R.styleable.ViewScanMask_inner_frame_width,
                UtScan.screenWidthPx / 2);
        // 扫描框的高度
        CameraManager.FRAME_HEIGHT = (int) ta.getDimension(R.styleable.ViewScanMask_inner_frame_height,
                UtScan.screenWidthPx / 2);
        // 扫描框距离顶部
        float innerMarginTop = ta.getDimension(R.styleable.ViewScanMask_inner_frame_margintop,
                -1);
        if (innerMarginTop != -1) {
            CameraManager.FRAME_MARGINTOP = (int) innerMarginTop;
        }
        // 扫描框边角颜色
        innerFrameColor = ta.getColor(R.styleable.ViewScanMask_inner_corner_color,
                R.color.libscan_frame);
        // 扫描框边角长度
        innerFrameLength = (int) ta.getDimension(R.styleable.ViewScanMask_inner_corner_length,
                R.dimen.libscan_cornerlinelength);
        // 扫描框边角宽度
        innerFrameWidth = (int) ta.getDimension(R.styleable.ViewScanMask_inner_corner_width,
                R.dimen.libscan_cornerlinewidth);
        // 扫描控件
//        innerLineBitmap = BitmapFactory.decodeResource(getResources(), ta.getResourceId(
//                R.styleable.ViewScanMask_inner_line_bitmap,
//                R.drawable.ico_libscan_line));
        // 扫描速度
//        innerLineSpeed = ta.getInt(R.styleable.ViewScanMask_inner_line_speed, 10);
        // 是否展示离散结果点
        innerResultPointShow = ta.getBoolean(R.styleable.ViewScanMask_inner_resultpoint_show,
                false);
        innerResultPointColor = ta.getColor(R.styleable.ViewScanMask_inner_resultpoint_color,
                R.color.libscan_result_point);
        ta.recycle();
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        Rect frame = CameraManager.self().getFramingRect();
        if (frame == null) {
            return;
        }
        int width = getWidth();
        int height = getHeight();
        paint.setColor(maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);
        drawFrameBounds(canvas, frame);
//        drawScanLine(canvas, frame);
        // 绘制预测点部分
        if (!innerResultPointShow) {
            return;
        }
        Collection<ResultPoint> currentPossible = possibleResultPoints;
        Collection<ResultPoint> currentLast = lastPossibleResultPoints;
        if (currentPossible.isEmpty()) {
            lastPossibleResultPoints = null;
        } else {
            possibleResultPoints = new HashSet<>(5);
            lastPossibleResultPoints = currentPossible;
//            paint.setAlpha(OPAQUE);
            paint.setColor(innerResultPointColor);
            for (ResultPoint point : currentPossible) {
                canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 6.0f, paint);
            }
        }
        if (currentLast != null) {
//            paint.setAlpha(OPAQUE / 2);
            paint.setColor(innerResultPointColor);
            for (ResultPoint point : currentLast) {
                canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 3.0f, paint);
            }
        }
        if(cameraRuning) {
            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
        }
    }

    /*
     * 绘制移动扫描线
     */
//    private boolean booAdd=true;
//    private void drawScanLine(Canvas canvas, Rect frame) {
//        int tempSpace = (frame.bottom-frame.top)/4;
//        int min = frame.top+tempSpace;
//        int max = frame.bottom - tempSpace;
//        if (lineBitmapTop <= min) {
//            lineBitmapTop = min;
//            booAdd=true;
//        }else if (lineBitmapTop >= max) {
//            lineBitmapTop = max;
//            booAdd=false;
//        }
//        if(booAdd){
//            lineBitmapTop += innerLineSpeed;
//        }else {
//            lineBitmapTop -= innerLineSpeed;
//        }
//        Rect scanRect = new Rect(frame.left, lineBitmapTop, frame.right,
//                lineBitmapTop + 6);
//        canvas.drawBitmap(innerLineBitmap, null, scanRect, paint);
//    }

    /**
     * 绘制取景框边框
     */
    private void drawFrameBounds(Canvas canvas, Rect frame) {
        paint.setColor(innerFrameColor);
        paint.setStyle(Paint.Style.FILL);
        int corWidth = innerFrameWidth;
        int corLength = innerFrameLength;
        // 左上角
        canvas.drawRect(frame.left, frame.top, frame.left + corWidth, frame.top
                + corLength, paint);
        canvas.drawRect(frame.left, frame.top, frame.left
                + corLength, frame.top + corWidth, paint);
        // 右上角
        canvas.drawRect(frame.right - corWidth, frame.top, frame.right,
                frame.top + corLength, paint);
        canvas.drawRect(frame.right - corLength, frame.top,
                frame.right, frame.top + corWidth, paint);
        // 左下角
        canvas.drawRect(frame.left, frame.bottom - corLength,
                frame.left + corWidth, frame.bottom, paint);
        canvas.drawRect(frame.left, frame.bottom - corWidth, frame.left
                + corLength, frame.bottom, paint);
        // 右下角
        canvas.drawRect(frame.right - corWidth, frame.bottom - corLength,
                frame.right, frame.bottom, paint);
        canvas.drawRect(frame.right - corLength, frame.bottom - corWidth,
                frame.right, frame.bottom, paint);
    }

    public void addPossibleResultPoint(ResultPoint point) {
        possibleResultPoints.add(point);
    }

    public void setCameraRuning(boolean cameraRuning) {
        this.cameraRuning = cameraRuning;
    }

}
