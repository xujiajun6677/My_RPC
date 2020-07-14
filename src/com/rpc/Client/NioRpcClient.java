package com.rpc.Client;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 文件注释
 *
 * @author Jiajun.Xu
 **/

public class NioRpcClient<T> {
    public static <T> T getRemoteProxyObj(final Class<?> serviceInterface, final InetSocketAddress addr) {
        return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[]{serviceInterface}, new DynProxy(serviceInterface, addr));
    }


    private static class DynProxy implements InvocationHandler {
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
            SocketChannel channel;
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            Selector selector = null;
            System.out.println("启动客户端!!!");
            selector = Selector.open();
            channel = SocketChannel.open(addr);
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
            String methodName = method.getName();
            String clazzName = serviceInterface.getSimpleName();
            Object result = null;
            if (args == null || args.length == 0) {  // 表示没有参数 它传递的类型// 接口名/方法名()
                channel.write(ByteBuffer.wrap((clazzName + "/" + methodName + "()").getBytes()));
            } else {
                int size = args.length;
                String[] types = new String[size];
                StringBuffer content = new StringBuffer(clazzName).append("/").append(methodName).append("(");
                for (int i = 0; i < size; i++) {
                    types[i] = args[i].getClass().getName();
                    content.append(types[i]).append(" ").append(args[i]);
                    if (i != size - 1)
                        content.append(",");
                }
                content.append(")");
                channel.write(ByteBuffer.wrap(content.toString().getBytes()));
            }
            while (selector.select() > 0) {
                Iterator<SelectionKey> iteKeys = selector.selectedKeys().iterator();
                while (iteKeys.hasNext()) {
                    SelectionKey key = iteKeys.next();
                    iteKeys.remove();
                    System.out.println("开始处理请求！！！");
                    if (key.isReadable()) {
                        channel = (SocketChannel) key.channel();
                        buffer.clear();
                        int flag = channel.read(buffer);
                        System.out.println(flag);
                        int postion = buffer.position();    // 获取大小
                        byte[] data = buffer.array();
                        String msg = new String(data, 0, postion).trim();
                        buffer.clear();
                        if (msg.endsWith("null") || msg.endsWith("NULL"))
                            result = null;
                        String[] typeValue = msg.split(":");
                        String type = typeValue[0];
                        String value = typeValue[1];
                        if (type.contains("Integer") || type.contains("int"))
                            result = Integer.parseInt(value);
                        else if (type.contains("Float") || type.contains("float"))
                            result = Float.parseFloat(value);
                        else if (type.contains("Double") || type.contains("double"))
                            result = Double.parseDouble(value);
                        else if (type.contains("Long") || type.contains("long"))
                            result = Long.parseLong(value);
                        else
                            result = value;
                    }
                }
            }
            return result;
        }
    }
}




//public class NioRpcClient {
//    private SocketChannel channel;
//    private ByteBuffer buffer = ByteBuffer.allocate(1024);
//    private static NioRpcClient nioRpcClient = new NioRpcClient();
//    private Selector selector = null;
//
//    private NioRpcClient(){
//
//    }
//
//    public static NioRpcClient getNioRpcClient(){
//        return nioRpcClient;
//    }
//
//    public NioRpcClient init(String addr, int port) throws IOException {
//        System.out.println("启动客户端!!!");
//        selector = Selector.open();
//        InetSocketAddress inetSocketAddress = new InetSocketAddress(addr, port);
//        channel = SocketChannel.open();
//        channel.configureBlocking(false);
//        channel.register(selector, SelectionKey.OP_READ);
//        return this;
//    }
//
//    // 获得代理
//    public Object getRemoteProxyObj(final Class clazz){
//        return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
//            @Override
//            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                String methodName = method.getName();
//                String clazzName = clazz.getSimpleName();
//                Object result = null;
//                if (args == null || args.length == 0){  // 表示没有参数 它传递的类型// 接口名/方法名()
//                    channel.write(ByteBuffer.wrap((clazzName + "/" + methodName + "()").getBytes()));
//                }else {
//                    int size = args.length;
//                    String[] types = new String[size];
//                    StringBuffer content = new StringBuffer(clazzName).append("/").append(methodName).append("(");
//                    for (int i = 0; i < size; i++){
//                        types[i] = args[i].getClass().getName();
//                        content.append(types[i]).append(" ").append(args[i]);
//                        if (i != size - 1)
//                            content.append(",");
//                    }
//                    content.append(")");
//                    channel.write(ByteBuffer.wrap(content.toString().getBytes()));
//                }
//                result = getResult();
//                return result;
//            }
//        });
//    }
//
//    private Object getResult() throws IOException {
//        while (selector.select() > 0) {
//            Iterator<SelectionKey> iteKeys = selector.selectedKeys().iterator();
//            while (iteKeys.hasNext()) {
//                SelectionKey key = iteKeys.next();
//                iteKeys.remove();
//                System.out.println("开始处理请求！！！");
//                if (key.isReadable()) {
//                    SocketChannel socketChannel = (SocketChannel) key.channel();
//                    buffer.clear();
//                    socketChannel.read(buffer);
//                    int postion = buffer.position();    // 获取大小
//                    byte[] data = buffer.array();
//                    String msg = new String(data, 0, postion).trim();
//                    buffer.clear();
//                    if (msg.endsWith("null") || msg.endsWith("NULL"))
//                        return null;
//                    String[] typeValue = msg.split(":");
//                    String type = typeValue[0];
//                    String value = typeValue[1];
//                    if (type.contains("Integer") || type.contains("int"))
//                        return Integer.parseInt(value);
//                    else if (type.contains("Float") || type.contains("float"))
//                        return Float.parseFloat(value);
//                    else if (type.contains("Double") || type.contains("double"))
//                        return Double.parseDouble(value);
//                    else if (type.contains("Long") || type.contains("long"))
//                        return Long.parseLong(value);
//                    else
//                        return value;
//                }
//            }
//        }
//        return null;
//    }
//}
