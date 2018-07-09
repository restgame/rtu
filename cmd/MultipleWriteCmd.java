package com.wgkj.rtucontrol.cmd;


import com.wgkj.rtucontrol.utils.HexStringUtils;

/**
 * Created by wgkj003 on 2017/10/19.
 */

public class MultipleWriteCmd extends Cmd {
    private String writeStringData;
    private byte[] byteData;
    private byte[] cmdBytes;
    private int num;//寄存器个数
    private int stationNum;
    private  int functionCode = 16;
    private int registerAddress;
    private boolean hexString;

    public MultipleWriteCmd(int stationNum, int registerAddress, int num, String writeData, Boolean hexString) {
        super( stationNum, 16/* 0x10是多个写寄存器功能码*/, registerAddress,num);
        this.writeStringData = writeData;
        this.registerAddress=registerAddress;
        this.stationNum = stationNum;
        this.hexString = hexString;
        this.num = num;

        if(hexString){
            byteData = splicingHexString();
        }else {
            byteData = splicingString();
        }
    }

    public MultipleWriteCmd(int stationNum, int registerAddress, int num, byte[] data) {
        super( stationNum, 16/* 0x10是多个写寄存器功能码*/, registerAddress,num);
        this.byteData = data;
        this.registerAddress = registerAddress;
        this.stationNum = stationNum;
        this.hexString = hexString;
        this.num = num;
    }



//    @Override
//    byte[] spliceCmdFrame() {
//        byte[] by = null;
//        byte[] bs = null;
//        by = new byte[]{0,(byte)num,(byte)(num*2)};
//        byte[] bys = HexStringUtils.addBytes(getCommonFrameHead(),by);
//
//        byte[] byCmd = HexStringUtils.addBytes(bys, byteData);
//
//        return byCmd;
//    }



    /**
     * 拼接0x10功能发送数据
     * @return
     */
    private byte[] splicingHexString(){
        byte[] bs = HexStringUtils.HexString2Bytes(writeStringData);
        return bs;
    }




    /**
     * 拼接发送String 转16进制 需要补齐0x00
     * @return
     */

    protected byte[] splicingString(){
        byte[] bs = new byte[num*2];

        byte[] b1 = writeStringData.getBytes();

        for(int i = 0;i < num*2;i++){
            if(i < b1.length){
                bs[i] = b1[i];
            }
        }
        return bs;
    }


    public byte[] getCmdBytes()
    {

        byte[] by = new byte[]{0,(byte)num,(byte)(num*2)};
        byte[] bys = HexStringUtils.addBytes(getCommonFrameHead(), by);

        cmdBytes = HexStringUtils.addBytes(bys, byteData);

        return cmdBytes;
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
