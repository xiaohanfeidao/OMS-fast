package com.lgy.oms.factory;

import com.lgy.common.constant.Constants;
import com.lgy.common.utils.DateUtils;
import com.lgy.common.utils.StringUtils;
import com.lgy.oms.disruptor.tracelog.TraceLogDisruptorUtil;
import com.lgy.oms.domain.StrategyConvert;
import com.lgy.oms.domain.dto.TradeParamDTO;
import com.lgy.oms.domain.order.*;
import com.lgy.oms.enums.order.*;
import com.lgy.oms.interfaces.common.dto.standard.StandardOrder;
import com.lgy.oms.interfaces.common.dto.standard.StandardOrderDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Description OrderMainFactory
 * @Author LGy
 * @Date 2020/6/18 10:28
 **/
public class OrderMainFactory {

    private static Logger log = LoggerFactory.getLogger(OrderMainFactory.class);

    public static OrderMain convert(StandardOrder standardOrder, StrategyConvert strategy, TradeParamDTO param) {

        //自动触发转换
        boolean auto = param != null && param.getAuto();
        //是否存在退款明细
        standardOrder.setExist_refund(false);

        /** 订单主信息 */
        OrderMain orderMain = new OrderMain();
        //订单流水编号,调用保存时再生成
        orderMain.setOrderId(StringUtils.EMPTY);
        //来源单号
        orderMain.setSourceId(standardOrder.getTid());
        //店铺编码
        orderMain.setShop(standardOrder.getShop());
        //平台编码
        orderMain.setPlatform(standardOrder.getPlatform());
        //货主编码
        orderMain.setOwner(standardOrder.getOwner());
        //订单是否冻结
        orderMain.setFrozen(Constants.NO);
        //是否参与活动
        orderMain.setActive(Constants.NO);
        //是否人工编辑
        orderMain.setHandEdit(Constants.NO);
        //是否拦截
        orderMain.setIntercept(Constants.NO);
        //是否售后
        orderMain.setAfterSales(Constants.NO);
        //是否已经开发票
        orderMain.setInvoice(Constants.NO);
        //是否用户锁定
        orderMain.setOrderLock(Constants.NO);
        //锁定人编码
        orderMain.setLockUser(StringUtils.EMPTY);
        //锁定时间
        orderMain.setLockTime(StringUtils.EMPTY);
        //订单标记
        orderMain.setMark(StringUtils.EMPTY);
        //标记内容
        orderMain.setMarkContent(StringUtils.EMPTY);
        //卖家备注旗帜
        orderMain.setSellerFlag(standardOrder.getSeller_flag());
        //尺码类型
        orderMain.setSizeType(0);
        //sku种类数量
        orderMain.setSkuNum(0);
        //总件数
        orderMain.setQty(0);
        //生成配货单数量
        orderMain.setDistributionQty(0);
        //商品编码集合
        orderMain.setCommodity(StringUtils.EMPTY);
        //总体积
        orderMain.setVolume(BigDecimal.ZERO);
        //总重量
        orderMain.setVolume(BigDecimal.ZERO);
        //发货仓库编码
        orderMain.setWarehouse(standardOrder.getWarehouse());
        //物流商编码
        orderMain.setLogistics(standardOrder.getLogistics());
        //快递单号
        orderMain.setExpressNumber(standardOrder.getExpress_number());
        //发货时间
        if (StringUtils.isNotBlank(standardOrder.getEst_con_time())) {
            try {
                orderMain.setSendoutTime(DateUtils.parseDate(standardOrder.getEst_con_time()));
            } catch (Exception e) {
                log.error("日期[{}]转换格式错误", standardOrder.getEst_con_time(), e);
            }
        }
        //备注
        orderMain.setRemark(StringUtils.EMPTY);

        orderStatusConvert(orderMain, standardOrder);
        orderPayConvert(orderMain, standardOrder);
        orderTypeConvert(orderMain, standardOrder);
        orderDetailConvert(orderMain, standardOrder, param);

        //是否存在退款明细
        orderMain.setRefund(standardOrder.getExist_refund() ? Constants.YES : Constants.NO);

        if (auto) {
            orderMain.setCreateBy(Constants.SYSTEM);
        }

        return orderMain;
    }

