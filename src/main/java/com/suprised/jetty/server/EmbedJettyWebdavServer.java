package com.suprised.jetty.server;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * 内嵌jetty下载服务
 */
public class EmbedJettyWebdavServer {

    
    public static void main(String[] args) throws Exception {
        Server server = new Server();
        
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(8889);
        connector.setMaxIdleTime(30000);
        connector.setRequestHeaderSize(8192);
        connector.setHost("192.168.1.109");
        server.setConnectors(new Connector[]{ connector });
        
        HandlerCollection handlerList = new HandlerCollection();
        
        WebAppContext context = new WebAppContext();
        // web.xml 必须存在: 可以直接拷贝一个空的web.xml文件到配置的路径下
        context.setDescriptor("D:/files-management/jetty/web.xml");
        context.setResourceBase("D:/files-management");
        context.setContextPath("/webdav");
        context.setParentLoaderPriority(true);
        // server.setHandler(context);
        handlerList.addHandler(context);
        
        /* WebAppContext context2 = new WebAppContext();
        // web.xml 必须存在: 可以直接拷贝一个空的web.xml文件到配置的路径下
        context2.setDescriptor("D:/files-management/jetty/web.xml");
        context2.setResourceBase("D:/files-management");
        context2.setContextPath("/download2");
        context.setParentLoaderPriority(true);
        // server.setHandler(context2);
        handlerList.addHandler(context2);*/
        
        server.setHandler(handlerList);
        
        // TODO: webdav support 
        // WebdavListener listener = new WebdavListener(null, null);
        
        
        server.start();
        server.join();
    }
    
}
