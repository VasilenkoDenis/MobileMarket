package com.icsmarket.developer.mobilemarket;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.icsmarket.developer.fpdriver.FPDriver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends Activity implements View.OnClickListener{

    private static final int BLUETOOTH_INTERFACE = 0;

   //private static final int USB_INTERFACE = 1;

    private static final int RECEIVE_MESSAGE = 1; // Сообщение для Handler;

    public static BluetoothSocket clientSocket = null;

    final String LOG_TAG = "myLogs";
    private static final String ACTION_USB_PERMISSION = "com.icsmarket.developer.mobilemarket.USB_PERMISSION";
    private UsbManager mUsbManager;
    private PendingIntent mPermissionIntent;
    private UsbInterface dataIface;

    ParcelFileDescriptor mFileDescriptor;
    private UsbAccessory accessoryList [];

    Menu menu;
    // Экземпляр класса драйвера
    FPDriver fpDriver;

    Button btnConnect, btnXReport, btnDisconnect, btnScan;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)){

                if (fpDriver!=null);
                    fpDriver.fpClose();
                try {
                    mFileDescriptor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "Accessory is detached", Toast.LENGTH_LONG).show();
            }
            else
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(accessory != null){
                            //call method to set up accessory communication
                            Toast.makeText(getApplicationContext(), accessory.getDescription(), Toast.LENGTH_LONG).show();
                            accessoryOpen(accessory);
                        }
                    }
                    else {
                        Log.d(LOG_TAG, "permission denied for accessory " + accessory);
                        Toast.makeText(getApplicationContext(), "Отказано!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){

            case BLUETOOTH_INTERFACE:
                switch (resultCode) {
                    case RESULT_OK:
                        // Пользовтель включил Bluetooth
                        Toast.makeText(MainActivity.this, "Пользователь включил Bluetooth", Toast.LENGTH_SHORT).show();
                        break;
                    case RESULT_CANCELED:
                        // Пользователь не включил Bluetooth
                        Toast.makeText(MainActivity.this,"Пользователь не включил Bluetooth",Toast.LENGTH_SHORT).show();
                        break;
                }
                break;


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnXReport = (Button) findViewById(R.id.btnXReport);
        btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
        btnScan = (Button) findViewById(R.id.btnScan);

        btnConnect.setOnClickListener(MainActivity.this);
        btnXReport.setOnClickListener(MainActivity.this);
        btnDisconnect.setOnClickListener(MainActivity.this);
        btnScan.setOnClickListener(MainActivity.this);

        // Получаем объект драйвера после поворота экрана
        fpDriver = (FPDriver) getLastNonConfigurationInstance();

        mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);    // ловим action в onResume
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);    // ловим action в BroadcastReceiver

        registerReceiver(mUsbReceiver, filter);
        Toast.makeText(getApplicationContext(), "USB receiver is registered!", Toast.LENGTH_LONG).show();

    }


    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String action = intent.getAction();
        if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
            //do something
            Toast.makeText(getApplicationContext(), "Accessory is atttached", Toast.LENGTH_LONG).show();
            UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
            // запрос на доступ к аксессуару.
            mUsbManager.requestPermission(accessory, mPermissionIntent);
        }
    }

    // Сохраняем объект драйвера перед поворотом экрана и onDestroy Activity
    @Override
    public Object onRetainNonConfigurationInstance() {
        return fpDriver;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // Сохраняем объект меню в классе
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem item_find_device =  menu.findItem(R.id.find_bluetooth_device);

        // Проверке наличия на борту BLUETOOTH адаптера
        if (BluetoothAdapter.getDefaultAdapter()!=null){
            // если есть Bluetooth адаптер, значит делаем доступным пункт меню поиска устройств
            item_find_device.setEnabled(true);
        }
        else
            // если нет Bluetooth адаптера, значит делаем пункт меню поиска устройств недоступным
            item_find_device.setEnabled(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch(item.getItemId()){

            // Общие настройки программы
            case R.id.action_settings:
                break;

            // Настройка соединения по Bluetooth
            case R.id.find_bluetooth_device:
                // Показываем активность с настройками Bluetooth
                Intent i = new Intent(MainActivity.this,BluetoothActivity.class);
                startActivityForResult(i, BLUETOOTH_INTERFACE);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnConnect:
                if (clientSocket.isConnected()) {
                    fpDriver = new FPDriver(MainActivity.this, clientSocket, 1/*Принтер id*/);
                }
                break;
            case R.id.btnXReport:
                fpDriver.fpPrintXReport();
                break;

            case R.id.btnDisconnect:
                fpDriver.fpClose();
                break;

//            case R.id.btnScan:
//                mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
//
//                mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
//                IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
//                filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
//                filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
//
//                registerReceiver(mUsbReceiver, filter);
//
//                Toast.makeText(getApplicationContext(), "USB receiver is registered!", Toast.LENGTH_LONG).show();
//                //startBroadcastReceiver();
//                break;
//            case R.id.btnScan:
//                checkForDevices();
        }
    }


//    private void startBroadcastReceiver()
//    {
//        mUsbReceiver = new BroadcastReceiver()
//        {
//            @Override
//            public void onReceive(Context context, Intent intent)
//            {
//                Toast.makeText(getApplicationContext(), "Do on Receive", Toast.LENGTH_LONG).show();
//                String action = intent.getAction();
//                if (ACTION_USB_PERMISSION.equals(action)) {
//                    synchronized (this) {
//                        UsbAccessory accessory = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
//                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
//                            if (accessory != null) {
//                                //call method to set up device communication
//                                // openPort(device);
//                                Toast.makeText(getApplicationContext(), accessory.getDescription(), Toast.LENGTH_LONG).show();
//                            }
//                        }
//                        else {
//                            Toast.makeText(getApplicationContext(), "Отказано!", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }
//
//            }
//        };
//    }


//   protected void checkForDevices(){
//       mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
//       deviceList = mUsbManager.getAccessoryList();
//       if (deviceList!=null){
//           for(UsbAccessory accessory: deviceList){
//               Toast.makeText(MainActivity.this, accessory.getDescription(), Toast.LENGTH_LONG).show();
//               Toast.makeText(MainActivity.this, accessory.getModel(), Toast.LENGTH_LONG).show();
//               Toast.makeText(MainActivity.this, accessory.getManufacturer(), Toast.LENGTH_LONG).show();
//               Toast.makeText(MainActivity.this, accessory.getVersion(), Toast.LENGTH_LONG).show();
//
//               if (mUsbManager.hasPermission(accessory)){
//                   Toast.makeText(MainActivity.this, "Accessory has permition" , Toast.LENGTH_SHORT).show();
//               }
//               else
//               {
//                   Toast.makeText(MainActivity.this, "Accessory hasn't permition" , Toast.LENGTH_SHORT).show();
//               }
//           }
//       }
//       else
//           Toast.makeText(MainActivity.this, "USB-device not found!", Toast.LENGTH_SHORT).show();
//   }

    private void accessoryOpen(UsbAccessory accessory) {
        Log.d(LOG_TAG, "openAccessory: " + accessory);
        if (mUsbManager.hasPermission(accessory)){

            accessoryList = mUsbManager.getAccessoryList();
            for(UsbAccessory list: accessoryList) {
                Toast.makeText(MainActivity.this, list.getDescription(), Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, list.getModel(), Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, list.getSerial(), Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, list.getUri(), Toast.LENGTH_SHORT).show();

            }
            mFileDescriptor = mUsbManager.openAccessory(accessory);
            if (mFileDescriptor != null) {
                 if (!mFileDescriptor.getFileDescriptor().valid()){
                     Toast.makeText(MainActivity.this, "File descriptor invalid" , Toast.LENGTH_SHORT).show();
                 }
                 Toast.makeText(MainActivity.this, "Accessory has permition" , Toast.LENGTH_SHORT).show();
                 fpDriver = new FPDriver(MainActivity.this, mFileDescriptor.getFileDescriptor(), 1/*Принтер id*/);
                 //fpDriver.fpPrintXReport();
            }
        }
        else
           Toast.makeText(MainActivity.this, "Accessory hasn't permition" , Toast.LENGTH_SHORT).show();
    }

}
