package com.pioteks.mina;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;

public class UDPInnerClient {
	private InetSocketAddress inetSocketAddress;  
    
    private IoSession session;  
      
    private IoConnector connector;  
      
    public UDPInnerClient() {  
        super();  
    }  
      
    public UDPInnerClient(String host,int port){  
          
        inetSocketAddress=new InetSocketAddress(host,port);  
          
    }  
      
  
    public IoSession getSession() {  
        return session;  
    }  
  
    public void setSession(IoSession session) {  
        this.session = session;  
    }  
  
    public IoConnector getConnector() {  
        return connector;  
    }  
  
    public void setConnector(IoConnector connector) {  
        this.connector = connector;  
    }  
  
    public InetSocketAddress getInetSocketAddress() {  
        return inetSocketAddress;  
    }  
  
    public void setInetSocketAddress(InetSocketAddress inetSocketAddress) {  
        this.inetSocketAddress = inetSocketAddress;  
    }  
}
