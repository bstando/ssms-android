package lab.android.bartosz.ssms;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeviceActivity extends AppCompatActivity {

    InetAddress address;
    int port;
    ListView listView;
    int deviceID;
    int setYear, setMonth, setDay, setMinutes, setHours;
    TimePickerDialog timePickerDialog;
    DatePickerDialog datePickerDialog;
    ProgressDialog dialog;
    boolean toast;


    protected SensorService sensorService;
    protected boolean bounded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        toast = prefs.getBoolean("show_toast", true);

        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            try {
                address = InetAddress.getByAddress(extrasBundle.getByteArray("address"));
            } catch (UnknownHostException e) {
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
            port = extrasBundle.getInt("port");
            deviceID = extrasBundle.getInt("deviceID");
        }
        listView = (ListView) findViewById(R.id.methodsListView);
        List<String> list = new ArrayList<String>();

        list.add(getString(R.string.show_device_info));
        list.add(getString(R.string.show_current_readings));
        list.add(getString(R.string.start_periodic_task));
        list.add(getString(R.string.stop_periodic_task));
        list.add(getString(R.string.show_reading_from_database));
        list.add(getString(R.string.show_temperature_chart));
        list.add(getString(R.string.show_humidity_chart));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        onClickDeviceInfoBtn(view);
                        break;
                    case 1:
                        onClickCurrentReadingsBtn(view);
                        break;
                    case 2:
                        startPeriodicTask(view);
                        break;
                    case 3:
                        stopPeriodicTask(view);
                        break;
                    case 4:
                        showSensorData();
                        break;
                    case 5:
                        showNoRowsWarning(true);
                        break;
                    case 6:
                        showNoRowsWarning(false);
                    default:
                        break;
                }
            }
        });

    }

    public void showSensorData() {
        if (sensorService.getDatabaseRowsCount() == 0) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.alert_title))
                    .setMessage(getString(R.string.string_noDatabaseCount))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            Intent intent = new Intent(getApplicationContext(), SensorReadingsActivity.class);
            intent.putExtra("sensorID", deviceID);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_device, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SensorService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bounded) {
            unbindService(connection);
            bounded = false;
        }
    }

    @Override
    protected void onDestroy()
    {
        if (bounded) {
            unbindService(connection);
            bounded = false;
        }
        super.onDestroy();
    }


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SensorService.LocalBinder binder = (SensorService.LocalBinder) service;
            sensorService = binder.getService();
            bounded = true;
            //sensorService.setHelpers(nsdHelper, sensorDataDbHelper);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //sensorService.stopSearching();
            bounded = false;
        }
    };

    public void onClickDeviceInfoBtn(View v) {
        DownloadData downloadData = new DownloadData();
        downloadData.execute(new Pair<InetAddress, Integer>(address, port));
    }

    void showNoRowsWarning(final boolean temp) {
        if (sensorService.getDatabaseRowsCount() == 0) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.alert_title))
                    .setMessage(getString(R.string.string_noDatabaseCount))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            showChart(temp);
        }
    }

    public void showChart(final boolean temperature) {
        Date currentDate = new Date();
        timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                setHours = hourOfDay;
                setMinutes = minute;


                timePickerDialog.dismiss();
            }
        }, currentDate.getHours(), currentDate.getMinutes(), true);
        timePickerDialog.setCancelable(true);
        timePickerDialog.setButton(TimePickerDialog.BUTTON_POSITIVE, getString(R.string.download_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                timePickerDialog.dismiss();
                String date = new String();
                setMonth += 1;
                date += setYear;
                if (setMonth < 10) {
                    date += "/0" + setMonth;
                } else {
                    date += "/" + setMonth;
                }
                if (setDay < 10) {
                    date += "/0" + setDay;
                } else {
                    date += "/" + setDay;
                }
                if (setHours < 10) {
                    date += " 0" + setHours;
                } else {
                    date += " " + setHours;
                }
                if (setMinutes < 10) {
                    date += ":0" + setMinutes + ":00";
                } else {
                    date += ":" + setMinutes + ":00";
                }

                //Toast.makeText(getApplicationContext(),date,Toast.LENGTH_LONG).show();
                dialog = ProgressDialog.show(DeviceActivity.this, getString(R.string.string_loading), getString(R.string.string_starting));
                ArrayList<SensorData> sensorDatas = (ArrayList<SensorData>) sensorService.getDataSinceFromDatabase(date);
                Intent intent = new Intent(getApplicationContext(), ChartActivity.class);
                intent.putExtra("list", sensorDatas);
                intent.putExtra("temperature", temperature);
                dialog.dismiss();
                //Intent intent = new Intent(getApplicationContext(), ChartActivity.class);
                //intent.putExtra("date",date);
                startActivity(intent);

            }
        });
        timePickerDialog.setButton(TimePickerDialog.BUTTON_NEGATIVE, getString(R.string.download_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                timePickerDialog.dismiss();
            }
        });


        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                setYear = year;
                setMonth = monthOfYear;
                setDay = dayOfMonth;
            }
        }, currentDate.getYear() + 1900, currentDate.getMonth(), currentDate.getDay());
        datePickerDialog.setCancelable(true);
        datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, getString(R.string.download_next), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                timePickerDialog.show();
                datePickerDialog.dismiss();
            }
        });
        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, getString(R.string.download_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                datePickerDialog.dismiss();
            }
        });


        datePickerDialog.show();
    }

    public void onClickCurrentReadingsBtn(View v) {
        if (bounded) {
            //sensorService.getDataFromSensor(address, port);
            DownloadSensorData downloadSensorData = new DownloadSensorData();
            downloadSensorData.execute(new Pair<InetAddress, Integer>(address, port));
        }
    }

    public void startPeriodicTask(View v) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String interval = prefs.getString("sync_frequency", "100000");
        Log.d("SYNC", interval);

        if (bounded) {
            if (sensorService.startTimerTask(address, port, Integer.valueOf(interval))) {
                new AlertDialog.Builder(DeviceActivity.this)
                        .setTitle(getString(R.string.alert_ok))
                        .setMessage(getString(R.string.alert_threadStartedOK))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            } else {
                new AlertDialog.Builder(DeviceActivity.this)
                        .setTitle(getString(R.string.alert_warning))
                        .setMessage(getString(R.string.alert_threadStartedError))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        } else {
            new AlertDialog.Builder(DeviceActivity.this)
                    .setTitle(getString(R.string.alert_warning))
                    .setMessage(getString(R.string.string_notBounded))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    public void stopPeriodicTask(View v) {
        if (bounded) {

            if (sensorService.stopTimerTask(address)) {
                new AlertDialog.Builder(DeviceActivity.this)
                        .setTitle(getString(R.string.alert_ok))
                        .setMessage(getString(R.string.alert_threadStoppedOK))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            } else {
                new AlertDialog.Builder(DeviceActivity.this)
                        .setTitle(getString(R.string.alert_warning))
                        .setMessage(getString(R.string.alert_threadStoppedError))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        } else {
            new AlertDialog.Builder(DeviceActivity.this)
                    .setTitle(getString(R.string.alert_warning))
                    .setMessage(getString(R.string.alert_notBounded))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private class DownloadData extends AsyncTask<Pair<InetAddress, Integer>, Void, DeviceInfo> {

        protected DownloadData() {

        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(DeviceActivity.this, getString(R.string.string_loading), getString(R.string.string_starting));

        }

        @Override
        protected DeviceInfo doInBackground(Pair<InetAddress, Integer>... pairs) {
            try {

                return sensorService.getDeviceInfo(pairs[0].first, pairs[0].second);
            } catch (IOException ex) {

                return null;
            }
        }

        @Override
        protected void onPostExecute(DeviceInfo deviceInfo) {
            if (deviceInfo != null) {
                //String info = deviceInfo.getName() + ", " + deviceInfo.getId() + ", " + deviceInfo.getLocalization();
                //toast.makeText(getApplicationContext(), info, Toast.LENGTH_LONG).show();
                dialog.dismiss();
                android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
                DeviceInfoDialog deviceInfoDialog = DeviceInfoDialog.createNewInstance(deviceInfo);
                deviceInfoDialog.show(fm, "tag");
            } else {
                dialog.dismiss();
                new AlertDialog.Builder(DeviceActivity.this)
                        .setTitle(getString(R.string.alert_title))
                        .setMessage(getString(R.string.string_downloadError))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }

    }

    private class DownloadSensorData extends AsyncTask<Pair<InetAddress, Integer>, Void, SensorData> {
        AlertDialog alertDialog;

        protected DownloadSensorData() {

        }

        @Override
        protected void onPreExecute() {
            alertDialog = new AlertDialog.Builder(DeviceActivity.this).create();
            dialog = ProgressDialog.show(DeviceActivity.this, getString(R.string.string_loading), getString(R.string.string_starting));

        }

        @Override
        protected SensorData doInBackground(Pair<InetAddress, Integer>... pairs) {
            try {
                return sensorService.getDataFromDevice(pairs[0].first, pairs[0].second);
            } catch (IOException ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(SensorData sensorData) {
            if (sensorData != null) {
                dialog.dismiss();
                android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
                SensorDialog sensorDialog = SensorDialog.createNewInstance(sensorData);
                sensorDialog.show(fm, "tag");
            } else {
                dialog.dismiss();

                alertDialog.setTitle(getString(R.string.alert_title));
                alertDialog.setMessage(getString(R.string.string_downloadError));
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                alertDialog.show();
            }
        }

    }
}
