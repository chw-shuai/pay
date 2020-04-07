package com.imooc.pay.service.impl;

import com.imooc.pay.dao.PayInfoMapper;
import com.imooc.pay.enums.PayPlatformEnum;
import com.imooc.pay.pojo.PayInfo;
import com.imooc.pay.service.IPayService;
import com.lly835.bestpay.enums.BestPayPlatformEnum;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.enums.OrderStatusEnum;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.BestPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class PayService implements IPayService {

    @Autowired
    private BestPayService bestPayService;

    @Autowired
    private PayInfoMapper payInfoMapper;

    /**
     * 创建/发起支付
     * @param orderId
     * @param amount
     */
    @Override
    public PayResponse create(String orderId, BigDecimal amount,BestPayTypeEnum bestPayTypeEnum) {
        //写入数据库
        PayInfo payInfo = new PayInfo(Long.parseLong(orderId),
                PayPlatformEnum.getBybestPayTypeEnum(bestPayTypeEnum).getCode(),
                OrderStatusEnum.NOTPAY.name(),
                amount);
        System.out.println(payInfo.toString());
        payInfoMapper.insertSelective(payInfo);
        PayRequest request = new PayRequest();
        request.setOrderName("8849462-最好的支付sdk");
        request.setOrderId(orderId);
        request.setOrderAmount(amount.doubleValue());
        request.setPayTypeEnum(bestPayTypeEnum);


        PayResponse payResponse= bestPayService.pay(request);
        log.info("发起支付的response={}" ,payResponse);

        return payResponse;
    }

    /**
     * 异步通知处理
     * @param notifyData
     */
    @Override
    public String asyncNotify(String notifyData) {
        //1.签名校验
        PayResponse payResponse =bestPayService.asyncNotify(notifyData);
        log.info("异步通知的payResponse={}",payResponse);

        //2.金额校验（从数据库里面查订单）
        //比较严重（正常情况下不会发生的） 发出告警：钉钉，短信
        PayInfo payInfo= payInfoMapper.selectByOrderNo(Long.parseLong(payResponse.getOrderId()));
        if (payInfo == null){
            //告警
            throw new RuntimeException("通过OrderNo查询的得结果为null");
        }
        //如果订单状态不是已支付在进行金额判断
        if (!payInfo.getPlatformStatus().equals(OrderStatusEnum.SUCCESS.name())){
            if (payInfo.getPayAmount().compareTo(BigDecimal.valueOf(payResponse.getOrderAmount())) !=0){
                //告警
                throw new RuntimeException("异步通知处理中订单金额和数据库里的不一致：orderNo="+payResponse.getOrderId());
            }
            //3.修改订单支付状态
            payInfo.setPlatformStatus(OrderStatusEnum.SUCCESS.name());
            payInfo.setPlatformNumber(payResponse.getOutTradeNo());
            payInfoMapper.updateByPrimaryKeySelective(payInfo);
        }

        // TODO pay发送MQ消息   mall接受MQ消息


        if (payResponse.getPayPlatformEnum() == BestPayPlatformEnum.WX){
            //4.告诉微信不要再次通知
            return "<xml> \n" +
                    "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                    "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                    "</xml> \n";
        }else if (payResponse.getPayPlatformEnum() == BestPayPlatformEnum.ALIPAY){
            //告诉支付宝不再通知
            return "success";
        }
        throw new RuntimeException("异步通知中错误的支付平台");
    }

    @Override
    public PayInfo queryByOrderId(String orderId) {
        PayInfo payInfo= payInfoMapper.selectByOrderNo(Long.parseLong(orderId));
        return payInfo;
    }
}
