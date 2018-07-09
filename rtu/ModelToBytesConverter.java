package com.wgkj.rtucontrol.rtu;

import com.wgkj.rtucontrol.utils.HexStringUtils;

import java.lang.reflect.Field;

/**
 * Created by wgkj003 on 2018/4/9.
 */

public class ModelToBytesConverter {
    public static byte[] convert( Object object)
    {
        Field[] fields = object.getClass().getDeclaredFields();
        byte[] data = new byte[0];
        for(Field field : fields) {
            String name = field.getName();
            try {
                int value = (int)field.get(object);
                data = HexStringUtils.addBytes( data, HexStringUtils.getHighLow(value));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return data;
    }
}
