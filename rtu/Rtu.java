package com.wgkj.rtucontrol.rtu;

import android.util.Log;

import com.wgkj.rtucontrol.tcpclient.Communicator;
import com.wgkj.rtucontrol.tcpclient.DeviceFinder;
import com.wgkj.rtucontrol.utils.HexStringUtils;
import com.wgkj.rtucontrol.utils.RTUUtils;
import com.wgkj.rtucontrol.parser.ConfigParam;
import com.wgkj.rtucontrol.parser.ConfigParamsParser;
import com.wgkj.rtucontrol.model.BlanceAdjustParamsModel;
import com.wgkj.rtucontrol.model.BlanceParamsModel;
import com.wgkj.rtucontrol.model.CoefficientParamsModel;
import com.wgkj.rtucontrol.model.ConfigParamsModel;
import com.wgkj.rtucontrol.model.OtherParamsModel;
import com.wgkj.rtucontrol.model.RtuModel;
import com.wgkj.rtucontrol.model.TestParamsModel;
import com.wgkj.rtucontrol.model.WellBasicDataModel;
import com.wgkj.rtucontrol.cmd.CmdReponse;
import com.wgkj.rtucontrol.cmd.MultipleWriteCmd;
import com.wgkj.rtucontrol.cmd.ReadCmd;
import com.wgkj.rtucontrol.parser.RtuModelParser;
import com.wgkj.rtucontrol.cmd.SingleWriteCmd;
import com.wgkj.rtucontrol.cmd.WifiWriteCmd;
import com.wgkj.rtucontrol.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Thread.sleep;


/**
 * Created by wgkj003 on 2018/1/4.
 */

public class Rtu extends Device {
    private Communicator communicator;

    public RtuModel getRtuModel() {
        return rtuModel;
    }

    public void setRtuModel(RtuModel rtuModel) {
        this.rtuModel = rtuModel;
    }

    RtuModel rtuModel = null;
    TestParamsModel testParamsModel = null;
    BlanceParamsModel blanceParamsModel= null;
    BlanceAdjustParamsModel blanceAdjustParamsModel = null;
    CoefficientParamsModel coefficientParamsModel = null;
    OtherParamsModel otherParamsModel = null;
    WellBasicDataModel wellBasicDataModel = null;

    public enum RtuInfoType { RTU_INFO, RTU_VIEW_INFO, RTU_VIEW2_INFO, RTU_VIEW3_INFO, RTU_CONTROL_INFO, KW_PACKAGE_DATA, RTU_PARAMS};
    public enum NetworkConfigStatus { SET_IP_COMPLETED, SET_88_COMPELETED, SET_92_COMPELETED, SET_IP_ERR, SET_88_ERR, SET_92_ERR };
    public enum ConfigParamsGroup { RtuParams, TestParams, BlanceParams,
        BlanceAdjustParams,  CoefficientParams, OtherParams, WellBasicData};

    public static final int DigitalType = 0;
    public static final int FixFrequencyType = 1;

    public final static int SET_WIFI_CONFIG_ADDRESS = 8900;
    public final static int SET_WIFI_CONFIG_88_ADDRESS = 8920;
    public final static int SET_WIFI_CONFIG_92_ADDRESS = 8921;

    public final static int RTU_INFO_ADDRESS = 0;  //基本信息和状态,地址段40101-40138
    public final static int RTU_CONTROL_INFO_ADDRESS = 100;   //控制、瞬时电参及其他,地址段40101-40138

    public final static int RTU_VIEW_INFO_ADDRESS = 200;  //示功图及电参曲线
    public final static int RTU_INDICATOR_DATA_ADDRESS = 210;  //地址段40211-41010
    public final static int RTU_POWER_DATA_ADDRESS = 1010;     //地址段41011-41410
    public final static int RTU_CURRENT_DATA_ADDRESS = 2000;     //地址段41011-41410

    public final static int RTU_VIEW2_INFO_ADDRESS = 1410;
    public final static int RTU_VIEW3_INFO_ADDRESS = 1960;
    public final static int RTU_TEST_TIME_ADDRESS = 1950;

    public final static int RTU_NAME_KZ =6;
    public final static int RTU_TYPE_ADDRESS = 4;
    public final static int RTU_SET_START_STOP=100;
    public final static int RTU_START_STOP=101;

    //功能码
    public final static int RTU_FUNCTION_03_CODE = 3;
    public final static int RTU_FUNCTION_06_CODE = 6;
    public final static int RTU_FUNCTION_16_CODE = 16;

    //rtu参数
    public final static int RTU_PARAMS_ADDRESS = 3803;
    private final static int TEST_PARAMS_ADDRESS = 3900;
    private final static int BLANCE_PARAMS_ADDRESS = 3930;
    private final static int BLANCE_ADJUST_PARAMS_ADDRESS = 3940;
    private final static int COEFFICIENT_PARAMS_ADDRESS = 3950;
    private final static int OTHER_PARAMS_ADDRESS = 3960;
    private final static int WELL_BASIC_DATA_ADDRESS = 3970;
    private boolean newVersion;

    public static int SET_ERR = -1;

    private String id;
    private String ip;
    private int port;
    private boolean rtuInfoLoading = false;

