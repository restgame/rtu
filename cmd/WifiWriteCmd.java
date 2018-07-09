package com.wgkj.rtucontrol.cmd;


import com.wgkj.rtucontrol.utils.HexStringUtils;

/**
 * Created by wgkj003 on 2017/10/19.
 */

public class WifiWriteCmd extends Cmd {
    private String writeData;
    private int num;//寄存器个数
    private int stationNum;
    private  int functionCode = 16;
    private int registerAddress;
    public WifiWriteCmd(int stationNum, int registerAddress, int num, String writeData) {
        super(stationNum, 16/* 0x10是多个写寄存器功能码*/, registerAddress,num);
        this.writeData = writeData;
        this.registerAddress=registerAddress;
        this.stationNum = stationNum;
        this.num =num;
    }



    /**
     * 拼接发送wifi数据 转16进制 需要补齐0x00
     * @return
     */
    protected byte[] splicingWifiDataAt0(){
        String[] dataStrs =writeData.split(":;");
        byte[] b1 = dataStrs[0].getBytes();
        byte[] b2 = dataStrs[1].getBytes();
        byte[] ssid = new byte[20];
        byte[] pwd = new byte[20];
        for(int i=0;i<20;i++){
            if(i<b1.length){
                ssid[i]=dataStrs[0].getBytes()[i];
            }
            if(i<b2.length){
                pwd[i]=dataStrs[1].getBytes()[i];
            }
        }
        byte[] bs =HexStringUtils.addBytes(ssid,pwd);
        return bs;
    }





    public byte[] getCmdBytes()
    {
        byte[] by = null;
        byte[] bs=null;
        by = new byte[]{0,(byte)num,(byte)(num*2)};
        byte[] bys= HexStringUtils.addBytes(getCommonFrameHead(),by);
        bs = splicingWifiDataAt0();
        byte[] byCmd = HexStringUtils.addBytes(bys,bs);

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
