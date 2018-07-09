package com.wgkj.rtucontrol.tcpclient;


import android.content.Context;
import android.util.Log;

import com.wgkj.rtucontrol.tcpclient.imp.OnGetUDPDataListener;
//import com.wgkj.rtucontrol.model.RtuViewModel;
//import com.wgkj.rtucontrol.utils.RTUUtils;

import java.net.DatagramPacket;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by wgkj003 on 2017/10/18.
 */

public class DeviceFinder extends UDPClient{

    public static final int FIND_DEVICE_TIMEOUT = 10000;
    private boolean deviceFound = false;
    private Timer findTimeoutTimer;
    private Timer sendIntervalTimer;
    private TimerTask sendTask;
    private boolean isFinding = false;
    private int sendCount = 0;
    public DeviceFinder(String host) {
        super(host);
    }


    protected void finalize()
    {
        stopLoopReceving();
    }



//    public void findDevice(Context context, int foundCount, int interval,final DeviceFoundListener deviceFoundListener)
//    {
//        findTimeoutTimer = new Timer();
//        findTimeoutTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                stopFind();
//                if (!deviceFound)
//                    observableErrData( "广播超时", deviceFoundListener);
//            }
//        }, duration);
//        findDevice( context, 3, deviceFoundListener);
//    }
    /**
     *
     * 上游发送数据
     *
     */
    public void findDevice(Context context, final int findCount, final int interval, int duration, final DeviceFoundListener deviceFoundListener){

        if ( isFinding)
        {
            deviceFoundListener.onErr("正在发现中,请稍后");
            return;
        }
        isFinding = true;
        sendCount = 0;
        sendIntervalTimer = new Timer();
        sendTask = new TimerTask() {
            @Override
            public void run() {
                sendCount++;
                send(new OnGetUDPDataListener() {
                    @Override
                    public void getStringData(DatagramPacket datagramPacket) {
                        observableData(datagramPacket, deviceFoundListener);
                    }

                    @Override
                    public void onErr(String errMsg) {
//                    observableErrData(errMsg,deviceFoundListener);
                    }
                });
                if (sendCount >= findCount) {

                    sendTask.cancel();
                    sendIntervalTimer.cancel();
                    sendIntervalTimer.purge();
                }

            }
        };
        sendIntervalTimer.schedule(sendTask, 0, interval);
        findTimeoutTimer = new Timer();
        findTimeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopFind();
                if (!deviceFound)
                    observableErrData( "未发现设备", deviceFoundListener);
            }
        }, duration);
    }

    public void stopFind()
    {
        isFinding = false;
        sendTask.cancel();
        sendIntervalTimer.cancel();
        sendIntervalTimer.purge();
        stopLoopReceving();
    }

    public static void setKeyWords(Context context){

    }

    public  DeviceFinder.DeviceInfo getUdpInfo(DatagramPacket datagramPacket){
        DeviceFinder.DeviceInfo deviceInfo = new DeviceFinder.DeviceInfo();
        String data = new String(datagramPacket.getData());
        // data = data.split( new String("{") )[1].split(new String("}"))[0];

        int endIndex = data.indexOf('}');
        if( data.charAt(0) != '{' || endIndex == -1)
        {
            Log.d("RTUUitls", "parse broadcast response error!");
            return deviceInfo;
        }
        data = data.substring(1, endIndex);
        String[] datas = null;
        try{
            datas = data.split(",", -1);
        }catch (Exception e){
            return deviceInfo;
        }
        if( datas.length > 2){
            deviceInfo.name = datas[0];
            String ip = datagramPacket.getAddress()+"";
            deviceInfo.ip = ip.substring(1,ip.length());
            deviceInfo.id = datas[1];
            deviceInfo.stationNum = Integer.parseInt(deviceInfo.name.substring(4,7));
            deviceInfo.wellName = datas[2];
        }
        return deviceInfo;

    }
    /**
     * 下游 解析获取传来的数据
     * @param datagramPacket 发送的消息
     * @param deviceFoundListener
     */

    private void observableData(final DatagramPacket datagramPacket,final DeviceFoundListener deviceFoundListener) {
        Observable.create(new ObservableOnSubscribe<DatagramPacket>() {
            @Override
            public void subscribe(ObservableEmitter<DatagramPacket> e) throws Exception {
                    e.onNext(datagramPacket);
            }
        }).map(new Function<DatagramPacket, DeviceInfo>() {
            @Override
            public DeviceInfo apply(DatagramPacket datagramPacket) throws Exception {

                DeviceInfo deviceInfo = getUdpInfo(datagramPacket);

                return deviceInfo;

            }
        }).subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<DeviceInfo>() {
            @Override
            public void accept(DeviceInfo deviceInfo) throws Exception {
                if (deviceInfo.id == null && deviceInfo.name == null)
                {
                    Log.i("DeviceFinder","解析错误的UDP应答");
                    return;
                }
               if(deviceInfo.id.equals("") || deviceInfo.id.equals("0000000000")){
                   deviceFoundListener.onErr("设备ＩＤ未设置");
                   return;
               }
                if(deviceInfo.name == null || deviceInfo.name.equals("")){
                    deviceFoundListener.onErr("设备名称不存在");
                    return;
                }
                if(deviceInfo.ip == null){
                    deviceFoundListener.onErr("设备ＩP不存在");
                    return;
                }
                deviceFound = true;
//                DeviceFinder.this.sendIntervalTimer.cancel();
//                sendIntervalTimer.purge();
                sendTask.cancel();
                deviceFoundListener.onDeviceFound(deviceInfo);
            }
        });
    }


    /**
     * 下游 解析获取传来的数据
     * @param err 错误码
     * @param deviceFoundListener
     */
    private void observableErrData(final String err,final DeviceFoundListener deviceFoundListener) {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext(err);
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        deviceFoundListener.onErr(s);
                    }

                });
    }

    public static class DeviceInfo{
        public String ip;//设备IP
        public String name;//设备名
        public String id;//设备id
        public int stationNum;//站号
        public String wellName;//井名

    }

    public interface DeviceFoundListener {
        void onDeviceFound(DeviceInfo deviceInfo);
        void onErr(String errMsg);
    }
}

