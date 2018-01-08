package hiliving.dev.com.ddd;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.jaredrummler.android.shell.CommandResult;

import hiliving.dev.com.ddd.util.ShellUtil;

public class MainActivity extends AppCompatActivity {

    private Intent jump;
    private Button openwd;
    private Button startServ;
    private RelativeLayout root;
    private CommandResult result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        openwd = (Button) findViewById(R.id.openwd);
        startServ = (Button) findViewById(R.id.startServ);
       /* TextView sysmsg = (TextView) findViewById(R.id.sysmsg);
        sysmsg.setText("分辨率："+ DpiUtils.getMobileInfo(this)[0]+"*"+DpiUtils.getMobileInfo(this)[1]);*/
        root = (RelativeLayout) findViewById(R.id.root);
        jump = new Intent(MainActivity.this,JumpService.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkRoot();
    }

    /**
     * 检查悬浮窗体权限
     */
    private void checkP() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (Settings.canDrawOverlays(MainActivity.this)) {
                //已成功获取悬浮窗权限
                openwd.setVisibility(View.INVISIBLE);
                startServ.setVisibility(View.VISIBLE);
            } else {
                startServ.setVisibility(View.INVISIBLE);
                openwd.setVisibility(View.VISIBLE);
                //未获取悬浮窗权限，前往权限管理
                openwd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        startActivity(intent);
                    }
                });
            }
        } else {
            //6.0以下无需询问
            openwd.setVisibility(View.INVISIBLE);
            startServ.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 启动服务
     * @param view
     */
    public void start(View view){
        startService(jump);
    }

    /**
     * 运行环境监测
     */
    public void checkRoot(){
        result = ShellUtil.execSu("su");
        if (!result.isSuccessful()){
            openwd.setText("当前设备尚未root\n无法运行此程序");
            openwd.setTextColor(Color.DKGRAY);
            startServ.setVisibility(View.INVISIBLE);
            openwd.setVisibility(View.VISIBLE);
        }else {
            //检查悬浮窗权限
            checkP();
        }
    }
}
