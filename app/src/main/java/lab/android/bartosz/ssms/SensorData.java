package lab.android.bartosz.ssms;



import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SensorData implements Serializable{
    private long id;
    private long sensorId;
    private float temperature;
    private float humidity;
    private Date date;


    public SensorData() {
        id=-1;
    }

    public long getId() {
        return id;
    }

    public long getSensorId()
    {
        return sensorId;
    }

    public float getTemperature()
    {
        return temperature;
    }

    public float getHumidity()
    {
        return humidity;
    }

    public Date getDate()
    {
        return date;
    }

    public void setId(long value)
    {
        this.id = value;
    }

    public void setSensorId(long value)
    {
        this.sensorId = value;
    }

    public void setTemperature(float value)
    {
        this.temperature = value;
    }

    public void setHumidity(float value)
    {
        this.humidity = value;
    }

    public void setDate(Date value)
    {
        this.date = value;
    }

    @Override
    public String toString()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(id!=-1) {
            return "ID: " + id + ", Sensor ID: "+ sensorId+", "+ dateFormat.format(date);
        } else
        {
            return "Sensor ID: "+ sensorId+", "+ dateFormat.format(date);
        }
    }
}