    //站号
    private int stationNum;
    public Rtu(DeviceFinder.DeviceInfo deviceInfo, int port)
    {
        super(DeviceType.RTU);
        this.id = deviceInfo.id;
        this.ip = deviceInfo.ip;
        this.port = port;
        this.stationNum = deviceInfo.stationNum;
        rtuModel = new RtuModel();
        rtuModel.setDeviceId(id);
        rtuModel.setWifiIp(ip);
        rtuModel.setPort(port);
        rtuModel.setRtuLocation(stationNum);
        rtuModel.setWellName(deviceInfo.wellName);
        communicator = new Communicator(ip, port);
    }

//    public Rtu(String id, String ip, int port, int stationNum) {
//        super(DeviceType.RTU);
//        this.id = id;
//        this.ip = ip;
//        this.port = port;
//        this.stationNum = stationNum;
//        rtuModel = new RtuModel();
//        rtuModel.setDeviceId(id);
//        rtuModel.setWifiIp(ip);
//        rtuModel.setPort(port);
//        rtuModel.setRtuLocation(stationNum);
//        communicator = new Communicator(ip, port);
//    }

    public Rtu(RtuModel model)
    {
        super(DeviceType.RTU);
        this.id = model.getDeviceId();
        this.ip = model.getWifiIp();
        this.port = model.getPort();
        this.stationNum = model.getRtuLocation();
        rtuModel = model;
        communicator = new Communicator(ip, port);
    }

    public String getId(){
        return id;
    }

    public String getIp(){
        return ip;
    }

    public void setIp(String ip)
    {
        this.ip = ip;
    }
    public void stopCommunicate()
    {
        communicator.stopSending();
    }

    public void keepConnection( boolean alive)
    {
        communicator.setKeepAlive( alive);
    }

    public void setNewVersion(boolean isNew)
    {
        newVersion = isNew;
    }
    public boolean isNewVersion()
    {
        return newVersion;
    }

