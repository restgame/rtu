package com.wgkj.rtucontrol.rtu;

/**
 * Created by zyk on 2018/1/12.
 */

public class Device {

    enum DeviceType { RTU }

    DeviceType deviceType = DeviceType.RTU;

    public Device( DeviceType type)
    {
        deviceType = type;
    }

    public DeviceType getDeviceType()
    {
        return  deviceType;
    }


    public interface DeviceFoundListener {
        void onDeviceFound(Device device);
        void onErr(String errMsg);
    }
}
