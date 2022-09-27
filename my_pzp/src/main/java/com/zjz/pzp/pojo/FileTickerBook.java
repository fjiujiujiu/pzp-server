package com.zjz.pzp.pojo;

import lombok.Data;

/**
 * @author zjz
 * @date 2022/9/22
 * 用来把ticker 捆在一起
 */
@Data
public class FileTickerBook {
    private FileTicketBean ticketBean;
    private String filePath;

    public FileTickerBook(FileTicketBean ticketBean, String filePath) {
        this.ticketBean = ticketBean;
        this.filePath = filePath;
    }

    public FileTickerBook() {
    }

    public String getRecipient() {
        return ticketBean.getRecipientName();
    }
    public String getSender() {
        return ticketBean.getSenderName();
    }

}
