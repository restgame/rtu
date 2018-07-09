package com.wgkj.rtucontrol.parser;

import com.wgkj.rtucontrol.rtu.Rtu;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wgkj003 on 2018/3/30.
 */

public class ConfigParam {


    public enum Type {
        rtuType,

        //测试参数
        curveTestPeriod,
        testDuration,                 //1-5分钟
        timeRateStartTime,          //0-23小时
        rtuWirelessFrequencyPoint,  //0-63
        indicatorTestMethod,          // 0-7
        loadZero,
        loadRangeDigitalQuantity,
        voltageRange,              //250v
        currentRange,              //100A
        loadRange,
        wellParamUploadMode,
        wellParamUploadInterval,
        testTime,
        DTUTimeoutResetTime,
        D01ControlMode,
        D02ControlMode,
        D03ControlMode,
        pulseAdjustMode,
        D04ControlMode,
        frequencyTransformerType,
        testDataUploadMode,
        angularDisplacementDirection,
        frequencyTransformerAutoBoot,

        //平衡参数
        wellBlanceWay,
        movedCrankBlanceBlockNum,
        singleCrankBlanceBlockWeight,
        walkingBeamBlanceBlockHeightDiff,
        wellStrokeRange,
        manualStroke,

        //平衡调整参数
        blanceControlMode,
        accordingAjudstCount,
        blanceMotorTimeAndDistance,
        currentProtectValue,
        powerBlanceUpperLimit,
        blanceWeightRatio,
        blanceControlAccordingMode,
        currentBlanceRange,

        //频率/冲次系数等
        frequencyStrokeRatio,
        angularStrokeRangeRatio,
        displacementTime,

        //其他参数
        alarmWarningDuration,
        wellStatusJudgeCondition,
        indicatorStopProcessWay,
        indicatorTransferFragmentation,

        //油井基础数据
        club58length,
        club68length,
        club78length,
        club254length,
        club286ength,

        weightClubLength,
        weightClubDiameter,
        pumpDepth,
        surfaceOilDensity,
        undergroundOilDensity,
        waterRatio,
    }

    static HashMap<Type, Object> paramsGroup;
    static HashMap<Type, Double> paramsScale;
    static HashMap<Type, Integer> paramsAddressSet;
    HashMap<Integer, String> paramMap;

    private static boolean firstInit = true;
    private Type type;
    private int value;

    private ConfigParam()
    {

    }

    static private void initAddress()
    {
        HashMap<Type, Integer> addressSet = new HashMap<Type, Integer>();
        addressSet.put( Type.curveTestPeriod, 3900);
        addressSet.put( Type.testDuration, 3901);
        addressSet.put( Type.timeRateStartTime, 3902);
        addressSet.put( Type.rtuWirelessFrequencyPoint, 3903);
        addressSet.put( Type.indicatorTestMethod, 3904);
        addressSet.put( Type.loadZero, 3905);
        addressSet.put( Type.loadRangeDigitalQuantity, 3906);
        addressSet.put( Type.voltageRange, 3907);
        addressSet.put( Type.currentRange, 3908);
        addressSet.put( Type.loadRange, 3909);
        addressSet.put( Type.wellParamUploadMode, 3910);
        addressSet.put( Type.wellParamUploadInterval, 3911);
        addressSet.put( Type.testTime, 3912);
        addressSet.put( Type.DTUTimeoutResetTime, 3913);
        addressSet.put( Type.D01ControlMode, 3914);
        addressSet.put( Type.D02ControlMode, 3915);
        addressSet.put( Type.D03ControlMode, 3916);
        addressSet.put( Type.pulseAdjustMode, 3917);
        addressSet.put( Type.D04ControlMode, 3918);
        addressSet.put( Type.frequencyTransformerType, 3919);
        addressSet.put( Type.testDataUploadMode, 3920);
        addressSet.put( Type.angularDisplacementDirection, 3921);
        addressSet.put( Type.frequencyTransformerAutoBoot, 3922);

        addressSet.put( Type.wellBlanceWay, 3930);
        addressSet.put( Type.movedCrankBlanceBlockNum, 3931);
        addressSet.put( Type.singleCrankBlanceBlockWeight, 3932);
        addressSet.put( Type.walkingBeamBlanceBlockHeightDiff, 3933);
        addressSet.put( Type.wellStrokeRange, 3934);
        addressSet.put( Type.manualStroke, 3936);

        addressSet.put( Type.blanceControlMode, 3940);
        addressSet.put( Type.accordingAjudstCount, 3941);
        addressSet.put( Type.blanceMotorTimeAndDistance, 3942);
        addressSet.put( Type.currentProtectValue, 3943);
        addressSet.put( Type.powerBlanceUpperLimit, 3944);
        addressSet.put( Type.blanceWeightRatio, 3945);
        addressSet.put( Type.blanceControlAccordingMode, 3946);
        addressSet.put( Type.currentBlanceRange, 3947);

        addressSet.put( Type.frequencyStrokeRatio, 3950);
        addressSet.put( Type.angularStrokeRangeRatio, 3951);
        addressSet.put( Type.displacementTime, 3952);

        addressSet.put( Type.alarmWarningDuration, 3960);
        addressSet.put( Type.wellStatusJudgeCondition, 3961);
        addressSet.put( Type.indicatorStopProcessWay, 3963);
        addressSet.put( Type.indicatorTransferFragmentation, 3964);

        addressSet.put( Type.club58length, 3970);
        addressSet.put( Type.club68length, 3971);
        addressSet.put( Type.club78length, 3972);
        addressSet.put( Type.club254length, 3973);
        addressSet.put( Type.club286ength, 3974);
        addressSet.put( Type.weightClubLength, 3975);
        addressSet.put( Type.weightClubDiameter, 3976);
        addressSet.put( Type.pumpDepth, 3977);
        addressSet.put( Type.surfaceOilDensity, 3978);
        addressSet.put( Type.undergroundOilDensity, 3979);
        addressSet.put( Type.waterRatio, 3980);

        ConfigParam.paramsAddressSet = addressSet;
    }

