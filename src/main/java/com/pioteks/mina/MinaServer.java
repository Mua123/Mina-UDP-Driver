package com.pioteks.mina;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.session.ExpiringSessionRecycler;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;


public class MinaServer {

    public static int port = 12345;

    private static final Logger logger=LoggerFactory.getLogger(MinaServer.class);

    public static void main(String[] args) throws Exception {
        // ** Acceptor设置
        NioDatagramAcceptor acceptor = new NioDatagramAcceptor();
        // 此行代码能让你的程序整体性能提升10倍
        DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
        chain.addLast("threadPool",new ExecutorFilter(Executors.newCachedThreadPool()));
        chain.addLast("logger", new LoggingFilter());
        // 设置MINA2的IoHandler实现类
        acceptor.setHandler(new MyMinaHandler());
        // 设置会话超时时间（单位：毫秒），不设置则默认是10秒，请按需设置
        acceptor.setSessionRecycler(new ExpiringSessionRecycler(15 * 1000));

        // ** UDP通信配置
        DatagramSessionConfig dcfg = acceptor.getSessionConfig();
        dcfg.setReuseAddress(true);
        // 设置输入缓冲区的大小，压力测试表明：调整到2048后性能反而降低
        dcfg.setReceiveBufferSize(1024);
        // 设置输出缓冲区的大小，压力测试表明：调整到2048后性能反而降低
        dcfg.setSendBufferSize(1024);

        // ** UDP服务端开始侦听
        acceptor.bind(new InetSocketAddress(port));

        System.out.println("UDPserver start in 12345 ..");
    }
}
