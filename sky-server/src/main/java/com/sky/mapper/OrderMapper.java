package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface OrderMapper {

    // 提交订单
    void submitOrder(Orders orders);

    // 根据订单id更新订单
    void updateOrder(Orders orders);

    @Select("select * from sky_take_out.orders where number = #{outTradeNo}")
    Orders getByNumber(String outTradeNo);

    @Select("select * from sky_take_out.orders where id = #{id}")
    Orders getById(Integer id);

    Page<OrderVO> page(OrdersPageQueryDTO ordersPageQueryDTO);

    // NOTE: 返回Map类型使用@MapKey
    @MapKey("status")
    Map<String,Integer> countByOrderStatus();
}
