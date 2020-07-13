package com.rpc.Client;

import com.rpc.Service.HelloService;

import java.net.InetSocketAddress;

/**
 * 文件注释
 *
 * @author Jiajun.Xu
 **/
public class MyClient {
    public static void main(String[] args) {
        HelloService helloService = RPCClient.getRemoteProxyObj(HelloService.class, new InetSocketAddress("127.0.0.1", 20006));
        System.out.println(helloService.sayHello("xujj"));
    }
}
