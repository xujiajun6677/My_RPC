package com.rpc.Client;

import com.rpc.Service.HelloService;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * 文件注释
 *
 * @author Jiajun.Xu
 **/
public class MyNioClient {
    public static void main(String[] args) throws IOException {
        HelloService helloService = NioRpcClient.getRemoteProxyObj(HelloService.class, new InetSocketAddress("127.0.0.1", 20007));
        System.out.println("客户端：" + helloService.sayHello("xujiajun"));
    }
}