    /**
     * 组装订单明细信息
     *
     * @param orderMain     主订单信息
     * @param standardOrder 标准订单
     * @param param         请求参数
     */
    private static void orderDetailConvert(OrderMain orderMain, StandardOrder standardOrder, TradeParamDTO param) {

        //生成退款明细
        boolean refund = param != null && param.getRefund();

        //订单明细信息
        List<OrderDetail> orderDetails = new ArrayList<>(standardOrder.getOrderDetails().size());
        //行序号
        int rowNumber = 1;
        for (StandardOrderDetail standardDetail : standardOrder.getOrderDetails()) {

            //明细退款状态
            if (!OrderDetailRefundStatusEnum.NO_REFUND.name().equals(standardDetail.getRefund_status())) {
                //存在退款明细
                standardOrder.setExist_refund(true);
                //是否生成退款订单
                if (!refund) {
                    continue;
                }
            }

            OrderDetail orderDetail = new OrderDetail();
            //订单编号
            orderDetail.setOrderId(StringUtils.EMPTY);
            //来源编号
            orderDetail.setSourceId(standardOrder.getTid());
            //发货仓库编码
            orderDetail.setWarehouse(standardDetail.getStore_code());
            //物流商编码
            orderDetail.setLogistics(standardOrder.getLogistics());
            //快递单号
            orderDetail.setExpressNumber(standardDetail.getInvoice_no());
            //行序号
            orderDetail.setRowNumber(rowNumber + "");
            //来源行序号
            orderDetail.setSourceRow(rowNumber + "");
            //商品编码
            orderDetail.setCommodity(StringUtils.EMPTY);
            //数量
            orderDetail.setQty(Integer.parseInt(standardDetail.getNum()));
            //商品名称
            orderDetail.setTitle(standardDetail.getTitle());
            //平台子订单编号
            orderDetail.setOid(standardDetail.getOid());
            //退款状态
            orderDetail.setRefundStatus(OrderDetailRefundStatusEnum.convert(standardDetail.getRefund_status()));
            //商品类型
            orderDetail.setType(OrderDetailTypeEnum.DEFAULT.getCode());
            //商品图片绝对路径
            orderDetail.setPicPath(standardDetail.getPic_path());
            //商品数字ID
            orderDetail.setNumIid(standardDetail.getNum_iid());
            //商家外部编码
            orderDetail.setOuterIid(standardDetail.getOuter_iid());
            //平台skuID
            orderDetail.setSkuId(standardDetail.getSku_id());
            //外部网店自己定义的Sku编号
            orderDetail.setOuterSkuId(standardDetail.getOuter_sku_id());
            //销售单价
            orderDetail.setPrice(standardDetail.getPrice() != null
                    ? new BigDecimal(standardDetail.getPrice()) : BigDecimal.ZERO);
            //应付金额
            orderDetail.setTotalFee(standardDetail.getTotal_fee() != null
                    ? new BigDecimal(standardDetail.getTotal_fee()) : BigDecimal.ZERO);
            //实付金额
            orderDetail.setPayment(standardDetail.getTotal_fee() != null
                    ? new BigDecimal(standardDetail.getTotal_fee()) : BigDecimal.ZERO);
            //分摊之后的实付金额
            orderDetail.setDivideOrderFee(standardDetail.getDivide_order_fee() != null
                    ? new BigDecimal(standardDetail.getDivide_order_fee()) : BigDecimal.ZERO);
            //尺寸
            orderDetail.setSize(StringUtils.EMPTY);
            //商品条码
            orderDetail.setBarCode(StringUtils.EMPTY);
            //品牌
            orderDetail.setBrand(StringUtils.EMPTY);
            //活动编码
            orderDetail.setActive(StringUtils.EMPTY);
            orderDetails.add(orderDetail);
            rowNumber++;
        }
        orderMain.setOrderDetails(orderDetails);

    }

