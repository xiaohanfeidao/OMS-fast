package com.lgy.oms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lgy.oms.domain.StrategyAudit;
import com.lgy.oms.domain.StrategyAuditShop;

import java.util.List;

/**
 * 审单策略 服务层
 *
 * @author lgy
 * @date 2019-12-17
 */
public interface IStrategyAuditService extends IService<StrategyAudit> {

    /**
     * 获取策略店铺
     *
     * @param gco 策略编码
     * @return
     */
    List<StrategyAuditShop> getStrategyShop(String gco);

    /**
     * 更改策略店铺是否开启自动
     *
     * @param id   关系ID
     * @param auto 是否开启自动
     * @return
     */
    boolean changeAuto(Long id, String auto);


    /**
     * 根据策略编码删除策略策略明细
     *
     * @param gco 策略编码
     * @return
     */
    boolean deleteByGco(String gco);

    /**
     * 根据ID删除策略店铺关系
     *
     * @param ids 多条Id
     * @return
     */
    Integer deleteShopById(List<String> ids);

    /**
     * 获取未加入该策略的店铺
     *
     * @param shopCode 店铺编码
     * @param shopName 店铺名称
     * @param gco      策略编码
     * @param enforce  是否强制添加
     * @return
     */
    List<StrategyAuditShop> addLoadShop(String shopCode, String shopName, String gco, boolean enforce);

    /**
     * 保存策略店铺
     *
     * @param strategyShopList
     * @param enforce          是否强制添加
     * @return
     */
    Integer saveStrategyShop(List<StrategyAuditShop> strategyShopList, boolean enforce);

    /**
     * 根据店铺编码获取策略
     *
     * @param shop 店铺编码
     * @return
     */
    StrategyAudit getStrategyByShop(String shop);

    /**
     * 根据店铺编码获取审单策略所有信息(包含拦截主信息、拦截订单明细)
     *
     * @param shop
     * @return
     */
    StrategyAudit getFullInfoStrategyByShop(String shop);
}