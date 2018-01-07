package hiliving.dev.com.ddd;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;



/**
 * Created by Huangyong on 2018/1/6.
 */

public class JumpService extends Service {
    private static final String IMAGE_NAME              = "current.png";

    private static final String STORE_DIR               = "/sdcard/jump_screencapture/";
    private final String ADB_SCREEN_CAPTURE_PATH =
           STORE_DIR +IMAGE_NAME;
    //Log用的TAG
    private static final String TAG = "JumpService";

    //要引用的布局文件.
    RelativeLayout toucherLayout;

    //布局参数.
    WindowManager.LayoutParams params;

    //实例化的WindowManager.
    WindowManager windowManager;

    Button bt;

    //状态栏高度.（接下来会用到）
    int statusBarHeight = -1;
    private String text= "开始";
    private boolean isRunning =false;
    private JumpHelper helper;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 2:
                    Bundle bundle = msg.getData();
                    String msgS = bundle.getString("MSG");
                    tvLog.setText(msgS);
                    break;
            }
        }
    };
    private TextView tvLog;
    private ProgressBar loading;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        creatWindow();
        helper = new JumpHelper();
    }

    private void creatWindow() {
        //赋值WindowManager&LayoutParam.
        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        //设置type.系统提示型窗口，一般都在应用程序窗口之上.
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //设置效果为背景透明.
        params.format = PixelFormat.RGBA_8888;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        //设置窗口初始停靠位置.
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;

        //设置悬浮窗口长宽数据.
        //注意，这里的width和height均使用px而非dp.这里我偷了个懒
        //如果你想完全对应布局设置，需要先获取到机器的dpi
        //px与dp的换算为px = dp * (dpi / 160).
        params.width = 1080;
        params.height = 200;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        toucherLayout = (RelativeLayout) inflater.inflate(R.layout.window,null);
        //添加toucherlayout
        windowManager.addView(toucherLayout,params);

        Log.i(TAG,"toucherlayout-->left:" + toucherLayout.getLeft());
        Log.i(TAG,"toucherlayout-->right:" + toucherLayout.getRight());
        Log.i(TAG,"toucherlayout-->top:" + toucherLayout.getTop());
        Log.i(TAG,"toucherlayout-->bottom:" + toucherLayout.getBottom());

        //主动计算出当前View的宽高信息.
        toucherLayout.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);

        //用于检测状态栏高度.
        int resourceId = getResources().getIdentifier("status_bar_height","dimen","android");
        if (resourceId > 0)
        {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        Log.i(TAG,"状态栏高度为:" + statusBarHeight);

        //浮动窗口按钮.
        bt = (Button) toucherLayout.findViewById(R.id.startnow);
        tvLog = (TextView) toucherLayout.findViewById(R.id.tvlog);
        loading = (ProgressBar) toucherLayout.findViewById(R.id.loading);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMode();
            }
        });
    }

    private void toggleMode() {
        isRunning = !isRunning;
        if (isRunning){
            Config.RUNNINGCMD = true;//打开开关
            text = "停止";
            new UiautomatorThread().start();
            loading.setVisibility(View.VISIBLE);
        }else {
            text ="开始";
            Config.RUNNINGCMD = false;//关闭开关
            loading.setVisibility(View.GONE);
        }
        bt.setText(text);
    }

    class UiautomatorThread extends Thread implements JumpHelper.SendMsgListener {
        @Override
        public void run() {
            super.run();
            helper.setOnSendListener(this);
            helper.exeProgram();
        }

        @Override
        public void onSend(String msg) {
            Message message = Message.obtain();
            message.what = 2;
            Bundle bundle = new Bundle();
            bundle.putString("MSG",msg);
            message.setData(bundle);
            handler.sendMessage(message);
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
