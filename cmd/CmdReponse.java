package com.wgkj.rtucontrol.cmd;

/**
 * Created by wgkj003 on 2018/1/18.
 */

public class CmdReponse
{
    public enum CmdReponseResult { READ_SUCCESS, READ_FAILS, WRITE_SUCCESS, WRITE_FAILS};

    protected CmdReponseResult result;
    protected int stationNo;
    protected byte[] content;
    protected byte functionCode;
    protected byte errCode;

    public CmdReponseResult getResult() {
        return result;
    }

    public void setResult(CmdReponseResult result) {
        this.result = result;
    }

    public int getStationNo() {
        return stationNo;
    }

    public void setStationNo(int stationNo) {
        this.stationNo = stationNo;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public byte getFunctionCode() {
        return functionCode;
    }

    public void setFunctionCode(byte functionCode) {
        this.functionCode = functionCode;
    }

    public byte getErrCode() {
        return errCode;
    }

    public void setErrCode(byte errCode) {
        this.errCode = errCode;
    }
}