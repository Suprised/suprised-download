package com.suprised.jetty.server;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.catalina.Globals;
import org.apache.catalina.servlets.WebdavServlet;
import org.apache.commons.io.IOUtils;
import org.apache.naming.resources.BaseDirContext;
import org.apache.naming.resources.FileDirContext;
import org.apache.naming.resources.ProxyDirContext;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * 内嵌jetty下载服务
 */
public class EmbedJettyServer {
    
    /**
     * jetty默认的web.xml路径
     */
    private static final String JETTY_WEB_XML_FLODER = "jetty";
    private static final String JETTY_WEB_XML = "/web.xml";
    
    public static final String DEF_JETTY_NAME = "内嵌下载服务";
    public static final String DEF_JETTY_DAV_NAME = "内嵌WEBDAV服务";
    
    private static EmbedJettyServer embedServer = new EmbedJettyServer();
    
    private Server server = null;
    
    private EmbedJettyServer() {
        // this.server = new Server();
        // this.configServer(server);
    }
    
    /**
     * 配置参数
     */
    private void configServer(Server server) {
        /*
          <New class="org.eclipse.jetty.util.thread.QueuedThreadPool">
            <!-- specify a bounded queue -->
            <Arg>
               <New class="java.util.concurrent.ArrayBlockingQueue">
                  <Arg type="int">6000</Arg>
               </New>
            </Arg>
            <Set name="minThreads">10</Set>
            <Set name="maxThreads">200</Set>
            <Set name="detailedDump">false</Set>
          </New>
        */
        QueuedThreadPool threadPool = new QueuedThreadPool(new ArrayBlockingQueue<Runnable>(6000));
        threadPool.setMinThreads(10);
        threadPool.setMaxThreads(200);
        threadPool.setDetailedDump(false);

        server.setThreadPool(threadPool);
    }
    
    /**
     * 获得一个内嵌的下载服务
     */
    public synchronized static final EmbedJettyServer getEmbedServer() {
        return embedServer;
    }

    private SelectChannelConnector getConnector() {
        int port = 8081;
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(port);
        connector.setMaxIdleTime(30000);
        connector.setRequestHeaderSize(8192);
        return connector;
    }
    
    /**
     * 启动内嵌下载服务
     */
    /*public void start() throws Exception {
        
        // connector.setHost("192.168.1.109");
        server.setConnectors(new Connector[]{ getConnector() });
        
        // download server
        addContextPath("/download", "D:/files-management");
        addContextPath("/download2", "D:/files-management");
        
        // webdav server
        addWebDavSupport("/webdav", "D:/files-management");
        
        server.start();
    }*/
    
    /**
     * 添加内嵌jetty的webdav服务支持
     * 
     * @param contextPath
     * @param storagePath
     */
    private void addWebDavSupport(String contextPath, String storagePath) {
        try {
            WebAppContext context = addContextPath(contextPath, storagePath);
            // 参考tomcat的webdav的实现
            ServletHolder servletHolder = context.addServlet(WebdavServlet.class, "/*");
            // 以下参数设置是参考tomcat的WebdavServlet实现参数设置。
            servletHolder.setInitParameter("readonly", "false");
            servletHolder.setInitParameter("listings", "true");
            servletHolder.setInitParameter("debug", "0");
            servletHolder.setInitParameter("allowSpecialPaths", "true");
            
            Hashtable<String, String> env = new Hashtable<String, String>();
            // env.put(ProxyDirContext.HOST, "localhost");
            env.put(ProxyDirContext.CONTEXT, contextPath);
            FileDirContext fileDirContext = new FileDirContext();
            ProxyDirContext proxyDirContext = new ProxyDirContext(env, fileDirContext);
            ((BaseDirContext) fileDirContext).setDocBase(storagePath);
            ((BaseDirContext) fileDirContext).setCached(true);
            
            context.getServletContext().setAttribute(Globals.RESOURCES_ATTR, proxyDirContext);
        } catch(Exception e) {
            // webdav 忽略异常
        }
    }
    
    public boolean isRunning() {
        return server.isRunning();
    }
    
    /**
     * 
     * 添加一个contextPath
     * 
     * @param storagePath 存储方案路径
     */
    /*public void addContextPath(String storagePath) throws Exception {
        // 创建默认的contextPath。注意：多个存储方案，contextPath不能重复
        String defaultContextPath = "";
        this.addContextPath(defaultContextPath, storagePath);
    }*/
    
    /**
     * 添加一个contextpath
     */
    private WebAppContext addContextPath(String contextPath, String storagePath) throws Exception {
        WebAppContext context = new WebAppContext();
        // context.setDefaultsDescriptor("/com/dascom/dafc/app/tool/embed/webdefault.xml");
        
        String webXmlPath = getWebXmlPath(storagePath);
        context.setDescriptor(webXmlPath);
        context.setResourceBase(storagePath);
        context.setContextPath(contextPath);
        context.setParentLoaderPriority(true);
        
        // 支持多个contextpath
        HandlerCollection handlers = (HandlerCollection) server.getHandler();
        if (handlers == null) {
            handlers = new HandlerCollection();
        }
        handlers.addHandler(context);
        
        server.setHandler(handlers);
        
        return context;
    }
    
