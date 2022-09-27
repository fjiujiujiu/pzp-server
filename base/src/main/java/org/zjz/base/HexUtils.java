package org.zjz.base;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.digest.BCrypt;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.charset.Charset;

public class HexUtils extends HexUtil {
    public  final static float MIN_VALUE = 0.00001f;

    /*IEEE754转浮点数*/
    public static Float bytes2Float(String hex) {
        return Float.intBitsToFloat(Integer.valueOf(hex, 16));
    }

    public static float bytes2Float(byte b[]) {
        int bits = b[3] & 0xff | (b[2] & 0xff) << 8 | (b[1] & 0xff) << 16
                | (b[0] & 0xff) << 24;

        int sign = ((bits & 0x80000000) == 0) ? 1 : -1;
        int exponent = ((bits & 0x7f800000) >> 23);
        int mantissa = (bits & 0x007fffff);

        mantissa |= 0x00800000;
        float f = (float) (sign * mantissa * Math.pow(2, exponent - 150));
        if (f < MIN_VALUE) {
            f = 0.0f;
        }
        return f;
    }

    public static byte[] highLowHex(byte[] str) {
        if (str.length == 2) {
            return new byte[]{str[1],str[0]};
        }
        return new byte[]{str[2], str[3], str[0], str[1]};
    }


    /**
     * 把16进制字符串转换成字节数组
     *
     * @param hex
     * @return byte[]
     */
    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    public static int getMaxNotZero(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == 0x00) {
                return i;
            }
        }
        return 0;
    }

    public static int getMaxNotZero(byte[] bytes, int offset) {
        int len = 0;
        for (int i = offset; i < bytes.length; i++,len++) {
            if (bytes[i] == 0x00) {
                return len;
            }
        }
        return bytes.length;
    }
    private static int toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    /**
     * 将byte数组转换为string
     *
     * @param bytes
     * @return
     */
    public static final String bytesToString(byte[] bytes) {
        return new String(bytes, Charset.defaultCharset());
    }


    /**
     * 数组转换成十六进制字符串
     *
     * @param bArray
     * @return HexString
     */
    public static final String bytesToHexString(byte[] bArray, String split) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (byte b : bArray) {
            sTemp = Integer.toHexString(0xFF & b);
            if (sTemp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTemp.toUpperCase() + split);
        }
        return sb.toString();
    }

    /**
     * byte 与 int 的相互转换
     *
     * @param x
     * @return
     */
    public static byte intToByte(int x) {
        return (byte) x;
    }

    public static int byteToInt(byte b) {
        //Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
    }

    /**
     * 把一个整数，转成4位表示 如果只取两字节就取byte[2]byte[3]
     *
     * @param intNum
     * @return
     */
    public static byte[] intToByteArray(int intNum) {
        return new byte[]{
                (byte) ((intNum >> 24) & 0xFF),
                (byte) ((intNum >> 16) & 0xFF),
                (byte) ((intNum >> 8) & 0xFF),
                (byte) (intNum & 0xFF)
        };
    }
    /**
     * 把一个整数，转成4位表示 并且取两字节byte[2]byte[3]
     *
     * @param intNum
     * @return
     */
    public static byte[] intToByteArray2(int intNum) {
        byte[] d = intToByteArray(intNum);
        return new byte[]{d[2], d[3]};
    }

    /**
     * 4位字节数组转换为整型
     *
     * @param b
     * @return
     */
    public static int byte2Int(byte[] b) {
        int intValue = 0;
        for (int i = 0; i < b.length; i++) {
            intValue += (b[i] & 0xFF) << (8 * (3 - i));
        }
        return intValue;
    }

    //byte转换为int
    public static int byteToInt2(byte[] b) {

        int mask = 0xff;
        int temp = 0;
        int n = 0;
        for (int i = 0; i < b.length; i++) {
            n <<= 8;
            temp = b[i] & mask;
            n |= temp;
        }
        return n;
    }
//    public static byte[] shortTobytes2(short number) {
//        byte[] abyte = new byte[2];
//        abyte[1] = (byte) (0xff & number);
//        abyte[0] = (byte) ((0xff00 & number) >> 8);
//        return abyte;
//    }

    /**
     * 字符串转化成为16进制字符串
     *
     * @param s
     * @return
     */
    public static String strTo16(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

    /**
     * 16进制转换成为string类型字符串
     *
     * @param s
     * @return
     */
    public static String hexStringToString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "UTF-8");
            new String();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    public static String formatToHexStringWithASCII(byte[] data) {
        StringBuilder str1 = new StringBuilder();
        str1.append("\r\n------------------------------------------------------------------------");
        String str = encodeHexStr(data).toUpperCase();
        str1.append("\r\n"+str);
        str1.append("\r\n------------------------------------------------------------------------");
        return str1.toString();    }

    /**
     * 通过ASCII码将十进制的字节数组格式化为十六进制字符串
     *
     * @param data   十进制的字节数组
     * @param offset 数组下标,标记从数组的第几个字节开始格式化输出
     * @param length 格式长度,其不得大于数组长度,否则抛出java.lang.ArrayIndexOutOfBoundsException
     * @return 格式化后的十六进制字符串
     */
    public static String formatToHexStringWithASCII(byte[] data, int offset, int length) {
        int end = offset + length;
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        sb.append("\r\n------------------------------------------------------------------------");
        boolean chineseCutFlag = false;
        for (int i = offset; i < end; i += 16) {
            sb.append(String.format("\r\n%04X: ", i - offset)); // X或x表示将结果格式化为十六进制整数
            sb2.setLength(0);
            for (int j = i; j < i + 16; j++) {
                if (j < end) {
                    byte b = data[j];
                    if (b >= 0) { // ENG ASCII
                        sb.append(String.format("%02X ", b));
                        if (b < 32 || b > 126) { // 不可见字符
                            sb2.append(" ");
                        } else {
                            sb2.append((char) b);
                        }
                    } else { // CHA ASCII
                        if (j == i + 15) { // 汉字前半个字节
                            sb.append(String.format("%02X ", data[j]));
                            chineseCutFlag = true;
                            String s = new String(data, j, 2);
                            sb2.append(s);
                        } else if (j == i && chineseCutFlag) { // 后半个字节
                            sb.append(String.format("%02X ", data[j]));
                            chineseCutFlag = false;
                            String s = new String(data, j, 1);
                            sb2.append(s);
                        } else {
                            sb.append(String.format("%02X %02X ", data[j], data[j + 1]));
                            String s = new String(data, j, 2);
                            sb2.append(s);
                            j++;
                        }
                    }
                } else {
                    sb.append("   ");
                }
            }
            sb.append("| ");
            sb.append(sb2.toString());
        }
        sb.append("\r\n------------------------------------------------------------------------");
        return sb.toString();
    }



}
