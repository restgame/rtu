package com.wgkj.rtucontrol.rtu;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.wgkj.rtucontrol.tcpclient.DeviceFinder;
import com.wgkj.rtucontrol.model.DBManager;
import com.wgkj.rtucontrol.model.NetworkModel;
import com.wgkj.rtucontrol.model.RtuViewModel;
import com.wgkj.rtucontrol.wifi.WifiAdmin;

import java.util.HashMap;
import java.util.List;

import static com.wgkj.rtucontrol.config.RTUConfig.RTU_TCP_PORT;
import static java.lang.Thread.sleep;

/**
 * Created by zyk on 2018/1/12.
 */

public class DeviceManager {

    private static DeviceManager currentManager = null;
    private boolean isRtuLife = true;
    private String managerName = "默认井场";
    public final static int NOT_FOUND_RTU_ERR = 500;

    private String broadcastIp;
    private WifiManager wifiManager;
    WifiAdmin mWifiAdmin;
    private Context context;

    private Device currentDevice;

    private DeviceFinder deviceFinder;
    private NetworkModel networkModel;
    private DBManager dbManager;
    final private int FIND_DEVICE_TIMEOUT = 8000;
    final public int DEVICE_NOT_FOUND = 2;
    private int verifyCount = 10;
    private HashMap<String, Device> devices = new HashMap<>();
    public static DeviceManager getCurrentManager() {
        return currentManager;
    }

    public DeviceManager(Context context, NetworkModel networkModel) {
        currentManager = this;
        this.context = context;
        this.networkModel = networkModel;

        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //broadcastIp = RTUUtils.ip255(HexStringUtils.intToIp(wifiManager.getConnectionInfo().getIpAddress()));
        broadcastIp = networkModel.getIp();
        deviceFinder = new DeviceFinder(broadcastIp);
        dbManager = DBManager.getInstance(context);
    }


    /**
     * 等待单个RTU连接至控制网络
     * @param id
     * @param deviceFoundListener
     */
    public void waitRtuConnectNetwork(final String id, final Rtu.DeviceFoundListener deviceFoundListener) {

        deviceFinder.findDevice(context, 15, 1000, 15000, new DeviceFinder.DeviceFoundListener() {
            @Override
            public void onDeviceFound(DeviceFinder.DeviceInfo deviceInfo) {
                if (deviceInfo.name.equals(id)) {
                    deviceFinder.stopFind();
                    Rtu rtu = null;
                    if(deviceInfo.id == null || "".equals(deviceInfo.id)){
                        return;
                    }
                    if (!devices.containsKey(deviceInfo.ip) ) {
                        rtu = new Rtu( deviceInfo, RTU_TCP_PORT);
                        devices.put(deviceInfo.id, rtu);
                    }
                    rtu = (Rtu) getDeviceById(deviceInfo.id);
                    deviceFoundListener.onDeviceFound(rtu);
                }

            }
            @Override
            public void onErr(String errMsg) {
                deviceFoundListener.onErr("未发现设备");
            }
        });
    }



    public Device getCurrentDevice(){
        return currentDevice;
    }

    public void setCurrentDevice(Device device)
    {
        this.currentDevice = device;
    }

    /**
     * 通过Device ID获取RTU设备
     * @param id
     * @return
     */
    public Device getDeviceById(String id)
    {
        return devices.get(id);
    }

    //    public void findDeviceById(final String id, final Rtu.DeviceFoundListener deviceFoundListener) throws Exception
//    {
//        final Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                deviceFoundListener.onErr( DEVICE_NOT_FOUND );
//            }
//        }, FIND_DEVICE_TIMEOUT);
//
//        deviceFinder.findDevice(context,new DeviceFinder.DeviceFoundListener() {
//
//            @Override
//            public void onDeviceFound(DeviceFinder.DeviceInfo deviceInfo) {
//                Rtu rtu = null;
//                if (!devices.containsKey(deviceInfo.ip) ) {
//                    rtu = new Rtu(deviceInfo.ip, RTU_TCP_PORT, context);
//                    devices.put(deviceInfo.ip, rtu);
//                }
//                rtu = (Rtu) devices.get(deviceInfo.ip);
//                if ( rtu.getId() == id) {
//                    timer.cancel();
//                    deviceFoundListener.onDeviceFound(rtu);
//                }
//            }
//
//            @Override
//            public void onErr(String errMsg) {
//
//            }
//        });
//    }

