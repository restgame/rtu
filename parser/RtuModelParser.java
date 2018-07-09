package com.wgkj.rtucontrol.parser;

import com.wgkj.rtucontrol.utils.RTUUtils;
import com.wgkj.rtucontrol.rtu.Rtu;
import com.wgkj.rtucontrol.model.RtuModel;
import com.wgkj.rtucontrol.utils.CommonUtils;

import java.util.Date;

/**
 * Created by wgkj003 on 2018/1/18.
 */

public class RtuModelParser {
    private RtuModel rtuModel = null;
    private byte[] data;
    public RtuModelParser( RtuModel model)
    {
        rtuModel = model;
    }

    public RtuModel parse(Rtu.RtuInfoType type , byte[] data)
    {
        this.data = data;
        switch ( type){
            case RTU_INFO:
                parseInfo();
                break;
            case RTU_CONTROL_INFO:
                parseControlInfo();
                break;
            case RTU_VIEW_INFO:
                parseViewInfo();
                break;
            case RTU_VIEW2_INFO:
                parseView2Info();
                break;
            case RTU_VIEW3_INFO:
                parseView3Info();
                break;
            case RTU_PARAMS:
                parseParams();
                break;
        }
        return rtuModel;
    }

    //获取RTU信息
    private void parseParams()
    {
        Date d = new Date();
        d.setYear(CommonUtils.Byte2BCD(data[0]) + 100);
        d.setMonth(CommonUtils.Byte2BCD(data[1]) - 1);
        d.setDate(CommonUtils.Byte2BCD(data[2]));
        d.setHours(CommonUtils.Byte2BCD(data[3]));
        d.setMinutes(CommonUtils.Byte2BCD(data[4]));
        d.setSeconds(CommonUtils.Byte2BCD(data[5]));
        rtuModel.setDateTime(d);

        rtuModel.setSoftwareVersion( String.valueOf(RTUUtils.getRegisterData(data[16], data[17])/ 100.0f));
        rtuModel.setHardwareVersion( String.valueOf(RTUUtils.getRegisterData(data[24], data[25])/ 100.0f));
    }

    //获取RTU信息 地址段40001-40059
    private void parseInfo()
    {
        //List<Integer> s = RTUUtils.getRtuDataInfoList(data);

        rtuModel.setWellName(RTUUtils.getRtuName(data));
        rtuModel.setWellType(RTUUtils.getRtuType(data));
        rtuModel.setFaultFlag(RTUUtils.getFaultFlag(data));

        if ( rtuModel.getFaultFlag() )
            rtuModel.setFaultStatus( RTUUtils.getRtuFault(data));
        else
            rtuModel.setFaultStatus("无");

    }
    //获取RTU控制信息,地址段40101-40138
    private void parseControlInfo()
    {
        rtuModel.setRunningStatu(RTUUtils.extractRegisterData( data, 1) == 2 ); //2:开机,1:停机
        rtuModel.setDlax(RTUUtils.extractRegisterData( data, 2) / 100.00 + "A");
        rtuModel.setDlbx(RTUUtils.extractRegisterData( data, 3) / 100.00 + "A");
        rtuModel.setDlcx(RTUUtils.extractRegisterData( data, 4) / 100.00 + "A");
        rtuModel.setDyax(RTUUtils.extractRegisterData( data, 5) / 10.0 + "V");
        rtuModel.setDybx(RTUUtils.extractRegisterData( data, 6) / 10.0 + "V");
        rtuModel.setDycx(RTUUtils.extractRegisterData( data, 7) / 10.0 + "V");
        rtuModel.setYggl(RTUUtils.extractRegisterSignedData( data, 8) / 100.0 + "Kw");  //有功功率

        rtuModel.setWggl( RTUUtils.extractRegisterSignedData( data, 9)/ 100.0 + "Kvar");
        rtuModel.setSzgl(RTUUtils.extractRegisterSignedData( data, 10) / 100.0 + "KVA");
        rtuModel.setYjyxpl(RTUUtils.extractRegisterData( data, 11) / 100.00 + "HZ");
        rtuModel.setDxzt(RTUUtils.extractRegisterData( data, 12) == 0? "正常":"断相");
        rtuModel.setYjglys(RTUUtils.extractRegisterSignedData( data, 13) / 100.00 + "");
        rtuModel.setTemperature(RTUUtils.extractRegisterData( data, 14) / 100.00 + "℃");

        rtuModel.setPlControlMode( RTUUtils.extractRegisterData( data, 30));

        rtuModel.setPlControlStatus( getAutoManualText(RTUUtils.extractRegisterData( data, 31)));
        rtuModel.setPlValue( RTUUtils.extractRegisterData( data, 32) / 100 +"Hz");

        rtuModel.setPhControlMode( RTUUtils.extractRegisterData( data, 33) );
        rtuModel.setPhControlStatus( getAutoManualText((RTUUtils.extractRegisterData( data, 34))) );
        rtuModel.setPhValue( RTUUtils.extractRegisterData( data, 36) / 100 + "");
        rtuModel.setControlMode( RTUUtils.extractRegisterData( data, 37) );
    }

