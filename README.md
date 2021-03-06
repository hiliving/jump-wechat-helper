# jump-wechat-helper
###跳一跳辅助安卓版

* 改进自java版，java算法是参考[python版本](https://github.com/wangshub/wechat_jump_game)，提升运行速度，增加随机休息时间防止被判定为作弊。

![](https://github.com/hiliving/jump-wechat-helper/blob/master/screenshot/QQ%E5%9B%BE%E7%89%8720180107123948.jpg)
> 原理：

在服务中创建悬浮窗，切换到跳一跳游戏界面，获取截屏并分析棋盘的坐标，算出所需的跳跃时间。想要在其他应用界面运行shell
命令模拟触摸事件，必须取得root权限，否则只在自己应用内生效。
> 须知：

* 必须取得root权限，可用刷机精灵PC版连手机尝试获取，建议用旧手机，毕竟root后不享受保修也不能正常更新升级。
* 6.0版本及以上需要手动开启所需权限。当前版本未做权限处理，若出现闪退应该是权限问题，前往权限管理开启权限即可。
* 应用需要悬浮窗权限，需要手动开启。

> 当前版本存在的问题：

 * 概率性会计算失误，跳跃失败。自测最高2100多分挂掉，更多的是不到1000就挂了，算法还需改进。
 * 服务会概率性停止，重新开启即可继续游戏，不影响使用，后期版本再改进吧。



> 可直接下载[jump-helper.apk](https://github.com/hiliving/jump-wechat-helper/raw/master/app/jump-helper.apk)体验