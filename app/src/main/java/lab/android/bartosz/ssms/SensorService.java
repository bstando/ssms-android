package lab.android.bartosz.ssms;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SensorService extends Service {
    private Handler handler = new Handler();
    private SensorDataDbHelper sensorDataDbHelper;
    private NsdHelper nsdHelper;
    private boolean searching = false;
    Context context;
    boolean toast;

    NotificationCompat.Builder mBuilder;

    Map<InetAddress,Timer> taskMap = new HashMap<>();


    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        SensorService getService() {
            return SensorService.this;
        }
    }

    public void setHelpers(NsdHelper nsdHelper1, SensorDataDbHelper sensorDataDbHelper1) {
        this.nsdHelper = nsdHelper1;
        this.sensorDataDbHelper = sensorDataDbHelper1;
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();
        nsdHelper = new NsdHelper(context);
        sensorDataDbHelper = new SensorDataDbHelper(context);
        nsdHelper.initializeNsd();
        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle("SSMS");
        mBuilder.setContentText(getString(R.string.notif_new_data));
        mBuilder.setSmallIcon(R.drawable.ic_info_black_24dp);
        Intent mIntent = new Intent(this, SensorReadingsActivity.class);
        mIntent.putExtra("sensorID",0);
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        mBuilder.setContentIntent(mPendingIntent);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        toast = prefs.getBoolean("show_toast",true);
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public List<SensorData> getFromDatabase() {
        return sensorDataDbHelper.retrieveAllData();
    }

    public void insertToDatabase(SensorData data) {
        sensorDataDbHelper.insertData(data);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }

    public void startSearching() {
        if (!searching) {
            nsdHelper.discoverServices();
            searching = true;
        }

    }

    public void stopSearching() {
        if (searching) {
            nsdHelper.stopDiscovery();
            searching = false;
        }
    }

    public Map<InetAddress, Integer> getClients() {
        return nsdHelper.getAvailableConnections();
    }

    public List<MDNSDevice> getDevices()
    {
        return nsdHelper.getAvailableDevices();
    }


    private class DownloadData extends AsyncTask<Pair<InetAddress,Integer>, Void,SensorData>
    {

        protected DownloadData()
        {

        }

        @Override
        protected SensorData doInBackground(Pair<InetAddress,Integer>... pairs)
        {
            InetAddress address = pairs[0].first;
            Integer port = pairs[0].second;
            SensorData sensorData = new SensorData();
            DatagramSocket dout = null;
            try {
                dout = new DatagramSocket(port);
                dout.setSoTimeout(20000);
                JSONObject json = new JSONObject();
                json.put("id", 2);
                String message = json.toString();
                Log.d("SENSOR:",message);
                DatagramPacket dp = new DatagramPacket(message.getBytes(), message.length(), address, port);
                dout.send(dp);
                byte[] bMsg = new byte[256];
                DatagramPacket datagramPacket = new DatagramPacket(bMsg,bMsg.length);
                dout.receive(datagramPacket);
                Log.d("TASK:",new String(bMsg));
                Log.d("LENGTH:",String.valueOf(bMsg.length));
                String jsonString = new String(bMsg,0,datagramPacket.getLength());
                JSONObject retJson = new JSONObject(jsonString);
                sensorData.setDate(new Date());
                sensorData.setSensorId(retJson.getInt("id"));
                sensorData.setTemperature((float)retJson.getDouble("temperature"));
                sensorData.setHumidity((float)retJson.getDouble("humidity"));
                dout.close();

           // } catch (SocketException ex) {
           //     Log.e("ERROR:", ex.getMessage());
            } catch (IOException e)
           {
              //Log.e("ERROR:", e.getMessage());
               //stopTimerTask(address);
               return null;
            } catch (JSONException ex)
            {
                Log.e("ERROR:", ex.getMessage());
            }                finally {
                if (dout != null) {
                    dout.close();
                }
            }

            Log.d("TASK",sensorData.toString());
            return sensorData;
        }

        @Override
        protected void onPostExecute(SensorData sensorData)
        {
            if(sensorData!=null) {
                insertToDatabase(sensorData);
                if (toast)
                    Toast.makeText(getApplicationContext(), sensorData.toString(), Toast.LENGTH_LONG).show();
            }

        }

    }

    public List<SensorData> getAddDataFromCollector(InetAddress address, int port) throws IOException
    {
        List<SensorData> retData = new ArrayList<>();
        DatagramSocket datagramSocket=null;
        boolean hasNext = true;

        try {
            datagramSocket = new DatagramSocket(port);
            datagramSocket.setSoTimeout(20000);
            JSONObject json = new JSONObject();
            json.put("id", 0);
            String message = json.toString();
            Log.d("SENSOR:", message);
            DatagramPacket sendDatagramPacket = new DatagramPacket(message.getBytes(), message.length(), address, port);
            datagramSocket.send(sendDatagramPacket);

            while(hasNext) {
                Log.e("DOWNLOAD","STARTING CHUNK DOWNLOAD");
                byte[] bMsg = new byte[1024];
                DatagramPacket recieveDatagramPacket = new DatagramPacket(bMsg, bMsg.length);
                datagramSocket.receive(recieveDatagramPacket);
                Log.e("DOWNLOAD","PACKET DOWNLOADED, START PARSE");
                Log.d("TASK:", new String(bMsg));
                Log.d("LENGTH:", String.valueOf(bMsg.length));
                String jsonString = new String(bMsg, 0, recieveDatagramPacket.getLength());
                JSONObject retJson = new JSONObject(jsonString);
                if (retJson.length()==0)
                {
                    Log.e("DOWNLOAD","OH NO, PARSE IS WRONG");
                }
                JSONArray array = retJson.getJSONArray("content");
                if(array.length()==0)
                {
                    Log.e("DOWNLOAD","OH NO, ARRAY LENGTH IS 0");
                }
                for(int i=0;i<retJson.getInt("content_length");i++) {
                    JSONObject object = array.getJSONObject(i);
                    SensorData sensorData = new SensorData();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = simpleDateFormat.parse(object.getString("date"));
                    sensorData.setDate(date);
                    sensorData.setSensorId(object.getInt("sensorID"));
                    sensorData.setTemperature((float) object.getDouble("temperature"));
                    sensorData.setHumidity((float) object.getDouble("humidity"));
                    retData.add(sensorData);
                }

                hasNext = retJson.getBoolean("has_next");
                Log.e("DOWNLOAD",String.valueOf(hasNext));

            }
            datagramSocket.close();


       // } catch (SocketException ex) {
       //     Log.e("ERROR:", ex.getMessage());
       // } catch (IOException e) {
       //     Log.e("ERROR:", e.getMessage());
        } catch (JSONException ex) {
            Log.e("ERROR:", ex.getMessage());
        } catch (ParseException ex) {
            Log.e("ERROR:", ex.getMessage());
        }
        finally {
            if (datagramSocket != null) {
                datagramSocket.close();
            }
        }

        return retData;
    }

    public List<SensorData> getLimitDataFromCollector(InetAddress address, int port,int limit) throws IOException
    {
        List<SensorData> retData = new ArrayList<>();
        DatagramSocket datagramSocket=null;
        boolean hasNext = true;

        try {
            datagramSocket = new DatagramSocket(port);
            datagramSocket.setSoTimeout(20000);
            JSONObject json = new JSONObject();
            json.put("id", 1);
            json.put("limit",limit);
            String message = json.toString();
            Log.d("SENSOR:", message);
            DatagramPacket sendDatagramPacket = new DatagramPacket(message.getBytes(), message.length(), address, port);
            datagramSocket.send(sendDatagramPacket);

            while(hasNext) {
                Log.e("DOWNLOAD","STARTING CHUNK DOWNLOAD");
                byte[] bMsg = new byte[1024];
                DatagramPacket recieveDatagramPacket = new DatagramPacket(bMsg, bMsg.length);
                datagramSocket.receive(recieveDatagramPacket);
                Log.e("DOWNLOAD","PACKET DOWNLOADED, START PARSE");
                Log.d("TASK:", new String(bMsg));
                Log.d("LENGTH:", String.valueOf(bMsg.length));
                String jsonString = new String(bMsg, 0, recieveDatagramPacket.getLength());
                JSONObject retJson = new JSONObject(jsonString);
                if (retJson.length()==0)
                {
                    Log.e("DOWNLOAD","OH NO, PARSE IS WRONG");
                }
                JSONArray array = retJson.getJSONArray("content");
                if(array.length()==0)
                {
                    Log.e("DOWNLOAD","OH NO, ARRAY LENGTH IS 0");
                }
                for(int i=0;i<retJson.getInt("content_length");i++) {
                    JSONObject object = array.getJSONObject(i);
                    SensorData sensorData = new SensorData();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = simpleDateFormat.parse(object.getString("date"));
                    sensorData.setDate(date);
                    sensorData.setSensorId(object.getInt("sensorID"));
                    sensorData.setTemperature((float) object.getDouble("temperature"));
                    sensorData.setHumidity((float) object.getDouble("humidity"));
                    retData.add(sensorData);
                }

                hasNext = retJson.getBoolean("has_next");
                Log.e("DOWNLOAD",String.valueOf(hasNext));

            }
            datagramSocket.close();


        //} catch (SocketException ex) {
        //    Log.e("ERROR:", ex.getMessage());
        //} catch (IOException e) {
        //    Log.e("ERROR:", e.getMessage());
        } catch (JSONException ex) {
            Log.e("ERROR:", ex.getMessage());
        } catch (ParseException ex) {
            Log.e("ERROR:", ex.getMessage());
        }
        finally {
            if (datagramSocket != null) {
                datagramSocket.close();
            }
        }

        return retData;
    }

    public List<SensorData> getDataSinceFromCollector(InetAddress address, int port,String since) throws IOException
    {
        List<SensorData> retData = new ArrayList<>();
        DatagramSocket datagramSocket=null;
        boolean hasNext = true;

        try {
            datagramSocket = new DatagramSocket(port);
            datagramSocket.setSoTimeout(20000);
            JSONObject json = new JSONObject();
            json.put("id", 2);
            json.put("date",since);
            String message = json.toString();
            Log.d("SENSOR:", message);
            DatagramPacket sendDatagramPacket = new DatagramPacket(message.getBytes(), message.length(), address, port);
            datagramSocket.send(sendDatagramPacket);

            while(hasNext) {
                Log.e("DOWNLOAD","STARTING CHUNK DOWNLOAD");
                byte[] bMsg = new byte[1024];
                DatagramPacket recieveDatagramPacket = new DatagramPacket(bMsg, bMsg.length);
                datagramSocket.receive(recieveDatagramPacket);
                Log.e("DOWNLOAD","PACKET DOWNLOADED, START PARSE");
                Log.d("TASK:", new String(bMsg));
                Log.d("LENGTH:", String.valueOf(bMsg.length));
                String jsonString = new String(bMsg, 0, recieveDatagramPacket.getLength());
                JSONObject retJson = new JSONObject(jsonString);
                if (retJson.length()==0)
                {
                    Log.e("DOWNLOAD","OH NO, PARSE IS WRONG");
                }
                JSONArray array = retJson.getJSONArray("content");
                if(array.length()==0)
                {
                    Log.e("DOWNLOAD","OH NO, ARRAY LENGTH IS 0");
                }
                for(int i=0;i<retJson.getInt("content_length");i++) {
                    JSONObject object = array.getJSONObject(i);
                    SensorData sensorData = new SensorData();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = simpleDateFormat.parse(object.getString("date"));
                    sensorData.setDate(date);
                    sensorData.setSensorId(object.getInt("sensorID"));
                    sensorData.setTemperature((float) object.getDouble("temperature"));
                    sensorData.setHumidity((float) object.getDouble("humidity"));
                    retData.add(sensorData);
                }

                hasNext = retJson.getBoolean("has_next");
                Log.e("DOWNLOAD",String.valueOf(hasNext));

            }
            datagramSocket.close();


        //} catch (SocketException ex) {
        //    Log.e("ERROR:", ex.getMessage());
        //} catch (IOException e) {
        //    Log.e("ERROR:", e.getMessage());
        } catch (JSONException ex) {
            Log.e("ERROR:", ex.getMessage());
        } catch (ParseException ex) {
            Log.e("ERROR:", ex.getMessage());
        }
        finally {
            if (datagramSocket != null) {
                datagramSocket.close();
            }
        }

        return retData;
    }

    public SensorData getDataFromDevice(InetAddress address, int port) throws IOException
    {
        SensorData sensorData = new SensorData();
        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket(port);
            datagramSocket.setSoTimeout(20000);
            JSONObject json = new JSONObject();
            json.put("id", 2);
            String message = json.toString();
            Log.d("SENSOR:", message);
            DatagramPacket sendDatagramPacket = new DatagramPacket(message.getBytes(), message.length(), address, port);
            datagramSocket.send(sendDatagramPacket);
            byte[] bMsg = new byte[256];
            DatagramPacket recieveDatagramPacket = new DatagramPacket(bMsg, bMsg.length);
            datagramSocket.receive(recieveDatagramPacket);
            Log.d("TASK:", new String(bMsg));
            Log.d("LENGTH:", String.valueOf(bMsg.length));
            String jsonString = new String(bMsg, 0, recieveDatagramPacket.getLength());
            JSONObject retJson = new JSONObject(jsonString);
            sensorData.setDate(new Date());
            sensorData.setSensorId(retJson.getInt("id"));
            sensorData.setTemperature((float) retJson.getDouble("temperature"));
            sensorData.setHumidity((float) retJson.getDouble("humidity"));
            datagramSocket.close();

        //} catch (SocketException ex) {
        //    Log.e("ERROR:", ex.getMessage());
        //} catch (IOException e) {
        //    Log.e("ERROR:", e.getMessage());
        } catch (JSONException ex) {
            Log.e("ERROR:", ex.getMessage());
        } finally {
            if (datagramSocket != null) {
                datagramSocket.close();
            }
        }
        return sensorData;

    }

    public DeviceInfo getDeviceInfo(InetAddress address, int port) throws IOException
    {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setAddress(address);
        deviceInfo.setPort(port);
        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket(port);
            datagramSocket.setSoTimeout(20000);
            JSONObject json = new JSONObject();
            json.put("id", 0);
            String message = json.toString();
            Log.d("SENSOR:", message);
            DatagramPacket sendDatagramPacket = new DatagramPacket(message.getBytes(), message.length(), address, port);
            datagramSocket.send(sendDatagramPacket);
            byte[] bMsg = new byte[256];
            DatagramPacket recieveDatagramPacket = new DatagramPacket(bMsg, bMsg.length);
            datagramSocket.receive(recieveDatagramPacket);
            Log.d("TASK:", new String(bMsg));
            Log.d("LENGTH:", String.valueOf(bMsg.length));
            String jsonString = new String(bMsg, 0, recieveDatagramPacket.getLength());
            JSONObject retJson = new JSONObject(jsonString);
            deviceInfo.setId(retJson.getInt("id"));
            deviceInfo.setName(retJson.getString("name"));
            deviceInfo.setLocalization(retJson.getString("localization"));
            datagramSocket.close();
        //} catch (SocketException ex) {
        //    Log.e("ERROR:", ex.getMessage());
        //} catch (IOException e) {
        //    Log.e("ERROR:", e.getMessage());
        } catch (JSONException ex) {
            Log.e("ERROR:", ex.getMessage());
        } finally {
            if (datagramSocket != null) {
                datagramSocket.close();
            }
        }
        return deviceInfo;

    }


    public void getDataFromSensor(InetAddress address, int port) {

        DownloadData data = new DownloadData();
        data.execute(new Pair<InetAddress, Integer>(address,port));


        //return new DownloadData().doInBackground();

    }

    public SensorData runSomeTest(final InetAddress address, final int port)
    {
        final SensorData sensorData = new SensorData();
        handler.post(new Runnable() {
            @Override
            public void run() {
                DatagramSocket dout = null;
                try {
                    dout = new DatagramSocket(port);
                    JSONObject json = new JSONObject();
                    json.put("id", 2);
                    String message = json.toString();
                    Log.d("SENSOR:", message);
                    DatagramPacket dp = new DatagramPacket(message.getBytes(), message.length(), address, port);
                    dout.send(dp);
                    byte[] bMsg = new byte[256];
                    DatagramPacket datagramPacket = new DatagramPacket(bMsg, bMsg.length);
                    dout.receive(datagramPacket);
                    Log.d("TASK:", new String(bMsg));
                    Log.d("LENGTH:", String.valueOf(bMsg.length));
                    String jsonString = new String(bMsg, 0, datagramPacket.getLength());
                    JSONObject retJson = new JSONObject(jsonString);
                    sensorData.setDate(new Date());
                    sensorData.setTemperature((float) retJson.getDouble("temperature"));
                    sensorData.setHumidity((float) retJson.getDouble("humidity"));
                    dout.close();
                    insertToDatabase(sensorData);

                } catch (SocketException ex) {
                    Log.e("ERROR:", ex.getMessage());
                } catch (IOException e) {
                    Log.e("ERROR:", e.getMessage());
                } catch (JSONException ex) {
                    Log.e("ERROR:", ex.getMessage());
                } finally {
                    if (dout != null) {
                        dout.close();
                    }
                }
            }
        });
        return sensorData;
    }

    boolean startTimerTask(final InetAddress address,final int port, long interval) {
        if (!taskMap.containsKey(address)) {
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Pair<InetAddress,Integer> pair = new Pair<InetAddress, Integer>(address,port);
                            new DownloadData().execute(pair);
                        }
                    });
                }
            };
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(timerTask,0,interval);
            taskMap.put(address,timer);
            return true;
        }
        else return false;
    }

    boolean stopTimerTask(InetAddress address)
    {
        if(taskMap.containsKey(address)) {
            Timer timer = taskMap.get(address);
            timer.cancel();
            taskMap.remove(address);
            return true;
        }
        else return false;
    }

    public List<SensorData> getBySensorID (int sensorID)
    {
        return sensorDataDbHelper.getByDeviceID(sensorID);
    }

    public List<SensorData> getDataSinceFromDatabase(String date)
    {
        return sensorDataDbHelper.getByDate(date);
    }

    public SensorData getByID(long id)
    {
        return sensorDataDbHelper.getByID(id);
    }

    public void updateSensorData(SensorData sensorData)
    {
        sensorDataDbHelper.updateData(sensorData);
    }

    public void removeFromDatabase(long id)
    {
        sensorDataDbHelper.deleteData(id);
    }

    public long getDatabaseRowsCount()
    {
        return sensorDataDbHelper.countRows();
    }

    public void resetDatabase()
    {
        sensorDataDbHelper.reset();
    }

    public boolean isConnectedViaWiFi() {
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        assert wifiMgr != null;
        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            return wifiInfo != null && wifiInfo.getNetworkId() != -1;
        }
        else return false; // Wi-Fi adapter is OFF
    }
}
