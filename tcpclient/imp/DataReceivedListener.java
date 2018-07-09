package com.wgkj.rtucontrol.tcpclient.imp;

/**
 * Created by wgkj003 on 2017/10/18.
 */

public interface DataReceivedListener {
    void onBytesDataReceived(byte[] b);
    void onErr(String errMsg);
}
