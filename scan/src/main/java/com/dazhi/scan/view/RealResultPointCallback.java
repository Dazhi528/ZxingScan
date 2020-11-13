package com.dazhi.scan.view;

import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;

public final class RealResultPointCallback implements ResultPointCallback {
    private final ViewScanMask viewScanMask;

    public RealResultPointCallback(ViewScanMask viewScanMask) {
        this.viewScanMask = viewScanMask;
    }

    @Override
    public void foundPossibleResultPoint(ResultPoint point) {
        if(viewScanMask!=null) {
            viewScanMask.addPossibleResultPoint(point);
        }
    }

}