    private String getAutoManualText( int val)
    {
        String str;
        if (val == 1)
            str = "手动";
        else if (val == 2)
            str = "自动";
        else
            str = "未知";
        return str;
    }

    //获取RTU视图信息  地址段40201-40207
    private void parseViewInfo()
    {
        //rtuModel.setViewTime(CommonUtils.getSystemsTime());
        rtuModel.setViewCc(RTUUtils.extractRegisterData( data, 0) / 100.00 + "m");
        rtuModel.setViewCci(RTUUtils.extractRegisterData( data, 1) / 100.00 + "次/分");
        rtuModel.setViewDataPointCount( RTUUtils.extractRegisterData( data, 2) );
        rtuModel.setViewZdzh(RTUUtils.extractRegisterData( data, 3) / 100.00 + "KN");
        rtuModel.setViewZxzh(RTUUtils.extractRegisterData( data, 4) / 100.00 + "KN");

        rtuModel.setViewZdwy(RTUUtils.extractRegisterData( data, 5) / 100.00 + "m");
        rtuModel.setViewZxwy(RTUUtils.extractRegisterData( data, 6) / 100.00 + "m");

        rtuModel.setZdzhInt(RTUUtils.extractRegisterData( data, 3) / 100);
        rtuModel.setZdwyInt(RTUUtils.extractRegisterData( data, 5) / 100);

    }

    //获取RTU View2视图信息  地址段41411-41418
    private void parseView2Info()
    {
        rtuModel.setViewMinDl( RTUUtils.extractRegisterData( data, 0)/ 100.00 + "A");
        rtuModel.setViewMaxDl(RTUUtils.extractRegisterData( data, 1) / 100.00 + "A");
        rtuModel.setViewMinYggl(RTUUtils.extractRegisterSignedData( data, 2) / 100.00 + "KW");
        rtuModel.setViewMaxYggl(RTUUtils.extractRegisterSignedData( data, 3) / 100.00 + "KW");
        rtuModel.setViewOnZddl(RTUUtils.extractRegisterSignedData( data, 4) / 100.00 + "A");
        rtuModel.setViewUnderZddl(RTUUtils.extractRegisterSignedData( data, 5) / 100.00 + "A");
        rtuModel.setViewOnZdyggl(RTUUtils.extractRegisterSignedData( data, 6) / 100.00 + "KW");
        rtuModel.setViewUnderZdyggl(RTUUtils.extractRegisterData( data, 7)/ 100.00 + "KW");
        rtuModel.setMaxInt(RTUUtils.getMaxMark(RTUUtils.extractRegisterSignedData( data, 3)));
        rtuModel.setMinInt(RTUUtils.getMinMark(RTUUtils.extractRegisterSignedData( data, 2)));
        //rtuModel.setDataNum(200);
    }

    //获取RTU视图信息  地址段41961-41986
    private void parseView3Info()
    {
        rtuModel.setPositivePower(RTUUtils.extractFloat( data, 0) + "kW.h");
        rtuModel.setNegativePower(RTUUtils.extractFloat( data, 4)  + "kW.h");
        rtuModel.setTodayRuntime( (int)data[24] + "小时" + (int)data[25] + "分钟");
        rtuModel.setYesterdayRuntime( (int)data[26] + "小时" + (int)data[27] + "分钟");

        Date d = new Date();
        d.setYear(CommonUtils.Byte2BCD(data[28]) + 100);
        d.setMonth(CommonUtils.Byte2BCD(data[29]) - 1);
        d.setDate(CommonUtils.Byte2BCD(data[30]));
        d.setHours(CommonUtils.Byte2BCD(data[31]));
        d.setMinutes(CommonUtils.Byte2BCD(data[32]));
        d.setSeconds(CommonUtils.Byte2BCD(data[33]));

        rtuModel.setLastStartTime(d);

        d = new Date();
        d.setYear(CommonUtils.Byte2BCD(data[34]) + 100);
        d.setMonth(CommonUtils.Byte2BCD(data[35]) - 1);
        d.setDate(CommonUtils.Byte2BCD(data[36]));
        d.setHours(CommonUtils.Byte2BCD(data[37]));
        d.setMinutes(CommonUtils.Byte2BCD(data[38]));
        d.setSeconds(CommonUtils.Byte2BCD(data[39]));

        rtuModel.setLastStopTime(d);
        rtuModel.setBlanceSuggestion(RTUUtils.extractRegisterSignedData( data, 20));  //unprocessd data
        rtuModel.setAverageYg(RTUUtils.extractRegisterSignedData( data, 21) / 100.00 + "KW");
        rtuModel.setAverageWg(RTUUtils.extractRegisterSignedData( data, 22) / 100.00 + "KW");
        rtuModel.setAverageGlys(RTUUtils.extractRegisterSignedData( data, 23) / 100.00 + "");
        rtuModel.setUpAverageYg(RTUUtils.extractRegisterSignedData( data, 24) / 100.00 + "KW");
        rtuModel.setDownAverageYg(RTUUtils.extractRegisterSignedData( data, 25) / 100.00 + "KW");
    }

}
