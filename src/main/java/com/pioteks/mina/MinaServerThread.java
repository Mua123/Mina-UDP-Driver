package com.pioteks.mina;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.session.ExpiringSessionRecycler;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pioteks.utils.Command;

public class MinaServerThread implements Runnable {
	private static int port;
	private static Command command;
	private static List<String> messageList;
	private static final Logger logger=LoggerFactory.getLogger(MinaServerThread.class);
	
	public MinaServerThread() {

	}
	
	public MinaServerThread(int port, Command command, List<String> messageList) {
		this.port = port;
		this.command = command;
		this.messageList = messageList;
	}
	
	@Override
	public void run() {
		try {
			
			// ** Acceptor设置
			NioDatagramAcceptor acceptor = new NioDatagramAcceptor();
			// 此行代码能让你的程序整体性能提升10倍
			DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
			chain.addLast("threadPool",new ExecutorFilter(Executors.newCachedThreadPool()));
			chain.addLast("logger", new LoggingFilter());
			// 设置MINA2的IoHandler实现类
			acceptor.setHandler(new ServerHandler(command, messageList));
			// 设置会话超时时间（单位：毫秒），不设置则默认是10秒，请按需设置
			acceptor.setSessionRecycler(new ExpiringSessionRecycler(15 * 1000));
			
			// ** UDP通信配置 设置是否重用地址？也就是每个发过来的udp信息都是一个地址？
			DatagramSessionConfig dcfg = acceptor.getSessionConfig();
			dcfg.setReuseAddress(true);
			// 设置输入缓冲区的大小，压力测试表明：调整到2048后性能反而降低
			dcfg.setReceiveBufferSize(1024);
			// 设置输出缓冲区的大小，压力测试表明：调整到2048后性能反而降低
			dcfg.setSendBufferSize(1024);
			
			// ** UDP服务端开始侦听
	
			acceptor.bind(new InetSocketAddress(port));

			System.out.println(System.currentTimeMillis());
			System.out.println("UDPserver start in "+ port +" ..");
		}catch(Exception e) {
			e.printStackTrace();
		}
        
	}
	
	

}
