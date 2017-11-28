package lab.android.bartosz.ssms;

import java.net.InetAddress;

public class MDNSDevice {
    private InetAddress address;
    private Integer port;
    private String serviceName;
    private boolean isSensor;

    public MDNSDevice(InetAddress inetAddress, Integer port, String serviceName, boolean isSensor)
    {
        this.address = inetAddress;
        this.port = port;
        this.serviceName = serviceName;
        this.isSensor = isSensor;
    }


    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public Integer getPort()
    {
        return port;
    }

    public void setPort(Integer port)
    {
        this.port = port;
    }
    public String getServiceName()
    {
        return serviceName;
    }
    public void setServiceName(String serviceName)
    {
        this.serviceName = serviceName;
    }
    public boolean getIsSensor()
    {
        return isSensor;
    }
    public void setIsSensor(boolean isSensor)
    {
        this.isSensor = isSensor;
    }

    @Override
    public String toString()
    {
        String head;
        if(isSensor)
        {
            head = "Sensor: ";
        }
        else
        {
            head = "Collector: ";
        }

        return head + serviceName + ", Address: " + address.toString() + ", Port: " + port;
    }

}
