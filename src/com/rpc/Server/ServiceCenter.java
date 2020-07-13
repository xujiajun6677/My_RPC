package com.rpc.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 文件注释
 * 服务注册中心
 * @author Jiajun.Xu
 **/
public class ServiceCenter implements Server {
    // 线程池
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    // 定义注册中心的静态对象
    private static final HashMap<String, Class> serviceRegistry = new HashMap<>();
    // 停止标志
    private static boolean isRunning = false;
    // 网络端口
    private static int port;

    public ServiceCenter(int port) {
        this.port = port;
    }

    @Override
    public void stop() {
        isRunning = false;
        executor.shutdown();
    }

    @Override
    public void start() throws IOException {
        ServerSocket server = new ServerSocket(port);
//        server.bind(new InetSocketAddress(port));
        try {
            while (true){
                // 监听客户端的TCP连接，接收到TCP连接后将其封装成ServiceTask,由线程池执行
                executor.execute(new ServiceTask(server.accept()));
            }
        } finally {
            server.close();
        }
    }
    // 服务的注册中心
    @Override
    public void register(Class serviceInterface, Class impl) {
        serviceRegistry.put(serviceInterface.getName(), impl);
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public int getPort() {
        return port;
    }
    // 服务的获取运行
    private static class ServiceTask implements Runnable{
        // 客户端socket
        Socket client = null;

        public ServiceTask(Socket client) {
            this.client = client;
        }
        // 远程请求到达服务端，执行请求结果，使用socket通讯把请求结果返回给客户端
        @Override
        public void run() {
            ObjectInputStream inputStream = null;
            ObjectOutputStream outputStream = null;
            try {
                // 拿到客户端发送的object对象
                inputStream = new ObjectInputStream(client.getInputStream());
                // 顺序发送：类名、方法名 、参数类型、参数值
                String serviceName = inputStream.readUTF();
                String methodName = inputStream.readUTF();
                Class<?>[] parameterTypes = (Class<?>[]) inputStream.readObject();
                Object[] arguments = (Object[]) inputStream.readObject();
                // 注册中心根据接口名拿到实现类
                Class serviceClass = serviceRegistry.get(serviceName);
                System.out.println(serviceName);
                if (serviceClass == null){
                    throw new ClassNotFoundException("not find" + serviceName);
                }
                // 使用反射机制
                Method method = serviceClass.getMethod(methodName, parameterTypes);
                // 反射调用方法
                Object result = method.invoke(serviceClass.newInstance(), arguments);
                // 执行socket返回给客户端
                outputStream = new ObjectOutputStream(client.getOutputStream());
                outputStream.writeObject(result);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null){
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStream != null){
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (client != null){
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
