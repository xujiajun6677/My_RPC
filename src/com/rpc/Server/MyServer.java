package com.rpc.Server;

import com.rpc.Service.HelloService;
import com.rpc.Service.HelloServiceImpl;

import java.io.IOException;

/**
 * 文件注释
 *
 * @author Jiajun.Xu
 **/
public class MyServer {
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
    }
}
