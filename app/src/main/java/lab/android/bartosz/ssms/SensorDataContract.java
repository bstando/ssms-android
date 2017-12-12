package lab.android.bartosz.ssms;

import android.provider.BaseColumns;


public class SensorDataContract {
    public SensorDataContract() {
    }

    public static abstract class SensorDataCol implements BaseColumns {
        public static final String TABLE_NAME = "sensor";
        public static final String COLUMN_NAME_SENSOR_ID = "sensorId";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_TEMPERATURE = "temperature";
        public static final String COLUMN_NAME_HUMIDITY = "humidity";
    }
}
