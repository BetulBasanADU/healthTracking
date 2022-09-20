package com.betulbasan.healthtracking;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MonitoringScreen extends Activity {

    private static final String TAG = "BlueTest5-MainActivity";
    private int mMaxChars = 50000;//Default
    private UUID mDeviceUUID;
    private BluetoothSocket mBTSocket;
    private ReadInput mReadThread = null;
    ArrayList<ILineDataSet> dataSets = new ArrayList<>();
    ArrayList<ILineDataSet> dataSets1 = new ArrayList<>();
    ArrayList<ILineDataSet> dataSets2 = new ArrayList<>();
    ArrayList<ILineDataSet> dataSets3 = new ArrayList<>();
    ArrayList<ILineDataSet> dataSets4 = new ArrayList<>();
    ArrayList<ILineDataSet> dataSets5 = new ArrayList<>();


    private boolean mIsUserInitiatedDisconnect = false;

    // All controls here
    private TextView mTxtReceive;
    private Button mBtnClearInput;
    private Button bpmButton;
    private Button spo2Button;
    private Button tempButton;
    private Button accButton;
    private Button tableButton;
    private ScrollView scrollView;
    private ScrollView scrollViewAcc1;


    private CheckBox chkScroll;
    private CheckBox chkReceiveText;
    private int count = 0;

    private boolean mIsBluetoothConnected = false;

    private BluetoothDevice mDevice;

    private ProgressDialog progressDialog;

    private LineChart lineChart; //bpm

    private LineChart lineChart1; //spo2

    private LineChart lineChart2; //temp

    private LineChart acc1;
    private LineChart acc2;
    private LineChart acc3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_screen);
        ActivityHelper.initialize(this);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        mDevice = b.getParcelable(MainActivity.DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString(b.getString(MainActivity.DEVICE_UUID));
        mMaxChars = b.getInt(MainActivity.BUFFER_SIZE);
        Log.d(TAG, "Ready");
        mTxtReceive = (TextView) findViewById(R.id.txtReceive);
        chkScroll = (CheckBox) findViewById(R.id.chkScroll);
        chkReceiveText = (CheckBox) findViewById(R.id.chkReceiveText);
        scrollView = (ScrollView) findViewById(R.id.viewScroll);
        mBtnClearInput = (Button) findViewById(R.id.btnClearInput);
        lineChart = findViewById(R.id.chart);
        lineChart1 = findViewById(R.id.chart1);
        bpmButton = findViewById(R.id.bpm);
        spo2Button = findViewById(R.id.spo2);
        tableButton = findViewById(R.id.table);
        tempButton = findViewById(R.id.temp);
        lineChart2 =  findViewById(R.id.chart2);
        acc1 = findViewById(R.id.acc1);
        acc2 = findViewById(R.id.acc2);
        acc3 = findViewById(R.id.acc3);
        scrollViewAcc1 = findViewById(R.id.viewScrollAcc1);


        accButton =  findViewById(R.id.acc);
        mTxtReceive.setMovementMethod(new ScrollingMovementMethod());
//tüm listeler cin tablo yapısı
        lineChart.setBackgroundColor(Color.WHITE);

        // disable description text
        lineChart.getDescription().setEnabled(false);

        // enable touch gestures
        lineChart.setTouchEnabled(true);

        // set listeners
        lineChart1.setDrawGridBackground(false);

        lineChart1.setBackgroundColor(Color.WHITE);

        // disable description text
        lineChart1.getDescription().setEnabled(false);

        // enable touch gestures
        lineChart1.setTouchEnabled(true);

        // set listeners
        lineChart1.setDrawGridBackground(false);

        lineChart2.setDrawGridBackground(false);

        lineChart2.setBackgroundColor(Color.WHITE);

        // disable description text
        lineChart2.getDescription().setEnabled(false);

        // enable touch gestures
        lineChart2.setTouchEnabled(true);

        // set listeners
        acc1.setDrawGridBackground(false);

        acc1.setDrawGridBackground(false);

        acc1.setBackgroundColor(Color.WHITE);

        // disable description text
        acc1.getDescription().setEnabled(false);

        // enable touch gestures
        acc1.setTouchEnabled(true);

        // set listeners
        acc2.setDrawGridBackground(false);

        acc2.setDrawGridBackground(false);

        acc2.setDrawGridBackground(false);

        acc2.setBackgroundColor(Color.WHITE);

        // disable description text
        acc2.getDescription().setEnabled(false);

        // enable touch gestures
        acc2.setTouchEnabled(true);

        // set listeners
        acc2.setDrawGridBackground(false);

        acc3.setDrawGridBackground(false);

        acc3.setDrawGridBackground(false);

        acc3.setBackgroundColor(Color.WHITE);

        // disable description text
        acc3.getDescription().setEnabled(false);

        // enable touch gestures
        acc3.setTouchEnabled(true);

        // set listeners
        acc3.setDrawGridBackground(false);

//tablo arkaplanı tamamlandı ustte
//butona tıklandıgında gorulmesi gereken alanı gosterip digerlerini göstermemesi
        mBtnClearInput.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mTxtReceive.setText("");
            }
        });
        lineChart.setVisibility(View.VISIBLE);
        lineChart1.setVisibility(View.GONE);
        lineChart2.setVisibility(View.GONE);
        scrollViewAcc1.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);

        bpmButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                lineChart.setVisibility(View.VISIBLE);
                lineChart1.setVisibility(View.GONE);
                lineChart2.setVisibility(View.GONE);
                scrollView.setVisibility(View.GONE);
                scrollViewAcc1.setVisibility(View.GONE);

            }
        });

        spo2Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                lineChart.setVisibility(View.GONE);
                lineChart1.setVisibility(View.VISIBLE);
                lineChart2.setVisibility(View.GONE);
                scrollView.setVisibility(View.GONE);
                scrollViewAcc1.setVisibility(View.GONE);

            }
        });

        tableButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                lineChart.setVisibility(View.GONE);
                lineChart1.setVisibility(View.GONE);
                lineChart2.setVisibility(View.GONE);

                scrollView.setVisibility(View.VISIBLE);
                scrollViewAcc1.setVisibility(View.GONE);

            }
        });

        tempButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                lineChart.setVisibility(View.GONE);
                lineChart1.setVisibility(View.GONE);
                lineChart2.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.GONE);
                scrollViewAcc1.setVisibility(View.GONE);

            }
        });
        accButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                lineChart.setVisibility(View.GONE);
                lineChart1.setVisibility(View.GONE);
                lineChart2.setVisibility(View.GONE);
                scrollView.setVisibility(View.GONE);
                scrollViewAcc1.setVisibility(View.VISIBLE);

            }
        });
    }
