package com.icsmarket.developer.fpdriver;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.widget.Toast;

import com.icsmarket.developer.mobilemarket.BluetoothActivity;
import com.icsmarket.developer.mobilemarket.MainActivity;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;


/**
 * Created by vasilenkoden on 21.07.14.
 */

public class FPDriver {

    private static final byte EPSON_EP08    = 1;
    private static final byte CITIZEN_MZ08  = 2;
    private static final byte POSIFLEX_MS08 = 3;


    Context context;
    ConnectedThread connectedThread;


    int printerID;

    // Пароль режима отчётов, по умолчанию = 0
    int reportKey = 0;
    // Пароль режима программирования, по умолчанию = 0
    int progKey = 0;
    // Пароль кассира, по умолчанию = 0
    int userKey = 0;


    // Конструктор
    public FPDriver(Context context,BluetoothSocket socket, int printerID) {

        this.context = context;
        this.printerID = printerID;
        // Инициализируем подключение
        bluetoothConnectionInitialize(socket);
    };

    // Конструктор
    public FPDriver(Context context,FileDescriptor fd, int printerID) {

        this.context = context;
        this.printerID = printerID;
        // Инициализируем подключение
        usbConnectionInitialize(fd);
    };

    public void bluetoothConnectionInitialize(BluetoothSocket clientSocket){

        if (connectedThread == null){
         connectedThread = new ConnectedThread(context, clientSocket);
         connectedThread.start();
        }
        else
         Toast.makeText(context, "Инициализация подключения сделано ранее", Toast.LENGTH_SHORT).show();
    }

    public void usbConnectionInitialize(FileDescriptor fd){

        if (connectedThread == null){
            connectedThread = new ConnectedThread(context,fd);
            connectedThread.start();
        }
        else
            Toast.makeText(context, "Инициализация подключения сделано ранее", Toast.LENGTH_SHORT).show();
    }

    public boolean fpPrintXReport(){

       switch (printerID){

       case  EPSON_EP08:

       case  CITIZEN_MZ08:

       case  POSIFLEX_MS08:

                          return  connectedThread.icsfpPrintXReport(reportKey);

       default:
           return  false;
       }
    }

    public boolean fpClose() {
        switch (printerID) {

            case EPSON_EP08:

            case CITIZEN_MZ08:

            case POSIFLEX_MS08:

                connectedThread.cancel();
                break;

            default:
                return false;

        }
        return true;
    }
   }
