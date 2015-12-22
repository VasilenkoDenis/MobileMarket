package com.icsmarket.developer.fpdriver;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Created by vasilenkoden on 22.07.14.
 */
public  class ConnectedThread extends Thread {

    private static  BluetoothSocket mmSocket;
    private static  InputStream mmInStream;
    private static  OutputStream mmOutStream;

    private  FileDescriptor fileDescriptor;
    private  FileInputStream mInputStream;
    private  FileOutputStream mOutputStream;


    //Константы
    private static final byte DLE = 0x10;
    private static final byte STX = 0x02;
    private static final byte ETX = 0x03;
    private static final byte SYN = 0x16;
    private static final byte ACK = 0x06;
    private static final byte NAK = 0x15;
    private static final byte ENQ = 0x05;

    static final int RECEIVED_DATA = 1;

    // Количество повторов команды
    int maxAttempt = 3;

    short CommandCounter = 0;

    Context context;

    ArrayList<byte[]> rcvData = new ArrayList<byte[]>();

    public  Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RECEIVED_DATA:
                    // your code goes here
                    Toast.makeText(context, "Поступили данные от РРО", Toast.LENGTH_SHORT).show();
                    // данные в RcvData
                    break;
            }

        }
    };


    public ConnectedThread(Context context, BluetoothSocket socket) {

       this.context = context;
        mmSocket = socket;

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        //Получить входящий и исходящий потоки данных
        try {
            tmpIn = mmSocket.getInputStream();
            tmpOut = mmSocket.getOutputStream();
        } catch (IOException e) {}


        mmInStream = tmpIn;
        mmOutStream = tmpOut;
     }

    public ConnectedThread(Context context, FileDescriptor fd) {

        this.context = context;
        fileDescriptor = fd;
        mInputStream = new FileInputStream(fileDescriptor);
        mOutputStream = new FileOutputStream(fileDescriptor);
    }


    public void run(){

        byte [] buffer = new byte[1024]; // буферный массив
        int bytes; // bytes returned from read()

        // Прослушиваем InputStream пока не произойдет исключение
        while(true){

            try{
                bytes = 0; // Количество прочитанных байтов
                if(mmSocket!=null) {
                    // Читаем из InputString
                    bytes = mmInStream.read(buffer);
                    // посылаем прочитанные байты главной деятельности
                }
                else
                if(mInputStream!=null) {
                    bytes = mInputStream.read(buffer);
                }

                rcvData.add(buffer);
                mHandler.obtainMessage(RECEIVED_DATA, bytes, -1, buffer).sendToTarget();
                }
            catch (IOException e){
                break;
                }
       }
   }

    // Очищаем данные буфера
    public  void clearBuffer(){
        rcvData.clear();
    }

    private String byteArrayToString(byte[] array) {
        String hex = new String();
        for (int i = 0; i < array.length; i++) {
            hex+=String.format("0x%02X ", (byte) array[i])+" ";
        }
        return hex;
    }

    /* Вызываем этот метод из главной деятельности, чтобы отправить данные
   удаленному устройству */
    public void write(byte[] bytes){
        if (mmSocket != null)
            try{
                mmOutStream.write(bytes);

            }catch(IOException e){}
        else
        if (mOutputStream != null){
            try {
                Toast.makeText(context, byteArrayToString(bytes),Toast.LENGTH_SHORT).show();
                mOutputStream.write(bytes, 0, bytes.length);

            } catch (IOException e) {
                Toast.makeText(context, "ERROR send data",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    /* Вызываем этот метод из главной деятельности,
        чтобы разорвать соединение */

    public void cancel() {

        if (mmSocket != null)
            try {
                mmSocket.close();
            } catch (IOException e) {}

        if (fileDescriptor != null) {
            try {
                mInputStream.close();
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    public static byte[] intToByteArray(int a) {
//        return BigInteger.valueOf(a).toByteArray();
//    }

    // Преобразование беззнакового байтового числа [short] в байтовый массив
    public static byte[] unsignedByteToByteArray(short a) {

        byte[] uByte = new byte[1];
        uByte[0] = (byte) (a & 0x000000FF);

        return uByte;
    }

    // Преобразование беззнакового двухбайтового числа [int] в байтовый массив
    public static byte[] unsignedShortToByteArray(int a) {

        byte[] uShort = new byte[2];
        uShort[0] = (byte) (a & 0x000000FF);
        uShort[1] = (byte) (a >> 8 & 0x000000FF);

        return uShort;
    }

    // Преобразование беззнакового четырёхбайтового числа [long] в байтовый массив
    public static byte[] unsignedIntToByteArray(long a) {

        byte[] uInt = new byte[4];
        uInt[0] = (byte) (a & 0x00000000000000FF);
        uInt[1] = (byte) (a >> 8 & 0x00000000000000FF);
        uInt[2] = (byte) (a >> 16 & 0x00000000000000FF);
        uInt[3] = (byte) (a >> 24 & 0x00000000000000FF);

        return uInt;
    }

    // Преобразование числа [long] в байтовый массив
    public static byte[] LongToByteArray(long a) {

        byte[] sLong = new byte[8];
        sLong[0] = (byte) (a & 0x00000000000000FF);
        sLong[1] = (byte) (a >> 8 & 0x00000000000000FF);
        sLong[2] = (byte) (a >> 16 & 0x00000000000000FF);
        sLong[3] = (byte) (a >> 24 & 0x00000000000000FF);
        sLong[4] = (byte) (a >> 32 & 0x00000000000000FF);
        sLong[5] = (byte) (a >> 40 & 0x00000000000000FF);
        sLong[6] = (byte) (a >> 48 & 0x00000000000000FF);
        sLong[7] = (byte) (a >> 56 & 0x000000000000007F);

        return sLong;
    }


    // Преобразование байтового массива в беззнаковое байтовое число, хранимое в [short]
    public static short bytesToUnsignedByte(byte[] buf) {

        return  (short) (0x000000FF & ((int)buf[0]));
    }


    // Преобразование байтового массива в беззнаковое двухбайтовое число, хранимое в [int]
    public static int bytesToUnsignedShort(byte[] buf) {

        return  (0x000000FF & ((int)buf[0])|
                (0x000000FF & ((int)buf[1])) << 8);
    }

    // Преобразование байтового массива в беззнаковое четырёхбайтовое число, хранимое в [long]
    public static long bytesToUnsignedInt(byte[] buf) {

        return  (0x000000FF & ((int)buf[0])|
                (0x000000FF & ((int)buf[1])) << 8 |
                (0x000000FF & ((int)buf[2])) << 16|
                (0x000000FF & ((long)buf[3])) << 24);
    }


    // Преобразование байтового массива в число [long]
    public static long bytesToSignedLong(byte[] buf) {

        return  (0x000000FF & ((int)buf[0])|
                (0x000000FF & ((int)buf[1])) << 8 |
                (0x000000FF & ((int)buf[2])) << 16|
                (0x000000FF & ((int)buf[3])) << 24|
                (0x000000FF & ((int)buf[5])) << 32|
                (0x000000FF & ((int)buf[3])) << 24);
    }


    private boolean writeData(ArrayList<Byte> source, int attempt) {

        if(attempt == 0)
            ++CommandCounter;
        // Контрольная сумма
        short CS =0;

        ArrayList<Byte> data = new ArrayList<Byte>();
        data.addAll(source);

        //Добавляем в команду счётчик команд
        data.add(0,unsignedByteToByteArray(CommandCounter)[0]);

        // Дублируем DLE,если встречается DLE внутри пакета
        for(int i=0; i < data.size(); i++){
            CS += data.get(i);
            if (data.get(i) == DLE) {
                data.add(i, DLE);
                i++;
                continue;
            }
        }

        CS=(short)(0-CS);

        // Добавляем байт контрольной суммы
        data.add(unsignedByteToByteArray(CS)[0]);
        if ((CS & 0xFF)== DLE) {
            data.add(unsignedByteToByteArray(CS)[0]);
        }

        // Добавляем начало пакета
        data.add(0,DLE);
        data.add(1,STX);

        // Добавляем конец пакета
        data.add(DLE);
        data.add(ETX);

        // Преобразовываем данные в пакет (массив)
        byte[] packet = new byte[data.size()];
        for (int i = 0; i < packet.length; i++) {
            packet[i] = (byte) data.get(i);
        }

        // Отправляем пакет на принтер
        write(packet);

        return true;
    }


    private boolean poolData(ArrayList<Byte> data){

      // Номер попытки отправки данных
      int i=0;
      while (i < maxAttempt){
          clearBuffer();
          writeData(data,i);
        i++;
      }

      return true;
    }

    public boolean icsfpPrintXReport(int password){

        final byte CommandID = 9;
        ArrayList<Byte> data = new ArrayList<Byte>();

        // добавляем номер команды
        data.add(CommandID);
        // добавляем ключ отчётов
        byte[] keyReport  = unsignedShortToByteArray(password);
        for (byte item:keyReport)
            data.add(item);

        if (!poolData(data)) {
            Toast.makeText(context,"Ошибка печати X-отчёта",Toast.LENGTH_SHORT).show();
            return  false;
         }

        Toast.makeText(context,"X-отчёт успешно распечатан",Toast.LENGTH_SHORT).show();
        return true;
//            case Lang1 of
//                0:LastErrorMessage:='Fiscal printer does not answer!';
//                1:LastErrorMessage:='ЭККР не отвечает!';
//                2:LastErrorMessage:='ЕККР не відповідає!';
//                end;
//                exit;
//                end;
        }
//        ErrorMessage(Get_Status,Get_Res,1);






}
