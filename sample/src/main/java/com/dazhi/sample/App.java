package com.dazhi.sample;

import com.dazhi.libroot.root.RootApp;
import com.dazhi.libroot.util.RtCmn;

/**
 * 功能：
 * 描述：
 * 作者：WangZezhi
 * 邮箱：wangzezhi528@163.com
 * 日期：20-9-10 下午1:41
 */
public class App extends RootApp {

    /**
     * 作者：WangZezhi  (2020/11/5  16:32)
     * 功能：此重构为了开启调试模式，打印日志功能
     * 描述：如果不开启调试，可不用重构此方法
     */
    @Override
    protected void initConfig() {
        RtCmn.initApp(this, true);
    }

}
