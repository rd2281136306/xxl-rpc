package com.xxl.rpc.remoting.provider.impl;

import com.xxl.rpc.registry.ServiceRegistry;
import com.xxl.rpc.remoting.net.NetEnum;
import com.xxl.rpc.remoting.provider.XxlRpcProviderFactory;
import com.xxl.rpc.remoting.provider.annotation.XxlRpcService;
import com.xxl.rpc.serialize.Serializer;
import com.xxl.rpc.util.XxlRpcException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * xxl-rpc provider (for spring)
 *
 * @author xuxueli 2018-10-18 18:09:20
 */
public class XxlRpcSpringProviderFactory extends XxlRpcProviderFactory implements ApplicationContextAware, InitializingBean,DisposableBean {

    // ---------------------- config ----------------------

    private String netType = NetEnum.NETTY.name();
    private String serialize = Serializer.SerializeEnum.HESSIAN.name();

    private int corePoolSize;
    private int maxPoolSize;

    private String ip;          	// for registry
    private int port;				// default port
    private String accessToken;

    private Class<? extends ServiceRegistry> serviceRegistryClass;                          // class.forname
    private Map<String, String> serviceRegistryParam;


    // set
    public void setNetType(String netType) {
        this.netType = netType;
    }

    public void setSerialize(String serialize) {
        this.serialize = serialize;
    }

    @Override
    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    @Override
    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setServiceRegistryClass(Class<? extends ServiceRegistry> serviceRegistryClass) {
        this.serviceRegistryClass = serviceRegistryClass;
    }

    public void setServiceRegistryParam(Map<String, String> serviceRegistryParam) {
        this.serviceRegistryParam = serviceRegistryParam;
    }


    // util
    private void prepareConfig(){

        // prepare config
        NetEnum netTypeEnum = NetEnum.autoMatch(netType, null);
        Serializer.SerializeEnum serializeEnum = Serializer.SerializeEnum.match(serialize, null);
        Serializer serializer = serializeEnum!=null?serializeEnum.getSerializer():null;

        // init config
        super.initConfig(netTypeEnum, serializer, corePoolSize, maxPoolSize, ip, port, accessToken, serviceRegistryClass, serviceRegistryParam);
    }


    // ---------------------- util ----------------------

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(XxlRpcService.class);
        if (serviceBeanMap!=null && serviceBeanMap.size()>0) {
            for (Object serviceBean : serviceBeanMap.values()) {
                // valid
                if (serviceBean.getClass().getInterfaces().length ==0) {
                    throw new XxlRpcException("xxl-rpc, service(XxlRpcService) must inherit interface.");
                }
                // add service
                XxlRpcService xxlRpcService = serviceBean.getClass().getAnnotation(XxlRpcService.class);

                String iface = serviceBean.getClass().getInterfaces()[0].getName();
                String version = xxlRpcService.version();

                super.addService(iface, version, serviceBean);
            }
        }

        // TODO，addServices by api + prop

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.prepareConfig();
        super.start();
    }

    @Override
    public void destroy() throws Exception {
        super.stop();
    }

}