    /**
     * 每个存储方案下都会创建一个web.xml 路径: jetty/web.xml
     * 
     * @param storagePath 存储方案路径
     * @return web.xml 全路径
     */
    private String getWebXmlPath(String storagePath) throws Exception {
        // web.xml 必须存在: 可以直接拷贝一个空的web.xml文件到配置的路径下
        String webXmlFloder = storagePath + File.separator + JETTY_WEB_XML_FLODER;
        File jettyFloder = new File(webXmlFloder);
        if (!jettyFloder.exists()) {// 创建jetty文件夹
            jettyFloder.mkdirs();
        }
        File file = new File(webXmlFloder + JETTY_WEB_XML);
        // 创建一个默认的web.xml
        if (!file.exists()) {
            file.createNewFile();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                IOUtils.write(EMPTY_WEB_XML, fos);
                fos.flush();
            }
        }
        return file.getAbsolutePath();
    }

    /**
     * 重启jetty服务
     */
    public void restart(List<ServiceInstanceBean> services) throws Exception {
        if (server != null && server.isRunning()) {
            server.stop();
            server = null;
        }
        this.server = new Server();
        this.configServer(server);
        server.setConnectors(new Connector[]{ getConnector() });
        for (ServiceInstanceBean service : services) {
            if (service.getType() == ServiceInstanceBean.THREE_DOWNLOAD_SERVICE) {
                addContextPath(service.getVirPath(), service.getResourcePath());    
            } else if (service.getType() == ServiceInstanceBean.MSOFFICE_SERVICE) {
                addWebDavSupport(service.getVirPath(), service.getResourcePath());
            }
        }
        server.start();
    }
    
    // jetty需要一个web.xml文件，内容可以为空
    private static String EMPTY_WEB_XML = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
                                            "<web-app xmlns=\"http://java.sun.com/xml/ns/javaee\" " + 
                                                     "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + 
                                                     "xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee " +
                                                      "http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd\" " + 
                                                     "version=\"3.0\" metadata-complete=\"true\">"+
                                                     "<filter>" + 
                                                      "<filter-name>cross-origin</filter-name>" +   
                                                      "<filter-class>org.eclipse.jetty.servlets.CrossOriginFilter</filter-class>" +   
                                                      "<init-param>" + 
                                                          "<param-name>allowedOrigins</param-name>" +   
                                                          "<param-value>*</param-value>" + 
                                                      "</init-param>" + 
                                                      "<init-param>" + 
                                                          "<param-name>allowedMethods</param-name>" +   
                                                          "<param-value>GET,POST,HEAD,OPTIONS</param-value>" + 
                                                      "</init-param>" + 
                                                      "<init-param>" + 
                                                          "<param-name>allowedHeaders</param-name>" +   
                                                          "<param-value>X-Requested-With,Content-Type,Accept,Origin,range</param-value>" +   
                                                      "</init-param>" + 
                                                  "</filter>" + 
                                                  "<filter-mapping>" +   
                                                      "<filter-name>cross-origin</filter-name>" +   
                                                      "<url-pattern>/*</url-pattern>" + 
                                                  "</filter-mapping>" +
                                                  // 重载默认的servlet，设置jetty不锁定文件 begin 
                                                  /*"<servlet>" + 
                                                    "<servlet-name>default</servlet-name>" + 
                                                    "<servlet-class>org.eclipse.jetty.servlet.DefaultServlet</servlet-class>" + 
                                                    "<init-param>" + 
                                                      "<param-name>aliases</param-name>" + 
                                                      "<param-value>false</param-value>" + 
                                                    "</init-param>" + 
                                                    "<init-param>" + 
                                                      "<param-name>acceptRanges</param-name>" + 
                                                      "<param-value>true</param-value>" + 
                                                    "</init-param>" + 
                                                    "<init-param>" + 
                                                      "<param-name>dirAllowed</param-name>" + 
                                                      "<param-value>true</param-value>" + 
                                                    "</init-param>" + 
                                                    "<init-param>" + 
                                                      "<param-name>welcomeServlets</param-name>" + 
                                                      "<param-value>false</param-value>" + 
                                                    "</init-param>" + 
                                                    "<init-param>" + 
                                                      "<param-name>redirectWelcome</param-name>" + 
                                                      "<param-value>false</param-value>" + 
                                                    "</init-param>" + 
                                                    "<init-param>" + 
                                                      "<param-name>maxCacheSize</param-name>" + 
                                                      "<param-value>256000000</param-value>" + 
                                                    "</init-param>" + 
                                                    "<init-param>" + 
                                                      "<param-name>maxCachedFileSize</param-name>" + 
                                                      "<param-value>200000000</param-value>" + 
                                                    "</init-param>" + 
                                                    "<init-param>" + 
                                                      "<param-name>maxCachedFiles</param-name>" + 
                                                      "<param-value>2048</param-value>" + 
                                                    "</init-param>" + 
                                                    "<init-param>" + 
                                                      "<param-name>gzip</param-name>" + 
                                                      "<param-value>true</param-value>" + 
                                                    "</init-param>" + 
                                                    "<init-param>" + 
                                                      "<param-name>useFileMappedBuffer</param-name>" + 
                                                      "<param-value>false</param-value>" + 
                                                    "</init-param>" + 
                                                    "<load-on-startup>0</load-on-startup>" + 
                                                  "</servlet>" + 
                                                  "<servlet-mapping>" + 
                                                    "<servlet-name>default</servlet-name>" + 
                                                    "<url-pattern>/</url-pattern>" + 
                                                  "</servlet-mapping>" +*/ 
                                                  // end
                                            "</web-app>";
}
