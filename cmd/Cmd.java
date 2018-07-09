package com.wgkj.rtucontrol.cmd;

//import com.wgkj.rtucontrol.utils.CommonUtils;
import com.wgkj.rtucontrol.utils.HexStringUtils;
/**
 * Created by wgkj003 on 2018/1/9.
 */

abstract public class Cmd {

    private byte[] CmdBytes;

    //寄存器地址
    private int registerAddress;
    //站号
    private int stationNum;
    //功能码
    private int functionCode;
    //个数
    private int num;
    //发送的数据
    private String data;

    //是否补齐0X00
    private Boolean b;
    //是否Int转16进制
    Boolean isInt;
    private byte firstbyte;
    private byte secondbyte;

    public Cmd(int stationNum, int functionCode, int registerAddress,int num) {
        this.functionCode = functionCode;
        this.registerAddress = registerAddress;
        this.stationNum = stationNum;
        this.num=num;
    }

    public Cmd(int stationNum, int functionCode, int registerAddress) {
        this.functionCode = functionCode;
        this.registerAddress = registerAddress;
        this.stationNum = stationNum;
    }
    /**
     *  除帧头外 长度
     */
    private int  removedFrameHeaderSize(){
        if(functionCode == 3 || functionCode==6){
            return 6;
        }
        return 7 + num*2;
    }

    protected byte[] getCommonFrameHead()
    {
        byte[] address = HexStringUtils.getHighLow(registerAddress);
        firstbyte = (byte)randomNum();
        secondbyte =(byte)randomNum();
        return new byte[]{firstbyte, secondbyte, 0, 0, 0,(byte)removedFrameHeaderSize(),(byte) stationNum, (byte) functionCode, address[0], address[1]};
    }

    public  int randomNum(){
        java.util.Random random=new java.util.Random();// 定义随机类
        int result=random.nextInt(10);// 返回[0,10)集合中的整数，注意不包括10
        return result;
    }

    abstract public byte[] getCmdBytes();


    public int getFunctionCode()
    {
        return this.functionCode;
    }

    public byte getFrameFirstByte(){
        return this.firstbyte;
    }

    public byte getFrameSecondByte(){
        return this.secondbyte;
    }
}
