package com.wgkj.rtucontrol.tcpclient.imp;

import com.wgkj.rtucontrol.cmd.FrameParser;

/**
 * Created by wgkj003 on 2017/10/18.
 */

public interface TCPClientInterface {
    Boolean initClient();
    void closeClient();
    void sendData(byte[] data, FrameParser frameParser, DataReceivedListener dataReceivedListener) throws Exception;
    void logI(String msg);
}
