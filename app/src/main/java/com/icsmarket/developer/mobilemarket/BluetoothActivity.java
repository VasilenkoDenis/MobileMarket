package com.icsmarket.developer.mobilemarket;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends Activity implements View.OnClickListener {

    static BluetoothAdapter btAdapter;
    BluetoothDevice device;

    //public static BluetoothSocket clientSocket = null;

    TextView btAdapter_tv;
    Button btnFindDevices;

    ListView btDeviceList;
    ArrayList<BluetoothDevice> arrDeviceList = new ArrayList<BluetoothDevice>();
    ArrayList<String> arrDeviceListView = new ArrayList<String>();
    ArrayAdapter<String> adapter;



    private boolean useFallback = false; // <---- new
    private BluetoothSocket _sockFallback = null; // <--------------- new



    //AlertDialog alertDialog;

    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
//    private static final UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

    // Порт подключения к серверу (1..30)
    private static final int PORT = 1;
    // request codes
    private static final int REQUEST_ENABLE_BT = 10;//
    private static final int DISCOVERY_REQUEST_CODE = 40;

    // Время видимости устройства в секундах
    private static int REQUEST_DURATION_ENABLED_BT = 300;


    Intent btAdapterIntent;
    IntentFilter filter;

    Boolean isReceiverRegistered = false;


    public void btShowState() {

        String state = "Состояние: ";
        btAdapter_tv.setText("");
        if (btAdapter.isEnabled()) {
            btAdapter_tv.append(btAdapter.getName() + "\n");
            btAdapter_tv.append("MAC адрес: " + btAdapter.getAddress() + "\n");
            switch (btAdapter.getState()) {
                case BluetoothAdapter.STATE_CONNECTED:
                    state += "подключено";
                    break;
                case BluetoothAdapter.STATE_CONNECTING:
                    state += "подключение";
                    break;
                case BluetoothAdapter.STATE_DISCONNECTED:
                    state += "отключено";
                    break;
                case BluetoothAdapter.STATE_DISCONNECTING:
                    state += "отключение";
                    break;
                case BluetoothAdapter.STATE_OFF:
                    state += "адаптер выключен";
                    break;
                case BluetoothAdapter.STATE_ON:
                    state += "адаптер включен";
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    state += "адаптер выключается";
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    state += "адаптер включается";
                    break;
            }
            btAdapter_tv.append(state);
        } else {
            Toast.makeText(this, "Bluetooth адаптер  выключен", Toast.LENGTH_LONG).show();
        }

    }

    // Процедура установки видимости адаптера
    public void btShareAdapter() {

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        // Устанавливаем время из REQUEST_DURATION_ENABLED_BT для видимости Bluetooth адаптера для других устройств
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, REQUEST_DURATION_ENABLED_BT);
        // Пытаемся перейти в режим видимости
        startActivityForResult(discoverableIntent, DISCOVERY_REQUEST_CODE);


    }


    public boolean btConnect() {
        try {
          if(Build.VERSION.SDK_INT >=10){



//              Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
//              MainActivity.clientSocket=(BluetoothSocket) m.invoke(device, uuid);

//              Method m = device.getClass().getMethod("createRfcommSocket",new Class[] { int.class });
//              MainActivity.clientSocket = (BluetoothSocket) m.invoke(device, PORT);

              Method  m = device.getClass().getMethod("createRfcommSocketToServiceRecord", new Class[] { UUID.class });
              MainActivity.clientSocket=(BluetoothSocket) m.invoke(device, uuid);


     //         Method m = device.getClass().getMethod("createRfcommSocketToServiceRecord", new Class[] {UUID.class});
     //         MainActivity.clientSocket = (BluetoothSocket) m.invoke(device, uuid);




              // Instantiate a BluetoothSocket for the remote device and connect it.
//              MainActivity.clientSocket = device.createRfcommSocketToServiceRecord(uuid);



              //ArrayList<UUID> uuidList = new  ArrayList<UUID>();
              //uuidList.add(uuid);

              // connector = new BluetoothConnector(device,false,btAdapter,uuidList);


//              MainActivity.clientSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
//              final Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
//              MainActivity.clientSocket = (BluetoothSocket) m.invoke(device, PORT);



          }
           else {
              MainActivity.clientSocket = device.createRfcommSocketToServiceRecord(uuid);

          }



//              Thread.sleep(1000);
//            connector.connect();

            MainActivity.clientSocket.connect();
            useFallback = false;
        } catch (IOException e) {

            // TODO Auto-generated catch block
           // Log.e(TAG, "There was an error while establishing connection. -> " + e.getMessage());


           /************** Fallback Socket based on ********/
        /* http://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3/18786701#18786701*/
            Class<?> clazz = MainActivity.clientSocket.getRemoteDevice().getClass();
            Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};
            try {
                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[] {Integer.valueOf(1)};
                _sockFallback = (BluetoothSocket) m.invoke(MainActivity.clientSocket.getRemoteDevice(), params);
                _sockFallback.connect();
                useFallback = true;
                MainActivity.clientSocket = _sockFallback;
            } catch (Exception e2) {
                Toast.makeText(getBaseContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                e2.printStackTrace();
                //stopService();

            }
            //*************** END MODIFICATION

//            try{
//
//                Class<?> clazz = MainActivity.clientSocket.getRemoteDevice().getClass();
//                Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};
//
//                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
//                Object[] params = new Object[] {Integer.valueOf(1)};
//
//                fallbackSocket = (BluetoothSocket) m.invoke(MainActivity.clientSocket.getRemoteDevice(), params);
//                fallbackSocket.connect();
//
//
//            } catch (InvocationTargetException e1) {
//                e1.printStackTrace();
//            } catch (NoSuchMethodException e1) {
//                e1.printStackTrace();
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            } catch (IllegalAccessException e1) {
//                e1.printStackTrace();
//            }

        } catch (SecurityException e) {
            Toast.makeText(getBaseContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
//        } catch (NoSuchMethodException e) {
//            Toast.makeText(getBaseContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
//        } catch (IllegalArgumentException e) {
//            Toast.makeText(getBaseContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
//        } catch (IllegalAccessException e) {
//            Toast.makeText(getBaseContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
//        } catch (InvocationTargetException e) {
//            Toast.makeText(getBaseContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }  catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }

        return MainActivity.clientSocket.isConnected();

    }


    // Создаем BroadcastReceiver для ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_UUID.equals(action)){
                Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                if(uuidExtra == null)
                    Toast.makeText(BluetoothActivity.this," Service: null",Toast.LENGTH_SHORT).show();
                else
                   for (int i=0; i<uuidExtra.length; i++) {
                       Toast.makeText(BluetoothActivity.this," Service: " + uuidExtra[i].toString(),Toast.LENGTH_SHORT).show();
                   }

            }else
            // Когда найдено новое устройство
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Получаем объект Bluetooth из интента
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                device.fetchUuidsWithSdp();
                //Добавляем имя и адрес в array adapter, чтобы показвать в ListView

                // If it's already paired, skip it, because it's been listed
                // already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // Если в списке устройства ещё нет, то добавляем его
                    if (!arrDeviceList.contains(device)) {
                        arrDeviceList.add(device);
                        arrDeviceListView.add(device.getName() + " \t" + device.getAddress());
                        adapter.notifyDataSetChanged();
                    }
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Toast.makeText(context, "Усройство успешно сопряжено", Toast.LENGTH_SHORT).show();

                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                Toast.makeText(context, "Начато сканирование устройств!", Toast.LENGTH_SHORT).show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                btnFindDevices.setEnabled(true);
                Toast.makeText(context, "Сканирование устройств закончено!", Toast.LENGTH_SHORT).show();
            } else
                // Когда поменялся режим видимости
                if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {

                    switch (intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.SCAN_MODE_NONE)) {

                        case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                            Toast.makeText(context, "Режим SCAN_MODE_CONNECTABLE_DISCOVERABLE", Toast.LENGTH_SHORT).show();
                            break;

                        case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                            Toast.makeText(context, "Режим SCAN_MODE_CONNECTABLE", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothAdapter.SCAN_MODE_NONE:
                            Toast.makeText(context, "Режим SCAN_MODE_NONE", Toast.LENGTH_SHORT).show();
                            break;

                    }
            } else
                // Сообщение об установленном соединении
                if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
                     Toast.makeText(context, "Bluetooth cоединение установлено", Toast.LENGTH_SHORT).show();
                     // Пытаемся установить соединение c принтером
                     btConnect();

            }
             else
                // Когда поменялся режим видимости
                if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                    Toast.makeText(context, "Изменилось состояние соединения адаптера", Toast.LENGTH_SHORT).show();
             }
             else
                // Сообщение о подтверждении сопряжения
                if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                     Toast.makeText(context, "Подтверждение сопряжения  устройств", Toast.LENGTH_SHORT).show();
             }

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bluetooth);
        btAdapter_tv = (TextView) findViewById(R.id.bluetooth_adapter_id);
        btnFindDevices = (Button) findViewById(R.id.btn_find_bt);
        btnFindDevices.setOnClickListener(BluetoothActivity.this);

        btDeviceList = (ListView) findViewById(R.id.dev_list_bt);
        btDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Выбираем устройство с которым хотим связаться.
                device = btAdapter.getRemoteDevice(arrDeviceList.get(position).getAddress());


                // Если осуществляется поиск устройств, то прекращаем их искать
                if (btAdapter.isDiscovering())
                    btAdapter.cancelDiscovery();

                // Если устройство ещё не спарено
                // Пытаемся спарить его
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {

//                   LayoutInflater factory = LayoutInflater.from(BluetoothActivity.this);
//                   final View textEntryView = factory.inflate(R.layout.dialog_pin, null);
//
//                   AlertDialog.Builder alert = new AlertDialog.Builder(BluetoothActivity.this);
//                   alert.setTitle("Ключ доступа на РРО"); //Set Alert dialog title
//                   alert.setMessage("Введите ПИН-код:"); //Message
//                   alert.setView(textEntryView);
//
//
//                   alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                       public void onClick(DialogInterface dialog, int whichButton) {
//                           //You will get as string input data in this variable.
//                           // here we convert the input to a string and show in a toast.
//                           EditText pinText;
//                           pinText = (EditText) textEntryView.findViewById(R.id.pin);
//                           String pin = pinText.getText().toString();
//                           device.setPin(pinText.getText().toString().getBytes());
//                           device.createBond();
//                       } // End of onClick(DialogInterface dialog, int whichButton)
//                   }); //End of alert.setPositiveButton
//                   alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
//                       public void onClick(DialogInterface dialog, int whichButton) {
//                           // Canceled.
//                           dialog.cancel();
//                       }
//                   }); //End of alert.setNegativeButton
//
//                   alertDialog = alert.create();
//                   alertDialog.show();
                    // Пытаемся начать сопряжение.
                    device.createBond();

                } else
                    // Пытаемся установить соединениеж
                    btConnect();
            }
        });

        // создаём адаптер
        adapter = new ArrayAdapter<String>(BluetoothActivity.this,android.R.layout.simple_dropdown_item_1line, arrDeviceListView);
        //создаём адаптер для найденных устройств
        btDeviceList.setAdapter(adapter);



        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btAdapterIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        try {
              if (!btAdapter.isEnabled()) {
               // Если Bluetooth выключен, то пытаемся его включить
               startActivityForResult(btAdapterIntent, REQUEST_ENABLE_BT);
               // Устанавливаем доступность поля поиска устройств
               btnFindDevices.setEnabled(true);
             }
             } catch (NullPointerException e) {
                    Toast.makeText(this, "Ошибка включения Bluetooth адаптера!", Toast.LENGTH_SHORT).show();
                    // Устанавливаем недоступность поля поиска устройств в случае ошибки
                    btnFindDevices.setEnabled(false);
             }


        // Показываем состояние нашего адаптера Bluetooth
        btShowState();

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        // Если список спаренных устройств не пуст
        if (pairedDevices.size() > 0) {
            // проходимся в цикле по этому списку
            for (BluetoothDevice device : pairedDevices) {
                // Добавляем имена и адреса в mArrayAdapter, чтобы показать
                // через ListView
                arrDeviceList.add(device);
                arrDeviceListView.add(device.getName()+" \t"+device.getAddress());
            }
        }
    }

    @Override
    protected void onDestroy() {

        // Снимаем регистрацию
        if (isReceiverRegistered )
         unregisterReceiver(mReceiver);

        // Если осуществляется поиск устройств
        if (btAdapter.isDiscovering()){
            // Останавливаем его.
            btAdapter.cancelDiscovery();
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        Intent intent = this.getIntent();
        this.setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){

            // Включение адаптера Bluetooth
            case REQUEST_ENABLE_BT:
                switch (resultCode) {
                    case RESULT_OK:
                        // Пользовтель включил Bluetooth
                        Toast.makeText(this,"Пользователь включил Bluetooth",Toast.LENGTH_SHORT).show();
                        break;
                    case RESULT_CANCELED:
                        // Пользователь не включил Bluetooth
                        Toast.makeText(this,"Пользователь не включил Bluetooth",Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

            // Видимость адаптера другими устройствами
            case DISCOVERY_REQUEST_CODE:
                switch (resultCode) {
                    case RESULT_OK:
                        // Пользовтель включил Bluetooth
                        Toast.makeText(this,"Установка видимости на "+ REQUEST_DURATION_ENABLED_BT + " секунд.",Toast.LENGTH_SHORT).show();
                        break;
                    case RESULT_CANCELED:
                        // Пользователь не включил Bluetooth
                        Toast.makeText(this,"Пользователь запретил видимость устройства!",Toast.LENGTH_SHORT).show();
                        break;
                }
                break;


        }
        // Обновляем состояние адаптера
        btShowState();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            // Если нажали кнопку поиска, устанавливаем режим видимости адаптера и запускаем процедуру обнаружения устройств
            case R.id.btn_find_bt:   btShareAdapter();
                                     // Если поиск устройств был начат ранее, топрерываем
                                     if(btAdapter.isDiscovering())
                                        btAdapter.cancelDiscovery();

                                     // Фомируем фильтр сообщений от BroadcastReceiver
                                     filter = new IntentFilter();
                                     filter.addAction(BluetoothDevice.ACTION_FOUND);
                                     filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                                     filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
                                     filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
                                     filter.addAction(BluetoothDevice.ACTION_UUID);

                                     filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                                     filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
                                     filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                                     filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);

                                     // Регистрируем BroadcastReceiver
                                     // Не забудьте снять регистрацию в onDestroy
                                     registerReceiver(mReceiver, filter);

                                     isReceiverRegistered = true;

                                     // Запускаем новый поиск устройств
                                     btAdapter.startDiscovery();
                                     btnFindDevices.setEnabled(false);
                                     break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bluetooth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        // Если осуществляется поиск устройств
        if (btAdapter.isDiscovering()){
            // Останавливаем его.
            btAdapter.cancelDiscovery();
        }

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