    /**
     * 组装订单业务信息
     *
     * @param orderMain     主订单信息
     * @param standardOrder 标准订单
     */
    private static void orderTypeConvert(OrderMain orderMain, StandardOrder standardOrder) {

        //订单业务类型信息
        OrderTypeInfo orderTypeinfo = new OrderTypeInfo();
        //订单编码
        orderTypeinfo.setOrderId(StringUtils.EMPTY);
        //来源单号
        orderTypeinfo.setSourceId(standardOrder.getTid());
        //目标单号
        orderTypeinfo.setAimId(standardOrder.getTid());
        //来源类型
        orderTypeinfo.setSourceType(OrderSourceTypeEnum.TRADE.getCode());

        //发货类型:正常发货
        orderTypeinfo.setDeliveryType(OrderSendOutTypeEnum.NORMAL_DELIVER.getCode());
        if (Objects.nonNull(standardOrder.getPlatform_send()) && standardOrder.getPlatform_send()) {
            //发货类型:平台发货
            orderTypeinfo.setDeliveryType(OrderSendOutTypeEnum.PLATFORM_DELIVER.getCode());
        }

        //出库类型:一般交易出库
        orderTypeinfo.setOutboundType(OrderOutBoundTypeEnum.JYCK.getCode());

        //货到付款
        orderTypeinfo.setCod(Integer.valueOf(Constants.NO));
        if (Objects.nonNull(standardOrder.getCod()) && standardOrder.getCod()) {
            orderTypeinfo.setCod(Integer.valueOf(Constants.YES));
        }

        //是否存在发票申请
        orderTypeinfo.setInvoice(Integer.valueOf(Constants.NO));
        if (Objects.nonNull(standardOrder.getInvoice()) && standardOrder.getInvoice()) {
            orderTypeinfo.setInvoice(Integer.valueOf(Constants.YES));
        }

        //发货级别:普通
        orderTypeinfo.setLevel(OrderSendOutLevelEnum.GENERAL.getCode());
        orderMain.setOrderTypeinfo(orderTypeinfo);
    }

    /**
     * 组装订单支付信息
     *
     * @param orderMain     主订单信息
     * @param standardOrder 标准订单
     */
    private static void orderPayConvert(OrderMain orderMain, StandardOrder standardOrder) {
        //订单支付信息
        OrderPayInfo orderPayinfo = new OrderPayInfo();
        //订单编码
        orderPayinfo.setOrderId(StringUtils.EMPTY);
        //来源单号
        orderPayinfo.setSourceId(standardOrder.getTid());
        //下单时间
        orderPayinfo.setOrderTime(standardOrder.getCreated());
        //支付时间
        orderPayinfo.setPayTime(standardOrder.getPay_time());
        //币别
        orderPayinfo.setCurrency(standardOrder.getCurrency());
        //订单金额
        orderPayinfo.setOrderAmount(standardOrder.getTotal_fee() != null
                ? new BigDecimal(standardOrder.getTotal_fee()) : BigDecimal.ZERO);
        //支付金额
        orderPayinfo.setPayAmount(standardOrder.getPayment() != null
                ? new BigDecimal(standardOrder.getPayment()) : BigDecimal.ZERO);
        //实收金额
        orderPayinfo.setReceivedAmount(standardOrder.getPayment() != null
                ? new BigDecimal(standardOrder.getPayment()) : BigDecimal.ZERO);
        //折扣
        orderPayinfo.setDiscount(standardOrder.getDiscount_fee() != null
                ? new BigDecimal(standardOrder.getDiscount_fee()) : BigDecimal.ZERO);
        //税额
        orderPayinfo.setTaxAmount(standardOrder.getOrder_tax_fee() != null
                ? new BigDecimal(standardOrder.getOrder_tax_fee()) : BigDecimal.ZERO);
        //邮费
        orderPayinfo.setPost(standardOrder.getPost_fee() != null
                ? new BigDecimal(standardOrder.getPost_fee()) : BigDecimal.ZERO);
        //运费
        orderPayinfo.setFreightAmount(BigDecimal.ZERO);
        orderMain.setOrderPayinfo(orderPayinfo);
    }

