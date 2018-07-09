package com.wgkj.rtucontrol.tcpclient;

import android.util.Log;

import com.wgkj.rtucontrol.cmd.FrameParser;
import com.wgkj.rtucontrol.tcpclient.imp.DataReceivedListener;
import com.wgkj.rtucontrol.tcpclient.imp.TCPClientInterface;
import com.wgkj.rtucontrol.utils.HexStringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.LinkedList;

import static java.lang.Thread.sleep;

/**
 * Created by wgkj003 on 2017/10/18.
 */

public class TCPClient  implements TCPClientInterface {
    private static final String TAG = com.wgkj.rtucontrol.tcp.TCPClient.class.getSimpleName();
    private static final int CONNECT_TIMEOUT = 8000;
    private static final int INPUT_STREAM_READ_TIMEOUT = 5000;
    private Socket mSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    protected boolean keepAlive = false;

    private boolean isInitClient = false;
    byte[] data;
    //IP地址
    private String host;
    //端口
    private int port;
    //命令
    public TCPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public Boolean initClient() {
        if ( isInitClient ){
            return true;
        }
        if(port == 0 || host == null || "".equals(host)){
            Log.d("TCPClient: ", "illegal host or ip!");
            return false;
        }

        try {
            mSocket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress( host, port);
            // 设置连接超时时间
            mSocket.connect(socketAddress, CONNECT_TIMEOUT);
            if (mSocket.isConnected()) {
                logI("connected to host success");
                // 设置读流超时时间，必须在获取流之前设置
                mSocket.setSoTimeout(INPUT_STREAM_READ_TIMEOUT);
                mInputStream = mSocket.getInputStream();
                mOutputStream = mSocket.getOutputStream();
            } else {
                mSocket.close();
                mInputStream.close();
                mOutputStream.close();
                isInitClient = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    @Override
    public void closeClient() {
        if (mSocket != null) {
            try {
                if(mInputStream!=null){
                    mInputStream.close();
                }
                if(mOutputStream!=null){
                    mOutputStream.close();
                }
                mSocket.close();
                mInputStream = null;
                mOutputStream = null;
                mSocket = null;
                isInitClient = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public  void sendData(final byte[] cmdData, com.wgkj.rtucontrol.cmd.FrameParser frameParser, final DataReceivedListener dataReceivedListener)
    {

        if (!initClient()) {
            dataReceivedListener.onErr("建立网络连接失败");
            return;
        }
        try {
            mOutputStream.write(cmdData);
            mOutputStream.flush();
            String hexStr = HexStringUtils.bytesToHexString(cmdData);
            Log.i("tcpclient", "send data to [" + host + "]:" + hexStr);
            if(mSocket != null && mSocket.isConnected() && mInputStream != null) {
                // 读取流
                Log.i("tcpclient", "begin to receive data");
                data = new byte[0];
                byte[] data2 = new byte[0];
                byte[] buf = new byte[1024];
                int len;
                LinkedList<Byte> byteList = new LinkedList<Byte>();
                try {
//                    if ((len = mInputStream.read(buf)) != -1) {
//                        byte[] temp = new byte[data.length + len];
//                        System.arraycopy(data, 0, temp, 0, data.length);
//                        System.arraycopy(buf, 0, temp, data.length, len);
//                        data = temp;
//                    }
                    //int one;
                    int length;
                    int count = 0;
                    while( (length = mInputStream.read(buf)) != -1)
                    {
                        boolean stop = false;
                        for (int i = 0; i < length; i++)
                        {
                            byte one = buf[i];
                            byteList.add(one);
                            com.wgkj.rtucontrol.cmd.FrameParser.PARSE_STATUS status = frameParser.input((byte) one);
                            count++;
                            if ( com.wgkj.rtucontrol.cmd.FrameParser.PARSE_STATUS.FINISH == status)
                            {
                                data = frameParser.getFrameData();
                                stop = true;
                                break;
                            }
                            else if ( FrameParser.PARSE_STATUS.ERROR == status)
                            {
                                stop = true;
                                data = frameParser.getFrameData();
                                break;
                            }
                            if ( count > 300)
                            {
                                dataReceivedListener.onErr("接收数据长度过长");
                                return;
                            }
                        }
                        if (stop)
                            break;
                            //byteList.add( new Byte((byte)one));
                    }
                    if ( count == 0)
                    {
                        dataReceivedListener.onErr("接收数据超时或未接收到数据");
                        return;
                    }
                    byte[] bytes = new byte[byteList.size()];
                    for ( int i = 0; i < byteList.size(); i++)
                    {
                        bytes[i] = byteList.get(i);
                    }
                    // 处理流
                    Log.i("tcpclient", "receive data from [" + host + "]:" + HexStringUtils.bytesToHexString(bytes));
                    Log.i("tcpclient", "filter data from [" + host + "]:" + HexStringUtils.bytesToHexString(data));
                    dataReceivedListener.onBytesDataReceived(data);
                } catch (IOException e) {
                    e.printStackTrace();
                    dataReceivedListener.onErr("发送数据失败:" + e.getMessage());
                }

//                            }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if ( !keepAlive)
                closeClient();
        }

    }

    public void setKeepAlive(boolean alive)
    {
        keepAlive = alive;
        if( !keepAlive)
            closeClient();
    }



    @Override
    public void logI(String msg) {
        Log.i(TAG, msg);
    }
}