    //获取rtu 信息
    public void getRtuInfo(final RtuInfoListener rtuInfoListener) {

        ReadCmd readCmd = new ReadCmd(stationNum, RTU_INFO_ADDRESS, 100);  //获取RTU信息Cmd
        Log.d("getRtuInfo", "获取rtuInfo信息");

        communicator.sendCmd(readCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                RtuModelParser parser = new RtuModelParser(rtuModel);
                if (cmdReponse.getResult() == CmdReponse.CmdReponseResult.READ_SUCCESS)
                {
                    parser.parse( RtuInfoType.RTU_INFO, cmdReponse.getContent());
                    rtuInfoListener.onRtuInfoUpdated(rtuModel, RtuInfoType.RTU_INFO);
                }
                else {
                    rtuInfoListener.onErr(cmdReponse.getErrCode() + ""+"");
                }
            }
            @Override
            public void onErr(String errMsg) {
                rtuInfoListener.onErr(errMsg);
            }
        });
    }


    //获取rtu 所有信息
    public void getRtuAllInfo(final RtuInfoListener rtuInfoListener) {
        //communicator.setKeepAlive( true);
        getRtuInfo(new RtuInfoListener() {
            @Override
            public void onRtuInfoUpdated(RtuModel model, RtuInfoType type) {
                rtuInfoListener.onRtuInfoUpdated(rtuModel, type);
            }

            @Override
            public void onErr(String errMsg) {
                rtuInfoListener.onErr(errMsg);
            }
        });

        getRtuControlInfo(new RtuInfoListener() {
            @Override
            public void onRtuInfoUpdated(RtuModel model, RtuInfoType type) {
                rtuInfoListener.onRtuInfoUpdated( rtuModel, type);
            }

            @Override
            public void onErr(String errMsg) {
                rtuInfoListener.onErr(errMsg);
            }
        });

        getViewInfo(new RtuInfoListener() {
            @Override
            public void onRtuInfoUpdated(RtuModel model, RtuInfoType type) {
                rtuInfoListener.onRtuInfoUpdated( rtuModel, type);
            }

            @Override
            public void onErr(String errMsg) {
                rtuInfoListener.onErr(errMsg);
            }
        });
        getView3Info(new RtuInfoListener() {
            @Override
            public void onRtuInfoUpdated(RtuModel model, RtuInfoType type) {
                rtuInfoListener.onRtuInfoUpdated( rtuModel, type);
                //communicator.setKeepAlive( false);
            }

            @Override
            public void onErr(String errMsg) {
                //communicator.setKeepAlive( false);
            }
        });
    }

    public void setConfigParam(ConfigParam param, final OnCmdOkListener onCmdOkListener)
    {
        SingleWriteCmd cmd = new SingleWriteCmd( stationNum, param.getAddress(), param.getValue());
        communicator.sendCmd(cmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                if ( cmdReponse.getResult() == CmdReponse.CmdReponseResult.WRITE_SUCCESS) {
                    onCmdOkListener.onSuccess();
                }
                else
                {
                    onCmdOkListener.onErr(cmdReponse.getErrCode() + "");
                }
            }

            @Override
            public void onErr(String errMsg) {

            }
        });
    }

    public void getRtuParams(final RtuInfoListener rtuInfoListener) {
        Log.d("getRtuControlInfo", "获取rtu params信息");
        ReadCmd readCmd = new ReadCmd(stationNum, RTU_PARAMS_ADDRESS, 13);
        communicator.sendCmd(readCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                RtuModelParser parser = new RtuModelParser( rtuModel);
                if ( cmdReponse.getResult() == CmdReponse.CmdReponseResult.READ_SUCCESS)
                {
                    parser.parse(RtuInfoType.RTU_PARAMS, cmdReponse.getContent());
                    rtuInfoListener.onRtuInfoUpdated(rtuModel, RtuInfoType.RTU_PARAMS);
                }
                else {
                    rtuInfoListener.onErr(cmdReponse.getErrCode() + "");
                }
            }

            @Override
            public void onErr(String errMsg) {
                rtuInfoListener.onErr(errMsg);
            }
        });
    }

    public void getTestParams(final RtuConfigParamsListener rtuConfigParamsListener)
    {
        ReadCmd readCmd = new ReadCmd(stationNum, TEST_PARAMS_ADDRESS, 23);
        communicator.sendCmd(readCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                ConfigParamsParser parser = new ConfigParamsParser( ConfigParamsGroup.TestParams);
                if ( cmdReponse.getResult() == CmdReponse.CmdReponseResult.READ_SUCCESS)
                {
                    try{
                        ConfigParamsModel model = parser.parse(cmdReponse.getContent());
                        testParamsModel = (TestParamsModel) model;
                        rtuConfigParamsListener.onConfigParamsRead( model, ConfigParamsGroup.TestParams);
                    }
                    catch (Exception ex)
                    {
                        Log.d("getTestParams", ex.getMessage());
                    }
                }
                else {
                    rtuConfigParamsListener.onErr(cmdReponse.getErrCode() + "");
                }
            }

            @Override
            public void onErr(String errMsg) {

            }
        });
    }

    public void setTestParams(TestParamsModel model,final OnCmdOkListener onCmdOkListener)
    {
        MultipleWriteCmd cmd = new MultipleWriteCmd( stationNum, TEST_PARAMS_ADDRESS, 23, model.toBytes());
        communicator.sendCmd(cmd, new Communicator.CmdReponseListener() {
                @Override
                public void onCmdReponse(CmdReponse cmdReponse) {
                    onCmdOkListener.onSuccess();
                }

                @Override
                public void onErr(String errMsg) {
                    onCmdOkListener.onErr(errMsg);
                }
            }
        );
    }

    public void getBlanceParams(final RtuConfigParamsListener rtuConfigParamsListener)
    {
        ReadCmd readCmd = new ReadCmd(stationNum, BLANCE_PARAMS_ADDRESS, 7);
        communicator.sendCmd(readCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                ConfigParamsParser parser = new ConfigParamsParser( ConfigParamsGroup.BlanceParams);
                if ( cmdReponse.getResult() == CmdReponse.CmdReponseResult.READ_SUCCESS)
                {
                    try {
                        ConfigParamsModel model = parser.parse(cmdReponse.getContent());
                        blanceParamsModel = (BlanceParamsModel)model;
                        rtuModel.setWellBlanceWay( blanceParamsModel.getWellBlanceWay());
                        rtuConfigParamsListener.onConfigParamsRead(model, ConfigParamsGroup.BlanceParams);
                    }
                    catch (Exception ex)
                    {
                        Log.d("getBlanceParams", ex.getMessage());
                    }
                }
                else {
                    rtuConfigParamsListener.onErr(cmdReponse.getErrCode() + "");
                }
            }

            @Override
            public void onErr(String errMsg) {

            }
        });
    }


    public void setBlanceParams(BlanceParamsModel model,final OnCmdOkListener onCmdOkListener)
    {
        MultipleWriteCmd cmd = new MultipleWriteCmd( stationNum, BLANCE_PARAMS_ADDRESS, 7, model.toBytes() );
        communicator.sendCmd(cmd, new Communicator.CmdReponseListener() {
                @Override
                public void onCmdReponse(CmdReponse cmdReponse) {
                    onCmdOkListener.onSuccess();
                }

                @Override
                public void onErr(String errMsg) {
                    onCmdOkListener.onErr(errMsg);
                }
            }
        );
    }

    public void getBlanceAdjustParams(final RtuConfigParamsListener rtuConfigParamsListener)
    {
        ReadCmd readCmd = new ReadCmd(stationNum, BLANCE_ADJUST_PARAMS_ADDRESS, 8);
        communicator.sendCmd(readCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                ConfigParamsParser parser = new ConfigParamsParser( ConfigParamsGroup.BlanceAdjustParams);
                if ( cmdReponse.getResult() == CmdReponse.CmdReponseResult.READ_SUCCESS)
                {
                    try {
                        ConfigParamsModel model = parser.parse(cmdReponse.getContent());
                        blanceAdjustParamsModel = (BlanceAdjustParamsModel)model;
                        rtuConfigParamsListener.onConfigParamsRead( model, ConfigParamsGroup.BlanceAdjustParams);
                    }catch (Exception ex)
                    {
                        Log.d("getBlanceAdjustParams", ex.getMessage());
                    }

                }
                else {
                    rtuConfigParamsListener.onErr(cmdReponse.getErrCode() + "");
                }
            }

            @Override
            public void onErr(String errMsg) {

            }
        });
    }

    public void setBlanceAdjustParams(BlanceAdjustParamsModel model, final OnCmdOkListener onCmdOkListener)
    {
        MultipleWriteCmd cmd = new MultipleWriteCmd( stationNum, BLANCE_ADJUST_PARAMS_ADDRESS, 8, model.toBytes() );
        communicator.sendCmd(cmd, new Communicator.CmdReponseListener() {
                @Override
                public void onCmdReponse(CmdReponse cmdReponse) {
                    onCmdOkListener.onSuccess();
                }

                @Override
                public void onErr(String errMsg) {
                    onCmdOkListener.onErr(errMsg);
                }
            }
        );
    }

    public void getCoefficientParams(final RtuConfigParamsListener rtuConfigParamsListener)
    {
        ReadCmd readCmd = new ReadCmd(stationNum, COEFFICIENT_PARAMS_ADDRESS, 3);
        communicator.sendCmd(readCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                ConfigParamsParser parser = new ConfigParamsParser( ConfigParamsGroup.CoefficientParams);
                if ( cmdReponse.getResult() == CmdReponse.CmdReponseResult.READ_SUCCESS)
                {
                    try{
                        ConfigParamsModel model = parser.parse(cmdReponse.getContent());
                        coefficientParamsModel = (CoefficientParamsModel)model;
                        rtuConfigParamsListener.onConfigParamsRead( model, ConfigParamsGroup.BlanceAdjustParams);
                    }catch (Exception ex)
                    {
                        Log.d("getCoefficientParams", ex.getMessage());
                    }

                }
                else {
                    rtuConfigParamsListener.onErr(cmdReponse.getErrCode() + "");
                }
            }

            @Override
            public void onErr(String errMsg) {

            }
        });
    }


    public void setCoefficientParams(CoefficientParamsModel model,final OnCmdOkListener onCmdOkListener)
    {
        MultipleWriteCmd cmd = new MultipleWriteCmd( stationNum, COEFFICIENT_PARAMS_ADDRESS, 3, model.toBytes());
        communicator.sendCmd(cmd, new Communicator.CmdReponseListener() {
                    @Override
                    public void onCmdReponse(CmdReponse cmdReponse) {
                        onCmdOkListener.onSuccess();
                    }

                    @Override
                    public void onErr(String errMsg) {
                        onCmdOkListener.onErr(errMsg);
                    }
                }
        );
    }

    public void getOtherParams(final RtuConfigParamsListener rtuConfigParamsListener)
    {
        ReadCmd readCmd = new ReadCmd(stationNum, OTHER_PARAMS_ADDRESS, 5);
        communicator.sendCmd(readCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                ConfigParamsParser parser = new ConfigParamsParser( ConfigParamsGroup.OtherParams);
                if ( cmdReponse.getResult() == CmdReponse.CmdReponseResult.READ_SUCCESS)
                {
                    try {
                        ConfigParamsModel model = parser.parse(cmdReponse.getContent());
                        OtherParamsModel  otherParamsModel = (OtherParamsModel)model;
                        rtuConfigParamsListener.onConfigParamsRead( model, ConfigParamsGroup.BlanceAdjustParams);
                    }
                    catch (Exception ex)
                    {
                        Log.d("getOtherParams", ex.getMessage());
                    }
                }
                else {
                    rtuConfigParamsListener.onErr(cmdReponse.getErrCode() + "");
                }
            }

            @Override
            public void onErr(String errMsg) {

            }
        });
    }


    public void setOtherParams(OtherParamsModel model,final OnCmdOkListener onCmdOkListener)
    {
        MultipleWriteCmd cmd = new MultipleWriteCmd( stationNum, OTHER_PARAMS_ADDRESS, 5, model.toBytes() );
        communicator.sendCmd(cmd, new Communicator.CmdReponseListener() {
                @Override
                public void onCmdReponse(CmdReponse cmdReponse) {
                    onCmdOkListener.onSuccess();
                }

                @Override
                public void onErr(String errMsg) {
                    onCmdOkListener.onErr(errMsg);
                }
            }
        );
    }

    public void getWellBasicData(final RtuConfigParamsListener rtuConfigParamsListener)
    {
        ReadCmd readCmd = new ReadCmd(stationNum, WELL_BASIC_DATA_ADDRESS, 11);
        communicator.sendCmd(readCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                ConfigParamsParser parser = new ConfigParamsParser( ConfigParamsGroup.WellBasicData);
                if ( cmdReponse.getResult() == CmdReponse.CmdReponseResult.READ_SUCCESS)
                {
                    try {
                        ConfigParamsModel model = parser.parse(cmdReponse.getContent());
                        wellBasicDataModel = (WellBasicDataModel)model;
                        rtuConfigParamsListener.onConfigParamsRead(model, ConfigParamsGroup.WellBasicData);
                    }
                    catch (Exception ex)
                    {
                        Log.d("getWellBasicData", ex.getMessage());
                    }
                }
                else {
                    rtuConfigParamsListener.onErr(cmdReponse.getErrCode() + "");
                }
            }

            @Override
            public void onErr(String errMsg) {

            }
        });
    }


    public void setWellBasicData(WellBasicDataModel model,final OnCmdOkListener onCmdOkListener)
    {
        MultipleWriteCmd cmd = new MultipleWriteCmd( stationNum, WELL_BASIC_DATA_ADDRESS, 11, model.toBytes() );
        communicator.sendCmd(cmd, new Communicator.CmdReponseListener() {
                    @Override
                    public void onCmdReponse(CmdReponse cmdReponse) {
                        onCmdOkListener.onSuccess();
                    }

                    @Override
                    public void onErr(String errMsg) {
                        onCmdOkListener.onErr(errMsg);
                    }
                }
        );
    }

    public void setRtuDateTime(int year, int month, int day, int hour, int minute, int second, final OnCmdOkListener onCmdOkListener)
    {
        year -= 2000;
        byte[] data = new byte[]{ (byte)((year/10)*16 + year%10),
                (byte)((month/10)*16 + month%10),
                (byte)((day/10)*16 + day%10),
                (byte)((hour/10)*16 + hour%10),
                (byte)((minute/10)*16 + minute%10),
                (byte)((second/10)*16 + second%10)};
        MultipleWriteCmd writeCmd = new MultipleWriteCmd( stationNum, RTU_PARAMS_ADDRESS, 3, new String( data), false);
        communicator.sendCmd(writeCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                RtuModelParser parser = new RtuModelParser( rtuModel);
                if ( cmdReponse.getResult() == CmdReponse.CmdReponseResult.WRITE_SUCCESS)
                {
                    onCmdOkListener.onSuccess();
                }
                else {
                    onCmdOkListener.onErr("写入失败");
                }
            }

            @Override
            public void onErr(String errMsg) {

            }
        });
    }

    /**
     * 获取控制自动 手动状态
     *
     * @param rtuInfoListener
     */
    public void getRtuControlInfo(final RtuInfoListener rtuInfoListener) {
        Log.d("getRtuControlInfo", "获取rtu Control信息");
        ReadCmd readCmd = new ReadCmd(stationNum, RTU_CONTROL_INFO_ADDRESS, 38);
        communicator.sendCmd(readCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                RtuModelParser parser = new RtuModelParser( rtuModel);
                if ( cmdReponse.getResult() == CmdReponse.CmdReponseResult.READ_SUCCESS)
                {
                    parser.parse(RtuInfoType.RTU_CONTROL_INFO, cmdReponse.getContent());
                    rtuInfoListener.onRtuInfoUpdated(rtuModel, RtuInfoType.RTU_CONTROL_INFO);
                }
                else {
                    rtuInfoListener.onErr(cmdReponse.getErrCode() + "");
                }
            }

            @Override
            public void onErr(String errMsg) {
                rtuInfoListener.onErr(errMsg);
            }
        });
    }




    //设置油井名
    public void setRtuName(final String rtuName, final OnCmdOkListener onCmdOkListener) {
        MultipleWriteCmd multipleWriteCmd;
        if (rtuName.length() < 8) {
            multipleWriteCmd = new MultipleWriteCmd(stationNum, RTU_INFO_ADDRESS, 4, rtuName, false);

            communicator.sendCmd(multipleWriteCmd, new Communicator.CmdReponseListener() {
                @Override
                public void onCmdReponse(CmdReponse cmdReponse) {
                    if (cmdReponse.getResult() == CmdReponse.CmdReponseResult.WRITE_SUCCESS){
                        onCmdOkListener.onSuccess();
                        //Toast.makeText(context, "设置成功", Toast.LENGTH_SHORT).show();

                    }else {
                        onCmdOkListener.onErr( "设置油井名失败" );
                        //Toast.makeText(context, "设置失败!请重新设置", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onErr(String errMsg) {
                    onCmdOkListener.onErr(errMsg);
                }
            });

        } else {
            //multipleWriteCmd = new MultipleWriteCmd(stationNum, RTU_INFO_ADDRESS, 4, "FFFFFFFFFFFFFFFF", true);
            byte[] data = new byte[]{ (byte)0xff, (byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff };
            multipleWriteCmd = new MultipleWriteCmd(stationNum, RTU_INFO_ADDRESS, 4, data);
            communicator.sendCmd(multipleWriteCmd, new Communicator.CmdReponseListener() {
                @Override
                public void onCmdReponse(CmdReponse cmdReponse) {
                    if( cmdReponse.getResult() == CmdReponse.CmdReponseResult.WRITE_SUCCESS){
                        //Toast.makeText(context, "设置成功", Toast.LENGTH_SHORT).show();
                       // onCmdOkListener.onSuccess();
                    }else {
                        //Toast.makeText(context, "设置失败!请重新设置", Toast.LENGTH_SHORT).show();
                        onCmdOkListener.onErr( "设置油井名失败");
                    }
                }

                @Override
                public void onErr(String errMsg) {
                    onCmdOkListener.onErr(errMsg);
                }
            });

            multipleWriteCmd = new MultipleWriteCmd(stationNum, RTU_INFO_ADDRESS + 6, 9, rtuName, false);
            communicator.sendCmd(multipleWriteCmd, new Communicator.CmdReponseListener() {
                @Override
                public void onCmdReponse(CmdReponse cmdReponse) {
                    if( cmdReponse.getResult() == CmdReponse.CmdReponseResult.WRITE_SUCCESS){
                        //Toast.makeText(context, "设置成功", Toast.LENGTH_SHORT).show();
                        onCmdOkListener.onSuccess();
                    }else {
                        //Toast.makeText(context, "设置失败!请重新设置", Toast.LENGTH_SHORT).show();
                        onCmdOkListener.onErr( "设置油井名失败");
                    }
                }

                @Override
                public void onErr(String errMsg) {
                    onCmdOkListener.onErr(errMsg);
                }
            });
        }
    }

    public void setWellType( int type, final OnCmdOkListener onCmdOkListener)
    {
        byte[] data;
        if ( type == DigitalType)
            data = new byte[]{ (byte)0xff, (byte)0xff, (byte)0x00, (byte)0x00 };
        else
            data = new byte[]{ (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 };
        MultipleWriteCmd cmd = new MultipleWriteCmd( stationNum, RTU_INFO_ADDRESS + 4, 2, data);
        communicator.sendCmd(cmd, new Communicator.CmdReponseListener() {
                    @Override
                    public void onCmdReponse(CmdReponse cmdReponse) {
                        onCmdOkListener.onSuccess();
                    }

                    @Override
                    public void onErr(String errMsg) {
                        onCmdOkListener.onErr(errMsg);
                    }
                }
        );

    }

    //设置启动状态
    public void start(final OnCmdOkListener onCmdOkListener) {
        if ( rtuModel.getRunningStatu() == true)
        {
            onCmdOkListener.onErr("设备正在运行中,无法启动");
            return;
        }
        SingleWriteCmd singleWriteCmd = new SingleWriteCmd(stationNum, RTU_SET_START_STOP, 81);
        communicator.sendCmd(singleWriteCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                onCmdOkListener.onSuccess();
            }

            @Override
            public void onErr(String errMsg) {
                onCmdOkListener.onErr(errMsg);
            }
        });
    }

    //设置停止状态
    public void stop(final OnCmdOkListener onCmdOkListener ) {
        if ( rtuModel.getRunningStatu() == false)
        {
            onCmdOkListener.onErr("设备已停止运行,无法再次停止");
            return;
        }
        SingleWriteCmd singleWriteCmd = new SingleWriteCmd(stationNum, RTU_SET_START_STOP, 82);
        communicator.sendCmd(singleWriteCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                onCmdOkListener.onSuccess();
            }

            @Override
            public void onErr(String errMsg) {
                onCmdOkListener.onErr(errMsg);
            }
        });
    }

    //设置控制方式
    public void setControlWay(boolean changeFrequency, final OnCmdOkListener onCmdOkListener)
    {
        int data = changeFrequency? 2:1;
        SingleWriteCmd singleWriteCmd = new SingleWriteCmd(stationNum, RTU_CONTROL_INFO_ADDRESS + 37, data);
        communicator.sendCmd(singleWriteCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                onCmdOkListener.onSuccess();
            }

            @Override
            public void onErr(String errMsg) {
                onCmdOkListener.onErr(errMsg);
            }
        });
    }

    //设置频率控制方式
    public void setFrequencyControlWay(boolean auto, final OnCmdOkListener onCmdOkListener)
    {
        int data = auto? 2:1;
        SingleWriteCmd singleWriteCmd = new SingleWriteCmd(stationNum, RTU_CONTROL_INFO_ADDRESS + 30, data);
        communicator.sendCmd(singleWriteCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                onCmdOkListener.onSuccess();
            }

            @Override
            public void onErr(String errMsg) {
                onCmdOkListener.onErr(errMsg);
            }
        });
    }

    //设置平衡控制方式
    public void setBlanceControlWay(boolean auto, final OnCmdOkListener onCmdOkListener)
    {
        int data = auto? 2:1;
        SingleWriteCmd singleWriteCmd = new SingleWriteCmd(stationNum, RTU_CONTROL_INFO_ADDRESS + 33, data);
        communicator.sendCmd(singleWriteCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                onCmdOkListener.onSuccess();
            }

            @Override
            public void onErr(String errMsg) {
                onCmdOkListener.onErr(errMsg);
            }
        });
    }

    public void adjustFrequency(int frequency, final OnCmdOkListener onCmdOkListener)
    {
        SingleWriteCmd singleWriteCmd = new SingleWriteCmd(stationNum, RTU_CONTROL_INFO_ADDRESS + 32, frequency);
        communicator.sendCmd(singleWriteCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                onCmdOkListener.onSuccess();
            }

            @Override
            public void onErr(String errMsg) {
                onCmdOkListener.onErr(errMsg);
            }
        });
    }

    //设置平衡度
    public void adjustBlance(boolean increase, final OnCmdOkListener onCmdOkListener)
    {
        int data = increase? 2:1;
        SingleWriteCmd singleWriteCmd = new SingleWriteCmd(stationNum, RTU_CONTROL_INFO_ADDRESS + 35, data);
        communicator.sendCmd(singleWriteCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                onCmdOkListener.onSuccess();
            }

            @Override
            public void onErr(String errMsg) {
                onCmdOkListener.onErr(errMsg);
            }
        });
    }