    /**
     * 组装订单状态
     *
     * @param orderMain     主订单信息
     * @param standardOrder 标准订单
     */
    private static void orderStatusConvert(OrderMain orderMain, StandardOrder standardOrder) {
        //订单状态信息
        OrderStatusInfo orderStatusInfo = new OrderStatusInfo();
        //订单流水编号
        orderStatusInfo.setOrderId(StringUtils.EMPTY);
        //来源单号
        orderStatusInfo.setSourceId(standardOrder.getTid());
        //订单状态:新增
        orderStatusInfo.setFlag(OrderFlagEnum.NEW.getCode());
        //合并状态
        orderStatusInfo.setMerger(OrderMergeEnum.WAIT.getCode());
        //拆分状态
        orderStatusInfo.setSplit(OrderSplitEnum.WAIT.getCode());
        //订单状态:有效
        orderStatusInfo.setStatus(Constants.VALID);
        orderMain.setOrderStatusinfo(orderStatusInfo);

        //订单买家信息
        OrderBuyerInfo orderBuyer = new OrderBuyerInfo();
        //订单编码
        orderBuyer.setOrderId(StringUtils.EMPTY);
        //来源单号
        orderBuyer.setSourceId(standardOrder.getTid());
        //买家ID
        orderBuyer.setBuyerId(standardOrder.getBuyer_nick());
        //买家姓名
        orderBuyer.setBuyerName(standardOrder.getBuyer_name());
        //买家电话
        orderBuyer.setBuyerPhone(standardOrder.getBuyer_phone());
        //买家邮件地址
        orderBuyer.setBuyerEmail(standardOrder.getBuyer_email());
        //买家身份证号
        orderBuyer.setBuyerCardId(standardOrder.getBuyer_card_id());
        //收件人姓名
        orderBuyer.setConsigneeName(standardOrder.getReceiver_name());
        //收件人移动电话
        orderBuyer.setConsigneeMobile(standardOrder.getReceiver_mobile());
        //收件人邮箱地址
        orderBuyer.setConsigneeEmail(standardOrder.getReceiver_email());
        //收件人身份证号
        orderBuyer.setConsigneeCardId(standardOrder.getReceiver_card_id());
        //收件人国家编码
        orderBuyer.setNationCode(StringUtils.EMPTY);
        //收件人国家
        orderBuyer.setNation(standardOrder.getReceiver_country());
        //收件人省份编码
        orderBuyer.setProvinceCode(StringUtils.EMPTY);
        //收件人省/州
        orderBuyer.setProvince(standardOrder.getReceiver_state());
        //收件人城市编码
        orderBuyer.setCityCode(StringUtils.EMPTY);
        //收件人市
        orderBuyer.setCity(standardOrder.getReceiver_city());
        //收件人区编码
        orderBuyer.setDistrictCode(StringUtils.EMPTY);
        //收件人区
        orderBuyer.setDistrict(standardOrder.getReceiver_district());
        //详细地址
        orderBuyer.setAddress(standardOrder.getReceiver_address());
        //邮政编码
        orderBuyer.setZipCode(standardOrder.getReceiver_zip());
        //买家留言
        orderBuyer.setBuyerMessage(standardOrder.getBuyer_message());
        //卖家留言
        orderBuyer.setSellerMessage(standardOrder.getSeller_memo());
        orderMain.setOrderBuyerinfo(orderBuyer);
    }

}
