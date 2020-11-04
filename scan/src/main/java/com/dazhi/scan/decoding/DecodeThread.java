/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dazhi.scan.decoding;

import android.os.Handler;
import android.os.HandlerThread;
import com.dazhi.scan.ScanFragment;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPointCallback;
import java.util.Hashtable;
import java.util.Vector;

final class DecodeThread extends HandlerThread {
    public static final String BARCODE_BITMAP = "barcode_bitmap";
    private final ScanFragment fragment;
    private final Hashtable<DecodeHintType, Object> hints;
    private Handler handler;

    DecodeThread(ScanFragment fragment,
                 Vector<BarcodeFormat> decodeFormats,
                 String characterSet,
                 ResultPointCallback resultPointCallback) {
        super("DecodeThread");
        this.fragment = fragment;
        hints = new Hashtable<>(3);
        if (decodeFormats == null || decodeFormats.isEmpty()) {
            decodeFormats = new Vector<>();
            decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS); //条码
            decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);//二维码
            decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);//特殊二维码
        }
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        if (characterSet != null) {
            hints.put(DecodeHintType.CHARACTER_SET, characterSet);
        }
        hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);
    }

    Handler getHandler() {
        if(handler==null) {
            handler = new DecodeHandler(getLooper(), fragment, hints);
        }
        return handler;
    }

}
