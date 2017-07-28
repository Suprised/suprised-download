package com.suprised.jetty.server;

import java.util.Map;

public class ServiceInstanceBean {
    public static final short NOW_SERVICE = 0; // 当前应用服务
    public static final short THREE_UPLOAD_SERVICE = 1; // 第三方上传服务
    public static final short THREE_DOWNLOAD_SERVICE = 2; // 第三方下载服务
    public static final short THREE_DEMAND_SERVICE = 4; // 第三方点播服务
    public static final short MSOFFICE_SERVICE  = 8; // MSOffice文档在线编辑服务
    
    private short type = 1;
    private String protocol="ftp";
    private String address;
    private int port;
    private String portStr;
    private String virPath;
    private String account;
    private String password;
    private String extNames;
    private Map<String, String> serverNamesMap;
    private boolean enabled = false;
    private boolean localServer = false;
    private String storageKey;
    private short source = 1;// 0-内嵌 1-第三方
    
    private String resourcePath;
    
    private long useCount = 0;// 使用次数

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public ServiceInstanceBean() {
        super();
    }

    public ServiceInstanceBean(String key, short type) {
        setType(type);
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAddress() {
        return address;
    }
    
    public String getRealAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public String getPortStr() {
        return portStr;
    }

    public void setPortStr(String portStr) {
        this.portStr = portStr;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setVirPath(String virPath) {
        this.virPath = virPath;
    }
    
    public String getVirPath() {
        return virPath;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getServerNamesMap() {
        return serverNamesMap;
    }

    public void setServerNamesMap(Map<String, String> serverNamesMap) {
        this.serverNamesMap = serverNamesMap;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getExtNames() {
        return extNames;
    }

    public void setExtNames(String extNames) {
        this.extNames = extNames;
    }

    public boolean isLocalServer() {
        return localServer;
    }

    public void setLocalServer(boolean localServer) {
        this.localServer = localServer;
    }
    
    public short getSource() {
        return source;
    }

    public void setSource(short source) {
        this.source = source;
    }

    public long getUseCount() {
        return useCount;
    }

    public void setUseCount(long useCount) {
        this.useCount = useCount;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

}
