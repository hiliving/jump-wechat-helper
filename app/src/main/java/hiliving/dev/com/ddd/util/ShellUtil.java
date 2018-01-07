package hiliving.dev.com.ddd.util;

import com.jaredrummler.android.shell.Shell;

/**
 * Created by Huangyong on 2018/1/6.
 */

public class ShellUtil {

    public static void execSu(String cmd){
        Shell.SU.run(cmd);
    }
    public static void execSH(String cmd){
        Shell.SH.run(cmd);
    }
}
