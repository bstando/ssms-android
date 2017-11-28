package lab.android.bartosz.ssms;


import java.io.Serializable;
import java.net.InetAddress;

public class DeviceInfo implements Serializable{
    private int id;
    private String name;
    private String localization;
    private InetAddress address;
    private int port;

    public int getId()
    {
        return id;
    }
    public String getName()
    {
        return name;
    }
    public String getLocalization()
    {
        return localization;
    }
    public InetAddress getAddress()
    {
        return address;
    }
    public int getPort()
    {
        return port;
    }
    public void setId(int value)
    {
        this.id = value;
    }
    public void setName(String value)
    {
        this.name = value;
    }
    public void setLocalization(String value)
    {
        this.localization = value;
    }
    public void setAddress(InetAddress value)
    {
        this.address = value;
    }
    public void setPort(int value)
    {
        this.port = value;
    }

    @Override
    public String toString()
    {
        return "Name: " + name + ", ID: " + id;
    }
}
