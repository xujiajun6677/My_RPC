package Message;


import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件注释
 *
 * @author Jiajun.Xu
 **/
public class SerializetionUtil {

    private static Map<Class<?>, Schema<?>> cacheSchema = new ConcurrentHashMap<>();
    private static Objenesis objenesis = new ObjenesisStd(true);

    private SerializetionUtil() {
    }



    /**
    * 序列化对象(对象 -> 字节数组)
    * */
    public static <T> byte[] serialize(T obj){
        try {
            Class<T> tClass = (Class<T>) obj.getClass();
            LinkedBuffer linkedBuffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
            Schema<T> schema = getSchema(tClass);
            linkedBuffer.clear();
            return ProtostuffIOUtil.toByteArray(obj, schema, linkedBuffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
    * 反序列化(字节数组 -> 对象)
    * */
    public static <T> T deserilize(byte[] data, Class<T> tClass){
        try {
            T message  = objenesis.newInstance(tClass);
            Schema<T> schema = getSchema(tClass);
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static <T> Schema<T> getSchema(Class<T> tClass){
        Schema<T> schema = (Schema<T>) cacheSchema.get(tClass);
        if (schema == null){
            schema = RuntimeSchema.createFrom(tClass);
            cacheSchema.put(tClass, schema);
        }
        return schema;
    }
}
