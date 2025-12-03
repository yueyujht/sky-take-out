package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper {

    void submitOrder(Orders orders);

    void updateOrder(Orders orders);

    @Select("select * from sky_take_out.orders where number = #{outTradeNo}")
    Orders getByNumber(String outTradeNo);

    @Select("select * from sky_take_out.orders where id = #{id}")
    Orders getById(Integer id);

    Page<OrderVO> page(OrdersPageQueryDTO ordersPageQueryDTO);
}
