package com.wgkj.rtucontrol.cmd;


import com.wgkj.rtucontrol.utils.HexStringUtils;

/**
 * Created by wgkj003 on 2017/10/19.
 */

public class ReadCmd extends Cmd {
    private int num;//寄存器个数
    private int stationNum;
    private  int functionCode = 3;
    private int registerAddress;
    public ReadCmd(int stationNum, int registerAddress, int num) {
        super( stationNum, 3/* 0x10是多个写寄存器功能码*/, registerAddress,num);
        this.stationNum = stationNum;
        this.num = num;
        this.registerAddress = registerAddress;
    }


    public byte[] getCmdBytes()
    {
        byte[] by = new byte[]{0, (byte)num};
        byte[] bys= HexStringUtils.addBytes(getCommonFrameHead(),by);
        byte[] byCmd = HexStringUtils.addBytes(bys,by);
        return byCmd;
    }


}
