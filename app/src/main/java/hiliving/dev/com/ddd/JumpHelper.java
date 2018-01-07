
package hiliving.dev.com.ddd;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import hiliving.dev.com.ddd.util.ShellUtil;

import static hiliving.dev.com.ddd.Config.RUNNINGCMD;


/**
 * 参考知乎
 *
 *  @link <a href="https://zhuanlan.zhihu.com/p/32452473" target="_blank">https://zhuanlan.zhihu.com/p/32452473</a>
 *
 * 跳一跳辅助
 *
 * @author LeeHo
 * 修改 2018.1.7 by hiliving
 */
public class JumpHelper
{
    public String log = "";
    private static final String IMAGE_NAME              = "current.png";

    private static final String STORE_DIR               = "/sdcard/jump_screencapture/";

    //数量
    private static final int    imageLengthLength       = 5;

    //存放图片的大小
    private static final long[] imageLength             = new long[imageLengthLength];

    private final RGBInfo       rgbInfo                 = new RGBInfo();

    private final String     ADB_SCREEN_CAPTURE_CMDS =
           "/system/bin/screencap -p  "+STORE_DIR + IMAGE_NAME;

    //截屏中游戏分数显示区域最下方的Y坐标，300是 1920x1080的值，根据实际情况修改
    private final int           gameScoreBottomY        = 300;

    //按压的时间系数，可根据具体情况适当调节
    private final double        pressTimeCoefficient    = 1.357;

    //按压的起始点坐标，也是再来一局的起始点坐标
    private final int           swipeX                  = 550;

    private final int           swipeY                  = 1580;

    //二分之一的棋子底座高度
    private final int           halfBaseBoardHeight     = 20;

    //棋子的宽度，从截屏中量取，自行调节
    private final int           halmaBodyWidth          = 74;

    //游戏截屏里的两个跳板的中点坐标，主要用来计算角度，可依据实际的截屏计算，计算XY的比例
    private final int           boardX1                 = 813;

    private final int           boardY1                 = 1122;

    private final int           boardX2                 = 310;

    private final int           boardY2                 = 813;

    /**
     * 获取跳棋以及下一块跳板的中心坐标
     *
     * @return
     * @author LeeHo
     * @throws IOException
     * @update 2017年12月31日 下午12:18:22
     */
    private int[] getHalmaAndBoardXYValue(File currentImage) throws IOException
    {
        Bitmap bufferedImage = BitmapFactory.decodeFile(currentImage.getPath());
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        System.out.println("宽度：" + width + "，高度：" + height);
        int halmaXSum = 0;
        int halmaXCount = 0;
        int halmaYMax = 0;
        int boardX = 0;
        int boardY = 0;
        //从截屏从上往下逐行遍历像素点，以棋子颜色作为位置识别的依据，最终取出棋子颜色最低行所有像素点的平均值，即计算出棋子所在的坐标
        Log.d("TIMECACULATE",System.currentTimeMillis()+"");
        for (int y = gameScoreBottomY; y < height; y+=10)
        {
            for (int x = 0; x < width; x+=10)
            {
                processRGBInfo(bufferedImage, x, y);
                int rValue = rgbInfo.getRValue();
                int gValue = rgbInfo.getGValue();
                int bValue = rgbInfo.getBValue();
                //根据RGB的颜色来识别棋子的位置，
                if (rValue > 50 && rValue < 60 && gValue > 53 && gValue < 63 && bValue > 95 && bValue < 110)
                {
                    halmaXSum += x;
                    halmaXCount++;
                    //棋子底行的Y坐标值
                    halmaYMax = y > halmaYMax ? y : halmaYMax;
                }
            }
        }
        Log.d("TIMECACULATE",System.currentTimeMillis()+"");
        if (halmaXSum != 0 && halmaXCount != 0)
        {
            //棋子底行的X坐标值
            int halmaX = halmaXSum / halmaXCount;
            //上移棋子底盘高度的一半
            int halmaY = halmaYMax - halfBaseBoardHeight;
            //从gameScoreBottomY开始
            for (int y = gameScoreBottomY; y < height; y+=10)
            {
                processRGBInfo(bufferedImage, 0, y);
                int lastPixelR = this.rgbInfo.getRValue();
                int lastPixelG = this.rgbInfo.getGValue();
                int lastPixelB = this.rgbInfo.getBValue();
                //只要计算出来的boardX的值大于0，就表示下个跳板的中心坐标X值取到了。
                if (boardX > 0)
                {
                    break;
                }
                int boardXSum = 0;
                int boardXCount = 0;
                for (int x = 0; x < width; x+=10)
                {
                    processRGBInfo(bufferedImage, x, y);
                    int pixelR = rgbInfo.getRValue();
                    int pixelG = rgbInfo.getGValue();
                    int pixelB = rgbInfo.getBValue();
                    //处理棋子头部比下一个跳板还高的情况
                    if (Math.abs(x - halmaX) < halmaBodyWidth)
                    {
                        continue;
                    }

                    //从上往下逐行扫描至下一个跳板的顶点位置，下个跳板可能为圆形，也可能为方框，取多个点，求平均值
                    if ((Math.abs(pixelR - lastPixelR) + Math.abs(pixelG - lastPixelG) + Math.abs(pixelB - lastPixelB)) > 10)
                    {
                        boardXSum += x;
                        boardXCount++;
                    }
                }

                if (boardXSum > 0)
                {
                    boardX = boardXSum / boardXCount;
                }
            }

            //按实际的角度来算，找到接近下一个 board 中心的坐标
            boardY = (int) (halmaY - Math.abs(boardX - halmaX) * Math.abs(boardY1 - boardY2)
                    / Math.abs(boardX1 - boardX2));
            if (boardX > 0 && boardY > 0)
            {
                int[] result = new int[4];
                //棋子的X坐标
                result[0] = halmaX;
                //棋子的Y坐标
                result[1] = halmaY;
                //下一块跳板的X坐标
                result[2] = boardX;
                //下一块跳板的Y坐标
                result[3] = boardY;
                return result;
            }
        }

        return null;
    }


