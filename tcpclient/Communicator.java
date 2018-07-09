package com.wgkj.rtucontrol.tcpclient;


import android.util.Log;
import android.util.Pair;

import com.wgkj.rtucontrol.cmd.FrameParser;
import com.wgkj.rtucontrol.tcpclient.imp.DataReceivedListener;
import com.wgkj.rtucontrol.cmd.Cmd;
import com.wgkj.rtucontrol.cmd.CmdReponse;
import com.wgkj.rtucontrol.utils.HexStringUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by wgkj003 on 2017/10/18.
 */

public class Communicator extends TCPClient{
    public static final int PARSER_ERROR = 100001;

    BlockingQueue<Pair<Cmd,CmdReponseListener>> blockingQueue = new LinkedBlockingQueue<Pair<Cmd,CmdReponseListener>>();
    boolean isRunning = true;
    public Communicator(String host, int port) {
        super(host, port);
        startSendingQueue();
    }

    protected void finalize()
    {
        isRunning = false;
    }
    /**
     *
     * 上游解析发送数据
     *
     */
    public void sendCmd(final Cmd cmd, final CmdReponseListener cmdReponseListener){
        try {
            if ( cmd != null)
                blockingQueue.put( new Pair<Cmd, CmdReponseListener>(cmd, cmdReponseListener));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopSending()
    {
        blockingQueue.clear();
    }

    private void startSendingQueue( )
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
            while (isRunning) {
                Log.i("startSendingQueue", "run: blockingQueue size: " + blockingQueue.size());
                Pair<Cmd, CmdReponseListener> pair = null;
                try {
                    pair = blockingQueue.take();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                final Cmd cmd = pair.first;
                final CmdReponseListener cmdReponseListener = pair.second;
                byte[] hexByte = cmd.getCmdBytes();

//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                final com.wgkj.rtucontrol.cmd.FrameParser frameParser = new FrameParser(cmd);
                Log.i("Communicator","准备发送指令" );
                sendData(hexByte, frameParser, new DataReceivedListener() {
                    @Override
                    public void onBytesDataReceived(byte[] data) {

                        byte[] content = null;
                        CmdReponse cmdReponse = null;
                        try {
                            Log.d("Communicator","已接收指令" );
                            cmdReponse = frameParser.parse(data);
                            observableData(cmdReponse, cmdReponseListener);
                        } catch (Exception e) {
                            closeClient();
                            observableErrData("解析数据失败", cmdReponseListener);
                            Log.d("Communicator", "parse data error:" + e.getMessage());
                        }
                    }

                    @Override
                    public void onErr(String errMsg) {

                        observableErrData(errMsg, cmdReponseListener);
                    }
                });


            }
            }
        }).start();
    }


    /**
     * 下游 解析获取传来的数据
     * @param cmdReponse
     * @param cmdReponseListener
     */
    private void observableData(final CmdReponse cmdReponse, final CmdReponseListener cmdReponseListener) {
        Observable.create(new ObservableOnSubscribe<CmdReponse>() {
            @Override
            public void subscribe(ObservableEmitter<CmdReponse> e) throws Exception {
                e.onNext(cmdReponse);

            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<CmdReponse>() {
                    @Override
                    public void accept(CmdReponse cmdReponse) throws Exception {
//                        for(byte b:s){
//                            Log.d("Communicator", "b:" + b);
//                        }
                        if ( cmdReponse.getContent() != null)
                            Log.d("cmdReponse.getContent()", "data:" + HexStringUtils.bytesToHexString(cmdReponse.getContent()));
                        cmdReponseListener.onCmdReponse( cmdReponse);
                    }

                });
    }


    /**
     * 下游 解析获取传来的数据
     * @param err 错误码
     * @param cmdReponseListener
     */
    private void observableErrData(final String err,final CmdReponseListener cmdReponseListener) {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext(err);
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        cmdReponseListener.onErr(s);
                    }

                });
    }

    public interface CmdReponseListener {
        void onCmdReponse( CmdReponse cmdReponse);
        void onErr(String errMsg);
    }
}
