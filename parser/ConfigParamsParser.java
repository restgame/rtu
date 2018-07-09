package com.wgkj.rtucontrol.parser;

import com.wgkj.rtucontrol.utils.RTUUtils;
import com.wgkj.rtucontrol.model.BlanceAdjustParamsModel;
import com.wgkj.rtucontrol.model.BlanceParamsModel;
import com.wgkj.rtucontrol.model.CoefficientParamsModel;
import com.wgkj.rtucontrol.model.ConfigParamsModel;
import com.wgkj.rtucontrol.model.OtherParamsModel;
import com.wgkj.rtucontrol.model.TestParamsModel;
import com.wgkj.rtucontrol.model.WellBasicDataModel;
import com.wgkj.rtucontrol.rtu.Rtu;

/**
 * Created by wgkj003 on 2018/3/30.
 */

public class ConfigParamsParser {

    ConfigParamsModel model;
    Rtu.ConfigParamsGroup type;

    public ConfigParamsParser(Rtu.ConfigParamsGroup type )
    {
        this.type = type;
    }

    public ConfigParamsModel parse( byte[] data) throws Exception
    {
        try {
            switch (type) {
                case RtuParams:

                    break;
                case TestParams:
                    model = new TestParamsModel();
                    parseTestParams(data);
                    break;
                case BlanceParams:
                    model = new BlanceParamsModel();
                    parseBlanceParams(data);
                    break;
                case BlanceAdjustParams:
                    model = new BlanceAdjustParamsModel();
                    parseBlanceAdjustParams(data);
                    break;
                case CoefficientParams:
                    model = new CoefficientParamsModel();
                    parseCoefficientParams(data);
                    break;
                case OtherParams:
                    model = new OtherParamsModel();
                    parseOtherParams(data);
                    break;
                case WellBasicData:
                    model = new WellBasicDataModel();
                    parseWellBasicData(data);
                    break;
            }
        }catch (Exception e)
        {
            throw e;
        }
        return model;
    }

    protected void parseTestParams(byte[] data) throws Exception
    {
        if ( model != null )
        {
            if ( data.length < 46)
            {
                throw new Exception("测试参数读取数据长度不足46字节");
            }

            TestParamsModel testParamsModel = (TestParamsModel)model;
            testParamsModel.setCurveTestPeriod(RTUUtils.getRegisterData(data[0], data[1]));
            testParamsModel.setTestDuration( RTUUtils.getRegisterData(data[2], data[3]));
            testParamsModel.setTimeRateStartTime( RTUUtils.getRegisterData(data[4], data[5]));
            testParamsModel.setRtuWirelessFrequencyPoint( RTUUtils.getRegisterData(data[6], data[7]));
            testParamsModel.setIndicatorTestMethod( RTUUtils.getRegisterData(data[8], data[9]));
            testParamsModel.setLoadZero( RTUUtils.getRegisterData(data[10], data[11]));
            testParamsModel.setLoadRangeDigitalQuantity( RTUUtils.getRegisterData(data[12], data[13]));
            testParamsModel.setVoltageRange( RTUUtils.getRegisterData(data[14], data[15]));
            testParamsModel.setCurrentRange( RTUUtils.getRegisterData(data[16], data[17]));

            testParamsModel.setLoadRange( RTUUtils.getRegisterData(data[18], data[19]));
            testParamsModel.setWellParamUploadMode( RTUUtils.getRegisterData(data[20], data[21]));
            testParamsModel.setWellParamUploadInterval( RTUUtils.getRegisterData(data[22], data[23]));
            testParamsModel.setTestTime( RTUUtils.getRegisterData(data[24], data[25]));
            testParamsModel.setDTUTimeoutResetTime( RTUUtils.getRegisterData(data[26], data[27]));
            testParamsModel.setD01ControlMode( RTUUtils.getRegisterData(data[28], data[29]));
            testParamsModel.setD02ControlMode( RTUUtils.getRegisterData(data[30], data[31]));
            testParamsModel.setD03ControlMode( RTUUtils.getRegisterData(data[32], data[33]));
            testParamsModel.setPulseAdjustMode( RTUUtils.getRegisterData(data[34], data[35]));
            testParamsModel.setD04ControlMode( RTUUtils.getRegisterData(data[36], data[37]));
            testParamsModel.setFrequencyTransformerType( RTUUtils.getRegisterData(data[38], data[39]));
            testParamsModel.setTestDataUploadMode( RTUUtils.getRegisterData(data[40], data[41]));
            testParamsModel.setAngularDisplacementDirection( RTUUtils.getRegisterData(data[42], data[43]));
            testParamsModel.setFrequencyTransformerAutoBoot( RTUUtils.getRegisterData(data[44], data[45]));
        }
    }

