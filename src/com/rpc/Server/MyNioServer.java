package com.rpc.Server;

import com.rpc.Service.HelloService;
import com.rpc.Service.HelloServiceImpl;

import java.io.IOException;

/**
 * 文件注释
 *
 * @author Jiajun.Xu
 **/
public class MyNioServer {
    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    NioServerService nioServerService = new NioServerService(20007);
                    nioServerService.register(HelloService.class, HelloServiceImpl.class);
                    nioServerService.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