    static private void initParamsMapper()
    {
        ConfigParam.paramsGroup = new HashMap<Type, Object>();
        HashMap<Integer, String> mapper;
        Converter converter;

        mapper = new HashMap<Integer, String>();
        mapper.put( Rtu.FixFrequencyType, "定频抽油机");
        mapper.put( Rtu.DigitalType, "数字化抽油机");
        ConfigParam.paramsGroup.put(Type.rtuType, mapper);

        mapper = new HashMap<Integer, String>();
        mapper.put( 0, "30分钟");
        mapper.put( 1, "1小时");
        mapper.put( 2, "2小时");
        mapper.put( 3, "3小时");
        mapper.put( 4, "4小时");
        mapper.put( 24, "15分钟");
        mapper.put( 25, "10分钟");
        ConfigParam.paramsGroup.put(Type.curveTestPeriod, mapper);


//        converter = new Converter() {
//            @Override
//            public int toValue(String str) throws Exception{
//                int endIndex = str.indexOf("分钟");
//                if ( endIndex == -1)
//                    throw new Exception("toValue非法参数:" + str);
//                str = str.substring(0, endIndex);
//                return Integer.valueOf(str);
//            }
//
//            @Override
//            public String toString(int value) throws Exception {
//                if ( value >= 1 && value <= 5)
//                    return String.valueOf(value) + "分钟";
//                throw new Exception("参数值必须在0-5之间");
//            }
//        };
//        ConfigParam.paramsGroup.put( Type.testDuration, converter);
        ConfigParam.paramsGroup.put( Type.testDuration, "分钟");
        ConfigParam.paramsGroup.put( Type.timeRateStartTime, "小时");

        mapper = new HashMap<Integer, String>();
        mapper.put( 0, "无示功图");
        mapper.put( 1, "无线一体化示功仪");
        mapper.put( 2, "手工输入冲次");
        mapper.put( 3, "脉冲方式");
        mapper.put( 4, "角位移&无线载荷");
        mapper.put( 5, "反送示功图,周期为脉冲方式");
        mapper.put( 6, "反送示功图,人工输入冲次");
        ConfigParam.paramsGroup.put(Type.indicatorTestMethod, mapper);

        ConfigParam.paramsGroup.put( Type.voltageRange, "V");
        ConfigParam.paramsGroup.put( Type.currentRange, "A");
        ConfigParam.paramsGroup.put( Type.loadRange, "kN");


        mapper = new HashMap<Integer, String>();
        mapper.put( 1, "指令发送");
        mapper.put( 2, "定时上传");
        ConfigParam.paramsGroup.put( Type.wellParamUploadMode, mapper);

        ConfigParam.paramsGroup.put( Type.wellParamUploadInterval, "分");

        mapper = new HashMap<Integer, String>();
        mapper.put( 1, "脉冲方式,直接控制");
        mapper.put( 2, "电平方式");
        mapper.put( 3, "脉冲方式,控制D03持续20s");
        ConfigParam.paramsGroup.put( Type.D01ControlMode, mapper);

        mapper = new HashMap<Integer, String>();
        mapper.put( 1, "脉冲方式");
        mapper.put( 2, "电平方式");
        ConfigParam.paramsGroup.put( Type.D02ControlMode, mapper);
        ConfigParam.paramsGroup.put( Type.D03ControlMode, mapper);
        ConfigParam.paramsGroup.put( Type.D04ControlMode, mapper);

        mapper = new HashMap<Integer, String>();
        mapper.put( 1, "手动模式");
        mapper.put( 2, "自动模式");
        ConfigParam.paramsGroup.put( Type.pulseAdjustMode, mapper);

        mapper = new HashMap<Integer, String>();
        mapper.put( 1, "蓝海华腾V5-H");
        mapper.put( 2, "台达C2000 ");
        mapper.put( 3, "SRD");
        mapper.put( 4, "施耐德ATV61");
        mapper.put( 5, "CHF100A非标");
        mapper.put( 6, "成都厚信健");
        mapper.put( 7, "GD300标准");
        mapper.put( 8, "GK600");
        mapper.put( 9, "SMARTDRV");
        mapper.put( 10, "汇川MD280");
        mapper.put( 11, "博世力士乐");
        mapper.put( 12, "台达C2000");
        mapper.put( 13, "TD620");
        mapper.put( 14, "合康");
        mapper.put( 15, "武汉科华动力D4-15Q");
        ConfigParam.paramsGroup.put( Type.frequencyTransformerType, mapper);

        mapper = new HashMap<Integer, String>();
        mapper.put( 1, "指令发送");
        mapper.put( 2, "主动上传");
        ConfigParam.paramsGroup.put( Type.testDataUploadMode, mapper);

        mapper = new HashMap<Integer, String>();
        mapper.put( 1, "正常");
        mapper.put( 2, "反向");
        ConfigParam.paramsGroup.put( Type.angularDisplacementDirection, mapper);

        mapper = new HashMap<Integer, String>();
        mapper.put( 1, "否");
        mapper.put( 2, "是");
        mapper.put( 3, "塔式机");
        mapper.put( 4, "电磁刹");
        ConfigParam.paramsGroup.put( Type.frequencyTransformerAutoBoot, mapper);

        //初始化平衡参数Map
        mapper = new HashMap<Integer, String>();
        mapper.put( 1, "曲柄式");
        mapper.put( 2, "游梁式");
        mapper.put( 3, "皮带式");
        mapper.put( 4, "等效曲柄方式");
        ConfigParam.paramsGroup.put( Type.wellBlanceWay, mapper);

        ConfigParam.paramsGroup.put( Type.singleCrankBlanceBlockWeight, "kN");
        ConfigParam.paramsGroup.put( Type.walkingBeamBlanceBlockHeightDiff, "m");
        ConfigParam.paramsGroup.put( Type.wellStrokeRange, "m");
        ConfigParam.paramsGroup.put( Type.manualStroke, "/min");

        mapper = new HashMap<Integer, String>();
        mapper.put( 1, "不控");
        mapper.put( 4, "自动控");
        ConfigParam.paramsGroup.put( Type.blanceControlMode, mapper);

        mapper = new HashMap<Integer, String>();
        mapper.put( 1, "功率平衡模式");
        mapper.put( 2, "功率平衡兼顾电流平衡模式");
        mapper.put( 3, "电流平衡模式");
        ConfigParam.paramsGroup.put( Type.blanceControlAccordingMode, mapper);

        ConfigParam.paramsGroup.put( Type.currentBlanceRange, "%");
        ConfigParam.paramsGroup.put( Type.displacementTime, "ms");
        ConfigParam.paramsGroup.put( Type.alarmWarningDuration, "s");

        mapper = new HashMap<Integer, String>();
        mapper.put( 1, "电参");
        mapper.put( 2, "状态");
        ConfigParam.paramsGroup.put( Type.wellStatusJudgeCondition, mapper);

        mapper = new HashMap<Integer, String>();
        mapper.put( 1, "点数不清零");
        mapper.put( 2, "点数清零");
        ConfigParam.paramsGroup.put( Type.indicatorStopProcessWay, mapper);

        mapper = new HashMap<Integer, String>();
        mapper.put( 1, "不分包");
        mapper.put( 2, "分包");
        ConfigParam.paramsGroup.put( Type.indicatorTransferFragmentation, mapper);


        ConfigParam.paramsGroup.put( Type.club58length, "m");
        ConfigParam.paramsGroup.put( Type.club68length, "m");
        ConfigParam.paramsGroup.put( Type.club78length, "m");
        ConfigParam.paramsGroup.put( Type.club254length, "m");
        ConfigParam.paramsGroup.put( Type.club286ength, "m");
        ConfigParam.paramsGroup.put( Type.weightClubLength, "m");
        ConfigParam.paramsGroup.put( Type.weightClubDiameter, "mm");
        ConfigParam.paramsGroup.put( Type.pumpDepth, "m");
        ConfigParam.paramsGroup.put( Type.surfaceOilDensity, "t/m^3");
        ConfigParam.paramsGroup.put( Type.undergroundOilDensity, "t/m^3");
        ConfigParam.paramsGroup.put( Type.waterRatio, "%");

    }

