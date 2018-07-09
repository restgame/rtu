package com.wgkj.rtucontrol.cmd;


import com.wgkj.rtucontrol.utils.HexStringUtils;

/**
 * Created by wgkj003 on 2017/10/19.
 */

public class SingleWriteCmd extends Cmd {


    private int writeData;
    private int registerAddress;
    private int stationNum;
    private  int functionCode = 6;
    public SingleWriteCmd(int stationNum, int registerAddress, int data) {
        super( stationNum, 0x06 /* 0x06是单个写寄存器功能码*/, registerAddress);
        this.writeData = data;
        this.registerAddress = registerAddress;
        this.stationNum = stationNum;
    }



    public byte[] getCmdBytes()
    {
        byte[] hl = HexStringUtils.getHighLow(writeData);
        byte[] byCmd = HexStringUtils.addBytes(getCommonFrameHead(), hl);
        return byCmd;
    }


//
//    /**
//     *  拼接请求帧头
//     */
//    protected byte[] splicingCmdFrame(){
//        byte[] by = null;
//        byte heightAddress = (byte) HexStringUtils.getHighLow(registerAddress)[0];
//        byte lowAddress = (byte) HexStringUtils.getHighLow(registerAddress)[1];
//        byte  frameSize = (byte)removeFrameSize();
//        int  frameOne = CommonUtils.randomNum();
//        RTU_FRAME_ONE = frameOne;
//        int  frameTwo = CommonUtils.randomNum();
//        RTU_FRAME_TWO = frameTwo;
//        byte dataSize = (byte)(num*2);
//        if(functionCode==3){
//            by = new byte[]{(byte)frameOne, (byte)frameTwo, 0, 0, 0, frameSize, (byte) stationNum, (byte) functionCode, heightAddress, lowAddress, 0, (byte) offset};
//        }else if(functionCode==6) {
//            byte heightData = 0;
//            byte lowData = 0;
//            if(isInt){
//                int  dataInt = Integer.parseInt(data);
//                heightData = (byte) HexStringUtils.getHighLow(dataInt)[0];
//                lowData = (byte) HexStringUtils.getHighLow(dataInt)[1];
//            }
//            by = new byte[]{(byte)frameOne, (byte)frameTwo, 0, 0, 0, frameSize, (byte) stationNum, (byte) functionCode, heightAddress, lowAddress, heightData, lowData};
//        }else if(functionCode==16){
//            by = new byte[]{(byte)frameOne, (byte)frameTwo,0,0,0,frameSize,(byte)stationNum,(byte)functionCode,heightAddress,lowAddress,0,(byte)offset,dataSize};
//        }
//        return by;
//    }
}
