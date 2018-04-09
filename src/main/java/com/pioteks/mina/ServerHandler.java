package com.pioteks.mina;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pioteks.utils.BytesHexString;
import com.pioteks.utils.Command;
import com.pioteks.utils.RequestResponse;

public class ServerHandler extends IoHandlerAdapter {

    public static final Logger logger=LoggerFactory.getLogger(ServerHandler.class);
    public static final CharsetDecoder decoder = (Charset.forName("UTF-8")).newDecoder();
    public static DatagramSocket socket = null;
    
    
	// 定义发送数据报的目的地  
    public static final int DEST_PORT = 30000;  
    public static final String DEST_IP = "127.0.0.1";  
    // 定义每个数据报的最大大小为4KB  
    private static final int DATA_LEN = 4096;     
    // 定义接收网络数据的字节数组  
    byte[] inBuff = new byte[DATA_LEN];  
    // 定义一个用于发送的DatagramPacket对象  
    private DatagramPacket outPacket = null; 
    
    
    private Command command;
    private List<String> messageList;
    public ServerHandler() {
		// TODO Auto-generated constructor stub
	}
    
    public ServerHandler(Command command, List<String> messageList) {
    	this.command = command;
    	this.messageList = messageList;
    }
    
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
            
            
            //将NB模块的IP和端口信息转化为字符串，发送
            
//                result = send(all, session);	//使用传统方式进行内部通信
            
//            clientSend(all, session);		// 使用NIMA框架进行内部通信
            
            response(data, session);			//jar包形式的回复
            
        }
    }
    
    private void response(byte[] data, IoSession session) throws InterruptedException {
    	List<RequestResponse> commandList = null;
    	if(command.getMode() == 1 ) {
    		commandList = command.getCommandListMode1();
    	}else if(command.getMode() ==2) {
    		commandList = command.getCommandListMode2();
    	}
    	
		int flag = 0;
		byte[] result = null;
//    	for (Entry<String, String> entry : commandMap.entrySet()) {
//    		String key = (String) entry.getKey();
//			if(byteToStr(data).equals(key)) {
//				flag = 1;
//				result =  strToByte((String) entry.getValue());
//				if(command.getMode() == 1 ) {
//					messageList.add("Up Cover Opened");
//		    	}else if(command.getMode() ==2) {
//		    		commandList = command.getCommandListMode2();
//		    	}
//				break;
//			}
//    	}
    	for(int i = 0; i < commandList.size(); i++) {
    		RequestResponse rr = commandList.get(i);
    		String request = rr.getRequest();
			if(byteToStr(data).equals(request)) {
				flag = 1;
				result =  strToByte(rr.getResponse());
				if(command.getMode() == 1 ) {
					switch(i) {
					case 0:
						messageList.add("Up Cover Opened");
						break;
					case 1:
						messageList.add("Down Cover Opened");
						break;
					case 2:
						messageList.add("Vibrating");
						break;
					}
		    	}else if(command.getMode() ==2) {
		    		commandList = command.getCommandListMode2();
		    		switch(i) {
					case 0:
						messageList.add("Down Cover Opening");
						break;
					case 1:
						messageList.add("Down Cover Closing");
						break;
					}
		    	}
				break;
			}
    	}
    	
    	if(flag == 0) {
    		StringBuffer sbu = new StringBuffer();
    		sbu.append((char) 69);sbu.append((char) 82);sbu.append((char) 82);sbu.append((char) 79);
    		sbu.append((char) 82);
    		result = sbu.toString().getBytes();
    	}
    	
    	Thread.currentThread().sleep(command.getDelayTime());
    	
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

	private void clientSend(byte[] all, IoSession session) throws InterruptedException {
	
    	UDPInnerClient udpClient=new UDPInnerClient(DEST_IP,DEST_PORT);  
        udpClient.setConnector(new NioDatagramConnector());  
        udpClient.getConnector().setHandler(new InnerClientIoHandler());  
        IoConnector connector=udpClient.getConnector();  
//            connector.getFilterChain().addLast("codec",   		//客户端和服务器使用的过滤器不一致将导致接受不到信息
//                    new ProtocolCodecFilter(  
//                            new TextLineCodecFactory(  
//                                    Charset.forName("UTF-8"),   
//                                    LineDelimiter.WINDOWS.getValue(),  
//                                    LineDelimiter.WINDOWS.getValue())));  
//              
        ConnectFuture connectFuture=connector.connect(udpClient.getInetSocketAddress());  
        // 等待是否连接成功，相当于是转异步执行为同步执行。  
        connectFuture.awaitUninterruptibly();  
        //连接成功后获取会话对象。如果没有上面的等待，由于connect()方法是异步的，  
        //connectFuture.getSession(),session可能会无法获取。  
        udpClient.setSession(connectFuture.getSession());  
        udpClient.getSession().setAttribute("session_NB", session);
            IoBuffer buf = IoBuffer.wrap(all);
            udpClient.getSession().write(buf);  
        }

 
        public byte[] send(byte[] buff, IoSession session)throws IOException  
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
                byte[] bytes = inPacket.getData();
                
                byte[] result = new byte[inPacket.getLength()];
                System.arraycopy(bytes, 0, result, 0, result.length);
                
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
            return result;
        
    }  
    
    
    public static byte[] addBytes(byte[] data1, byte[] data2) {  
        byte[] data3 = new byte[data1.length + data2.length];  
        System.arraycopy(data1, 0, data3, 0, data1.length);  
        System.arraycopy(data2, 0, data3, data1.length, data2.length);  
        return data3;  
      
    }
    
    /**
     * 将16进制的字符串转成byte[]数组
     * “AA-41-42-43”
     * @param s
     * @return
     */
	public static byte[] strToByte(String s) {
		String[] sList = s.split("-");
		byte[] bytes = new byte[sList.length];
		for(int i = 0; i < sList.length; i++) {
			int iValue = Integer.parseInt(sList[i], 16);
			bytes[i] = (byte)iValue;
		}
		
		return bytes;
	}
	
	/**
	 * 将byte[]数组转化为16进制字符串
	 * @param data
	 * @return
	 */
	private String byteToStr(byte[] data) {
		String contentHex = "";
		for(int i = 0; i < data.length; i++ ) {
			String s = Integer.toHexString(data[i] & 0xFF);
			if(s.length() == 1) {
				s = "0" + s;
			}
			contentHex = contentHex + s + "-";
		}
		contentHex = contentHex.substring(0, contentHex.length()-1);
		
		return contentHex.toUpperCase();
	}
}
