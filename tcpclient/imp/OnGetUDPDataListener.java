package com.wgkj.rtucontrol.tcpclient.imp;

import java.net.DatagramPacket;

/**
 * Created by wgkj003 on 2017/10/18.
 */

public interface OnGetUDPDataListener {
    void getStringData(DatagramPacket datagramPacket);
    void onErr(String errMsg);
}
