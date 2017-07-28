package com.suprised.jetty.server;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * 内嵌jetty下载服务
 */
public class EmbedJettyServerTest {

    
    public static void main(String[] args) throws Exception {
        Server server = new Server();
        
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(8887);
        connector.setMaxIdleTime(30000);
        connector.setRequestHeaderSize(8192);
        // connector.setHost("192.168.1.109");
        server.setConnectors(new Connector[]{ connector });
        
        HandlerCollection handlerList = new HandlerCollection();
        // http://192.168.1.109:8887/download/psdenglish.psd
        WebAppContext context = new WebAppContext();
        context.setDefaultsDescriptor("/com/suprised/jetty/server/webdefault.xml");
        // web.xml 必须存在: 可以直接拷贝一个空的web.xml文件到配置的路径下
        context.setDescriptor("D:/files-management/jetty/web.xml");
        context.setResourceBase("D:/files-management");
        context.setContextPath("/download");
        context.setParentLoaderPriority(true);
        // server.setHandler(context);
        handlerList.addHandler(context);
        
        WebAppContext context2 = new WebAppContext();
        // web.xml 必须存在: 可以直接拷贝一个空的web.xml文件到配置的路径下
        context2.setDescriptor("D:/files-management/jetty/web.xml");
        context2.setResourceBase("D:/files-management");
        context2.setContextPath("/download2");
        context.setParentLoaderPriority(true);
        // server.setHandler(context2);
        handlerList.addHandler(context2);
        
        // 资源处理
        /*ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);// 列出目录
        resourceHandler.setResourceBase(storagePath);
        resourceHandler.setStylesheet("");
        handlerList.addHandler(resourceHandler);*/
        
        server.setHandler(handlerList);
        
        // TODO: webdav support 
        // WebdavListener listener = new WebdavListener(null, null);
        
        server.start();
        server.join();
    }
    
}
