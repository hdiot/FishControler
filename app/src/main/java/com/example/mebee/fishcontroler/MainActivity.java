package com.example.mebee.fishcontroler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mebee.fishcontroler.Utils.ConstantValues;
import com.example.mebee.fishcontroler.View.RockerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.mebee.fishcontroler.View.RockerView.DirectionMode.DIRECTION_2_VERTICAL;
import static com.example.mebee.fishcontroler.View.RockerView.DirectionMode.DIRECTION_8;

public class MainActivity extends AppCompatActivity {

    private static final int SOCKET_CONNECT_FAIL = 101;
    private static final int SOCKET_CONNECT_SUCCEED = 102;
    private static final int SOCKET_DISCONNECTED = 103;
    private static final int SOCKET_REVEIVE_CONTENT = 104;
    private static final int SOCKET_CLOSE = 105;
    private static final String LOWSPEECH = "1";
    private static final String HEIGHTSPEECH = "0";
    private static final String CAMERAOFF = "1";
    private static final String CAMERAON = "0";
    private static final String ElectromagnetON = "0";
    private static final String ElectromagnetOFF = "1";

    private static final String DEF_DERICTION = "1111111";


    @BindView(R.id.horizontal_rocker)
    RockerView horizontalRocker;
    @BindView(R.id.tv_horizontal_now_shake)
    TextView tvHorizontalNowShake;
    @BindView(R.id.vertical_rocker)
    RockerView verticalRocker;
    @BindView(R.id.tv_vertical_now_shake)
    TextView tvVerticalNowShake;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.et_humidity)
    EditText etHumidity;
    @BindView(R.id.et_score)
    EditText etScore;
    @BindView(R.id.pb_battery)
    ProgressBar pbBattery;
    @BindView(R.id.activity_main)
    LinearLayout activityMain;
    @BindView(R.id.sw_speech)
    Switch swSpeech;
    @BindView(R.id.sw_camera)
    Switch swCamera;
    @BindView(R.id.sw_electromagnet)
    Switch swElectromagnet;


    private String SocketReceiveTag = "SocketReceive";

    private AlertDialog.Builder mBuilder;
    private AlertDialog mAlertDialog;
    private View mAlertView;
    private EditText ET_IP;
    private EditText ET_Port;
    private Button BT_Connect;
    private Button BT_Disconnect;
    private EditText ET_ConnectState;


    private Socket mSocket;
    private String mIP;
    private String mPort;
    private PrintWriter mWriter;

    private BufferedReader mReader;
    private int mTmp;
    private char[] mCharBuffer;

    private String mVerticalDirection = ConstantValues.VERTICALDIR_CENTER;
    private String mHorizontalDirection = ConstantValues.HORIZONTAL_CENTER;
    private String mDirection = DEF_DERICTION;

    private boolean mConnectState = false;


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SOCKET_CONNECT_FAIL:
                    setViewStates(false);
                    Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                    break;
                case SOCKET_CONNECT_SUCCEED:
                    setViewStates(true);
                    Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                    mAlertDialog.dismiss();
                    break;
                case SOCKET_DISCONNECTED:
                    setViewStates(false);
                    Toast.makeText(MainActivity.this, "连接中断", Toast.LENGTH_SHORT).show();
                    break;
                case SOCKET_REVEIVE_CONTENT:
                    disposeReceiveData(msg.obj.toString());
                    break;
                case SOCKET_CLOSE:
                    setViewStates(false);
                    Toast.makeText(MainActivity.this, "已断开连接", Toast.LENGTH_SHORT).show();
                    mAlertDialog.dismiss();
                    break;
            }
        }
    };


    /**
     * 处理接收到的shuju
     *
     * @param s String 接收到字符串
     */
    private void disposeReceiveData(String s) {
        if (s != "") {
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
            if (s.charAt(0) == '0') {
                String mHumidity =  s.substring(1,3);

                String mScore = s.substring(3);

                etHumidity.setText(strToInt(mHumidity)+"");
                etScore.setText(strToInt(mScore)+"");

            } else if (s.charAt(0) == '1'){
                String mBattery = s.substring(1);

                final AlertDialog.Builder a;
                if (mBattery.equals("000000")) {
                    Log.d("mBattery", mBattery);
                    pbBattery.setProgress(0);
                    pbBattery.setBackgroundColor(getResources().getColor(R.color.red));
                    a = new AlertDialog.Builder(this);
                    a.setTitle("警告！");
                    a.setMessage("设备电量过低");
                    a.setPositiveButton("知道", null);
                    a.show();
                } else if (mBattery.equals("111111")){
                    Log.d("mBattery", mBattery);
                    pbBattery.setProgress(strToInt(mBattery)*2);
                    a = new AlertDialog.Builder(this);
                    a.setTitle("警告！");
                    a.setMessage("设备电池过压");
                    a.setPositiveButton("知道", null);
                    a.show();
                } else {
                    pbBattery.setProgress(strToInt(mBattery)*2);

                }
            }
        }

        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private int strToInt(String s) {
        int result = 0;

        if (s != ""){
            for(int i = 0; i < s.length(); i++){
                Log.d("Math", s.charAt(i)+"");
                if (s.charAt(i) == '1') {
                    result += Math.pow(2,s.length()-i-1);

                    Log.d("Math:", result+"");
                }
            }
            Log.d("Math", "------");
            return result;
        }

        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        initUI();
    }

    /**
     * 初始化界面
     */
    private void initUI() {
        initDeviceInfo();
        initAlert();
        setListener();
    }

    /**
     * 设置方向键监听
     */
    public void setListener() {
        horizontalRocker.setOnShakeListener(DIRECTION_8, new RockerView.OnShakeListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void direction(RockerView.Direction direction) {
                if (!mConnectState) {
                    Toast.makeText(MainActivity.this, "请先连接设备", Toast.LENGTH_SHORT).show();
                    mAlertDialog.show();
                } else {
                    mHorizontalDirection = ConstantValues.VERTICALDIR_CENTER;
                    if (direction == RockerView.Direction.DIRECTION_CENTER) {
                        mHorizontalDirection = ConstantValues.HORIZONTAL_CENTER;
                        tvHorizontalNowShake.setText("当前方向：中心");
                        sendDirection();
                    } else if (direction == RockerView.Direction.DIRECTION_UP) {
                        mHorizontalDirection = ConstantValues.HORIZONTAL_FORWER;
                        tvHorizontalNowShake.setText("当前方向：前");
                        sendDirection();
                    } else if (direction == RockerView.Direction.DIRECTION_UP_RIGHT) {
                        mHorizontalDirection = ConstantValues.HORIZONTAL_FORWER_RIGHT;
                        tvHorizontalNowShake.setText("当前方向：右前");
                        sendDirection();
                    } else if (direction == RockerView.Direction.DIRECTION_RIGHT) {
                        mHorizontalDirection = ConstantValues.HORIZONTAL_RIGHT;
                        tvHorizontalNowShake.setText("当前方向：右");
                        sendDirection();
                    } else if (direction == RockerView.Direction.DIRECTION_DOWN_RIGHT) {
                        mHorizontalDirection = ConstantValues.HORIZONTAL_BACK_RIGHT;
                        tvHorizontalNowShake.setText("当前方向：右后");
                        sendDirection();
                    } else if (direction == RockerView.Direction.DIRECTION_DOWN) {
                        mHorizontalDirection = ConstantValues.HORIZONTAL_BACK;
                        tvHorizontalNowShake.setText("当前方向：后");
                        sendDirection();
                    } else if (direction == RockerView.Direction.DIRECTION_DOWN_LEFT) {
                        mHorizontalDirection = ConstantValues.HORIZONTAL_BACK_LEFT;
                        tvHorizontalNowShake.setText("当前方向：左后");
                        sendDirection();
                    } else if (direction == RockerView.Direction.DIRECTION_LEFT) {
                        mHorizontalDirection = ConstantValues.HORIZONTAL_LEFT;
                        tvHorizontalNowShake.setText("当前方向：左");
                        sendDirection();
                    } else if (direction == RockerView.Direction.DIRECTION_UP_LEFT) {
                        mHorizontalDirection = ConstantValues.HORIZONTAL_FORWER_LEFT;
                        tvHorizontalNowShake.setText("当前方向：左前");
                        sendDirection();
                    } else {
                        mHorizontalDirection = ConstantValues.HORIZONTAL_CENTER;
                        sendDirection();
                    }
                }
            }

            @Override
            public void onFinish() {

            }
        });


        verticalRocker.setOnShakeListener(DIRECTION_2_VERTICAL, new RockerView.OnShakeListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void direction(RockerView.Direction direction) {

                if (!mConnectState) {
                    Toast.makeText(MainActivity.this, "请先连接设备", Toast.LENGTH_SHORT).show();
                    mAlertDialog.show();
                } else {
                    mVerticalDirection = ConstantValues.VERTICALDIR_CENTER;
                    if (direction == RockerView.Direction.DIRECTION_CENTER) {
                        mVerticalDirection = ConstantValues.VERTICALDIR_CENTER;
                        tvVerticalNowShake.setText("当前方向：中心");
                        sendDirection();
                    } else if (direction == RockerView.Direction.DIRECTION_DOWN) {
                        mVerticalDirection = ConstantValues.VERTICALDIR_DOWN;
                        sendDirection();
                        tvVerticalNowShake.setText("当前方向：下");
                    } else if (direction == RockerView.Direction.DIRECTION_UP) {
                        mVerticalDirection = ConstantValues.VERTICALDIR_UP;
                        sendDirection();
                        tvVerticalNowShake.setText("当前方向：上");
                    } else {
                        mVerticalDirection = ConstantValues.VERTICALDIR_CENTER;
                        sendDirection();
                    }
                }


            }


            @Override
            public void onFinish() {

            }
        });

        swCamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    swCamera.setText(swCamera.getTextOn());
                } else {
                    swCamera.setText(swCamera.getTextOff());
                }
            }
        });

        swSpeech.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    swSpeech.setText(swSpeech.getTextOn());
                } else {
                    swSpeech.setText(swSpeech.getTextOff());
                }
            }
        });

        swElectromagnet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    swElectromagnet.setText(swElectromagnet.getTextOn());
                } else {
                    swElectromagnet.setText(swElectromagnet.getTextOff());
                }
            }
        });

    }

    /**
     * 初始化连接设备的 AlertDialog
     */
    public void initAlert() {
        mBuilder = new AlertDialog.Builder(MainActivity.this);
        mAlertDialog = mBuilder.create();
        mAlertView = View.inflate(getApplicationContext(), R.layout.menu_dialog_view, null);
        ET_IP = (EditText) mAlertView.findViewById(R.id.et_menu_dia_ip);
        ET_Port = (EditText) mAlertView.findViewById(R.id.et_menu_dia_port);
        ET_ConnectState = (EditText) mAlertView.findViewById(R.id.et_menu_dia_state);
        BT_Disconnect = (Button) mAlertView.findViewById(R.id.bt_menu_dia_disconnect);
        BT_Connect = (Button) mAlertView.findViewById(R.id.bt_menu_dia_connect);

        BT_Disconnect.setEnabled(mConnectState);
        BT_Connect.setEnabled(!mConnectState);

        if (mConnectState)
            ET_ConnectState.setText("已连接");
        else
            ET_ConnectState.setText("未连接");

        BT_Connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIP = ET_IP.getText().toString();
                mPort = ET_Port.getText().toString();
                if (mIP.isEmpty() || mPort.isEmpty()) {
                    Toast.makeText(MainActivity.this, "IP和端口号不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "正在连接...", Toast.LENGTH_SHORT).show();
                    connectServer();
                }
            }
        });
        BT_Disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mConnectState) {
                    disconnectDirection();
                } else {
                    Toast.makeText(MainActivity.this, "未建立连接", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mAlertDialog.setView(mAlertView, 0, 0, 0, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            mAlertDialog.show();
            return true;
        }

        if (id == R.id.action_finsh) {
            if (mSocket != null) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            MainActivity.this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 设置alertdialog的控件状态
     *
     * @param state boolean
     */
    public void setViewStates(boolean state) {
        mConnectState = state;
        ET_IP.setEnabled(!state);
        ET_Port.setEnabled(!state);
        BT_Connect.setEnabled(!state);
        BT_Disconnect.setEnabled(state);

        if (state)
            ET_ConnectState.setText("已连接");
        else
            ET_ConnectState.setText("未连接");
    }

    /**
     * 初始化连接设备的信息
     */
    public void initDeviceInfo() {
        etHumidity.setText("0");
        etScore.setText("0");
        pbBattery.setProgress(0);
    }

    /**
     * 设置连接设备的信息
     *
     * @param speech  速度     String
     * @param score   得分      String
     * @param battery 电量    int
     */
    public void setDeviceInfo(String speech, String score, int battery) {
        etHumidity.setText(speech);
        etScore.setText(score);
        pbBattery.setProgress(battery);
    }

    /**
     * 发送方向控制信息
     */
    public void sendDirection() {

        if (mHorizontalDirection.equals("")) {
            mHorizontalDirection = "110";
            mDirection = getSpeechState()+mHorizontalDirection + mVerticalDirection + getCameraState() + getElectromagnetState();
        } else {
            mDirection = getSpeechState()+mVerticalDirection + mHorizontalDirection + getCameraState() + getElectromagnetState();
        }
        new Thread(mSendDirectionRunnable).start();
    }

    /**
     * 断开socket连接
     */
    public void disconnectDirection() {
        new Thread(mDisconnectRunnabel).start();
    }

    /**
     * 建立socket连接
     */
    public void connectServer() {
        new Thread(mConnectRunnable).start();
    }

    /**
     * socket连接
     */
    private Runnable mConnectRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Log.d(SocketReceiveTag, "run: ");
                mSocket = new Socket(mIP, Integer.parseInt(mPort));
                Log.d(SocketReceiveTag, "isrun: ");
                if (mSocket == null) {
                    mHandler.sendEmptyMessage(SOCKET_CONNECT_FAIL);
                } else {
                    Log.d(SocketReceiveTag, "run: connect ");
                    mHandler.sendEmptyMessage(SOCKET_CONNECT_SUCCEED);
                    while (true) {
                        Thread.sleep(1000);
                        new Thread(mReceiveRunnable).start();
                        mSocket.sendUrgentData(0xFF);
                    }
                }
                mSocket.close();
            } catch (UnknownHostException e) {
                mHandler.sendEmptyMessage(SOCKET_CONNECT_FAIL);
            } catch (IOException e) {
                if (mSocket == null) {
                    mHandler.sendEmptyMessage(SOCKET_CONNECT_FAIL);
                } else {
                    mHandler.sendEmptyMessage(SOCKET_DISCONNECTED);
                }
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * socket 数据接收
     */
    private Runnable mReceiveRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

                int tmp;
                char[] buf = new char[1024];
                String receiveTxt = "";

                while ((tmp = mReader.read(buf)) != -1) {
                    Log.d(SocketReceiveTag, new String(buf, 0, tmp));
                    Message msg = new Message();
                    msg.obj = new String(buf, 0, tmp);
                    msg.what = SOCKET_REVEIVE_CONTENT;
                    mHandler.sendMessage(msg);
                }

            } catch (IOException e) {
                mHandler.sendEmptyMessage(SOCKET_DISCONNECTED);
                e.printStackTrace();
            }
        }
    };


    /**
     * socket 发送方向
     */
    private Runnable mSendDirectionRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (mSocket != null) {

                    Log.d(SocketReceiveTag, "sending: " + mDirection);
                    mWriter = new PrintWriter(mSocket.getOutputStream(), true);
                    mWriter.print(mDirection);
                    mSocket.getOutputStream()
                            .write(mDirection
                                    .getBytes());
                } else {
                    // 异常处理
                    mHandler.sendEmptyMessage(SOCKET_DISCONNECTED);
                }
            } catch (IOException e) {
                mHandler.sendEmptyMessage(SOCKET_DISCONNECTED);
                e.printStackTrace();
            }
        }
    };

    private String getSpeechState() {
        return swSpeech.isChecked() ?  HEIGHTSPEECH : LOWSPEECH;
    }

    private String getCameraState() {
        return swCamera.isChecked() ? CAMERAON : CAMERAOFF;
    }

    private String getElectromagnetState(){
        return  swElectromagnet.isChecked() ? ElectromagnetON: ElectromagnetOFF;
    }


    /**
     * socket 断开
     */
    private Runnable mDisconnectRunnabel = new Runnable() {
        @Override
        public void run() {
            try {
                if (mSocket == null) {
                    mHandler.sendEmptyMessage(SOCKET_DISCONNECTED);
                } else {
                    mSocket.sendUrgentData(0xFF);
                    mSocket.close();
                    mHandler.sendEmptyMessage(SOCKET_CLOSE);
                }
            } catch (IOException e) {
                mHandler.sendEmptyMessage(SOCKET_DISCONNECTED);
                e.printStackTrace();
            }
        }
    };
}
