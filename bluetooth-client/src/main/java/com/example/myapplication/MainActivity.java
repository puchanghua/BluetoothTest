package com.example.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private ListView lvDevices;
    private BluetoothAdapter mBluetoothAdapter;
    private List<String> bluetoothDevices = new ArrayList<String>();
    private ArrayAdapter<String> arrayAdapter;
    private final UUID MY_UUID = UUID
            .fromString("abcd1234-ab12-ab12-ab12-abcdef123456");//随便定义一个
    private BluetoothSocket clientSocket;
    private BluetoothDevice device;
    private OutputStream os;//输出流
    private static final int REQUEST_BLUETOOTH_PERMISSION=10;
    private Button search;

    private void requestBluetoothPermission(){
        //判断系统版本
        if (Build.VERSION.SDK_INT >= 23) {
            //检测当前app是否拥有某个权限
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);
            //判断这个权限是否已经授权过
            if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
                //判断是否需要 向用户解释，为什么要申请该权限
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION))
                    Toast.makeText(this,"Need bluetooth permission.",
                            Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this ,new String[]
                        {Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_BLUETOOTH_PERMISSION);
                return;
            }else{

            }
        } else {

        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvDevices = (ListView) findViewById(R.id.lv_devices);
        search = (Button)findViewById(R.id.search_bluetooth);
        requestBluetoothPermission();
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果当前在搜索，就先取消搜索
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                //开启搜索
                mBluetoothAdapter.startDiscovery();
            }
        });
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.enable(); //开启
        //获取已经配对的蓝牙设备
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                bluetoothDevices.add(device.getName() + ":"+ device.getAddress());
            }
        }
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1,bluetoothDevices);
        lvDevices.setAdapter(arrayAdapter);
        lvDevices.setOnItemClickListener(this);//Activity实现OnItemClickListener接口
        //每搜索到一个设备就会发送一个该广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(receiver, filter);
        //当全部搜索完后发送该广播
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(receiver, filter);
    }


    /**
     * 定义广播接收器
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    bluetoothDevices.add(device.getName() + ":" + device.getAddress());
                    arrayAdapter.notifyDataSetChanged();//更新适配器
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //已搜素完成
            }
        }
    };


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String s = arrayAdapter.getItem(position);
        String address = s.substring(s.indexOf(":") + 1).trim();//把地址解析出来
        //主动连接蓝牙服务端
        try {
            //判断当前是否正在搜索
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            try {
                if (device == null) {
                    //获得远程设备
                    device = mBluetoothAdapter.getRemoteDevice(address);
                }
                if (clientSocket == null) {
                    //创建客户端蓝牙Socket
                    clientSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                    //开始连接蓝牙，如果没有配对则弹出对话框提示我们进行配对
                    clientSocket.connect();
                    //获得输出流（客户端指向服务端输出文本）
                    os = clientSocket.getOutputStream();
                }
            } catch (Exception e) {
            }
            if (os != null) {
                //往服务端写信息
                os.write("蓝牙信息来了".getBytes("utf-8"));
            }
        } catch (Exception e) {
        }
    }
}
