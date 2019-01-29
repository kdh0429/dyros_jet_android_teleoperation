/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.android.android_tutorial_teleop;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.ros.address.InetAddressFactory;
import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.view.RosImageView;

//button

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import android.os.StrictMode;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import android.content.Intent;

import android.hardware.input.InputManager.InputDeviceListener;

import android.view.InputDevice;
import android.view.MotionEvent;
import android.widget.Toast;

import android.widget.TabHost;

import android.hardware.Sensor;
import android.hardware.SensorManager;


public class MainActivity extends RosActivity implements OnClickListener, InputDeviceListener, OnTouchListener {


    private RosImageView<sensor_msgs.CompressedImage> image;
    private String server = "192.168.1.88";                         // server address
    private Socket socket;
    private int port = 8888;                                        // port number
    private OutputStream outs;
    private Thread rcvThread;
    private rcvthread rcvthread;
    public logger logger;
    public logger logger_drag;
    public logger logger_Complement_L;
    public logger logger_Complement_R;
    public logger logger_Proximity;
    public logger logger_GPS;

    private InputMethodManager imm;

    private TextView textViewStatus;
    private TextView textViewDrag;
    private TextView textviewComplement_L;
    private TextView textviewComplement_R;
    private TextView textviewProximity;
    private TextView textviewGPS;
    private ImageView imageviewmode;

    private Button buttonUpL;
    private Button buttonDownL;
    private Button buttonRightL;
    private Button buttonLeftL;
    private Button buttonForwardL;
    private Button buttonBackwardL;
    private Button buttonUpR;
    private Button buttonDownR;
    private Button buttonRightR;
    private Button buttonLeftR;
    private Button buttonForwardR;
    private Button buttonBackwardR;
    private Button buttonWalk;
    private Button buttonDrag;
    private Button buttonModechange;
    private Button buttonRoll;
    private Button buttonPitch;
    private Button buttonYaw;
    private Button buttonComplement_L;
    private Button buttonComplement_R;
    private Button buttonProximity_U;
    private Button buttonProximity_D;
    private Button buttonGPS;

    private Switch switchArm;
    private Switch switchDirection;

    private TabHost tabHost1;
    private TabHost.TabSpec ts1;
    private TabHost.TabSpec ts2;
    private TabHost.TabSpec ts3;
    private TabHost.TabSpec ts4;
    private TabHost.TabSpec ts5;
    private TabHost.TabSpec ts6;

    private EditText edittextwalkdistance_x;
    private EditText edittextwalkdistance_y;
    private EditText edittextwalkdistance_theta;
    private String sndOpkey;

    private float[] joyarray = new float[19];                           // joystick signal array
    private float[] joyarray_pre = new float[19];

    private Vibrator vibrator;
    public Vibrator vibrator_phone;
    private boolean isvibrator;

    private int mode = 0;
    private boolean startmode = true;
    private int joy_even;

    private boolean isfirst_L=true;
    private boolean isfirst_R=true;
    private boolean isfirst_Proximity_U=true;
    private boolean isfirst_Proximity_D=true;
    private boolean closed;
    private NodeConfiguration nodeConfiguration;

    private boolean joybuttonup;

    private int selected_plane;
    private int dragXup;
    private int dragXdown;
    private int dragYup;
    private int dragYdown;

    private MyView myview;

    //Using the Accelometer & Gyroscoper
    private SensorManager mSensorManager_L = null;
    private SensorManager mSensorManager_R = null;


    //Using the Accelometer
    private Sensor mAccelometerSensor_L = null;
    private Sensor mAccelometerSensor_R = null;

    //Using the Gyroscope
    private Sensor mGgyroSensor_L = null;
    private Sensor mGgyroSensor_R = null;
    //
    private UserSensorListner userSensorListner_L;
    private UserSensorListner userSensorListner_R;

    private boolean running_L;
    private boolean running_R;
    private boolean running_Proximity_U;
    private boolean running_Proximity_D;

    private SensorManager mSensorManager_Proximity = null;
    private Sensor mProximitySensor = null;
    private UserSensorListner userSensorListner_Proximity_U;
    private UserSensorListner userSensorListner_Proximity_D;
    private GpsInfo gps;

    private MediaPlayer mediaPlayer;