//    //设置油井类型
//    public void setType(RTU_TYPE rtuType,final OnCmdOkListener onCmdOkListener) {
//        SingleWriteCmd singleWriteCmd = new SingleWriteCmd(stationNum, RTU_TYPE_ADDRESS,
//                rtuType == RTU_TYPE.DIGITAL? 0xFFFF:0x0);
//        communicator.sendCmd(singleWriteCmd, new Communicator.CmdReponseListener() {
//            @Override
//            public void onCmdReponse(CmdReponse cmdReponse) {
//                onCmdOkListener.onSuccess();
//                //Toast.makeText(context, "设置成功", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onErr(String errMsg) {
//                onCmdOkListener.onErr(errMsg);
//               // Toast.makeText(context, "设置失败!请重新设置", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }



    private byte[] indicatorDataBuf;
    private int indicatorDataPointCount = 400;
    //读取示功图数据
    public void getIndicatorData(final RtuInfoListener rtuInfoListener) {
        indicatorDataPointCount = rtuModel.getViewDataPointCount();
        indicatorDataBuf = new byte[0];

        for (int i = 0; indicatorDataPointCount > 0; indicatorDataPointCount -= 60, i++) {
            int registerNumberToRead = 120;
            if (indicatorDataPointCount < 60) {
                registerNumberToRead = indicatorDataPointCount * 2;
            }

            ReadCmd readCmd = new ReadCmd(stationNum, RTU_INDICATOR_DATA_ADDRESS + i * 120, registerNumberToRead);
            communicator.sendCmd(readCmd, new Communicator.CmdReponseListener() {
                @Override
                public void onCmdReponse(CmdReponse cmdReponse) {
                    indicatorDataBuf = HexStringUtils.addBytes(indicatorDataBuf, cmdReponse.getContent());
//                    if (indicatorDataPointCount < 60) {
                        List<Float>[] XYdata = RTUUtils.getXYData(indicatorDataBuf);
                        rtuModel.setXData(XYdata[0]);
                        rtuModel.setYData(XYdata[1]);
                        rtuInfoListener.onRtuInfoUpdated(rtuModel, RtuInfoType.RTU_CONTROL_INFO);
//                    }
                }


                @Override
                public void onErr(String errMsg) {
                    rtuInfoListener.onErr(errMsg);
                }
            });
        }
    }


    //rtu示功图详情
    public void getViewInfo(final RtuInfoListener rtuInfoListener) {
        Log.d("getViewInfo", "获取rtu View信息");
        ReadCmd readCmd = new ReadCmd(stationNum, RTU_VIEW_INFO_ADDRESS,7);
        communicator.sendCmd(readCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                RtuModelParser parser = new RtuModelParser( rtuModel);
                if ( cmdReponse.getResult() == CmdReponse.CmdReponseResult.READ_SUCCESS)
                {
                    parser.parse(RtuInfoType.RTU_VIEW_INFO, cmdReponse.getContent());
                    rtuInfoListener.onRtuInfoUpdated(rtuModel, RtuInfoType.RTU_VIEW_INFO);
                }
                else {
                    rtuInfoListener.onErr(cmdReponse.getErrCode() + "");
                }
            }

            @Override
            public void onErr(String errMsg) {

            }
        });
    }

    //rtu示功图详情
    public void getView3Info(final RtuInfoListener rtuInfoListener) {
        Log.d("getView3Info", "获取rtu View3信息");
        ReadCmd readCmd = new ReadCmd(stationNum, RTU_VIEW3_INFO_ADDRESS,26);
        communicator.sendCmd(readCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                RtuModelParser parser = new RtuModelParser( rtuModel);
                if ( cmdReponse.getResult() == CmdReponse.CmdReponseResult.READ_SUCCESS)
                {
                    parser.parse(RtuInfoType.RTU_VIEW3_INFO, cmdReponse.getContent());
                    rtuInfoListener.onRtuInfoUpdated(rtuModel, RtuInfoType.RTU_VIEW3_INFO);
                }
                else {
                    rtuInfoListener.onErr(cmdReponse.getErrCode() + "");
                }
            }

            @Override
            public void onErr(String errMsg) {

            }
        });
    }

    Timer delayTimer = new Timer();
    //配置wifi网络
    public void configWifi(String wifiStr,final NetworkConfigStatusListener networkConfigStatusListener)
    {
        communicator.setKeepAlive( true);
        setWifi(wifiStr, new OnCmdOkListener() {
            @Override
            public void onSuccess()
            {
                networkConfigStatusListener.onStatusChanged( NetworkConfigStatus.SET_IP_COMPLETED );
                delayTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        set88Cmd(new OnCmdOkListener() {
                            @Override
                            public void onSuccess() {
                                networkConfigStatusListener.onStatusChanged( NetworkConfigStatus.SET_88_COMPELETED );

                                delayTimer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        set92Cmd(new OnCmdOkListener() {
                                            @Override
                                            public void onSuccess() {
                                                networkConfigStatusListener.onStatusChanged( NetworkConfigStatus.SET_92_COMPELETED );
                                                communicator.setKeepAlive( false);
                                            }

                                            @Override
                                            public void onErr(String errMsg) {
                                                networkConfigStatusListener.onStatusErr( NetworkConfigStatus.SET_92_ERR );
                                                communicator.setKeepAlive( false);
                                            }
                                        });
                                    }
                                },1000);
                            }

                            @Override
                            public void onErr(String errMsg) {
                                networkConfigStatusListener.onStatusErr( NetworkConfigStatus.SET_88_ERR );
                                communicator.setKeepAlive( false);
                            }
                        });
                    }
                }, 1000);

            }

            @Override
            public void onErr(String errMsg) {
                networkConfigStatusListener.onStatusErr( NetworkConfigStatus.SET_IP_ERR );
                communicator.setKeepAlive( false);
            }
        });
    }





    //设置WIFI名 密码
    private void setWifi(String wifiStr,final OnCmdOkListener onCmdOkListener) {
        WifiWriteCmd wifiWriteCmd = new WifiWriteCmd(222,SET_WIFI_CONFIG_ADDRESS,20,wifiStr);
        communicator.sendCmd(wifiWriteCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                onCmdOkListener.onSuccess();
            }

            @Override
            public void onErr(String errMsg) {
                onCmdOkListener.onErr(errMsg);
            }
        });
    }


    //设置88 指令
    public void set88Cmd(final OnCmdOkListener onCmdOkListener) {
        SingleWriteCmd singleWriteCmd = new SingleWriteCmd(222, SET_WIFI_CONFIG_88_ADDRESS, 88);
        communicator.sendCmd(singleWriteCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                onCmdOkListener.onSuccess();
            }

            @Override
            public void onErr(String errMsg) {
                onCmdOkListener.onErr(errMsg);
            }
        });

    }


    //设置92指令
    public void set92Cmd(final OnCmdOkListener onCmdOkListener) {
        SingleWriteCmd singleWriteCmd = new SingleWriteCmd(222,SET_WIFI_CONFIG_92_ADDRESS, 92);
        communicator.sendCmd(singleWriteCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                onCmdOkListener.onSuccess();
            }

            @Override
            public void onErr(String errMsg) {
                onCmdOkListener.onErr(errMsg);
            }
        });
    }




    //rtu view2详情
    public void getView2Info(final RtuInfoListener rtuInfoListener) {
        ReadCmd readCmd = new ReadCmd(stationNum,RTU_VIEW2_INFO_ADDRESS,8);

        communicator.sendCmd(readCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {

                                if(cmdReponse.getContent()!=null){
                    List<Integer> s = RTUUtils.extractRtuViewData(cmdReponse.getContent());

                    rtuModel.setViewMinDl(s.get(0) / 100.00 + "A");
                    rtuModel.setViewMaxDl(s.get(1) / 100.00 + "A");
                    rtuModel.setViewMinYggl(s.get(2) / 100.00 + "KW");
                    rtuModel.setViewMaxYggl(s.get(3) / 100.00 + "KW");
                    rtuModel.setViewOnZddl(s.get(4) / 100.00 + "A");
                    rtuModel.setViewUnderZddl(s.get(5) / 100.00 + "A");
                    rtuModel.setViewOnZdyggl(s.get(6) / 100.00 + "KW");
                    rtuModel.setViewUnderZdyggl(s.get(7) / 100 + "KW");
                    rtuModel.setMaxInt(RTUUtils.getMaxMark(s.get(3)));
                    rtuModel.setMinInt(RTUUtils.getMinMark(s.get(2)));
                    //rtuModel.setDataNum(200);
                    rtuInfoListener.onRtuInfoUpdated(rtuModel,null);
                }

                RtuModelParser parser = new RtuModelParser( rtuModel);
                if ( cmdReponse.getResult() == CmdReponse.CmdReponseResult.READ_SUCCESS)
                {
                    parser.parse(RtuInfoType.RTU_VIEW2_INFO, cmdReponse.getContent());
                    rtuInfoListener.onRtuInfoUpdated(rtuModel, RtuInfoType.RTU_VIEW2_INFO);
                }
                else {
                    rtuInfoListener.onErr(cmdReponse.getErrCode() + "");
                }


            }

            @Override
            public void onErr(String errMsg) {

            }
        });
    }
    public void getTestTime( final RtuInfoListener rtuInfoListener)
    {
        ReadCmd readCmd = new ReadCmd(stationNum, RTU_TEST_TIME_ADDRESS, 3);
        communicator.sendCmd(readCmd, new Communicator.CmdReponseListener() {
            @Override
            public void onCmdReponse(CmdReponse cmdReponse) {
                byte[] data = cmdReponse.getContent();
                Date d = new Date();
                d.setYear(CommonUtils.Byte2BCD(data[0]) + 100);
                d.setMonth(CommonUtils.Byte2BCD(data[1]) - 1);
                d.setDate(CommonUtils.Byte2BCD(data[2]));
                d.setHours(CommonUtils.Byte2BCD(data[3]));
                d.setMinutes(CommonUtils.Byte2BCD(data[4]));
                d.setSeconds(CommonUtils.Byte2BCD(data[5]));
                rtuModel.setTestTime(d);
                rtuInfoListener.onRtuInfoUpdated(rtuModel, RtuInfoType.RTU_PARAMS);
            }

            @Override
            public void onErr(String errMsg) {

            }
        });
    }

    private int powerDataPointCount = 400;
    private byte[] powerDataBuf;
    //获取功率图
    public void getPowerData(final RtuInfoListener rtuInfoListener) {
        powerDataPointCount = rtuModel.getViewDataPointCount();
        powerDataBuf = new byte[0];
        for (int i = 0; powerDataPointCount > 0; powerDataPointCount -= 120, i++) {
            int registerNumberToRead = 120;
            if (powerDataPointCount < 120) {
                registerNumberToRead = powerDataPointCount;
            }

            ReadCmd readCmd = new ReadCmd(
                    stationNum,
                    RTU_POWER_DATA_ADDRESS + i * 120,
                    registerNumberToRead
            );
            communicator.sendCmd(readCmd, new Communicator.CmdReponseListener() {
                @Override
                public void onCmdReponse(CmdReponse cmdReponse) {
                    powerDataBuf = HexStringUtils.addBytes(powerDataBuf, cmdReponse.getContent());
//                    if(powerDataPointCount < 120){
                        List<Integer> s = RTUUtils.extractRtuViewData(powerDataBuf);
                        List<Float> fs = new ArrayList<Float>();
                        for (int i : s) {
                            fs.add((float) (RTUUtils.symbolInt(i) / 100.00));
                        }
                        rtuModel.setPowerData(fs);
                        rtuInfoListener.onRtuInfoUpdated(rtuModel, RtuInfoType.KW_PACKAGE_DATA);
//                    }
                }


                @Override
                public void onErr(String errMsg) {
                    rtuInfoListener.onErr(errMsg);
                }
            });
        }
    }


    private int currentDataPointCount = 400;
    private byte[] currentDataBuf;
    //获取功率图
    public void getCurrentData(final RtuInfoListener rtuInfoListener) {
        currentDataPointCount = rtuModel.getViewDataPointCount();
        currentDataBuf = new byte[0];
        for (int i = 0; currentDataPointCount > 0; currentDataPointCount -= 120, i++) {
            int registerNumberToRead = 120;
            if (currentDataPointCount < 120) {
                registerNumberToRead = currentDataPointCount;
            }

            ReadCmd readCmd = new ReadCmd(
                    stationNum,
                    RTU_CURRENT_DATA_ADDRESS + i * 120,
                    registerNumberToRead
            );
            communicator.sendCmd(readCmd, new Communicator.CmdReponseListener() {
                @Override
                public void onCmdReponse(CmdReponse cmdReponse) {
                    currentDataBuf = HexStringUtils.addBytes(currentDataBuf, cmdReponse.getContent());
//                    if(powerDataPointCount < 120){
                    List<Integer> s = RTUUtils.extractRtuViewData(currentDataBuf);
                    List<Float> fs = new ArrayList<Float>();
                    for (int i : s) {
                        fs.add((float) ( i / 100.00));
                    }
                    rtuModel.setCurrentData(fs);
                    rtuInfoListener.onRtuInfoUpdated(rtuModel, RtuInfoType.KW_PACKAGE_DATA);
//                    }
                }


                @Override
                public void onErr(String errMsg) {
                    rtuInfoListener.onErr(errMsg);
                }
            });
        }
    }


    public interface RtuInfoListener{
        void onRtuInfoUpdated( RtuModel model, RtuInfoType type);
        void onErr(String errMsg);
    }

    public interface RtuConfigParamsListener{
        void onConfigParamsRead(ConfigParamsModel model, ConfigParamsGroup type);
        void onErr(String errMsg);
    }

    public interface OnCmdOkListener {
        void onSuccess();
        void onErr(String errMsg);
    }

    public interface NetworkConfigStatusListener {
        void onStatusChanged(NetworkConfigStatus status);
        void onStatusErr(NetworkConfigStatus status);
    }
}


