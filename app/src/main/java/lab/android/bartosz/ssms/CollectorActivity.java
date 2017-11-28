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
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CollectorActivity extends AppCompatActivity {

    protected SensorService sensorService;
    protected boolean bounded = false;
    ProgressDialog dialog;
    int downloadDataCount = 1;
    int setYear, setMonth, setDay, setMinutes, setHours;
    TimePickerDialog timePickerDialog;
    DatePickerDialog datePickerDialog;
    boolean toast;


    InetAddress address;
    int port;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collector);
        Toolbar toolbar = (Toolbar) findViewById(R.id.collector_toolbar);
        setSupportActionBar(toolbar);

        Bundle extrasBundle = getIntent().getExtras();
        if (extrasBundle != null) {
            try {
                address = InetAddress.getByAddress(extrasBundle.getByteArray("address"));
            } catch (UnknownHostException e) {
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
            port = extrasBundle.getInt("port");
        }
        listView = (ListView) findViewById(R.id.collectorListView);
        listView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        List<String> list = new ArrayList<String>();

        list.add(getString(R.string.collector_all_data));
        list.add(getString(R.string.collector_get_count));
        list.add(getString(R.string.collector_get_since));
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
                        showWarning();
                        break;
                    case 1:
                        downloadCount();
                        break;
                    case 2:
                        downloadSince();
                        break;
                    case 3:
                        showDatabase();
                        break;
                    case 4:
                        showNoRowsWarning(true);
                        break;
                    case 5:
                        showNoRowsWarning(false);
                        break;
                    default:
                        break;
                }
            }
        });
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        toast = prefs.getBoolean("show_toast",true);
    }

    public void showDatabase() {
        Intent intent = new Intent(getApplicationContext(), SensorReadingsActivity.class);
        intent.putExtra("sensorID",0);
        startActivity(intent);
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
                    dialog = ProgressDialog.show(CollectorActivity.this, getString(R.string.string_loading), getString(R.string.string_starting));
                    ArrayList<SensorData> sensorDatas = (ArrayList<SensorData>) sensorService.getDataSinceFromDatabase(date);
                    Intent intent = new Intent(getApplicationContext(), ChartActivity.class);
                    intent.putExtra("list",sensorDatas);
                    intent.putExtra("temperature",temperature);
                    dialog.dismiss();
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
            }, currentDate.getYear(), currentDate.getMonth(), currentDate.getDay());
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

    void showWarning()
    {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.alert_title))
                .setMessage(getString(R.string.string_deleteAll))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        downloadAll();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                       //Nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    void showNoRowsWarning(final boolean temp)
    {
        if(sensorService.getDatabaseRowsCount()==0) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.alert_title))
                    .setMessage(getString(R.string.string_lowDatabaseCount))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            downloadAll();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else
        {
            showChart(temp);
        }
    }

    void downloadAll() {


        DownloadAllData worker = new DownloadAllData();
        worker.execute(new Pair<InetAddress, Integer>(address, port));
    }

    void downloadCount() {

        NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(9999);
        NumberPicker.OnValueChangeListener onValueChangeListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                downloadDataCount = newVal;
            }
        };
        numberPicker.setOnValueChangedListener(onValueChangeListener);
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(numberPicker);
        builder.setTitle(getString(R.string.number_picker_title));
        builder.setPositiveButton(R.string.download_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DownloadLimitedData worker = new DownloadLimitedData();

                worker.execute(new Pair<Pair<InetAddress, Integer>, Integer>(new Pair<InetAddress, Integer>(address, port), downloadDataCount));
                downloadDataCount=1;
            }
        });
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.download_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();

    }


    void downloadSince() {
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
                DownloadDataSince worker = new DownloadDataSince();
                String date = new String();
                setMonth += 1;
                date += setYear;
                if (setMonth < 10) {
                    date += "-0" + setMonth;
                } else {
                    date += "-" + setMonth;
                }
                if (setDay < 10) {
                    date += "-0" + setDay;
                } else {
                    date += "-" + setDay;
                }
                if (setHours < 10) {
                    date += " 0" + setHours;
                } else {
                    date += " " + setHours + ":" + setMinutes + ":00";
                }
                if (setMinutes < 10) {
                    date += ":0" + setMinutes + ":00";
                } else {
                    date += ":" + setMinutes + ":00";
                }

                //Toast.makeText(getApplicationContext(),date,Toast.LENGTH_LONG).show();
                worker.execute(new Pair<Pair<InetAddress, Integer>, String>(new Pair<InetAddress, Integer>(address, port), date));
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
        }, currentDate.getYear(), currentDate.getMonth(), currentDate.getDay());
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

    private class DownloadAllData extends AsyncTask<Pair<InetAddress, Integer>, Integer, List<SensorData>> {


        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(CollectorActivity.this, getString(R.string.string_loading), getString(R.string.string_starting));

        }

        @Override
        protected List<SensorData> doInBackground(Pair<InetAddress, Integer>... params) {
            try {
                List<SensorData> dataList = sensorService.getAddDataFromCollector(params[0].first, params[0].second);
                publishProgress(0, dataList.size());
                int count = 1;
                sensorService.resetDatabase();
                for (SensorData data : dataList) {
                    sensorService.insertToDatabase(data);
                    publishProgress(1, dataList.size(), count);
                    count++;
                }
                return dataList;
            } catch (IOException ex)
            {

                return null;
            }
        }

        @Override
        protected void onPostExecute(List<SensorData> sensorDataList) {

            if(sensorDataList!=null) {
                dialog.dismiss();

                if (toast)
                    Toast.makeText(getApplicationContext(), "OK", Toast.LENGTH_LONG).show();

                new AlertDialog.Builder(CollectorActivity.this)
                        .setTitle(getString(R.string.alert_info))
                        .setMessage(getString(R.string.alert_downloadOK))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();

            } else
            {
                dialog.dismiss();
                new AlertDialog.Builder(CollectorActivity.this)
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

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0] == 0) {
                dialog.setMessage(getString(R.string.string_download) + values[1] + getString(R.string.string_adding));
            } else {
                dialog.setMessage(getString(R.string.string_inserted) + values[2] + getString(R.string.string_of) + values[1]);
            }
        }


    }

    private class DownloadLimitedData extends AsyncTask<Pair<Pair<InetAddress, Integer>, Integer>, Integer, List<SensorData>> {


        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(CollectorActivity.this, getString(R.string.string_loading), getString(R.string.string_starting));

        }

        @Override
        protected List<SensorData> doInBackground(Pair<Pair<InetAddress, Integer>, Integer>... params) {
            try {
                List<SensorData> dataList = sensorService.getLimitDataFromCollector(params[0].first.first, params[0].first.second, params[0].second);

                return dataList;
            } catch (IOException ex)
            {
                dialog.dismiss();
                new AlertDialog.Builder(getApplicationContext())
                        .setTitle(getString(R.string.alert_title))
                        .setMessage(getString(R.string.string_downloadError))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<SensorData> sensorDataList) {

            if(sensorDataList!=null) {
                dialog.dismiss();

                Intent intent = new Intent(getApplicationContext(), SensorReadingsActivity.class);
                intent.putExtra("sensorID", -1);
                intent.putExtra("list", (Serializable) sensorDataList);
                startActivity(intent);
            }

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0] == 0) {
                dialog.setMessage(getString(R.string.string_download) + values[1] + getString(R.string.string_adding));
            } else {
                dialog.setMessage(getString(R.string.string_inserted) + values[2] + getString(R.string.string_of) + values[1]);
            }
        }
    }

    private class DownloadDataSince extends AsyncTask<Pair<Pair<InetAddress, Integer>, String>, Integer, List<SensorData>> {


        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(CollectorActivity.this, getString(R.string.string_loading), getString(R.string.string_starting));

        }

        @Override
        protected List<SensorData> doInBackground(Pair<Pair<InetAddress, Integer>, String>... params) {
            try {
                return sensorService.getDataSinceFromCollector(params[0].first.first, params[0].first.second, params[0].second);
            } catch (IOException ex)
            {
                dialog.dismiss();
                new AlertDialog.Builder(getApplicationContext())
                        .setTitle(getString(R.string.alert_title))
                        .setMessage(getString(R.string.string_downloadError))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<SensorData> sensorDataList) {

            if(sensorDataList!=null) {
                dialog.dismiss();

                Intent intent = new Intent(getApplicationContext(), SensorReadingsActivity.class);
                intent.putExtra("sensorID", -1);
                intent.putExtra("list", (Serializable) sensorDataList);
                startActivity(intent);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0] == 0) {
                dialog.setMessage(getString(R.string.string_download) + values[1] + getString(R.string.string_adding));
            } else {
                dialog.setMessage(getString(R.string.string_inserted) + values[2] + getString(R.string.string_of) + values[1]);
            }
        }
    }
}
