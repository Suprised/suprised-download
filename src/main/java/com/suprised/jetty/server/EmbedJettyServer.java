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
 * ��Ƕjetty���ط���
 */
public class EmbedJettyServer {
    
    /**
     * jettyĬ�ϵ�web.xml·��
     */
    private static final String JETTY_WEB_XML_FLODER = "jetty";
    private static final String JETTY_WEB_XML = "/web.xml";
    
    public static final String DEF_JETTY_NAME = "��Ƕ���ط���";
    public static final String DEF_JETTY_DAV_NAME = "��ǶWEBDAV����";
    
    private static EmbedJettyServer embedServer = new EmbedJettyServer();
    
    private Server server = null;
    
    private EmbedJettyServer() {
        // this.server = new Server();
        // this.configServer(server);
    }
    
    /**
     * ���ò���
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
     * ���һ����Ƕ�����ط���
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
     * ������Ƕ���ط���
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
     * �����Ƕjetty��webdav����֧��
     * 
     * @param contextPath
     * @param storagePath
     */
    private void addWebDavSupport(String contextPath, String storagePath) {
        try {
            WebAppContext context = addContextPath(contextPath, storagePath);
            // �ο�tomcat��webdav��ʵ��
            ServletHolder servletHolder = context.addServlet(WebdavServlet.class, "/*");
            // ���²��������ǲο�tomcat��WebdavServletʵ�ֲ������á�
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
            // webdav �����쳣
        }
    }
    
    public boolean isRunning() {
        return server.isRunning();
    }
    
    /**
     * 
     * ���һ��contextPath
     * 
     * @param storagePath �洢����·��
     */
    /*public void addContextPath(String storagePath) throws Exception {
        // ����Ĭ�ϵ�contextPath��ע�⣺����洢������contextPath�����ظ�
        String defaultContextPath = "";
        this.addContextPath(defaultContextPath, storagePath);
    }*/
    
    /**
     * ���һ��contextpath
     */
    private WebAppContext addContextPath(String contextPath, String storagePath) throws Exception {
        WebAppContext context = new WebAppContext();
        // context.setDefaultsDescriptor("/com/dascom/dafc/app/tool/embed/webdefault.xml");
        
        String webXmlPath = getWebXmlPath(storagePath);
        context.setDescriptor(webXmlPath);
        context.setResourceBase(storagePath);
        context.setContextPath(contextPath);
        context.setParentLoaderPriority(true);
        
        // ֧�ֶ��contextpath
        HandlerCollection handlers = (HandlerCollection) server.getHandler();
        if (handlers == null) {
            handlers = new HandlerCollection();
        }
        handlers.addHandler(context);
        
        server.setHandler(handlers);
        
        return context;
    }
    
    /**
     * ÿ���洢�����¶��ᴴ��һ��web.xml ·��: jetty/web.xml
     * 
     * @param storagePath �洢����·��
     * @return web.xml ȫ·��
     */
    private String getWebXmlPath(String storagePath) throws Exception {
        // web.xml �������: ����ֱ�ӿ���һ���յ�web.xml�ļ������õ�·����
        String webXmlFloder = storagePath + File.separator + JETTY_WEB_XML_FLODER;
        File jettyFloder = new File(webXmlFloder);
        if (!jettyFloder.exists()) {// ����jetty�ļ���
            jettyFloder.mkdirs();
        }
        File file = new File(webXmlFloder + JETTY_WEB_XML);
        // ����һ��Ĭ�ϵ�web.xml
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
     * ����jetty����
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
    
    // jetty��Ҫһ��web.xml�ļ������ݿ���Ϊ��
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
                                                  // ����Ĭ�ϵ�servlet������jetty�������ļ� begin 
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
