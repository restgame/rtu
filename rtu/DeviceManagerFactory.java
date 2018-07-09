package com.wgkj.rtucontrol.rtu;

import java.util.List;

/**
 * Created by wgkj003 on 2018/1/24.
 */

public class DeviceManagerFactory {

    private static DeviceManager currentManager = null;

    public static DeviceManager getCurrentManager() {
        return currentManager;
    }

//    static DeviceManager createManager( String ssid, String pswd)
//    {
//        currentManager = new DeviceManager( ssid, pswd);
//        return currentManager;
//    }

    static List<DeviceManager> obtainAllManagersFromDB()
    {
        return null;
    }

//    static void SaveManagerToDB( DeviceManager dm)
//    {
//        DeviceManager.Wifi wifi = dm.getWifiInfo();
//    }
}
