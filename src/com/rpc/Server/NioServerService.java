package com.rpc.Server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 文件注释
 *
 * @author Jiajun.Xu
 **/
public class NioServerService {
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static int port;
    // 停止标志
    private static boolean isRunning = false;
//    private static NioServerService insance;
//    static {
//        try {
//            insance = new NioServerService(port);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    // 定义注册中心的静态对象
    private static final HashMap<String, Object> serviceRegistry = new HashMap<String, Object>();
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public NioServerService(int port) throws IOException {
        this.port = port;
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.configureBlocking(false);
        this.serverSocketChannel.bind(new InetSocketAddress(port));
        this.selector = Selector.open();
        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

//    public static NioServerService getInsance(){
//        return insance;
//    }

    public void stop() {
        isRunning = false;
        executor.shutdown();
    }
    public boolean isRunning() {
        return isRunning;
    }

    public int getPort() {
        return port;
    }

//    public NioServerService addClass(Object object){
//        String name = object.getClass().getInterfaces()[0].getSimpleName();
//        serviceRegistry.put(name,object);
//        return this;
//    }
    public void register(Class serviceInterface, Class impl) {
        serviceRegistry.put(serviceInterface.getName(), impl);
    }

    public void start() throws IOException {
        System.out.println("服务器启动了！！！");
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (selector.select() > 0){
            Iterator<SelectionKey> iteKeys = selector.selectedKeys().iterator();
            while (iteKeys.hasNext()){
                SelectionKey key = iteKeys.next();
                iteKeys.remove();
                System.out.println("开始处理请求！！！");
                if (key.isAcceptable()){
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    // 将key对应的Channel设置成准备接受其他请求
                    key.interestOps(SelectionKey.OP_ACCEPT);
                }else if (key.isReadable()){
                    SocketChannel socketChannel = (SocketChannel) key.channel();
//                    executor.execute(new ServiceTask(buffer, key, socketChannel));
                    try {
                        buffer.clear();
                        socketChannel.read(buffer);
                        int postion = buffer.position();    // 获取大小
                        byte[] data = buffer.array();
                        String msg = new String(data, 0, postion).trim();  // msg包含class,方法名等
                        buffer.clear();
                        String[] clazzData = msg.split("/");
                        String className = clazzData[0];
                        String methodName = clazzData[1].substring(0, clazzData[1].indexOf("("));
                        String typeValuesStr = clazzData[1].substring(clazzData[1].indexOf("(") + 1,
                                clazzData[1].indexOf(")"));
                        String[] typeValues = decodeParamsTypeAndValue(typeValuesStr);
                        Class serviceClass = (Class) serviceRegistry.get(className);
                        Object result = null;
                        if (typeValues == null){
                            Method method = serviceClass.getDeclaredMethod(methodName, null);
                            result = method.invoke(serviceClass.newInstance(), null);
                        }else {
                            Class[] types = new Class[typeValues.length];
                            Object[] values = new Object[typeValues.length];
                            for (int i = 0; i < typeValues.length; i++){
                                String[] tv = typeValues[i].split(" ");
                                String type = tv[0];
                                String value = tv[1];
                                types[i] = Class.forName(type);
                                if (type.contains("Integer") || type.contains("int"))
                                    values[i] = Integer.parseInt(value);
                                else if (type.contains("Float") || type.contains("float"))
                                    values[i] = Float.parseFloat(value);
                                else if (type.contains("Double") || type.contains("double"))
                                    values[i] = Double.parseDouble(value);
                                else if (type.contains("Long") || type.contains("long"))
                                    values[i] = Long.parseLong(value);
                                else
                                    values[i] = value;
                                Method method = serviceClass.getDeclaredMethod(methodName, types);
                                result = method.invoke(serviceClass.newInstance(), values);
                            }
//                    if (result == null) {
//                        result = "Void:null";
//                    } else {
//                        result = result.getClass().getSimpleName() + " " + result;
//                    }
                            // 发送结果
                            socketChannel.write(ByteBuffer.wrap(result.toString().getBytes()));
                            key.interestOps(SelectionKey.OP_READ);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // 它返回的格式是 参数类型：参数值
    private String[] decodeParamsTypeAndValue(String params){
        if (params == null || params.equals(""))
            return null;
        if (!params.contains(","))
            return new String[]{params};
        return params.split(",");
    }

//    // 运行服务
//    private static class ServiceTask implements Runnable{
//        ByteBuffer buffer = null;
//        SelectionKey key = null;
//        SocketChannel socketChannel = null;
//
//        public ServiceTask(ByteBuffer buffer, SelectionKey key, SocketChannel socketChannel) {
//            this.buffer = buffer;
//            this.key = key;
//            this.socketChannel = socketChannel;
//        }
//
//        @Override
//        public void run() {
//            try {
//                buffer.clear();
//                socketChannel.read(buffer);
//                int postion = buffer.position();    // 获取大小
//                byte[] data = buffer.array();
//                String msg = new String(data, 0, postion).trim();  // msg包含class,方法名等
//                buffer.clear();
//                String[] clazzData = msg.split("/");
//                String className = clazzData[0];
//                String methodName = clazzData[1].substring(0, clazzData[1].indexOf("("));
//                String typeValuesStr = clazzData[1].substring(clazzData[1].indexOf("(") + 1,
//                        clazzData[1].indexOf(")"));
//                String[] typeValues = decodeParamsTypeAndValue(typeValuesStr);
//                Class serviceClass = (Class) serviceRegistry.get(className);
//                Object result = null;
//                if (typeValues == null){
//                    Method method = serviceClass.getDeclaredMethod(methodName, null);
//                    result = method.invoke(serviceClass.newInstance(), null);
//                }else {
//                    Class[] types = new Class[typeValues.length];
//                    Object[] values = new Object[typeValues.length];
//                    for (int i = 0; i < typeValues.length; i++){
//                        String[] tv = typeValues[i].split(" ");
//                        String type = tv[0];
//                        String value = tv[1];
//                        types[i] = Class.forName(type);
//                        if (type.contains("Integer") || type.contains("int"))
//                            values[i] = Integer.parseInt(value);
//                        else if (type.contains("Float") || type.contains("float"))
//                            values[i] = Float.parseFloat(value);
//                        else if (type.contains("Double") || type.contains("double"))
//                            values[i] = Double.parseDouble(value);
//                        else if (type.contains("Long") || type.contains("long"))
//                            values[i] = Long.parseLong(value);
//                        else
//                            values[i] = value;
//                        Method method = serviceClass.getDeclaredMethod(methodName, types);
//                        result = method.invoke(serviceClass.newInstance(), values);
//                    }
////                    if (result == null) {
////                        result = "Void:null";
////                    } else {
////                        result = result.getClass().getSimpleName() + " " + result;
////                    }
//                    // 发送结果
//                    socketChannel.write(ByteBuffer.wrap(result.toString().getBytes()));
//                    key.interestOps(SelectionKey.OP_READ);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            } catch (InstantiationException e) {
//                e.printStackTrace();
//            }
//        }
//
//        // 它返回的格式是 参数类型：参数值
//        private String[] decodeParamsTypeAndValue(String params){
//            if (params == null || params.equals(""))
//                return null;
//            if (!params.contains(","))
//                return new String[]{params};
//            return params.split(",");
//        }
//    }
}
