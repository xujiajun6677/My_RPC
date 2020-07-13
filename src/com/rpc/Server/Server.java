package com.rpc.Server;

import java.io.IOException;

/**
 * 文件注释
 *
 * @author Jiajun.Xu
 **/
public interface Server {

    public void stop();

    public void start() throws IOException;

    public void register(Class serviceInterface, Class impl);

    public boolean isRunning();

    public int getPort();
}