    /**
     * 发现网络下所有RTU设备
     * @param deviceFoundListener
     */
    public void findAllDeviceByBroadcast(final Rtu.DeviceFoundListener deviceFoundListener)
    {

        deviceFinder.findDevice(context,1, 1000, 5000, new DeviceFinder.DeviceFoundListener() {
            @Override
            public void onDeviceFound(DeviceFinder.DeviceInfo deviceInfo) {
                Rtu rtu = null;
                if(deviceInfo.id == null || "".equals(deviceInfo.id) || deviceInfo.id.equals("0000000000")){
                    deviceFoundListener.onErr(deviceInfo.wellName + "设备ID未设置");
                    return;
                }

                if (!devices.containsKey(deviceInfo.id) ) {
                    rtu = new Rtu( deviceInfo, RTU_TCP_PORT);
                    rtu.getRtuModel().setRtuName(deviceInfo.name);
                    devices.put(deviceInfo.id, rtu);
                }
                else
                    rtu = (Rtu) getDeviceById(deviceInfo.id);
                rtu.setIp(deviceInfo.ip);
                rtu.getRtuModel().setWifiIp(deviceInfo.ip);

                deviceFoundListener.onDeviceFound(rtu);
            }

            @Override
            public void onErr(String errMsg) {

            }
        });//10秒后关闭
    }

    private boolean isExist(String s,List<RtuViewModel> rtuModels) {
        for (int i = 0; i < rtuModels.size(); i++) {
            if (s.equals(rtuModels.get(i).getWellName())) {
                return true;
            }
        }

        return false;

    }


    public HashMap<String,Device> getDeviceList()
    {
        return devices;
    }

    public Device getDeviceByIndex(int index)
    {
        return devices.get(index);
    }

    public Device getDeviceByNo(String id)
    {
        return null;
    }

    public void closeFind(){
        if (deviceFinder != null) {
            deviceFinder.stopFind();
            //deviceFinder.closeClient();
        }
    }

//    public void saveDevicesToDBCache()
//    {
//        for ( String key : devices.keySet())
//        {
//            Rtu rtu = (Rtu)devices.get(key);
//            RtuModel model = rtu.getRtuModel();
//            model.setNetworkId( networkModel.get_id());
//            if(!"".equals(rtu.getId()) && rtu.getId() != null && model.getDeviceId() != null){
//                DBManager.getInstance(context).saveRtuModel( model );
//            }
//        }
//    }

//    public void loadDevicesFromDBCache()
//    {
//        List<RtuModel> models = dbManager.queryRtuModel(networkModel);
//
//        for ( RtuModel model : models) {
//            Rtu rtu = new Rtu( model );
//            if(rtu.getId() != null && !"".equals(rtu.getId())){
//                devices.put(rtu.getId(), rtu);
//            }
//        }
//    }

    public void removeDevice(Device device)
    {
        Rtu rtu = (Rtu)device;
        devices.remove(rtu.getId());
//        if (rtu.rtuModel.get_id() != null)
//            dbManager.deleteRtuModel(rtu.getRtuModel());
    }

//    public List<RtuModel> getDeviceModelList()
//    {
//        List<RtuModel> modelList = new ArrayList<RtuModel>();
//        for( String key: devices.keySet())
//        {
//            Rtu rtu = (Rtu)devices.get(key);
//            modelList.add( rtu.getRtuModel());
//        }
//        return modelList;
//    }

}

