package com.pioteks.mina;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pioteks.utils.BytesHexString;

public class MyMinaHandler extends IoHandlerAdapter {

    public static final Logger logger=LoggerFactory.getLogger(MyMinaHandler.class);
    public static final CharsetDecoder decoder = (Charset.forName("UTF-8")).newDecoder();
    public static DatagramSocket socket = null;
        /**
         * MINA的异常回调方法。
         * <p>
         * 本类中将在异常发生时，立即close当前会话。
         *
         * @param session 发生异常的会话
         * @param cause 异常内容
         * @see IoSession#close(boolean)
         */
        @Override
        public void exceptionCaught(IoSession session, Throwable cause) throws Exception
        {
            cause.printStackTrace();
            socket.close();
            session.closeNow();
        }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        System.out.println("Session closed...");
        logger.info("Session closed...");
        SocketAddress remoteAddress = session.getRemoteAddress();
        System.out.println(remoteAddress.toString());
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        System.out.println("Session created...");
        logger.info("Session created...");
        SocketAddress remoteAddress = session.getRemoteAddress();
        System.out.println(remoteAddress.toString());
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        System.out.println("Session opened...");
        logger.info("Session opened...");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        System.out.println("Session Idle...");
        logger.info("Session Idle...");
    }

    /**
         * MINA框架中收到客户端消息的回调方法。
         * <p>
         * 本类将在此方法中实现完整的即时通讯数据交互和处理策略。
         * <p>
         * 为了提升并发性能，本方法将运行在独立于MINA的IoProcessor之外的线程池中，
         *
         * @param session 收到消息对应的会话引用
         * @param message 收到的MINA的原始消息封装对象，本类中是 {@link IoBuffer}对象
         * @throws Exception 当有错误发生时将抛出异常
         */
        @Override
        public void messageReceived(IoSession session, Object message)throws Exception
        {
        	
            byte[] result=new byte[1];
            IoBuffer ioBuffer=(IoBuffer) message;
            if(ioBuffer.remaining()>16){
                result[0]=(byte)0xFF;
            }else{
                byte[] data = new byte[ioBuffer.limit()-ioBuffer.position()];
                ioBuffer.get(data);
                
                System.out.println("received data "+ BytesHexString.bytesToHexString(data));

                byte[] address = (session.getRemoteAddress() + ":").getBytes();

                byte[] all = addBytes(address, data);			//拼接上传内容
                
                all = addBytes(all, ":".getBytes());
                
                
                result = send(all);//将NB模块的IP和端口信息转化为字符串，发送
                System.out.println(new String(all, 0, all.length));
                System.out.println(new String(result, 0, result.length));
            }
            	
            //*********************************************** 回复数据

            // 组织IoBuffer数据包的方法：本方法才可以正确地让客户端UDP收到byte数组
            IoBuffer buf = IoBuffer.wrap(result);

            // 向客户端写数据
            WriteFuture future = session.write(buf);
            // 在100毫秒超时间内等待写完成
            future.awaitUninterruptibly(100);
            // The message has been written successfully
            if( future.isWritten() ) {
                System.out.println("return successfully");
                logger.info("return successfully");
            }else{
                System.out.println("return failed");
                logger.info("return failed");
            }
            
            
        }
        
        // 定义发送数据报的目的地  
        public static final int DEST_PORT = 30000;  
        public static final String DEST_IP = "127.0.0.1";  
        // 定义每个数据报的最大大小为4KB  
        private static final int DATA_LEN = 4096;     
        // 定义接收网络数据的字节数组  
        byte[] inBuff = new byte[DATA_LEN];  
        // 以指定的字节数组创建准备接收数据的DatagramPacket对象  
//        private DatagramPacket inPacket =   null;
        // 定义一个用于发送的DatagramPacket对象  
        private DatagramPacket outPacket = null;  
        public byte[] send(byte[] buff)throws IOException  
        {  
                // 创建一个客户端DatagramSocket，使用端口30001发送  
                if(socket == null) {
                	socket = new DatagramSocket(30001);
                }
                // 初始化发送用的DatagramSocket，它包含一个长度为0的字节数组  
                outPacket = new DatagramPacket(new byte[0] , 0  
                    , InetAddress.getByName(DEST_IP) , DEST_PORT);  
                // 设置发送用的DatagramPacket中的字节数据  
                outPacket.setData(buff);  
                // 发送数据报  
                socket.send(outPacket);  
                // 读取Socket中的数据，读到的数据放在inPacket所封装的字节数组中  
                DatagramPacket inPacket = new DatagramPacket(inBuff , inBuff.length);  
                socket.receive(inPacket);  
//                System.out.println(new String(inBuff , 0 , inPacket.getLength()));  
//                String msg=new String(inPacket.getData(), 0, inPacket.getLength());
                byte[] bytes = inPacket.getData();
                
                byte[] result = new byte[inPacket.getLength()];
                System.arraycopy(bytes, 0, result, 0, result.length);
                return result;
            
        }  
        
        
        public static byte[] addBytes(byte[] data1, byte[] data2) {  
            byte[] data3 = new byte[data1.length + data2.length];  
            System.arraycopy(data1, 0, data3, 0, data1.length);  
            System.arraycopy(data2, 0, data3, data1.length, data2.length);  
            return data3;  
          
        }
}