    static public void initScale()
    {
        ConfigParam.paramsScale = new HashMap<Type, Double>();
        ConfigParam.paramsScale.put( Type.singleCrankBlanceBlockWeight, 0.1 );
        ConfigParam.paramsScale.put( Type.walkingBeamBlanceBlockHeightDiff, 0.01 );
        ConfigParam.paramsScale.put( Type.wellStrokeRange, 0.01 );
        ConfigParam.paramsScale.put( Type.manualStroke, 0.01 );
        ConfigParam.paramsScale.put( Type.powerBlanceUpperLimit, 0.01 );
        ConfigParam.paramsScale.put( Type.frequencyStrokeRatio, 0.01 );
        ConfigParam.paramsScale.put( Type.angularStrokeRangeRatio, 0.1 );
        ConfigParam.paramsScale.put( Type.club58length, 0.1 );
        ConfigParam.paramsScale.put( Type.club68length, 0.1 );
        ConfigParam.paramsScale.put( Type.club78length, 0.1 );
        ConfigParam.paramsScale.put( Type.club254length, 0.1 );
        ConfigParam.paramsScale.put( Type.club286ength, 0.1 );
        ConfigParam.paramsScale.put( Type.club78length, 0.1);
        ConfigParam.paramsScale.put( Type.weightClubLength, 0.1 );
        ConfigParam.paramsScale.put( Type.pumpDepth, 0.1 );
        ConfigParam.paramsScale.put( Type.surfaceOilDensity, 0.01 );
        ConfigParam.paramsScale.put( Type.undergroundOilDensity, 0.01 );
        ConfigParam.paramsScale.put( Type.waterRatio, 0.01 );
    }

