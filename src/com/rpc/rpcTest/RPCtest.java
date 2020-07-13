package com.rpc.rpcTest;

//import com.rpc.Client.Client;
import com.rpc.Client.RPCClient;
import com.rpc.Server.Server;
import com.rpc.Server.ServiceCenter;
import com.rpc.Service.HelloService;
import com.rpc.Service.HelloServiceImpl;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * 文件注释
 *
 * @author Jiajun.Xu
 **/
public class RPCtest {
    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Server serviceServer = new ServiceCenter(20006);
                    serviceServer.register(HelloService.class, HelloServiceImpl.class);
                    serviceServer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        HelloService helloService = RPCClient.getRemoteProxyObj(HelloService.class, new InetSocketAddress("127.0.0.1", 20006));
        System.out.println(helloService.sayHello("friend"));
    }
}