package hiliving.dev.com.ddd;

import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import hiliving.dev.com.ddd.util.ShellUtil;

public class MainActivity extends AppCompatActivity {

    private Intent jump;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       /* TextView sysmsg = (TextView) findViewById(R.id.sysmsg);
        sysmsg.setText("分辨率："+ DpiUtils.getMobileInfo(this)[0]+"*"+DpiUtils.getMobileInfo(this)[1]);*/
        if (Build.VERSION.SDK_INT >= 23) {
            if (Settings.canDrawOverlays(MainActivity.this)) {
                ShellUtil.execSu("su");
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                Toast.makeText(MainActivity.this,"需要取得使用悬浮窗权限",Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        } else {
            ShellUtil.execSu("su");
        }

        jump = new Intent(MainActivity.this,JumpService.class);
    }
    //启动服务
    public void start(View view){
        startService(jump);
    }
}