    public MainActivity() {
        super("Teleop", "Teleop");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build();
        StrictMode.setThreadPolicy(policy);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        vibrator_phone = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        mediaPlayer = (MediaPlayer) MediaPlayer.create(this,R.raw.warning___singularity);

        image = (RosImageView<sensor_msgs.CompressedImage>) findViewById(R.id.image);
        image.setTopicName("camera_image/compressed");
        image.setMessageType(sensor_msgs.CompressedImage._TYPE);
        image.setMessageToBitmapCallable(new BitmapFromCompressedImage());

        textViewStatus = (TextView) this.findViewById(R.id.textViewStatus);
        textViewStatus.setText("TeleOp Client");
        logger = new logger(textViewStatus);

        textViewDrag = (TextView) this.findViewById(R.id.textViewDrag);
        textViewDrag.setText("Select plane on option menu");
        logger_drag = new logger(textViewDrag);

        textviewComplement_L = (TextView) this.findViewById(R.id.textviewComplement_L);
        logger_Complement_L = new logger(textviewComplement_L);
        textviewComplement_R = (TextView) this.findViewById(R.id.textviewComplement_R);
        logger_Complement_R = new logger(textviewComplement_R);
        textviewProximity = (TextView) this.findViewById(R.id.textviewProximity);
        logger_Proximity = new logger(textviewProximity);

        textviewGPS = (TextView) this.findViewById(R.id.textviewGPS);
        logger_GPS = new logger(textviewGPS);

        imageviewmode = (ImageView) this.findViewById(R.id.imageViewMode);
        imageviewmode.setOnClickListener(this);

        buttonModechange = (Button) this.findViewById(R.id.buttonModechange);
        buttonUpL = (Button) this.findViewById(R.id.buttonUpL);
        buttonDownL = (Button) this.findViewById(R.id.buttonDownL);
        buttonRightL = (Button) this.findViewById(R.id.buttonRightL);
        buttonLeftL = (Button) this.findViewById(R.id.buttonLeftL);
        buttonForwardL = (Button) this.findViewById(R.id.buttonForwardL);
        buttonBackwardL = (Button) this.findViewById(R.id.buttonBackwardL);
        buttonUpR = (Button) this.findViewById(R.id.buttonUpR);
        buttonDownR = (Button) this.findViewById(R.id.buttonDownR);
        buttonRightR = (Button) this.findViewById(R.id.buttonRightR);
        buttonLeftR = (Button) this.findViewById(R.id.buttonLeftR);
        buttonForwardR = (Button) this.findViewById(R.id.buttonForwardR);
        buttonBackwardR = (Button) this.findViewById(R.id.buttonBackwardR);
        buttonWalk = (Button) this.findViewById(R.id.buttonWalk);
        buttonDrag = (Button) this.findViewById(R.id.buttonDrag);
        edittextwalkdistance_x = (EditText) this.findViewById(R.id.editTextWalkdistance_x);
        edittextwalkdistance_y = (EditText) this.findViewById(R.id.editTextWalkdistance_y);
        edittextwalkdistance_theta = (EditText) this.findViewById(R.id.editTextWalkdistance_theta);
        buttonRoll = (Button) this.findViewById(R.id.buttonRoll);
        buttonPitch = (Button) this.findViewById(R.id.buttonPitch);
        buttonYaw = (Button) this.findViewById(R.id.buttonYaw);
        buttonComplement_L = (Button) this.findViewById(R.id.buttonComplement_L);
        buttonComplement_R = (Button) this.findViewById(R.id.buttonComplement_R);
        buttonProximity_U = (Button) this.findViewById(R.id.buttonProximity_U);
        buttonProximity_D = (Button) this.findViewById(R.id.buttonProximity_D);
        buttonGPS = (Button) this.findViewById(R.id.buttonGPS);

        switchArm = (Switch) this.findViewById(R.id.switchArm);
        switchDirection = (Switch) this.findViewById(R.id.switchDirection);

        buttonUpL.setOnTouchListener(this);
        buttonDownL.setOnTouchListener(this);
        buttonRightL.setOnTouchListener(this);
        buttonLeftL.setOnTouchListener(this);
        buttonForwardL.setOnTouchListener(this);
        buttonBackwardL.setOnTouchListener(this);
        buttonUpR.setOnTouchListener(this);
        buttonDownR.setOnTouchListener(this);
        buttonRightR.setOnTouchListener(this);
        buttonLeftR.setOnTouchListener(this);
        buttonForwardR.setOnTouchListener(this);
        buttonBackwardR.setOnTouchListener(this);
        buttonDrag.setOnTouchListener(this);
        buttonWalk.setOnClickListener(this);
        buttonModechange.setOnClickListener(this);
        buttonRoll.setOnTouchListener(this);
        buttonPitch.setOnTouchListener(this);
        buttonYaw.setOnTouchListener(this);
        buttonComplement_L.setOnClickListener(this);
        buttonComplement_R.setOnClickListener(this);
        buttonProximity_U.setOnClickListener(this);
        buttonProximity_D.setOnClickListener(this);
        buttonGPS.setOnClickListener(this);

        myview = (MyView) this.findViewById(R.id.myview);

        //Using the Gyroscope & Accelometer
        mSensorManager_L = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager_R = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Using the Accelometer
        mAccelometerSensor_L = mSensorManager_L.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccelometerSensor_R = mSensorManager_R.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //Using the Accelometer
        mGgyroSensor_L = mSensorManager_L.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mGgyroSensor_R = mSensorManager_R.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mSensorManager_Proximity = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximitySensor = mSensorManager_Proximity.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        tabHost1 = (TabHost) findViewById(R.id.tabHost1);
        tabHost1.setup();

        // 첫 번째 Tab. (탭 표시 텍스트:"Joystick"), (페이지 뷰:"content1")
        ts1 = tabHost1.newTabSpec("Tab Spec 1");
        ts1.setContent(R.id.JoyLayout);
        ts1.setIndicator("", ContextCompat.getDrawable(getBaseContext(), R.drawable.joystick));
        tabHost1.addTab(ts1);

        // 두 번째 Tab. (탭 표시 텍스트:"TAB 2"), (페이지 뷰:"content2")
        ts2 = tabHost1.newTabSpec("Tab Spec 2");
        ts2.setContent(R.id.TaskLayout);
        ts2.setIndicator("Task cmd");
        ts2.setIndicator("", ContextCompat.getDrawable(getBaseContext(), R.drawable.touch));
        tabHost1.addTab(ts2);

        // 세 번째 Tab. (탭 표시 텍스트:"TAB 3"), (페이지 뷰:"content3")
        ts3 = tabHost1.newTabSpec("Tab Spec 3");
        ts3.setContent(R.id.DragLayout);
        ts3.setIndicator("Drag ctrl");
        ts3.setIndicator("", ContextCompat.getDrawable(getBaseContext(), R.drawable.drag));
        tabHost1.addTab(ts3);

        // 네 번째 Tab
        ts4 = tabHost1.newTabSpec("Tab Spec 4");
        ts4.setContent(R.id.Gyrolayout);
        ts4.setIndicator("Gyro");
        ts4.setIndicator("", ContextCompat.getDrawable(getBaseContext(), R.drawable.tilt));
        tabHost1.addTab(ts4);

        //  다섯 번째 Tab. (탭 표시 텍스트:"TAB 5"), (페이지 뷰:"content5")
        ts5 = tabHost1.newTabSpec("Tab Spec 5");
        ts5.setContent(R.id.FootLayout);
        ts5.setIndicator("Map");
        ts5.setIndicator("", ContextCompat.getDrawable(getBaseContext(), R.drawable.footprint));
        tabHost1.addTab(ts5);

        //  여섯 번째 Tab. (탭 표시 텍스트:"TAB 6"), (페이지 뷰:"content6")
        ts6 = tabHost1.newTabSpec("Tab Spec 6");
        ts6.setContent(R.id.GPSlayout);
        ts6.setIndicator("GPS");
        ts6.setIndicator("", ContextCompat.getDrawable(getBaseContext(), R.drawable.gps));
        tabHost1.addTab(ts6);

    }


    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        nodeConfiguration = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(), getMasterUri());
        nodeMainExecutor.execute(image, nodeConfiguration.setNodeName("android"));

        // get server address from masteruri. MasterUri is form of http;//192.169.1.96:11311. So trim http:// and :11311/ (substring)
        server = getMasterUri().toString();
        int num = server.length();
        server = server.substring(7, num - 7);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case (R.id.connect):
                if (!item.isChecked()) {
                    try {
                        if (socket != null) {
                            socket.close();
                            socket = null;
                        }

                        socket = new Socket(server, port);
                        socket.setReceiveBufferSize(16392);
                        socket.setTcpNoDelay(true);
                        outs = socket.getOutputStream();

                        rcvthread= new rcvthread(logger, socket, myview,vibrator_phone,mediaPlayer);
                        rcvThread = new Thread(rcvthread);
                        rcvThread.start();

                        item.setChecked(true);
                        logger.log("Connected");
                        Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        logger.log("Fail to connect");
                        e.printStackTrace();
                    }

                    if (closed) {
                        nodeMainExecutorService.execute(image, nodeConfiguration.setDefaultNodeName("android"));
                        closed = false;
                    }
                } else {
                    item.setChecked(false);
                    if (socket != null) {
                        exitFromRunLoop();
                        try {
                            socket.close();
                            socket = null;
                            logger.log("Closed!");
                            rcvThread = null;
                        } catch (IOException e) {
                            Toast.makeText(this, "Fail to close", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }

                        nodeMainExecutorService.shutdownNodeMain(image);
                        closed = true;
                    }
                    image.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.mipmap.icon));
                }
                break;

            case (R.id.Video_off):
                if (socket != null) {
                    if (!item.isChecked()) {
                        item.setChecked(true);
                        writeopkey("video_off");
                        nodeMainExecutorService.shutdownNodeMain(image);
                        image.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.mipmap.icon));
                        closed = true;
                    } else {
                        item.setChecked(false);
                        writeopkey("video_on");
                        if (closed) {
                            nodeMainExecutorService.execute(image, nodeConfiguration.setDefaultNodeName("android"));
                            closed = false;
                        }
                    }
                }
                break;

            case (R.id.planeXY):
                if (socket != null) {
                    if (!item.isChecked()) {
                        item.setChecked(true);
                        selected_plane = 1;
                    } else
                        item.setChecked(false);
                }
                break;
            case (R.id.planeYZ):
                if (socket != null) {
                    if (!item.isChecked()) {
                        item.setChecked(true);
                        selected_plane = 2;
                    } else
                        item.setChecked(false);
                }
                break;
            case (R.id.planeZX):
                if (socket != null) {
                    if (!item.isChecked()) {
                        item.setChecked(true);
                        selected_plane = 3;
                    } else
                        item.setChecked(false);
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View arg0) {

        if (socket != null) {
            if (arg0 == imageviewmode) {
                Intent intent = new Intent(this, ModeActivity.class);
                intent.putExtra("mode", mode);
                startActivity(intent);
            }

            if (arg0 == buttonWalk) {
                sndOpkey = "taskcmd Walk " + edittextwalkdistance_x.getText().toString() + " " + edittextwalkdistance_y.getText().toString() + " " + edittextwalkdistance_theta.getText().toString();
                writeopkey(sndOpkey);
                edittextwalkdistance_x.setText("");
                edittextwalkdistance_y.setText("");
                edittextwalkdistance_theta.setText("");
                imm.hideSoftInputFromWindow(edittextwalkdistance_x.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(edittextwalkdistance_y.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(edittextwalkdistance_theta.getWindowToken(), 0);
            }

            if (arg0 == buttonModechange) {
                startmode = false;
                logger.log(Integer.toString(myview.current_step));
                switch (mode) {
                    case 0:
                        imageviewmode.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.mode1));
                        joyarray[16] = 1.0f;
                        sndOpkey = "joy " + Arrays.toString(joyarray);
                        writeopkey(sndOpkey);
                        joyarray[16] = 0.0f;
                        sndOpkey = "joy " + Arrays.toString(joyarray);
                        writeopkey(sndOpkey);
                        mode = 1;
                        break;

                    case 1:
                        imageviewmode.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.mode2));
                        joyarray[16] = 1.0f;
                        sndOpkey = "joy " + Arrays.toString(joyarray);
                        writeopkey(sndOpkey);
                        joyarray[16] = 0.0f;
                        sndOpkey = "joy " + Arrays.toString(joyarray);
                        writeopkey(sndOpkey);
                        mode = 2;
                        break;
                    case 2:
                        imageviewmode.setImageDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.mode0));
                        joyarray[16] = 1.0f;
                        sndOpkey = "joy " + Arrays.toString(joyarray);
                        writeopkey(sndOpkey);
                        joyarray[16] = 0.0f;
                        sndOpkey = "joy " + Arrays.toString(joyarray);
                        writeopkey(sndOpkey);
                        mode = 0;
                        break;
                }
            }
            if (arg0 == buttonComplement_L) {
                if(isfirst_L) {
                    userSensorListner_L = new UserSensorListner(logger_Complement_L, outs, true, false, false, false);
                    isfirst_L=false;
                }
                if (!running_L) {
                    running_L = true;
                    mSensorManager_L.registerListener(userSensorListner_L, mGgyroSensor_L, SensorManager.SENSOR_DELAY_UI);
                    mSensorManager_L.registerListener(userSensorListner_L, mAccelometerSensor_L, SensorManager.SENSOR_DELAY_UI);
                }
                else if (running_L) {
                    running_L = false;
                    mSensorManager_L.unregisterListener(userSensorListner_L);
                    sndOpkey="tilt left "+0+" "+0;
                    writeopkey(sndOpkey);
                }
            }
            if (arg0 == buttonComplement_R) {
                if(isfirst_R) {
                    userSensorListner_R = new UserSensorListner(logger_Complement_R, outs, false, true, false, false);
                    isfirst_R=false;
                }
                if (!running_R) {
                    running_R = true;
                    mSensorManager_R.registerListener(userSensorListner_R, mGgyroSensor_R, SensorManager.SENSOR_DELAY_UI);
                    mSensorManager_R.registerListener(userSensorListner_R, mAccelometerSensor_R, SensorManager.SENSOR_DELAY_UI);
                }
                else if (running_R) {
                    running_R = false;
                    mSensorManager_R.unregisterListener(userSensorListner_R);
                    sndOpkey="tilt right "+0+" "+0;
                    writeopkey(sndOpkey);
                }
            }
            if (arg0 == buttonProximity_U) {
                if(isfirst_Proximity_U) {
                    userSensorListner_Proximity_U = new UserSensorListner(logger_Proximity, outs, false, false, true, false);
                    isfirst_Proximity_U=false;
                }

                if(running_Proximity_D)
                    buttonProximity_D.performClick();

                if (!running_Proximity_U) {
                    running_Proximity_U = true;
                    mSensorManager_Proximity.registerListener(userSensorListner_Proximity_U, mProximitySensor, SensorManager.SENSOR_DELAY_UI);
                }
                else if (running_Proximity_U) {
                    logger_Proximity.log("Off");
                    running_Proximity_U = false;
                    mSensorManager_Proximity.unregisterListener(userSensorListner_Proximity_U);
                    sndOpkey="proximity "+0;
                    writeopkey(sndOpkey);
                }
            }

            if (arg0 == buttonProximity_D) {
                if(isfirst_Proximity_D) {
                    userSensorListner_Proximity_D = new UserSensorListner(logger_Proximity, outs, false, false, false, true);
                    isfirst_Proximity_D=false;
                }

                if(running_Proximity_U)
                    buttonProximity_U.performClick();

                if (!running_Proximity_D) {
                    running_Proximity_D = true;
                    mSensorManager_Proximity.registerListener(userSensorListner_Proximity_D, mProximitySensor, SensorManager.SENSOR_DELAY_UI);
                }
                else if (running_Proximity_D) {
                    logger_Proximity.log("Off");
                    running_Proximity_D = false;
                    mSensorManager_Proximity.unregisterListener(userSensorListner_Proximity_D);
                    sndOpkey="proximity "+0;
                    writeopkey(sndOpkey);
                }
            }

            if (arg0 == buttonGPS)
            {
                gps = new GpsInfo(MainActivity.this);
                // GPS 사용유무 가져오기
                if (gps.isGetLocation()) {

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

                    logger_GPS.log("latitude : " + latitude + '\n' + "longitude : " + longitude);

                } else {
                    // GPS 를 사용할수 없으므로
                    gps.showSettingsAlert();
                }
            }

        }
    }


    public boolean onTouch(View v, MotionEvent event) {
        if (socket != null) {
            sndOpkey = "";

            if (v == buttonUpL) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    sndOpkey = "taskcmd Up L D";
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    sndOpkey = "taskcmd Up L U";
                }
            }

            if (v == buttonDownL) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    sndOpkey = "taskcmd Down L D";
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    sndOpkey = "taskcmd Down L U";
                }
            }

            if (v == buttonRightL) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    sndOpkey = "taskcmd Right L D";
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    sndOpkey = "taskcmd Right L U";
                }
            }

            if (v == buttonLeftL) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    sndOpkey = "taskcmd Left L D";
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    sndOpkey = "taskcmd Left L U";
                }
            }

            if (v == buttonForwardL) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    sndOpkey = "taskcmd Forward L D";
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    sndOpkey = "taskcmd Forward L U";
                }
            }

            if (v == buttonBackwardL) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    sndOpkey = "taskcmd Backward L D";
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    sndOpkey = "taskcmd Backward L U";
                }
            }

            if (v == buttonUpR) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    sndOpkey = "taskcmd Up R D";
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    sndOpkey = "taskcmd Up R U";
                }
            }

            if (v == buttonDownR) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    sndOpkey = "taskcmd Down R D";
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    sndOpkey = "taskcmd Down R U";
                }
            }

            if (v == buttonRightR) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    sndOpkey = "taskcmd Right R D";
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    sndOpkey = "taskcmd Right R U";
                }
            }

            if (v == buttonLeftR) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    sndOpkey = "taskcmd Left R D";
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    sndOpkey = "taskcmd Left R U";
                }
            }

            if (v == buttonForwardR) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    sndOpkey = "taskcmd Forward R D";
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    sndOpkey = "taskcmd Forward R U";
                }
            }

            if (v == buttonBackwardR) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    sndOpkey = "taskcmd Backward R D";
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    sndOpkey = "taskcmd Backward R U";
                }
            }

            if (v == buttonRoll) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!switchArm.isChecked()) {
                        if (!switchDirection.isChecked())
                            sndOpkey = "taskcmd Roll L CW";
                        else
                            sndOpkey = "taskcmd Roll L CCW";
                    } else {
                        if (!switchDirection.isChecked())
                            sndOpkey = "taskcmd Roll R CW";
                        else
                            sndOpkey = "taskcmd Roll R CCW";
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (!switchArm.isChecked())
                        sndOpkey = "taskcmd Roll L 0";
                    else
                        sndOpkey = "taskcmd Roll R 0";
                }
            }

            if (v == buttonPitch) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!switchArm.isChecked()) {
                        if (!switchDirection.isChecked())
                            sndOpkey = "taskcmd Pitch L CW";
                        else
                            sndOpkey = "taskcmd Pitch L CCW";
                    } else {
                        if (!switchDirection.isChecked())
                            sndOpkey = "taskcmd Pitch R CW";
                        else
                            sndOpkey = "taskcmd Pitch R CCW";
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (!switchArm.isChecked())
                        sndOpkey = "taskcmd Pitch L 0";
                    else
                        sndOpkey = "taskcmd Pitch R 0";
                }
            }

            if (v == buttonYaw) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!switchArm.isChecked()) {
                        if (!switchDirection.isChecked())
                            sndOpkey = "taskcmd Yaw L CW";
                        else
                            sndOpkey = "taskcmd Yaw L CCW";
                    } else {
                        if (!switchDirection.isChecked())
                            sndOpkey = "taskcmd Yaw R CW";
                        else
                            sndOpkey = "taskcmd Yaw R CCW";
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (!switchArm.isChecked())
                        sndOpkey = "taskcmd Yaw L 0";
                    else
                        sndOpkey = "taskcmd Yaw R 0";
                }
            }

            if (v == buttonDrag) {
                int width = v.getWidth();
                int height = v.getHeight();

                int X = (int) event.getX();
                int Y = (int) event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dragXdown = X;
                        dragYdown = Y;
                        logger_drag.log("ACTION_DOWN AT COORDS" + " X: " + Integer.toString(X) + " Y: " + Integer.toString(Y));
                        break;

                    case MotionEvent.ACTION_MOVE:
                        logger_drag.log("MOVE" + " X: " + Integer.toString(X) + " Y: " + Integer.toString(Y));
                        break;

                    case MotionEvent.ACTION_UP:
                        dragXup = X;
                        dragYup = Y;

                        if (selected_plane == 1) {
                            if (dragXdown < width / 2 && dragXup < width / 2)
                                logger_drag.log("LEFT ARM" + " X: " + Float.toString((float) 30 * (dragYdown - dragYup) / height) + " Y: " + Float.toString((float) 30 * (dragXdown - dragXup) / (width / 2)));
                            if (dragXdown > width / 2 && dragXup > width / 2)
                                logger_drag.log("RIGHT ARM" + " X: " + Float.toString((float) 30 * (dragYdown - dragYup) / height) + " Y: " + Float.toString((float) 30 * (dragXdown - dragXup) / (width / 2)));

                            sndOpkey = "dragctrl" + " " + "XY" + " " + Integer.toString(dragXdown) + " " + Integer.toString(dragYdown) + " " + Integer.toString(dragXup) + " " + Integer.toString(dragYup) + " " + Integer.toString(width) + " " + Integer.toString(height);
                        } else if (selected_plane == 2) {
                            if (dragXdown < width / 2 && dragXup < width / 2)
                                logger_drag.log("LEFT ARM" + " Y: " + Float.toString((float) 30 * (dragXdown - dragXup) / height) + " Z: " + Float.toString((float) 30 * (dragYdown - dragYup) / (width / 2)));
                            if (dragXdown > width / 2 && dragXup > width / 20)
                                logger_drag.log("RIGHT ARM" + " Y: " + Float.toString((float) 30 * (dragXdown - dragXup) / height) + " Z: " + Float.toString((float) 30 * (dragYdown - dragYup) / (width / 2)));

                            sndOpkey = "dragctrl" + " " + "YZ" + " " + Integer.toString(dragXdown) + " " + Integer.toString(dragYdown) + " " + Integer.toString(dragXup) + " " + Integer.toString(dragYup) + " " + Integer.toString(width) + " " + Integer.toString(height);
                        } else if (selected_plane == 3) {
                            if (dragXdown < width / 2 && dragXup < width / 2)
                                logger_drag.log("LEFT ARM" + " Z: " + Float.toString((float) 30 * (dragYdown - dragYup) / height) + " X: " + Float.toString((float) 30 * (dragXdown - dragXup) / (width / 2)));
                            if (dragXdown > width / 2 && dragXup > width / 2)
                                logger_drag.log("RIGHT ARM" + " Z: " + Float.toString((float) 30 * (dragYdown - dragYup) / height) + " X: " + Float.toString((float) 30 * (dragXdown - dragXup) / (width / 2)));

                            sndOpkey = "dragctrl" + " " + "ZX" + " " + Integer.toString(dragXdown) + " " + Integer.toString(dragYdown) + " " + Integer.toString(dragXup) + " " + Integer.toString(dragYup) + " " + Integer.toString(width) + " " + Integer.toString(height);
                        } else {
                            Toast.makeText(this, "select plane on option menu", Toast.LENGTH_SHORT).show();
                            openOptionsMenu();
                        }

                        break;
                }
            }

            writeopkey(sndOpkey);
        }
        return false;//false indicates the event is not consumed
    }

    // Joystick
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handle keys going down
        boolean handled = false;

        if (socket != null && mode != 0 && !startmode) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BUTTON_A:
                    handled = true;
                    joyarray[8] = 1.0f;
                    break;
                case KeyEvent.KEYCODE_BUTTON_B:
                    handled = true;
                    joyarray[9] = 1.0f;
                    break;
                case KeyEvent.KEYCODE_BUTTON_X:
                    handled = true;
                    joyarray[10] = 1.0f;
                    break;
                case KeyEvent.KEYCODE_BUTTON_Y:
                    handled = true;
                    joyarray[11] = 1.0f;
                    break;
                case KeyEvent.KEYCODE_BUTTON_L1:
                    handled = true;
                    joyarray[12] = 1.0f;
                    break;
                case KeyEvent.KEYCODE_BUTTON_R1:
                    handled = true;
                    joyarray[13] = 1.0f;
                    break;
                case KeyEvent.KEYCODE_BACK:
                    handled = true;
                    joyarray[14] = 1.0f;
                    break;
                case KeyEvent.KEYCODE_BUTTON_START:
                    handled = true;
                    joyarray[15] = 1.0f;
                    break;
                case KeyEvent.KEYCODE_BUTTON_THUMBL:
                    handled = true;
                    joyarray[17] = 1.0f;
                    break;
                case KeyEvent.KEYCODE_BUTTON_THUMBR:
                    handled = true;
                    joyarray[18] = 1.0f;
                    break;

                default:
                    break;
            }

            if (joybuttonup) {
                sndOpkey = "joy " + Arrays.toString(joyarray);
                writeopkey(sndOpkey);
            }

            joybuttonup = false;

            if (isvibrator && rcvthread.getSingularity())
                vibrator.vibrate(5);

        }
        return handled;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Handle keys going up.

        joybuttonup = true;
        boolean handled = false;

        if (socket != null && mode != 0 && !startmode) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BUTTON_A:
                    handled = true;
                    joyarray[8] = 0.0f;
                    break;
                case KeyEvent.KEYCODE_BUTTON_B:
                    handled = true;
                    joyarray[9] = 0.0f;
                    break;
                case KeyEvent.KEYCODE_BUTTON_X:
                    handled = true;
                    joyarray[10] = 0.0f;
                    break;
                case KeyEvent.KEYCODE_BUTTON_Y:
                    handled = true;
                    joyarray[11] = 0.0f;
                    break;
                case KeyEvent.KEYCODE_BUTTON_L1:
                    handled = true;
                    joyarray[12] = 0.0f;
                    break;
                case KeyEvent.KEYCODE_BUTTON_R1:
                    handled = true;
                    joyarray[13] = 0.0f;
                    break;
                case KeyEvent.KEYCODE_BACK:
                    handled = true;
                    joyarray[14] = 0.0f;
                    break;
                case KeyEvent.KEYCODE_BUTTON_START:
                    handled = true;
                    joyarray[15] = 0.0f;
                    break;
                case KeyEvent.KEYCODE_BUTTON_THUMBL:
                    handled = true;
                    joyarray[17] = 0.0f;
                    break;
                case KeyEvent.KEYCODE_BUTTON_THUMBR:
                    handled = true;
                    joyarray[18] = 0.0f;
                    break;
                default:
                    break;
            }


            sndOpkey = "joy " + Arrays.toString(joyarray);
            writeopkey(sndOpkey);
        }
        return handled;
    }

    public boolean dispatchGenericMotionEvent(MotionEvent event) {

        if (socket != null && mode != 0 && !startmode) {
            // Check that the event came from a game controller
            if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK && event.getAction() == MotionEvent.ACTION_MOVE) {
                // Process all historical movement samples in the batch
                final int historySize = event.getHistorySize();

                // Process the movements starting from the
                // earliest historical position in the batch
                for (int i = 0; i < historySize; i++) {
                    // Process the event at historical position i
                    processJoystickInput(event, i);
                }

                // Process the current movement sample in the batch (position -1)
                processJoystickInput(event, -1);
                return true;
            }
        }
        return super.dispatchGenericMotionEvent(event);
    }

    private void processJoystickInput(MotionEvent event, int historyPos) {

        InputDevice mInputDevice = event.getDevice();

        if (!isvibrator) {
            vibrator = mInputDevice.getVibrator();
            isvibrator = true;
        }

        if(isvibrator && rcvthread.getSingularity())
            vibrator.vibrate(5);

        float axis_x = -getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_X, historyPos);

        float axis_y = -getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_Y, historyPos);

        float axis_ltrigger = -2 * getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_LTRIGGER, historyPos) + 1;

        float axis_z_x = -getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_Z, historyPos);

        float axis_z_y = -getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_RZ, historyPos);

        float axis_rtrigger = -2 * getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_RTRIGGER, historyPos) + 1;

        float axis_hat_x = -getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_HAT_X, historyPos);

        float axis_hat_y = -getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_HAT_Y, historyPos);


        String joy = "AXIS_X :" + Float.toString(axis_x) + '\n'
                + "AXIS_Y :" + Float.toString(axis_y) + '\n'
                + "AXIS_LTRIGGER :" + Float.toString(axis_ltrigger) + '\n'
                + "AXIS_Z_X :" + Float.toString(axis_z_x) + '\n'
                + "AXIS_Z_Y :" + Float.toString(axis_z_y) + '\n'
                + "AXIS_RTRIGGER :" + Float.toString(axis_rtrigger) + '\n'
                + "AXIS_HAT_X :" + Float.toString(axis_hat_x) + '\n'
                + "AXIS_HAT_Y :" + Float.toString(axis_hat_y) + '\n';


        joyarray[0] = round2(axis_x, 3);
        joyarray[1] = round2(axis_y, 3);
        joyarray[2] = round2(axis_ltrigger, 3);
        joyarray[3] = round2(axis_z_x, 3);
        joyarray[4] = round2(axis_z_y, 3);
        joyarray[5] = round2(axis_rtrigger, 3);
        joyarray[6] = round2(axis_hat_x, 3);
        joyarray[7] = round2(axis_hat_y, 3);

        //logger.log(joy);

        if ((joyarray[6] != joyarray_pre[6]) || (joyarray[7] != joyarray_pre[7])) {
            sndOpkey = "joy " + Arrays.toString(joyarray);
            writeopkey(sndOpkey);
        } else if (joy_even % 4 == 0) {
            sndOpkey = "joy " + Arrays.toString(joyarray);
            writeopkey(sndOpkey);
        }

        joy_even++;
        joyarray_pre[6] = joyarray[6];
        joyarray_pre[7] = joyarray[7];
        logger.log(Float.toString(joyarray_pre[6]));

    }

    private static float getCenteredAxis(MotionEvent event,
                                         InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());


        if (range != null) {
            final float flat = range.getFlat();
            final float value =
                    historyPos < 0 ? event.getAxisValue(axis) :
                            event.getHistoricalAxisValue(axis, historyPos);


            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }

    public static float round2(float number, int scale) {
        int pow = 10;
        for (int i = 1; i < scale; i++)
            pow *= 10;
        float tmp = number * pow;
        return (float) (int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) / pow;
    }

    public void writeopkey(String Opkey) {
        try {
            outs.write(Opkey.getBytes("UTF-8"));
            outs.flush();
        } catch (IOException e) {
            logger.log("Fail to send");
            e.printStackTrace();
        }
    }

    void exitFromRunLoop() {
        sndOpkey = "[close]";
        writeopkey(sndOpkey);
    }

    @Override
    public void onInputDeviceAdded(int deviceId) {
        logger.log("JOY ADDED");
    }

    @Override
    public void onInputDeviceRemoved(int deviceId) {
        logger.log("JOY REMOVED");
    }

    @Override
    public void onInputDeviceChanged(int deviceId) {

    }

}