//buton goruntuleme bitti
    private class ReadInput implements Runnable {

        private boolean bStop = false;
        private Thread t;

        public ReadInput() {
            t = new Thread(this, "Input Thread");
            t.start();
        }

        public boolean isRunning() {
            return t.isAlive();
        }

        @Override
        public void run() {
            InputStream inputStream;

            try {
                inputStream = mBTSocket.getInputStream();
                while (!bStop) {
                    byte[] buffer = new byte[256];
                    if (inputStream.available() > 0) {
                        inputStream.read(buffer);
                        int i = 0;

                        for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
                        }
                        final String strInput = new String(buffer, 0, i);



                        if (chkReceiveText.isChecked()) {
                            mTxtReceive.post(new Runnable() {
                                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                @Override
                                public void run() {

                                    String text = strInput;
                                    if(text.equals("emergency")){
                                        try {
                                            SmsManager smsManager = SmsManager.getDefault();
                                            smsManager.sendTextMessage("+905389772673", null, "ACİL DURUM MESAJI! ARKADAŞINIZ GÜLFİDE YERE DÜŞTÜ.", null, null);
                                        } catch (Exception ex) {
                                            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                                                    Toast.LENGTH_LONG).show();
                                            ex.printStackTrace();
                                        }
                                    }
                                     try {
                                         List<Entry> list = new ArrayList<Entry>();
                                         List<Entry> listSPO2 = new ArrayList<Entry>();
                                         List<Entry> listTemp = new ArrayList<Entry>();
                                         List<Entry> listAcc1 = new ArrayList<Entry>();
                                         List<Entry> listAcc2 = new ArrayList<Entry>();
                                         List<Entry> listAcc3 = new ArrayList<Entry>();


                                         List<String> dataList = new ArrayList<String>();
                                         List<String> accDataList = new ArrayList<String>();
                                         dataList = Arrays.asList(text.split("/"));
                                         String spo2 = dataList.get(0);
                                         String heartRate  = dataList.get(1);
                                         String temp = dataList.get(2);
                                         String acc = dataList.get(3);
                                         accDataList = Arrays.asList(acc.split(","));
                                         acc = "gx:"+accDataList.get(0) + " gy:" + accDataList.get(1) + " gz:" + accDataList.get(2);
                                         String steps = dataList.get(4);
                                         String distance = dataList.get(5);
                                         String calories = dataList.get(6);
                                         String tableString = "BPM : "+ heartRate + " SPO2 : " + spo2 + "%"+" Temperature : "+temp+"C"+"\n"+"Acc : "+ acc +"\n" +
                                                 "steps : " +steps + " distance : " + distance + " calories : " +calories + "\n";
                                         mTxtReceive.append(tableString);


                                         listSPO2.add(new Entry(count,Float.valueOf(spo2)));
                                         list.add(new Entry(count,Float.valueOf(heartRate)));
                                         listTemp.add(new Entry(count,Float.valueOf(temp)));
                                         listAcc1.add(new Entry(count,Float.valueOf(accDataList.get(0))));
                                         listAcc2.add(new Entry(count,Float.valueOf(accDataList.get(1))));
                                         listAcc3.add(new Entry(count,Float.valueOf(accDataList.get(2))));
                                         LineDataSet set1 =  new LineDataSet(list, "");
                                         LineDataSet set2 =  new LineDataSet(listSPO2, "");
                                         LineDataSet set3=  new LineDataSet(listTemp, "");
                                         LineDataSet set4=  new LineDataSet(listAcc1, "");
                                         LineDataSet set5=  new LineDataSet(listAcc2, "");
                                         LineDataSet set6=  new LineDataSet(listAcc3, "");

                                         dataSets.add(set1); // add the data sets
                                         dataSets1.add(set2);
                                         dataSets2.add(set3);
                                         dataSets3.add(set4);
                                         dataSets4.add(set5);
                                         dataSets5.add(set6);
                                         // create a data object with the data sets
                                         LineData data = new LineData(dataSets);
                                         LineData data1 = new LineData(dataSets1);
                                         LineData data2 = new LineData(dataSets2);
                                         LineData data3 = new LineData(dataSets3);
                                         LineData data4 = new LineData(dataSets4);
                                         LineData data5 = new LineData(dataSets5);
                                         lineChart.setData(data);
                                         lineChart1.setData(data1);
                                         lineChart2.setData(data2);
                                         acc1.setData(data3);
                                         acc2.setData(data4);acc3.setData(data5);

                                         count++;
                                         int txtLength = mTxtReceive.getEditableText().length();
                                         if(txtLength > mMaxChars){
                                             mTxtReceive.getEditableText().delete(0, txtLength - mMaxChars);
                                         }

                                     }catch(Exception exception){
                                     }




                                    if (chkScroll.isChecked()) { // Scroll only if this is checked
                                        scrollView.post(new Runnable() { // Snippet from http://stackoverflow.com/a/4612082/1287554
                                            @Override
                                            public void run() {
                                                scrollView.fullScroll(View.FOCUS_DOWN);
                                            }
                                        });
                                    }
                                }
                            });
                        }

                    }
                    Thread.sleep(500);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        public void stop() {
            bStop = true;
        }

    }
//BAGLANTI BİTTİGNDE VERİ AKISINI DURDURMAK İCİN
    private class DisConnectBT extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (mReadThread != null) {
                mReadThread.stop();
                while (mReadThread.isRunning())
                    ; // Wait until it stops
                mReadThread = null;

            }

            try {
                mBTSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
//BAGLANTI DEVAM EDIYORSA DEVAM ETSIN DEGILSE BITIR
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mIsBluetoothConnected = false;
            if (mIsUserInitiatedDisconnect) {
                finish();
            }
        }

    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        if (mBTSocket != null && mIsBluetoothConnected) {
            new DisConnectBT().execute();
        }
        Log.d(TAG, "Paused");
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mBTSocket == null || !mIsBluetoothConnected) {
            new ConnectBT().execute();
        }
        Log.d(TAG, "Resumed");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopped");
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }//ANLIK DEGISIKLIKLERI KAYDET

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean mConnectSuccessful = true;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MonitoringScreen.this, "Hold on", "Connecting");
        }

        @Override
        protected Void doInBackground(Void... devices) {

            try {
                if (mBTSocket == null || !mIsBluetoothConnected) {
                    mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mBTSocket.connect();
                }
            } catch (IOException e) {
// Unable to connect to device
                e.printStackTrace();
                mConnectSuccessful = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!mConnectSuccessful) {
                Toast.makeText(getApplicationContext(), "Could not connect to device. Is it a Serial device? Also check if the UUID is correct in the settings", Toast.LENGTH_LONG).show();
                finish();
            } else {
                msg("Connected to device");
                mIsBluetoothConnected = true;
                mReadThread = new ReadInput(); // Kick off input reader
            }

            progressDialog.dismiss();
        }

    }



}