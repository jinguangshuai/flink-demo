package com.example.demo.config;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.nio.charset.Charset;
/**
 * @Auther：jinguangshuai
 * @Data：2023/9/1 - 09 - 01 - 17:25
 * @Description:com.example.demo.config
 * @version:1.0
 */

public class CharsetCustomNewSerializer extends Serializer {
    @Override
    public void write(Kryo kryo, Output output, Object object) {
        Charset charset = (Charset) object;
        kryo.writeObject(output, charset.name());
    }

    @Override
    public Object read(Kryo kryo, Input input, Class aClass) {
        return Charset.forName(kryo.readObject(input, String.class));
    }
}
