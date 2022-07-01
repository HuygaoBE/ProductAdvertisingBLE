package com.example.bluetoothgatt;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;

import android.view.View;

import android.widget.Button;

import android.widget.Toast;


import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.util.List;
import java.util.UUID;

/**
 * Created by Dave Smith
 * Double Encore, Inc.
 * BeaconActivity
 */
public class MainActivity extends Activity implements BluetoothAdapter.LeScanCallback {
    private static final String TAG = "MainActivity";

    Button view, delete;


    private BluetoothAdapter mBluetoothAdapter;
    DBAdapter DB;

    private static final int UUID_SERVICE_THERMOMETER = 0x1809;
    public static final ParcelUuid THERM_SERVICE = ParcelUuid.fromString("00001809-0000-1000-8000-00805f9b34fb");

    private String ID;
    private String AdvCount;
    private String Off_set;
    private String MF;
    private String DataAdv;
    private double Radius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view = (Button) findViewById(R.id.btnView);
        delete = (Button) findViewById(R.id.btnDelete);


        view.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {


                Cursor res = DB.getdata();

                if(res.getCount()==0){
                    Toast.makeText(MainActivity.this, "Không có sản phẩm quảng cáo", Toast.LENGTH_SHORT).show();
                    return;
                }




                //StringBuffer đại diện cho các chuỗi ký tự có thể phát triển và có thể ghi,
                // các ký tự và chuỗi con được chèn vào giữa hoặc nối vào cuối

                StringBuffer buffer = new StringBuffer();
                while(res.moveToNext()){
                    buffer.append(""+res.getString(5)+"\n");
                    buffer.append("Khoảng cách xung quanh: "+res.getString(6)+"m\n\n");


                }



                //AlertDialog là một hộp thoại hiển thị một thông điệp, và hỗ trợ 1, 2
                // hoặc 3 button, nó giúp bạn dễ dàng tạo được một hộp thoại chỉ với một vài dòng code.
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(true);
                builder.setTitle("Danh sách các sản phẩm");

                builder.setMessage(buffer.toString());
                builder.show();
            }        });

//--------------------delete

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();


        //------------------------
        DB = new DBAdapter(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        }


        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        startScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Cancel any scans in progress
        mHandler.removeCallbacks(mStopRunnable);
        mHandler.removeCallbacks(mStartRunnable);
        mBluetoothAdapter.stopLeScan(this);
    }

    private Runnable mStopRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };
    private Runnable mStartRunnable = new Runnable() {
        @Override
        public void run() {
            startScan();
        }
    };

    private void startScan() {
        mBluetoothAdapter.startLeScan(new UUID[] {THERM_SERVICE.getUuid()}, this);
        setProgressBarIndeterminateVisibility(true);
        // Scan 6s
        mHandler.postDelayed(mStopRunnable, 6000);
    }

    private void stopScan() {
        mBluetoothAdapter.stopLeScan(this);
        setProgressBarIndeterminateVisibility(false);
        //Delay 0,1s
        mHandler.postDelayed(mStartRunnable, 100);
    }


    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.i(TAG, "New LE Device: " + device.getName() + " @ " + rssi);

        List<AdRecord> records = AdRecord.parseScanRecord(scanRecord);
        if (records.size() == 0) {
            Log.i(TAG, "Scan Record Empty");
        } else {
            Log.i(TAG, "Scan Record: "
                    + TextUtils.join(",", records));
        }

        for(AdRecord packet : records) {
            if (packet.getType() == AdRecord.TYPE_SERVICEDATA
                    && AdRecord.getServiceDataUuid(packet) == UUID_SERVICE_THERMOMETER) {
                byte[] data = AdRecord.getServiceData(packet);
                //Xac định tinh dung dan trong ma, nếu đúng thì được kich hoạt
                assert data != null;
                DataAdv = new String(data);
            }
        }


        int HeaderOpen = (device.getName()).indexOf("{");
        int HeaderClose = (device.getName()).indexOf("}");
        char[] HeaderIn = (device.getName()).toCharArray();
        int[] list = new int[3];
        int index = 0;

        for(int i = 0; i < HeaderIn.length; i++){
            if(HeaderIn[i] == '#'){
                list[index++] = i;
            }
        }

        Radius = Math.pow(10, ((-69 - Double.valueOf(rssi)) / (10*2)));
        NumberFormat formatter = new DecimalFormat("##.##");



        ID = (device.getName()).substring(HeaderOpen+1, list[0]);

        AdvCount = (device.getName()).substring(list[0] + 1, list[1]);

        Off_set = (device.getName()).substring(list[1] +1, list[2]);

        MF = (device.getName()).substring(list[2] +1, HeaderClose);

        DB.insertdata(device.getName(), ID, AdvCount, Off_set, MF, DataAdv, String.valueOf(formatter.format(Radius)));
        DB.updatedata();
        DB.close();


    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//-            AdFilter beacon = (AdFilter) msg.obj;
//            mBeacons.put(beacon.getName(), beacon);

            DBAdapter beacon2 = (DBAdapter) msg.obj;
//            mBeacons.put(beacon2.getName(), beacon);
            /*
            *Sau do cap nhat lai bo dieu hop danh sach ma chung toi da tao
             */
//            mAdapter.setNotifyOnChange(false);
//            mAdapter.clear();
//            mAdapter.addAll(mBeacons.values());
//            mAdapter.notifyDataSetChanged();
        }
    };

}