package Client;


import Message.RpcRequest;
import Message.RpcResponse;
import Register.ServiceDiscovery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * 文件注释
 *
 * @author Jiajun.Xu
 **/
public class RpcProxy {

    private String serverAddress;
    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public RpcProxy(String serverAddress, ServiceDiscovery serviceDiscovery) {
        this.serverAddress = serverAddress;
        this.serviceDiscovery = serviceDiscovery;
    }

    public <T> T getRemoteProxyObj(Class<T> interfaceClass){
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 创建RPC请求
                RpcRequest request = new RpcRequest();
                request.setRequestId(UUID.randomUUID().toString());
                request.setInterfaceName(method.getDeclaringClass().getName());
                request.setMethodName(method.getName());
                request.setParameterTypes(method.getParameterTypes());
                request.setParameters(args);
                if (serviceDiscovery != null) {
                    // 发现服务
                    serverAddress = serviceDiscovery.discover();
                }
                String[] addr = serverAddress.split(":");
                String host = addr[0];
                int port = Integer.parseInt(addr[1]);
                // 初始化RPC客户端
                RpcClient client = new RpcClient(host, port);
                // 通过RPC客户端发送RPC请求并获取RPC响应
                RpcResponse response = client.send(request);
                if (response.isError()){
                    throw response.getError();
                } else {
                    return response.getResult();
                }
            }
        });
    }
}
