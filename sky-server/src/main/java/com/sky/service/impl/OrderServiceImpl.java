package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ShoppingcartMapper shoppingcartMapper;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO subOrder(OrdersSubmitDTO ordersSubmitDTO) {
        Orders orders;
        Integer addressBookId = ordersSubmitDTO.getAddressBookId();
        Integer userId = BaseContext.getCurrentId();
        // 一、处理业务异常
        // （1）地址簿为空
        List<AddressBook> addressBookList = addressBookMapper.list(userId);
        if(addressBookList.isEmpty()) throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        // （2）购物车数据为空
        List<ShoppingCart> shoppingCartList = shoppingcartMapper.list(userId);
        if(shoppingCartList.isEmpty()) throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);

        // 二、往order表插入数据
        // address
        AddressBook addressBook = addressBookMapper.getAddrById(addressBookId);
        String addr = addressBook.getProvinceName() + addressBook.getCityName() +  addressBook.getDistrictName();
        String addrCode = addressBook.getProvinceCode() + addressBook.getCityCode() +  addressBook.getDistrictCode();
        // user
        User user = userMapper.getUserByid(userId);
        // 配送状态
        orders = Orders.builder()
                .payMethod(ordersSubmitDTO.getPayMethod())      // 支付方式 1微信，2支付宝
                .amount(ordersSubmitDTO.getAmount())            // 实收金额
                .remark(ordersSubmitDTO.getRemark())            // 备注
                .packAmount(ordersSubmitDTO.getPackAmount())    // 打包费
                .estimatedDeliveryTime(ordersSubmitDTO.getEstimatedDeliveryTime())
                .deliveryStatus(ordersSubmitDTO.getDeliveryStatus())
                .tablewareStatus(ordersSubmitDTO.getTablewareStatus())
                .tablewareNumber(ordersSubmitDTO.getTablewareNumber())
                .addressBookId(addressBookId)                   // 地址id
                .status(Orders.PENDING_PAYMENT)                                      // 订单状态
                .userId(userId)                                 // 下单用户id
                .orderTime(LocalDateTime.now())                 // 下单时间
                .payStatus(Orders.UN_PAID)                                   // 支付状态
                .userName(user.getName())                       //
                .phone(addressBook.getPhone())
                .address(addr)                                  // 地址
                .consignee(addressBook.getConsignee())
                .build();
        orderMapper.submitOrder(orders);

        // 设置订单号
        Integer id = orders.getId();
        orders.setNumber(addrCode + id);
        orders.setId(id);
        orderMapper.updateOrder(orders);

        // 三、往order_detail表插入数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for(ShoppingCart shoppingCart : shoppingCartList){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart,orderDetail);
            orderDetail.setOrderId(id);
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatchOrderDetail(orderDetailList);

        // 四、清空购物车
        shoppingcartMapper.delAllCart(userId);

        // 五、返回OrderSubmitVO
        return OrderSubmitVO.builder()
                .id(id)
                .orderNumber(addrCode + id)
                .orderAmount(ordersSubmitDTO.getAmount())
                .orderTime(LocalDateTime.now())
                .build();
    }
}