    static public ConfigParam typeOf(Type type)
    {
        if ( firstInit ) {
            initAddress();
            initParamsMapper();
            initScale();
            ConfigParam.firstInit = false;
        }
        ConfigParam configParam = new ConfigParam();
        configParam.type = type;
        return configParam;
    }

    public HashMap<Integer, String> getParamMap()
    {
        Object obj = paramsGroup.get(type);
        return paramMap = (HashMap<Integer, String>)obj;
    }

    public ConfigParam setScale( Double scale)
    {
        paramsScale.put(this.type, scale);
        return this;
    }

    public ConfigParam fromString(String str) throws Exception
    {
        Object obj = paramsGroup.get(type);

        if ( obj instanceof HashMap)
        {
            paramMap = (HashMap<Integer, String>)obj;
            if ( paramMap.containsValue(str))
            {
                for (Map.Entry<Integer, String> entry : paramMap.entrySet()) {
                    if ( entry.getValue().equals(str))
                    {
                        this.value = entry.getKey();
                        return this;
                    }
                }
            }
        }

        if ( obj instanceof String)
        {
            String unit = (String)obj;
            if ( str.contains(unit))
            {
                int endIndex = str.indexOf(unit);
                str = str.substring( 0, endIndex);
            }
        }
        try {
            Double scale = paramsScale.get(type);
            if ( scale != null)
                this.value = (int)(Double.parseDouble(str) / scale);
            else
                this.value = Integer.parseInt(str);
        }
        catch (Exception e)
        {
            throw e;
        }
        return this;
    }

    public ConfigParam withValue(int value)
    {
        this.value = value;
        return this;
    }

    public int getValue()
    {
        return this.value;
    }

    public String getText()
    {

        Object obj = paramsGroup.get(type);
        Double scale = paramsScale.get(type);
        String text;
        double num;

        if (scale != null)
            num = this.value * scale;
        else
            num = (double)this.value;

        DecimalFormat df = new DecimalFormat("###################.###########");


        if ( obj == null)
        {
            return String.valueOf( df.format(num));
        }
        else if ( obj instanceof String)   //是计量单位
        {
            return (String.valueOf(df.format(num))) + obj ;
        }

        paramMap = (HashMap<Integer, String>)obj;
        if ( paramMap.containsKey(this.value))
            return paramMap.get(this.value);
        else
            return String.valueOf(this.value);
    }

    private String stripDot0(String n)
    {
        if ( n.endsWith(".0"))
           return n.split("\\.")[0];
        return n;
    }

    public double getDisplayValue()
    {

        Double scale = paramsScale.get(type);
        if ( scale != null)
            return this.value * scale;
        return this.value;
    }

    public void setDisplayValue(double displayValue)
    {
        Double scale = paramsScale.get(type);
        if ( scale != null)
            this.value = (int)(displayValue / scale);
        else
            this.value = (int)displayValue;
    }


    public int getAddress()
    {
        return ConfigParam.paramsAddressSet.get(this.type);
    }

}

interface Converter
{
    public int toValue(String str) throws Exception;
    public String toString(int value) throws Exception;
}