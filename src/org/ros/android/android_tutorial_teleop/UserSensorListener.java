package org.ros.android.android_tutorial_teleop;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.io.IOException;
import java.io.OutputStream;

class UserSensorListner implements SensorEventListener {

    public float[] mGyroValues = new float[3];
    public float[] mAccValues = new float[3];
    public boolean gyroRunning;
    public boolean accRunning;
    public double mAccPitch, mAccRoll,mAccYaw;

    private float a = 0.2f;
    private double timestamp;
    private static final float NS2S = 1.0f/1000000000.0f;
    private double dt;
    private double temp;
    public double pitch = 0, roll = 0, yaw =0;
    private int quotient_pitch, quotient_roll;
    private int quotient_pitch_pre, quotient_roll_pre;
    private float distance_proximity;

    private logger logger;
    private OutputStream outs;

    private boolean left;
    private boolean right;
    private boolean proximity_up;
    private boolean proximity_down;
    private String sndOpkey;

    public UserSensorListner(logger logger, OutputStream outs, boolean left, boolean right, boolean proximity_up, boolean proximity_down) {
        this.logger = logger;
        this.outs = outs;
        this.left = left;
        this.right = right;
        this.proximity_up = proximity_up;
        this.proximity_down = proximity_down;
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {

            /** GYROSCOPE */
            case Sensor.TYPE_GYROSCOPE:

                    /*센서 값을 mGyroValues에 저장*/
                mGyroValues = event.values;

                if (!gyroRunning)
                    gyroRunning = true;

                break;

            /** ACCELEROMETER */
            case Sensor.TYPE_ACCELEROMETER:

                    /*센서 값을 mAccValues에 저장*/
                mAccValues = event.values;

                if (!accRunning)
                    accRunning = true;

                break;

            case Sensor.TYPE_PROXIMITY:

                if (proximity_up)
                    distance_proximity = event.values[0];
                else if (proximity_down)
                    distance_proximity = event.values[0]-event.sensor.getMaximumRange();

                if(distance_proximity > 0)
                    logger.log("UP");
                else if (distance_proximity==0)
                    logger.log("STOP");
                else
                    logger.log("DOWN");

                sndOpkey = "proximity " + distance_proximity;

                try {
                    outs.write(sndOpkey.getBytes("UTF-8"));
                    outs.flush();
                } catch (IOException e) {
                    logger.log("Fail to send");
                    e.printStackTrace();
                }

                break;
        }

        /**두 센서 새로운 값을 받으면 상보필터 적용*/
        if (gyroRunning && accRunning) {
            complementaty(event.timestamp);
        }

    }

    public void complementaty(double new_ts){

        /* 자이로랑 가속 해제 */
        gyroRunning = false;
        accRunning = false;

        /*센서 값 첫 출력시 dt(=timestamp - event.timestamp)에 오차가 생기므로 처음엔 break */
        if(timestamp == 0){
            timestamp = new_ts;
            return;
        }
        dt = (new_ts - timestamp) * NS2S; // ns->s 변환
        timestamp = new_ts;

        /* degree measure for accelerometer */
        mAccPitch = -Math.atan2(mAccValues[0], mAccValues[2]) * 180.0 / Math.PI; // Y 축 기준
        mAccRoll= Math.atan2(mAccValues[1], mAccValues[2]) * 180.0 / Math.PI; // X 축 기준
        mAccYaw= Math.atan2(mAccValues[0], mAccValues[1]) * 180.0 / Math.PI; // Z 축 기준

        /**
         * 1st complementary filter.
         *  mGyroValuess : 각속도 성분.
         *  mAccPitch : 가속도계를 통해 얻어낸 회전각.
         */
        temp = (1/a) * (mAccPitch - pitch) + mGyroValues[1];
        pitch = pitch + (temp*dt);
        pitch = round2(pitch, 3);
        quotient_pitch = (int)pitch/10;

        temp = (1/a) * (mAccRoll - roll) + mGyroValues[0];
        roll = roll + (temp*dt);
        roll = round2(roll, 3);
        quotient_roll = (int) roll/10;

        temp = (1/a) * (mAccYaw - yaw) + mGyroValues[2];
        yaw = yaw + (temp*dt);

        logger.log("roll : "+roll+'\n'+"pitch : "+pitch+'\n'+"yaw : "+yaw+'\n'+'\n'+"x: "+-quotient_roll*10+'\n'+"y: "+-quotient_pitch*10);

        logger.t.setBackgroundColor(Color.rgb(Math.max(Math.abs(quotient_roll*25),Math.abs(quotient_pitch*25)),0,0));

        if (left)
            sndOpkey="tilt left "+quotient_roll*10+" "+quotient_pitch*10;
        else if (right)
            sndOpkey="tilt right "+quotient_roll*10+" "+quotient_pitch*10;

        if(quotient_pitch!= quotient_pitch_pre || quotient_roll!=quotient_roll_pre) {
            try {
                outs.write(sndOpkey.getBytes("UTF-8"));
                outs.flush();
            } catch (IOException e) {
                logger.log("Fail to send");
                e.printStackTrace();
            }
        }

        quotient_roll_pre = quotient_roll;
        quotient_pitch_pre = quotient_pitch;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    public static double round2(double number, int scale) {
        int pow = 10;
        for (int i = 1; i < scale; i++)
            pow *= 10;
        double tmp = number * pow;
        return (double) (int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) / pow;
    }

}