    /**
     * ADB获取安卓截屏
     *
     * @author LeeHo
     * @update 2017年12月31日 下午12:11:42
     */
    private void executeADBCaptureCommands()
    {
        ShellUtil.execSu(ADB_SCREEN_CAPTURE_CMDS);
    }

    /**
     * 跳一下
     *
     * @param distance
     * @author LeeHo
     * @update 2017年12月31日 下午12:23:19
     */
    private void doJump(double distance)
    {
        System.out.println("distance: " + distance);
        //计算按压时间，最小200毫秒
        int pressTime = (int) Math.max(distance * pressTimeCoefficient, 200);
        System.out.println("pressTime: " + pressTime);
        //执行按压操作
        Log.d("pressTime","**"+swipeX+"**"+swipeY+"**"+swipeX+"**"+swipeY);
        ShellUtil.execSu("input touchscreen swipe "+swipeX+" "+swipeY+" "+swipeX+" "+swipeY+" "+pressTime);
    }

    /**
     * 再来一局
     *
     * @author LeeHo
     * @update 2017年12月31日 下午12:47:06
     */
    private void replayGame()
    {
        ShellUtil.execSu("input touchscreen tap "+swipeX +" "+swipeY);
    }

    /**
     * 计算跳跃的距离，也即两个点之间的距离
     *
     * @param halmaX
     * @param halmaY
     * @param boardX
     * @param boardY
     * @return
     * @author LeeHo
     * @update 2017年12月31日 下午12:27:30
     */
    private double computeJumpDistance(int halmaX, int halmaY, int boardX, int boardY)
    {
        return Math.sqrt(Math.pow(Math.abs(boardX - halmaX), 2) + Math.pow(Math.abs(boardY - halmaY), 2));
    }
    public  void exeProgram(){
        if (sendMsgListener!=null){
            sendMsgListener.onSend("准备截屏，请稍后");
        }
        try
        {
            File storeDir = new File(STORE_DIR);
            if (!storeDir.exists()) {
                boolean flag = storeDir.mkdir();
                if (!flag) {
                    System.err.println("创建图片存储目录失败");
                    return;
                }
            }

            JumpHelper jumpjumpHelper = new JumpHelper();
            //执行次数
            int executeCount = 0;
            while (RUNNINGCMD)
            {
                //执行ADB命令，获取安卓截屏
                jumpjumpHelper.executeADBCaptureCommands();
                File currentImage = new File(STORE_DIR, IMAGE_NAME);
                if (!currentImage.exists())
                {
                    System.out.println("图片不存在");
                    continue;
                }

                long length = currentImage.length();
                imageLength[executeCount % imageLengthLength] = length;
                //查看是否需要重新开局
                jumpjumpHelper.checkDoReplay();
                executeCount++;
                System.out.println("当前第" + executeCount + "次执行!");
                //获取跳棋和底板的中心坐标

                int[] result = jumpjumpHelper.getHalmaAndBoardXYValue(currentImage);
                if (result == null)
                {
                    System.out.println("The result of method getHalmaAndBoardXYValue is null!");
                    continue;
                }
                int halmaX = result[0];
                int halmaY = result[1];
                int boardX = result[2];
                int boardY = result[3];
                System.out.println("halmaX: " + halmaX + ", halmaY: " + halmaY + ", boardX: " + boardX + ", boardY: "
                        + boardY);
                //计算跳跃的距离

                double jumpDistance = jumpjumpHelper.computeJumpDistance(halmaX, halmaY, boardX, boardY);
                if (sendMsgListener!=null){
                    sendMsgListener.onSend("正在分析数据……\n"+"分析完成，准备跳");
                }
                jumpjumpHelper.doJump(jumpDistance);
                //每次停留2秒，加入随机种子，避免被认为作弊
                double rand = (Math.random())+2;//2-3秒
                if (sendMsgListener!=null){
                    BigDecimal b = new BigDecimal(rand);
                    double f1 = b.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
                    sendMsgListener.onSend("正在获取图形数据,"+f1*1000+"秒后继续");
                }
                TimeUnit.MILLISECONDS.sleep((long) (rand*1000));
                if (sendMsgListener!=null){
                    sendMsgListener.onSend("正在分析数据……\n"+"");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 检查是否需要重新开局
     *
     * @author LeeHo
     * @update 2017年12月31日 下午1:39:18
     */
    private void checkDoReplay()
    {
        if (imageLength[0] > 0 && imageLength[0] == imageLength[1] && imageLength[1] == imageLength[2]
                && imageLength[2] == imageLength[3] && imageLength[3] == imageLength[4])
        {
            //此时表示已经连续5次图片大小一样了，可知当前屏幕处于再来一局
            //模拟点击再来一局按钮重新开局
            replayGame();
        }
    }

    /**
     * 获取指定坐标的RGB值
     *
     * @param bufferedImage
     * @param x
     * @param y
     * @author LeeHo
     * @update 2017年12月31日 下午12:12:43
     */
    private void processRGBInfo(Bitmap bufferedImage, int x, int y)
    {
        this.rgbInfo.reset();
        int color = bufferedImage.getPixel(x, y);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        //转换为RGB数字
        rgbInfo.setRValue(r);
        rgbInfo.setGValue(g);
        rgbInfo.setBValue(b);
    }

    class RGBInfo
    {
        private int RValue;

        private int GValue;

        private int BValue;

        public int getRValue()
        {
            return RValue;
        }

        public void setRValue(int rValue)
        {
            RValue = rValue;
        }

        public int getGValue()
        {
            return GValue;
        }

        public void setGValue(int gValue)
        {
            GValue = gValue;
        }

        public int getBValue()
        {
            return BValue;
        }

        public void setBValue(int bValue)
        {
            BValue = bValue;
        }

        public void reset()
        {
            this.RValue = 0;
            this.GValue = 0;
            this.BValue = 0;
        }
    }


    /**
     * 消息接口，和逻辑无关，只发送消息
     */
    public interface SendMsgListener{
        void onSend(String msg);
    }
    private  SendMsgListener sendMsgListener;
    public void setOnSendListener(SendMsgListener sendListener){
        this.sendMsgListener = sendListener;
    }
}

