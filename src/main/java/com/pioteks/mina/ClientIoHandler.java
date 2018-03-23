package com.pioteks.mina;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

public class ClientIoHandler extends IoHandlerAdapter{

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		System.out.println("创建session连接");
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		System.out.println("会话连接关闭");
		session.close(true);
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		cause.printStackTrace();
        session.closeNow();
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		System.out.println("客户端消息已发送成功，发送数据:"+message.toString());
	}
	
}
