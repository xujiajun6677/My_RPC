package com.rpc.Service;

/**
 * 文件注释
 *
 * @author Jiajun.Xu
 **/
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
        return "hello" + name;
    }
}
