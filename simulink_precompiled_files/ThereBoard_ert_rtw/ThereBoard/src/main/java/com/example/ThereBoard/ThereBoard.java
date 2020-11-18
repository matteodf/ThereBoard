package com.example.ThereBoard;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.widget.Toast;
import android.util.Log;
import java.util.ArrayList;
import android.content.res.Configuration;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.View;
import android.widget.ToggleButton;
import android.widget.TextView;
import java.util.Hashtable;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import java.util.Arrays;
import java.util.UUID;
import java.util.Iterator;
import java.util.HashMap;
import android.annotation.TargetApi;
import android.os.Build;
import android.content.Context;
import android.media.AudioManager;

public class ThereBoard extends AppCompatActivity implements SensorEventListener, OnFragmentInteractionListener {
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private InfoFragment infoFragment = null;
     private Hashtable<Integer,Float> sliderValues = new Hashtable<Integer,Float>();
     private Hashtable<Integer,Float> buttonStates = new Hashtable<Integer,Float>();
     private Hashtable<Integer,TextView> textViews = new Hashtable<Integer,TextView>();
     private float[] mAccelerometerData = { 0.0f, 0.0f, 0.0f };
     private float[] mMagnetometerData = { 0.0f, 0.0f, 0.0f };
   private final float[] mRotationMatrix = new float[9];
private final float[] mOrientationAngles = new float[3];
     private SensorManager mSensorManager;
    String nativeSampleRate;
    String nativeSampleBufSize;
      private BluetoothAdapter mBluetoothAdapter; 
 HashMap<String, BLEClientConnection> mConnectionList = new HashMap<String, BLEClientConnection>(); 
      HashMap<Integer, BLECentraldevice> mCentralList = new HashMap<Integer, BLECentraldevice>();
      HashMap<Integer, BLEPeripheralDevice> mPeripheralList = new HashMap<Integer, BLEPeripheralDevice>();
      BLEServerConnection mBLEServerConnection = null;
      BluetoothScan mBluetoothScan = null;

     private void registerSensorManager() {
        mSensorManager.registerListener(this,
            mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this,
            mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_FASTEST);
     }

    void setupClient(int blockId, String serviceUUID,String charUUID, String address, String name) {
        BLEClientConnection connection = getBLEClientConnection(address,name);
        UUID newcharUUID = UUID.fromString(charUUID);
        BLECentraldevice cd = new BLECentraldevice(newcharUUID, connection);
        mCentralList.put(blockId,cd);
    }
    BLEClientConnection getBLEClientConnection(String address, String name) {
        if( mConnectionList.containsKey(address)){
            return mConnectionList.get(address);
         }
        else {
            BLEClientConnection conn =  new BLEClientConnection(address, mBluetoothAdapter, name, this, mBluetoothScan);
            conn.createConnection();
            mConnectionList.put(address,conn);
            return conn;
}
}

