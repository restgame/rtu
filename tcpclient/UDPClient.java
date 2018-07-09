package com.wgkj.rtucontrol.tcpclient;

import android.util.Log;

import com.wgkj.rtucontrol.tcpclient.imp.OnGetUDPDataListener;
import com.wgkj.rtucontrol.utils.HexStringUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public  class UDPClient{
    private static  int SERVER_PORT = 47788;
    private static  int CLIENT_PORT = 47789;
    DatagramSocket  dSocket;
//    private String msg;
    private byte[] msgs = {(byte)0xC8,(byte)0xC9,0x00,0x00,0x00,0x06,(byte)0xDE,0x06,0x23,(byte)0x1F,00,(byte) 0xFF};

    private boolean life = true;
    private boolean isRunning = false;
    private String remoteIp;
    private String localIp = "127.0.0.1";
    InetAddress remoteAddress = null;
    InetAddress localAddress = null;
    DatagramPacket dPacket;
    OnGetUDPDataListener onGetUDPDataListener;
    /**
     * @param
     */
    public UDPClient(String ip) {
        super();
        this.remoteIp = ip;
//        receive();
    }

    /**
     * 发送信息到服务器
     */
    public void send(final OnGetUDPDataListener onGetUDPDataListener){
        life = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    remoteAddress = InetAddress.getByName( remoteIp);
                    //localAddress = InetAddress.getByName( localIp);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                try {
                    if( dSocket == null || dSocket.isClosed()){
                        dSocket = new DatagramSocket( );
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
//                    closeClient();
                    onGetUDPDataListener.onErr("初始化UDP Socket失败");
                }
                int msg_len = msgs == null ? 0 : msgs.length;
                    dPacket = new DatagramPacket(msgs, msg_len,
                            remoteAddress, SERVER_PORT);

                try {
                    if(dSocket != null) {
                        dSocket.send(dPacket);
                    }
                    Log.i("UDPClient", "send data to " + remoteIp + ":" + HexStringUtils.bytesToHexString(msgs));
                    loopReceive(onGetUDPDataListener);

                } catch (IOException e) {
                    e.printStackTrace();
                    onGetUDPDataListener.onErr("发送UDP失败");
                }
            }
        }).start();
    }


    /**
     * @return the life
     */
    public boolean isLife() {
        return life;
    }

    /**
     * @param life
     *            the life to set
     */
//    public void setLife(boolean life) {
//        this.life = life;
//    }

    public void loopReceive( final OnGetUDPDataListener onGetUDPDataListener){
        if (isRunning)
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    isRunning = true;
                    if(dSocket == null || dSocket.isClosed()){
                        dSocket = new DatagramSocket();
                    }
                    while (isRunning) {
                        try {
                            if(dSocket == null || dSocket.isClosed()){
                                isRunning = false;
                            }

                            byte[] buf = new byte[1024];
                            DatagramPacket  dPacket = new DatagramPacket(buf, buf.length);
                            dSocket.setSoTimeout(1000);
                            dSocket.receive(dPacket);
                            // Log.i("udpclient", "received from:" "");
                            String ss = new String(dPacket.getData(), 0, dPacket.getData().length);
                            Log.i("udpclient","received data from [" +dPacket.getAddress() + "](hex,size=" + dPacket.getData().length + "):"
                                    + HexStringUtils.bytesToHexString( dPacket.getData()) );
                            Log.i("udpclient","received data from [" +dPacket.getAddress() + "](string,size=" + dPacket.getData().length + "):"  + ss );
                            if ( onGetUDPDataListener != null)
                                onGetUDPDataListener.getStringData(dPacket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }finally {

                        }
                    }

                } catch (SocketException e) {
                    e.printStackTrace();
                }
                closeClient();
            }
        }).start();
    }

    protected void stopLoopReceving()
    {
        isRunning = false;
    }

    public void closeClient() {
        if (dSocket != null ) {
            try {
                Log.i("UdpClient", "closeClient");
                if ( !dSocket.isClosed() )
                {
                    dSocket.close();
                    //dSocket.setReuseAddress(true);
                }
                isRunning = false;
                dSocket = null;
                //setLife(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}