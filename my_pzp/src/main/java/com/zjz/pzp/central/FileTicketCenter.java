package com.zjz.pzp.central;

import com.zjz.pzp.pojo.FileTickerBook;
import com.zjz.pzp.pojo.FileTicketBean;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zjz
 * @date 2022/9/21
 * file 单号文件
 */
public class FileTicketCenter {
    /**
     * fileCode ,path
     */
    public static volatile Map<Integer, FileTickerBook> FILE_CODE_FILE_BOOK_CENTER = new HashMap<>();

    public static volatile Map<Integer, FileTicketBean> FILE_TICKER_CENTER = new HashMap<>();
}
