package com.pioteks.mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pioteks.utils.BytesHexString;

public class InnerClientIoHandler extends IoHandlerAdapter{

	public static final Logger logger=LoggerFactory.getLogger(ServerHandler.class);
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		System.out.println("创建session连接，发送到web");
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		System.out.println("会话连接关闭，发送到web的回话关闭");
		session.close(true);
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		System.out.println("发送到web的回话异常");
		cause.printStackTrace();
        session.closeNow();
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		
        IoBuffer ioBuffer=(IoBuffer) message;
        byte[] data = new byte[ioBuffer.limit()-ioBuffer.position()];
        ioBuffer.get(data);
         
        IoBuffer buf = IoBuffer.wrap(data);
        
        IoSession sessionNB = (IoSession)session.getAttribute("session_NB");
        WriteFuture future = sessionNB.write(buf);
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

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		System.out.println("客户端消息已发送成功，发送数据:"+message.toString());
		System.out.println("发送到web的消息发送成功" + session.getRemoteAddress() + session.getLocalAddress() +session.getServiceAddress());
	}
	
}