    public void parseBlanceParams(byte[] data) throws Exception
    {
        if ( model != null)
        {
            if ( data.length < 14)
            {
                throw new Exception("平衡参数读取数据长度不足14字节");
            }
            BlanceParamsModel blanceParamsModel = (BlanceParamsModel)model;
            blanceParamsModel.setWellBlanceWay( RTUUtils.getRegisterData(data[0], data[1]));
            blanceParamsModel.setMovedCrankBlanceBlockNum( RTUUtils.getRegisterData(data[2], data[3]));
            blanceParamsModel.setSingleCrankBlanceBlockWeight( RTUUtils.getRegisterData(data[4], data[5]));
            blanceParamsModel.setWalkingBeamBlanceBlockHeightDiff( RTUUtils.getRegisterData(data[6], data[7]));
            blanceParamsModel.setWellStrokeRange( RTUUtils.getRegisterData(data[8], data[9]));
            blanceParamsModel.setManualStroke( RTUUtils.getRegisterData(data[12], data[13]));

        }
    }

    public void parseBlanceAdjustParams(byte[] data) throws Exception
    {
        if ( model != null)
        {
            if ( data.length < 16)
            {
                throw new Exception("平衡调整参数读取数据长度不足16个字节");
            }
            BlanceAdjustParamsModel blanceAdjustParamsModel = (BlanceAdjustParamsModel)model;
            blanceAdjustParamsModel.setBlanceControlMode( RTUUtils.getRegisterData(data[0], data[1]));
            blanceAdjustParamsModel.setAccordingAjudstCount( RTUUtils.getRegisterData(data[2], data[3]));
            blanceAdjustParamsModel.setBlanceMotorTimeAndDistance( RTUUtils.getRegisterData(data[4], data[5]));
            blanceAdjustParamsModel.setCurrentProtectValue( RTUUtils.getRegisterData(data[6], data[7]));
            blanceAdjustParamsModel.setPowerBlanceUpperLimit( RTUUtils.getRegisterData(data[8], data[9]));
            blanceAdjustParamsModel.setBlanceWeightRatio( RTUUtils.getRegisterData(data[10], data[11]));
            blanceAdjustParamsModel.setBlanceControlAccordingMode( RTUUtils.getRegisterData(data[12], data[13]));
            blanceAdjustParamsModel.setCurrentBlanceRange( RTUUtils.getRegisterData(data[14], data[15]));
        }
    }

    public void parseCoefficientParams(byte[] data) throws Exception
    {
        if ( model != null)
        {
            if ( data.length < 6)
            {
                throw new Exception("系数参数读取数据长度不足6个字节");
            }
            CoefficientParamsModel coefficientParamsModel = (CoefficientParamsModel)model;
            coefficientParamsModel.setFrequencyStrokeRatio( RTUUtils.getRegisterData(data[0], data[1]));
            coefficientParamsModel.setAngularStrokeRangeRatio( RTUUtils.getRegisterData(data[2], data[3]));
            coefficientParamsModel.setDisplacementTime( RTUUtils.getRegisterData(data[4], data[5]));
        }
    }

    public void parseOtherParams(byte[] data) throws Exception
    {
        if ( model != null)
        {
            if ( data.length < 10)
            {
                throw new Exception("其他相关参数读取数据长度不足10个字节");
            }
            OtherParamsModel otherParamsModel = (OtherParamsModel)model;
            otherParamsModel.setAlarmWarningDuration ( RTUUtils.getRegisterData(data[0], data[1]));
            otherParamsModel.setWellStatusJudgeCondition( RTUUtils.getRegisterData(data[2], data[3]));
            otherParamsModel.setIndicatorStopProcessWay( RTUUtils.getRegisterData(data[6], data[7]));
            otherParamsModel.setIndicatorTransferFragmentation( RTUUtils.getRegisterData(data[8], data[9]));
        }
    }


    public void parseWellBasicData(byte[] data) throws Exception
    {
        if ( model != null)
        {
            if ( data.length < 22)
            {
                throw new Exception("油井基础数据读取数据长度不足22个字节");
            }
            WellBasicDataModel wellBasicDataModel = (WellBasicDataModel)model;
            wellBasicDataModel.setClub58length ( RTUUtils.getRegisterData(data[0], data[1]));
            wellBasicDataModel.setClub68length( RTUUtils.getRegisterData(data[2], data[3]));
            wellBasicDataModel.setClub78length( RTUUtils.getRegisterData(data[4], data[5]));
            wellBasicDataModel.setClub254length( RTUUtils.getRegisterData(data[6], data[7]));
            wellBasicDataModel.setClub286ength( RTUUtils.getRegisterData(data[8], data[9]));
            wellBasicDataModel.setWeightClubLength( RTUUtils.getRegisterData(data[10], data[11]));
            wellBasicDataModel.setWeightClubDiameter( RTUUtils.getRegisterData(data[12], data[13]));
            wellBasicDataModel.setPumpDepth( RTUUtils.getRegisterData(data[14], data[15]));
            wellBasicDataModel.setSurfaceOilDensity( RTUUtils.getRegisterData(data[16], data[17]));
            wellBasicDataModel.setUndergroundOilDensity( RTUUtils.getRegisterData(data[18], data[19]));
            wellBasicDataModel.setWaterRatio( RTUUtils.getRegisterData(data[20], data[21]));
        }
    }
}