    byte[] stepClientReceive(int blockId) {
        BLECentraldevice bc = mCentralList.get(blockId);
        byte[] data = null;
        if (bc != null && bc.getValue() != null){
        byte[] value = bc.getValue();
        data = Arrays.copyOf(bc.getValue(), bc.getValue().length);
}
return data;
    }
    void stepClientSend(int blockId,byte[] chars) {
         BLECentraldevice bc = mCentralList.get(blockId);
         if (bc != null)
         bc.setValue(chars);}
    void setupServer(int BlockId, String serviceUUID, String charUUID) {
         Log.d("BLEServer","blockid" + BlockId + "serviceuuid " + serviceUUID + " charuuid " + charUUID + " mBLEServerConnection " + mBLEServerConnection);         if (mBLEServerConnection == null){
             mBLEServerConnection = BLEServerConnection.getInstance(this);
 mBLEServerConnection.startserver(this);
}
             mPeripheralList.put(BlockId, new BLEPeripheralDevice(UUID.fromString(serviceUUID),UUID.fromString(charUUID),mBLEServerConnection));
}
    void stepServerSend(int blockID, byte[] chars) {
         BLEPeripheralDevice bp = mPeripheralList.get(blockID);
         if(bp != null)
         bp.sendCharacteristic(chars);}
    byte[] stepServerReceive(int blockID) {
         BLEPeripheralDevice bp = mPeripheralList.get(blockID);
         byte[] chars = null;
         if (bp != null) {
             byte[] charValue = bp.receiveCharacteristic();
             if (charValue != null)
                  chars = Arrays.copyOf(charValue, charValue.length);
}
         return chars;
}
    void closeBLEConections(){
         Iterator it =  mConnectionList.entrySet().iterator();
         while(it.hasNext()) {
             HashMap.Entry pair = (HashMap.Entry)it.next();             BLEClientConnection g = (BLEClientConnection)pair.getValue();
             g.close();
}
}
    private boolean checkIfAllPermissionsGranted()
    {
        return true;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //Uncomment the following line to specify a custom App Title
        //toolbar.setTitle("My custom Title");
        setSupportActionBar(toolbar);

        // Create a FragmentPagerAdapter that returns individual fragments
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(SectionsPagerAdapter.getNumTabs()-1);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // Initiate the SensorManager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        queryNativeAudioParameters();
       final BluetoothManager bluetoothManager = 
       (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothScan = new BluetoothScan(mBluetoothAdapter);
        mBluetoothScan.scanLeDevice(true);
        thisClass = this;
     }

    private ThereBoard thisClass;
    private final Thread BgThread = new Thread() {
    @Override
    public void run() {
            String argv[] = new String[] {"MainActivity","ThereBoard"};
            naMain(argv, thisClass);
        }
    };

    public void flashMessage(final String inMessage) {
        runOnUiThread(new Runnable() {
              public void run() {
                    Toast.makeText(getBaseContext(), inMessage, Toast.LENGTH_SHORT).show();
              }
        });
    }

    protected void onDestroy() {
         if (BgThread.isAlive())
             naOnAppStateChange(6);
         closeBLEConections();
         if (mBLEServerConnection !=null)
             mBLEServerConnection.stopServer();
         super.onDestroy();
         System.exit(0); //to kill all our threads.
    }

	@Override
    public void onFragmentCreate(String name) {

    }

    @Override
    public void onFragmentStart(String name) {
        switch (name) {
            case "Info":
               break;
            case "App":
                registerDataDisplays();
                break;
            default:
                break;
        }
    }

    @Override
    public void onFragmentResume(String name) {
        switch (name) {
            case "App":
                for (int i=1;i<=3;i++) {
                    registerButtonFcn(i);
                }
                break;
            case "Info":
                if (checkIfAllPermissionsGranted()){
                    if (!BgThread.isAlive()) {
                        BgThread.start();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onFragmentPause(String name) {
    }
    @Override
    protected void onResume() {
         super.onResume();
         if (BgThread.isAlive())
             naOnAppStateChange(3);
         registerSensorManager();
    }

    @Override
    protected void onPause() {
        if (BgThread.isAlive())
            naOnAppStateChange(4);
         mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof InfoFragment) {
            this.infoFragment = (InfoFragment) fragment;
            infoFragment.setFragmentInteractionListener(this);
        }
    }

    public void registerDataDisplays() {
    // bind text views for data display block;
    for (int i = 1; i <= 3; i++) {
            TextView textView = (TextView) findViewById(
            getResources().getIdentifier("DataDisplay" + i, "id", getPackageName()));
            textViews.put(i, textView);
        }
    }
    public void registerSlider(int id) {
        String sliderid = "slider"+id;
        SeekBar seekBar = (SeekBar)findViewById(getResources().getIdentifier(sliderid, "id", getPackageName()));
        if (null == seekBar)
            return;
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
        			@Override
        			public void onStopTrackingTouch(SeekBar seekBar) {        				
        			}
        			@Override
        			public void onStartTrackingTouch(SeekBar seekBar) {        				
        			}
        			@Override
        			public void onProgressChanged(SeekBar seekBar, int progress,
        					boolean fromUser) {
        				// update the hash table with slider id and value 
        				sliderValues.put(seekBar.getId(), (float)seekBar.getProgress());
        			}
        		});
    }

    public void setSliderValue(int id, float value,int points) {
        String sliderid = "slider"+id;
        SeekBar seekBar = (SeekBar)findViewById(getResources().getIdentifier(sliderid, "id", getPackageName()));
        if (null == seekBar)
            return;
        seekBar.setMax(points);
        seekBar.setProgress((int)value);
		sliderValues.put(seekBar.getId(), value);
    }

    public float getSliderValue(int id) {
        String sliderid = "slider"+id;
        Float sliderValue = sliderValues.get(getResources().getIdentifier(sliderid, "id", getPackageName()));
        return sliderValue==null?-1:sliderValue.floatValue();
    }

    public void dispSliderValue(int id,float value) {
        final String tid  = "textview"+id;
        final TextView tv = (TextView)findViewById(getResources().getIdentifier(tid, "id", getPackageName()));
        if (null == tv)
            return;
        final String text = String.format("%s : %.3f",tv.getTag().toString(),value);
        runOnUiThread(new Runnable() {
            public void run() {
            	tv.setText(text);
            }
          });
    }

    public void registerButtonFcn(int id) {
        String buttonid     = "button"+id;
        final ToggleButton button = (ToggleButton)findViewById(getResources().getIdentifier(buttonid, "id", getPackageName()));
        if (null == button)
            return;
        setButtonState(button);
        button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
                setButtonState(button);
			}
		});
    }

    // update the hash table with button id and state
    public void setButtonState(ToggleButton button) {
    	if(button.isChecked()) {
            buttonStates.put(button.getId(),1.0f);
		} else {
            buttonStates.put(button.getId(),0.0f);
		}
    }

    public float getButtonState(int id) {
        String buttonid = "button"+id;
        Float buttonState = buttonStates.get(getResources().getIdentifier(buttonid, "id", getPackageName()));
        return buttonState == null?-1:buttonState.floatValue();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float [] values = event.values;
        //Comment out if you want to log the data in logcat
        //String logMessage = String.format("%d: 0'%g'", event.sensor.getType(), values[0]);
        //Log.d("Sensor Data IN:", logMessage);
        switch(event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                mAccelerometerData[0] = values[0];
                mAccelerometerData[1] = values[1];
                mAccelerometerData[2] = values[2];
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagnetometerData[0] = values[0];
                mMagnetometerData[1] = values[1];
                mMagnetometerData[2] = values[2];
                break;
        }
    }

    // Get SensorEvent Data throws exception if the data is null
    public float[] getOrientationData() {
        mSensorManager.getRotationMatrix(mRotationMatrix, null,mAccelerometerData, mMagnetometerData);
        mSensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
        mOrientationAngles[0] = (float)Math.toDegrees(mOrientationAngles[0]);
        mOrientationAngles[1] = (float)Math.toDegrees(mOrientationAngles[1]);
        mOrientationAngles[2] = (float)Math.toDegrees(mOrientationAngles[2]);
        return mOrientationAngles;
    }

    public void displayText(int id, byte[] data, byte[] format) {
        String formatString = new String(format);
        String toDisplay = String.format(formatString, data[0]);
        if (data.length > 1) {
            for (int i = 1; i < data.length; i++)
                toDisplay += "\n" + String.format(formatString, data[i]);
        }
        updateTextViewById(id, toDisplay);
    }

    public void displayText(int id, short[] data, byte[] format) {
        String formatString = new String(format);
        String toDisplay = String.format(formatString, data[0]);
        if (data.length > 1) {
            for (int i = 1; i < data.length; i++)
                toDisplay += "\n" + String.format(formatString, data[i]);
        }
        updateTextViewById(id, toDisplay);
    }

    public void displayText(int id, int[] data, byte[] format) {
        String formatString = new String(format);
        String toDisplay = String.format(formatString, data[0]);
        if (data.length > 1) {
            for (int i = 1; i < data.length; i++)
                toDisplay += "\n" + String.format(formatString, data[i]);
        }
        updateTextViewById(id, toDisplay);
    }

    public void displayText(int id, long[] data, byte[] format) {
        String formatString = new String(format);
        String toDisplay = String.format(formatString, data[0]);
        if (data.length > 1) {
            for (int i = 1; i < data.length; i++)
                toDisplay += "\n" + String.format(formatString, data[i]);
        }
        updateTextViewById(id, toDisplay);
    }

    public void displayText(int id, float[] data, byte[] format) {
        String formatString = new String(format);
        String toDisplay = String.format(formatString, data[0]);
        if (data.length > 1) {
            for (int i = 1; i < data.length; i++)
                toDisplay += "\n" + String.format(formatString, data[i]);
        }
        updateTextViewById(id, toDisplay);
    }

    public void displayText(int id, double[] data, byte[] format) {
        String formatString = new String(format);
        String toDisplay = String.format(formatString, data[0]);
        if (data.length > 1) {
            for (int i = 1; i < data.length; i++)
                toDisplay += "\n" + String.format(formatString, data[i]);
        }
        updateTextViewById(id, toDisplay);
    }

    private void updateTextViewById(final int id, final String finalStringToDisplay) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    textViews.get(id).setText(finalStringToDisplay);
                } catch (Exception ex) {
                    Log.e("ThereBoard.updateTextViewById", ex.getLocalizedMessage());
                }
            }
        });
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void queryNativeAudioParameters() {
        Log.d("audioEQ", "queryNativeAudioParameters called");
        AudioManager myAudioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        nativeSampleRate = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        nativeSampleBufSize = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
    }

    public int getNativeSampleRate() {
        Log.d("audioEQ", "JNI getNativeSampleRate called");
        return Integer.parseInt(nativeSampleRate);
    }
    public int getNativeSampleBufSize() {
        Log.d("audioEQ", "JNI getNativeSampleBufSize called");
        return Integer.parseInt(nativeSampleBufSize);
    }
    private native int naMain(String[] argv, ThereBoard pThis);
    private native void naOnAppStateChange(int state);
    static {
        System.loadLibrary("ThereBoard");
    }

}
