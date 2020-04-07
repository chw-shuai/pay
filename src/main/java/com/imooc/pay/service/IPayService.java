package com.imooc.pay.service;

import com.imooc.pay.pojo.PayInfo;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;

import java.math.BigDecimal;

/**
 * @author 常红伟
 */
public interface IPayService {

    /**
     * 创建
     * @param orderId
     * @param amount
     * @param bestPayTypeEnum
     * @return
     */
    PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum);

    /**
     * 异步通知处理
     * @param notifyData
     * @return
     */
    String asyncNotify(String notifyData);

    /**
     * 根据orderId查询订单
     * @param orderId
     * @return
     */
    PayInfo queryByOrderId(String orderId);
}
