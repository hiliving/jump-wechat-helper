package hiliving.dev.com.ddd.util;

import com.jaredrummler.android.shell.CommandResult;
import com.jaredrummler.android.shell.Shell;

/**
 * Created by Huangyong on 2018/1/6.
 */

public class ShellUtil {

    public static CommandResult execSu(String cmd){
       return Shell.SU.run(cmd);
    }
    public static void execSH(String cmd){
        Shell.SH.run(cmd);
    }
}
