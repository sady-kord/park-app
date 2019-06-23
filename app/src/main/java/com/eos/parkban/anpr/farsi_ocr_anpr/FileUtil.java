package com.eos.parkban.anpr.farsi_ocr_anpr;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public final class FileUtil {
    public static double read_double(FileInputStream fp) throws IOException
    {
        ByteBuffer buf = ByteBuffer.allocate(8);
        fp.read(buf.array(), 0, 8);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return buf.getDouble();
    }

    public static float read_float(FileInputStream fp) throws IOException
    {
        ByteBuffer buf = ByteBuffer.allocate(4);
        fp.read(buf.array(), 0, 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return buf.getFloat();
    }

    public static int read_int(FileInputStream fp) throws IOException
    {
        ByteBuffer buf = ByteBuffer.allocate(4);
        fp.read(buf.array(), 0, 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return buf.getInt();
    }

    public static short read_short(FileInputStream fp) throws IOException
    {
        ByteBuffer buf = ByteBuffer.allocate(2);
        fp.read(buf.array(), 0, 2);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return buf.getShort();
    }

}