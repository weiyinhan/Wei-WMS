package org.jeecg.modules.wms.wave.strategy;

import lombok.RequiredArgsConstructor;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrders;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrdersAllocation;
import org.jeecg.modules.wms.outorder.entity.WmsOutOrdersItems;
import org.jeecg.modules.wms.outorder.service.IWmsOutOrdersItemsService;
import org.jeecg.modules.wms.wave.service.IWaveStrategyService;
import org.jeecg.modules.wms.wave.service.IWmsWaveMasterService;
import org.springframework.stereotype.Component;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */

@Component
@RequiredArgsConstructor
public class SISQWaveStrategy extends IWaveStrategy {
    private final IWmsWaveMasterService wmsWaveMasterService;

    private final IWaveStrategyService waveStrategyService;

    private final IWmsOutOrdersItemsService wmsOutOrdersItemsService;

    @Override
    void process(List<WmsOutOrders> orders,
            Map<String, List<WmsOutOrdersAllocation>> allocatMap) {
        String shipmentStrategy = "STANDARD_STRATEGY";
        // 筛选单品单件的订单,sku,Quantity=1,
        List<WmsOutOrders> collect = orders.stream()
                .filter(order -> order.getTotalSku() == 1 && order.getTotalQuantity() == 1)
                .toList();
        if (collect.isEmpty()) {
            return;
        }
        // 从波次策略表查最小订单数
        Integer minOrderNum = waveStrategyService.getStrategyByCode(getStrategyType()).getMinOrderCount();
        // 从collect提取出库单id
        List<String> outOrderIds = collect.stream().map(WmsOutOrders::getId).collect(Collectors.toList());
        // 根据出库单id找出库单明细
        List<WmsOutOrdersItems> wmsOutOrdersItems = wmsOutOrdersItemsService.selectByOrderIds(outOrderIds);
        // 把明细中的skuid赋值到collect中
        collect.forEach(order -> {
            // 通过对应出库单与明细对应来找到skuid
            WmsOutOrdersItems wmsOutOrdersItemsItem1 = wmsOutOrdersItems.stream()
                    .filter(item -> item.getOrderId().equals(order.getId())).findFirst().orElse(null);
            order.setSkuId(wmsOutOrdersItemsItem1.getSkuId());
        });
        //定义一个List,存放创建波次的订单
        List<WmsOutOrders> waveOrders = new ArrayList<>();
        // 对collect订单按商品进行分组
        Map<String, List<WmsOutOrders>> collect1 = collect.stream()
                .collect(Collectors.groupingBy(WmsOutOrders::getSkuId));
        // 遍历<商品id,List<订单>>
        for (Map.Entry<String, List<WmsOutOrders>> entry : collect1.entrySet()) {
            // 商品id
            String skuId = entry.getKey();
            // 订单列表
            List<WmsOutOrders> value = entry.getValue();
            // 如果订单数小于最小订单数,跳过
            if (value.size() >= minOrderNum) {
                // 创建波次
                wmsWaveMasterService.addWave(value, getStrategyType(), shipmentStrategy);
                // 把订单添加到waveOrders中
                waveOrders.addAll(value);
            }
        }
        //orders中不包含waveOrders中的订单
        List<WmsOutOrders> remainingOrders=orders.stream().filter(order->!waveOrders.contains(order)).toList();
        //剩余订单交给下一个匹配
        processNext(remainingOrders,allocatMap);
    }

    @Override
    String getStrategyType() {
        return "";
    }

    @Override
    int getPriority() {
        return 0;
    }
}