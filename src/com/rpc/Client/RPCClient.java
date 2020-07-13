package com.rpc.Client;

import com.rpc.Service.HelloService;
import org.ietf.jgss.Oid;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 文件注释
 *
 * @author Jiajun.Xu
 **/
public class RPCClient<T> {
    public static<T> T getRemoteProxyObj(final Class<?> serviceInterface, final InetSocketAddress addr){
        return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[]{serviceInterface}, new DynProxy(serviceInterface, addr));
    }


    private static class DynProxy implements InvocationHandler{
        // 接口
        private final Class<?> serviceInterface;
        // 调用远程地址
        private final InetSocketAddress addr;

        public DynProxy(Class<?> serviceInterface, InetSocketAddress addr) {
            this.serviceInterface = serviceInterface;
            this.addr = addr;
        }

        // 动态代理类

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Socket socket = null;
            ObjectInputStream objectInputStream = null;
            ObjectOutputStream objectOutputStream = null;
            try {
                socket = new Socket();
                socket.connect(addr);
                // 往远端发送数据，按照顺序发送：类名、方法名、参数类型、参数值
                // 拿到对象输出流
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                // 发送调用方法的类名
                objectOutputStream.writeUTF(serviceInterface.getName());
                // 发送方法名
                objectOutputStream.writeUTF(method.getName());
                // 发送参数对象，使用Object
                objectOutputStream.writeObject(method.getParameterTypes());
                // 发送参数值
                objectOutputStream.writeObject(args);
                // 刷新缓冲区
                objectOutputStream.flush();

                // 同步阻塞拿到远程服务器执行结果
                objectInputStream = new ObjectInputStream(socket.getInputStream());
                System.out.println(serviceInterface.getName() + "远程调用成功！");
                // 返回网络请求
                return objectInputStream.readObject();
            } finally {
                // 关闭所有连接
                if (socket != null)
                    socket.close();
                if (objectInputStream != null)
                    objectInputStream.close();
                if (objectOutputStream != null)
                    objectOutputStream.close();
            }


        }
    }
}
