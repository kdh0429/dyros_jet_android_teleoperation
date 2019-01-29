package org.ros.android.android_tutorial_teleop;

import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Vibrator;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;


public class rcvthread implements Runnable {

    private logger logger;
    private MyView myview;
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;

    private int flag;

    private final int sizeBuf = 16392;
    private Socket socket;
    private String rcvData = "Error";
    private int rcvBufSize;
    private float[] array;
    private String[] rcvstr;

    private int step_number;
    private int current_step;
    private int i;

    public boolean singularity;

    public rcvthread(logger logger, Socket socket, MyView myview, Vibrator vibrator, MediaPlayer mediaPlayer) {
        this.myview = myview;
        this.logger = logger;
        flag = 1;
        this.socket = socket;
        this.vibrator = vibrator;
        this.mediaPlayer = mediaPlayer;
    }

    public void setFlag(int setflag) {
        flag = setflag;
    }

    public boolean getSingularity() { return singularity;}

    public void run() {
        while (flag == 1) {
            try {
                //rcvData = "";
                singularity = false;
                byte[] rcvBuf = new byte[sizeBuf];
                rcvBufSize = socket.getInputStream().read(rcvBuf);
                rcvData = new String(rcvBuf, 0, rcvBufSize, "UTF-8");
                logger.log(rcvData);
                rcvstr = rcvData.split(" ");


                if (rcvstr[0].equals("map_init")) {
                    //logger.log("here1");

                    step_number = Integer.parseInt(rcvstr[1]);
                    array = new float[2 * step_number + 1];

                    for (i = 0; i < 2 * step_number + 1; i++) {
                        array[i] = Float.parseFloat(rcvstr[i + 1]);
                    }

                    myview.SetData(step_number, array);
                    myview.postInvalidate();
                }

                if (rcvstr[0].equals("current_step"))
                {
                   // logger.log("here2");
                    current_step = Integer.parseInt(rcvstr[1]);
                    myview.StepRefresh(current_step);
                    myview.postInvalidate();
                }

                if (rcvstr[0].equals("singularity"))
                {
                    singularity = true;

                    if(!mediaPlayer.isPlaying())
                        mediaPlayer.start();

                    if(rcvstr[1].equals("1"))
                        vibrator.vibrate(300);
                }

                if (rcvData.compareTo("[close]") == 0) {
                    flag = 0;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.log("Exit loop");
    }
}
