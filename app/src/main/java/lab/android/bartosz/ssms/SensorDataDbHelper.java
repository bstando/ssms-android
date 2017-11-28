package lab.android.bartosz.ssms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class SensorDataDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SensorData.db";

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private static final String FLOAT_TYPE = " FLOAT";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SensorDataContract.SensorDataCol.TABLE_NAME + " ("
                    + SensorDataContract.SensorDataCol._ID + INT_TYPE +" PRIMARY KEY,"
                    + SensorDataContract.SensorDataCol.COLUMN_NAME_SENSOR_ID + INT_TYPE + COMMA_SEP
                    + SensorDataContract.SensorDataCol.COLUMN_NAME_DATE + TEXT_TYPE + COMMA_SEP
                    + SensorDataContract.SensorDataCol.COLUMN_NAME_TEMPERATURE + FLOAT_TYPE + COMMA_SEP
                    + SensorDataContract.SensorDataCol.COLUMN_NAME_HUMIDITY  + FLOAT_TYPE
                    + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SensorDataContract.SensorDataCol.TABLE_NAME;


    public SensorDataDbHelper(Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void insertData(SensorData data)
    {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SensorDataContract.SensorDataCol.COLUMN_NAME_SENSOR_ID,data.getSensorId());
        values.put(SensorDataContract.SensorDataCol.COLUMN_NAME_DATE,dateFormat.format(data.getDate()));
        values.put(SensorDataContract.SensorDataCol.COLUMN_NAME_TEMPERATURE,data.getTemperature());
        values.put(SensorDataContract.SensorDataCol.COLUMN_NAME_HUMIDITY,data.getHumidity());
        db.insert(SensorDataContract.SensorDataCol.TABLE_NAME, null,values);
    }

    public void deleteData(long id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = {""+id};
        db.delete(SensorDataContract.SensorDataCol.TABLE_NAME, SensorDataContract.SensorDataCol._ID+"=?",args);
    }

    public void updateData(SensorData data)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SensorDataContract.SensorDataCol.COLUMN_NAME_SENSOR_ID,data.getSensorId());
        values.put(SensorDataContract.SensorDataCol.COLUMN_NAME_DATE,dateFormat.format(data.getDate()));
        values.put(SensorDataContract.SensorDataCol.COLUMN_NAME_TEMPERATURE,data.getTemperature());
        values.put(SensorDataContract.SensorDataCol.COLUMN_NAME_HUMIDITY,data.getHumidity());
        String[] args = {""+ data.getId()};
        db.update(SensorDataContract.SensorDataCol.TABLE_NAME,values, SensorDataContract.SensorDataCol._ID+"=?",args);
    }

    public List<SensorData> retrieveAllData()
    {

        List<SensorData> sensorDataList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                SensorDataContract.SensorDataCol._ID,
                SensorDataContract.SensorDataCol.COLUMN_NAME_SENSOR_ID,
                SensorDataContract.SensorDataCol.COLUMN_NAME_DATE,
                SensorDataContract.SensorDataCol.COLUMN_NAME_TEMPERATURE,
                SensorDataContract.SensorDataCol.COLUMN_NAME_HUMIDITY
        };

        String sortOrder =
                SensorDataContract.SensorDataCol.COLUMN_NAME_DATE + " DESC";

        Cursor cursor = db.query(SensorDataContract.SensorDataCol.TABLE_NAME,projection,null,null,null,null,sortOrder);

        while (cursor.moveToNext())
        {
            SensorData sensorData = new SensorData();
            sensorData.setId(cursor.getLong(0));
            sensorData.setSensorId(cursor.getLong(1));
            try {
                sensorData.setDate(dateFormat.parse(cursor.getString(2)));
            } catch (ParseException ex)
            {
                sensorData.setDate(null);
            }
            sensorData.setHumidity(cursor.getFloat(4));
            sensorData.setTemperature(cursor.getFloat(3));
            sensorDataList.add(sensorData);

        }
        cursor.close();
        return sensorDataList;

    }

    public SensorData getLast() {

        SensorData sensorData = new SensorData();
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                SensorDataContract.SensorDataCol._ID,
                SensorDataContract.SensorDataCol.COLUMN_NAME_SENSOR_ID,
                SensorDataContract.SensorDataCol.COLUMN_NAME_DATE,
                SensorDataContract.SensorDataCol.COLUMN_NAME_TEMPERATURE,
                SensorDataContract.SensorDataCol.COLUMN_NAME_HUMIDITY
        };

        String sortOrder =
                SensorDataContract.SensorDataCol.COLUMN_NAME_DATE + " DESC";
        String limit = "1";

        Cursor cursor = db.query(SensorDataContract.SensorDataCol.TABLE_NAME, projection, null, null, null, null, sortOrder, limit);

        if (cursor != null) {
            cursor.moveToFirst();

            sensorData.setId(cursor.getLong(0));
            sensorData.setSensorId(cursor.getLong(1));
            try {
                sensorData.setDate(dateFormat.parse(cursor.getString(2)));
            } catch (ParseException ex) {
                sensorData.setDate(null);
            }
            sensorData.setHumidity(cursor.getFloat(4));
            sensorData.setTemperature(cursor.getFloat(3));


            cursor.close();
        }
        return sensorData;

    }

    public SensorData getByID(long id)
    {

        SensorData sensorData = new SensorData();
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                SensorDataContract.SensorDataCol._ID,
                SensorDataContract.SensorDataCol.COLUMN_NAME_SENSOR_ID,
                SensorDataContract.SensorDataCol.COLUMN_NAME_DATE,
                SensorDataContract.SensorDataCol.COLUMN_NAME_TEMPERATURE,
                SensorDataContract.SensorDataCol.COLUMN_NAME_HUMIDITY
        };

        String sortOrder =
                SensorDataContract.SensorDataCol.COLUMN_NAME_DATE + " DESC";
        String where = SensorDataContract.SensorDataCol._ID+"=?";
        String[] whereArgs= {""+id};

        Cursor cursor = db.query(SensorDataContract.SensorDataCol.TABLE_NAME,projection,where,whereArgs,null,null,sortOrder);
        if (cursor != null) {
            cursor.moveToFirst();

            sensorData.setId(cursor.getLong(0));
            sensorData.setSensorId(cursor.getLong(1));
            try {
                sensorData.setDate(dateFormat.parse(cursor.getString(2)));
            } catch (ParseException ex) {
                sensorData.setDate(null);
            }
            sensorData.setHumidity(cursor.getFloat(4));
            sensorData.setTemperature(cursor.getFloat(3));


            cursor.close();
        }
        return sensorData;

    }

    public List<SensorData> getByDate(String date)
    {

        List<SensorData> sensorDataList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                SensorDataContract.SensorDataCol._ID,
                SensorDataContract.SensorDataCol.COLUMN_NAME_SENSOR_ID,
                SensorDataContract.SensorDataCol.COLUMN_NAME_DATE,
                SensorDataContract.SensorDataCol.COLUMN_NAME_TEMPERATURE,
                SensorDataContract.SensorDataCol.COLUMN_NAME_HUMIDITY
        };

        String sortOrder =
                SensorDataContract.SensorDataCol.COLUMN_NAME_DATE + " ASC";

        String where = SensorDataContract.SensorDataCol.COLUMN_NAME_DATE+" >= ?";
        String[] whereArgs= {date};
        Log.e("DATE",date);

        Cursor cursor = db.query(SensorDataContract.SensorDataCol.TABLE_NAME,projection,where,whereArgs,null,null,sortOrder);


        while (cursor.moveToNext())
        {
            SensorData sensorData = new SensorData();
            sensorData.setId(cursor.getLong(0));
            sensorData.setSensorId(cursor.getLong(1));
            try {
                sensorData.setDate(dateFormat.parse(cursor.getString(2)));
            } catch (ParseException ex)
            {
                sensorData.setDate(null);
            }
            sensorData.setHumidity(cursor.getFloat(4));
            sensorData.setTemperature(cursor.getFloat(3));
            sensorDataList.add(sensorData);

        }
        cursor.close();
        return sensorDataList;

    }

    public List<SensorData> getByDeviceID(int deviceID)
    {

        List<SensorData> sensorDataList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                SensorDataContract.SensorDataCol._ID,
                SensorDataContract.SensorDataCol.COLUMN_NAME_SENSOR_ID,
                SensorDataContract.SensorDataCol.COLUMN_NAME_DATE,
                SensorDataContract.SensorDataCol.COLUMN_NAME_TEMPERATURE,
                SensorDataContract.SensorDataCol.COLUMN_NAME_HUMIDITY
        };

        String sortOrder =
                SensorDataContract.SensorDataCol.COLUMN_NAME_DATE + " DESC";

        String where = SensorDataContract.SensorDataCol.COLUMN_NAME_SENSOR_ID+"=?";
        String[] whereArgs= {""+deviceID};

        Cursor cursor = db.query(SensorDataContract.SensorDataCol.TABLE_NAME,projection,where,whereArgs,null,null,sortOrder);

        while (cursor.moveToNext())
        {
            SensorData sensorData = new SensorData();
            sensorData.setId(cursor.getLong(0));
            sensorData.setSensorId(cursor.getLong(1));
            try {
                sensorData.setDate(dateFormat.parse(cursor.getString(2)));
            } catch (ParseException ex)
            {
                sensorData.setDate(null);
            }
            sensorData.setHumidity(cursor.getFloat(4));
            sensorData.setTemperature(cursor.getFloat(3));
            sensorDataList.add(sensorData);

        }
        cursor.close();
        return sensorDataList;

    }

    public long countRows()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return DatabaseUtils.queryNumEntries(db,SensorDataContract.SensorDataCol.TABLE_NAME);
    }


    public void reset()
    {
        onUpgrade(this.getWritableDatabase(),1,1);
    }

}
