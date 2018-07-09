package com.wgkj.rtucontrol.cmd;

import java.util.LinkedList;

/**
 * Created by wgkj003 on 2017/10/19.
 */

public class FrameParser {
    final int HEADER_SIZE = 6;  //帧头长度
    private byte firstByte;
    private byte secondByte;
    private byte[] cmdBytes;
    private int functionCode;
    private Cmd cmd;
    private PARSE_STATUS currentStatus = PARSE_STATUS.TAG1;
    private int lenToRead;
    private LinkedList<Byte> byteList = new LinkedList<Byte>();
    int count = 0;


    public enum PARSE_STATUS{ START, TAG1, TAG2, TAG3, TAG4, TAG5, TAG_LEN, READ_CONTENT, FINISH, ERROR};

    final public byte READ_CODE = 0x03;
    final public byte READ_FAILS_CODE = (byte)0x83;
    final public byte SINGLE_WRITE_CODE = 0x06;
    final public byte MULTIPLE_WRITE_CODE = 0x10;
    final public byte WRITE_FAILS_CODE = (byte)0x86;


    public FrameParser(Cmd cmd) {
        this.cmd = cmd;
    }

    /**
     * 获取帧头数据
     * @return
     */
    public byte[] getFrameHeader(){
        byte[] FrameHeaderBytes = new byte[6];
        for(int i = 0; i < 6; i++){
            FrameHeaderBytes[i] = cmdBytes[i];
        }
        return FrameHeaderBytes;
    }


    /**
     * 获取帧头+站号+功能码+数据长 前部head长
     * @return
     */
    private int getHeaderSize(){
        return 9;
    }


    /**
     * 获取数据长度
     * @return
     */
    private int getDataSize(){
        int idint0 = cmdBytes[8];
        if(idint0 < 0)
            idint0 += 256;
        return idint0;
    }


    /**
     * 获取有效数据
     * @return
     */
    protected byte[] getCmdEffectiveData(){
        byte[] dataBytes = new byte[getDataSize()];
        for(int i = 0; i < getDataSize(); i++){
            dataBytes[i] = cmdBytes[i+getHeaderSize()];
        }
        return dataBytes;
    }

    private void validateFrameHeader()throws Exception
    {
        if(cmdBytes[0] == 123 && cmdBytes[1] == 67){
            byte[] byts = new byte[cmdBytes.length-27];
            for(int i = 0;i<cmdBytes.length-27;i++){
                byts[i] = cmdBytes[i+27];
            }
            cmdBytes = byts;
        }

        if (cmdBytes[0] != cmd.getFrameFirstByte() ||
                cmdBytes[1] != cmd.getFrameSecondByte()) {
            throw new Exception("front two bytes are wrong!");
        }

        if (cmdBytes[2] != 0 ||
                cmdBytes[3] != 0 || cmdBytes[4] != 0) {
            throw new Exception("middle three bytes are wrong!");
        }

        int DATA_SIZE = cmdBytes.length - HEADER_SIZE;
//        if ( cmdBytes[5] !=  DATA_SIZE )
//            throw new Exception("data size is wrong!");

    }


    //parse the whole frame as package data
    public CmdReponse parse(byte[] cmdBytes) throws Exception {
        this.cmdBytes = cmdBytes;
        try
        {
            validateFrameHeader();

            CmdReponse cmdReponse = new CmdReponse();
            cmdReponse.setStationNo(this.cmdBytes[6]);
            cmdReponse.setFunctionCode(this.cmdBytes[7]);
            switch ( cmdReponse.getFunctionCode())
            {
                case READ_CODE:
                    cmdReponse.setResult( CmdReponse.CmdReponseResult.READ_SUCCESS);
                    cmdReponse.setContent( getCmdEffectiveData());
                    break;
                case READ_FAILS_CODE:
                    cmdReponse.setResult(CmdReponse.CmdReponseResult.READ_FAILS);
                    cmdReponse.setErrCode( cmdBytes[8]);
                    break;
                case SINGLE_WRITE_CODE:
                case MULTIPLE_WRITE_CODE:
                    cmdReponse.setResult(CmdReponse.CmdReponseResult.WRITE_SUCCESS);
                    break;
                case WRITE_FAILS_CODE:
                    cmdReponse.setResult(CmdReponse.CmdReponseResult.WRITE_FAILS);
                    cmdReponse.setErrCode( cmdBytes[8]);
                    break;
            }
            return cmdReponse;
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    /*
        return code:
            err = -1,
            */
    public PARSE_STATUS input(byte inByte)
    {
        switch( currentStatus)
        {
            case TAG1:
                if (cmd.getFrameFirstByte() == inByte) {
                    byteList.add(inByte);
                    currentStatus = PARSE_STATUS.TAG2;
                }
                else {
                    currentStatus = PARSE_STATUS.TAG1;
                    byteList.clear();
                }
                break;
            case TAG2:
                if (cmd.getFrameSecondByte() == inByte) {
                    byteList.add(inByte);
                    currentStatus = PARSE_STATUS.TAG3;
                }
                else {
                    currentStatus = PARSE_STATUS.TAG1;
                    byteList.clear();
                }
                break;
            case TAG3:
                if ( inByte == 0) {
                    byteList.add(inByte);
                    currentStatus = PARSE_STATUS.TAG4;
                }
                else {
                    currentStatus = PARSE_STATUS.ERROR;
                    byteList.clear();
                }
                break;
            case TAG4:
                if ( inByte == 0) {
                    byteList.add(inByte);
                    currentStatus = PARSE_STATUS.TAG5;
                }
                else {
                    currentStatus = PARSE_STATUS.ERROR;
                    byteList.clear();
                }
                break;
            case TAG5:
                if ( inByte == 0) {
                    byteList.add(inByte);
                    currentStatus = PARSE_STATUS.TAG_LEN;
                }
                else {
                    currentStatus = PARSE_STATUS.ERROR;
                    byteList.clear();
                }
                break;
            case TAG_LEN:
                lenToRead = inByte & 0xFF;
                if ( lenToRead > 0 ) {
                    byteList.add(inByte);
                    currentStatus = PARSE_STATUS.READ_CONTENT;
                }
                else {
                    currentStatus = PARSE_STATUS.ERROR;
                    byteList.clear();
                }
                break;
            case READ_CONTENT:
                byteList.add(inByte);
                if ( --lenToRead == 0)
                    currentStatus = PARSE_STATUS.FINISH;
        }

        return currentStatus;
    }

    public byte[] getFrameData()
    {
        byte[] bytes = new byte[byteList.size()];
        for ( int i = 0; i < byteList.size(); i++)
        {
            bytes[i] = byteList.get(i);
        }
        return bytes;
    }
}
