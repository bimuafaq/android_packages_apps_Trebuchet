/*
 * Copyright (C) 2022 Project Kaleidoscope
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.quickstep.views;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.TextView;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;

import java.lang.Runnable;
import java.math.BigDecimal;

public class MemInfoView extends TextView {

    // When to show GB instead of MB
    private static final int UNIT_CONVERT_THRESHOLD = 1024; /* MiB */
    private static final BigDecimal GB2MB = new BigDecimal(1024);

    private ActivityManager mActivityManager;
    private Handler mHandler;
    private MemInfoWorker mWorker;
    private String mMemInfoText;

    public MemInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        mHandler = new Handler(Looper.getMainLooper());
        mWorker = new MemInfoWorker();

        try {
            mMemInfoText = context.getResources().getString(R.string.meminfo_text);
        } catch (Exception e) {
            mMemInfoText = "%1$s Available | %2$s Total";
        }
        
        updateMemInfo();
    }

    public void updateMemInfo() {
        if (!Utilities.isShowMemInfo(getContext())) {
            mHandler.removeCallbacks(mWorker);
            setVisibility(GONE);
            return;
        }

        if (getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
        }

        mHandler.post(mWorker);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacks(mWorker);
    }

    private String unitConvert(long valueMiB, boolean alignToGB) {
        BigDecimal rawVal = new BigDecimal(valueMiB);

        if (alignToGB)
            return rawVal.divide(GB2MB, 0, BigDecimal.ROUND_UP) + " GB";

        if (valueMiB > UNIT_CONVERT_THRESHOLD)
            return rawVal.divide(GB2MB, 1, BigDecimal.ROUND_HALF_UP) + " GB";
        else
            return rawVal + " MB";
    }

    private void updateMemInfoText(long availMemMiB, long totalMemMiB) {
        String text = String.format(mMemInfoText,
            unitConvert(availMemMiB, false), unitConvert(totalMemMiB, true));
        setText(text);
    }

    private class MemInfoWorker implements Runnable {
        @Override
        public void run() {
            if (!Utilities.isShowMemInfo(getContext())) {
                return;
            }

            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            mActivityManager.getMemoryInfo(memInfo);
            long availMemMiB = memInfo.availMem / (1024 * 1024);
            long totalMemMiB = memInfo.totalMem / (1024 * 1024);
            updateMemInfoText(availMemMiB, totalMemMiB);
        }
    }
}
