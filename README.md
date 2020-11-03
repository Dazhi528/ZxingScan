# ZxingScan

#### 介绍
提供二维码和一维码扫码的通用Activity页面，支持ARouter方式跳转
此库基于yipianfengye/android-zxingLibrary改进，感谢作者分享
以上库比较稳定，作者已3年没有再维护，而安卓更新换代速度这么快，
为了跟随安卓使其向前兼容，Fork过来，缺啥补啥，核心稳定，并没有动
Url：https://github.com/yipianfengye/android-zxingLibrary

#### Camera与Camera2说明
由于Api21以上弃用了Camera，因此需要把源码中的相机部分更新下，但
由于此库还兼容Api21以下，旧版接口依然能用且稳定，因此本人暂无
更换Camera2的想法，等再过个两三年，市面上不再怎么又Api21以下系统
即最低支持为Api21时候，我会回来再基于本库来一次大的升级~

#### 引入说明
本库是为了让com.github.Dazhi528:MvvmAndroidFrame:x.x.x库专注于项目架构
而从中解耦出来的用于扫码识别一维码/二维码的专用功能库，因此，它其实是此框架
库的扩展功能库，内部依赖了此架构库，不过不用担心，此架构库非常轻量，因此
此扫描库也可以单独使用

用法实例在sample里，值得注意点是，App的继承方式，其继承了RootApp做了些初始化工作

#### 解耦说明
由于此库是希望能够配合MvvmAndroidFrame，有一个完美的兼容配合，因此内部
引入了MvvmAndroidFrame，但如果不想使用MvvmAndroidFrame库，想解耦出来
单独用，可以Fork此项目，提出MvvmAndroidFrame库，本库用到MvvmAndroidFrame
的部分主要是基础了其RootActivity，简化代码，可以用安卓兼容Activity代替
再就是引用了阿里加的路由框架，也是为了解耦，如果不需要，也可以剔除