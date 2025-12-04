package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
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
    @Autowired
    private WeChatPayUtil weChatPayUtil;

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
        // user
        User user = userMapper.getUserByid(userId);
        orders = Orders.builder()
                .payMethod(ordersSubmitDTO.getPayMethod())              // 支付方式 1微信，2支付宝
                .amount(ordersSubmitDTO.getAmount())                    // 实收金额
                .remark(ordersSubmitDTO.getRemark())                    // 备注
                .packAmount(ordersSubmitDTO.getPackAmount())            // 打包费
                .estimatedDeliveryTime(ordersSubmitDTO.getEstimatedDeliveryTime()) //期望送达时间
                .deliveryStatus(ordersSubmitDTO.getDeliveryStatus())    //配送状态
                .tablewareStatus(ordersSubmitDTO.getTablewareStatus())  // 餐具数量状态
                .tablewareNumber(ordersSubmitDTO.getTablewareNumber())  // 餐具数量
                .addressBookId(addressBookId)                           // 地址id
                .status(Orders.PENDING_PAYMENT)                         // 订单状态
                .userId(userId)                                         // 下单用户id
                .orderTime(LocalDateTime.now())                         // 下单时间
                .payStatus(Orders.UN_PAID)                              // 支付状态
                .userName(user.getName())                               // 用户名
                .phone(addressBook.getPhone())                          // 电话
                .address(addr)                                          // 地址
                .consignee(addressBook.getConsignee())                  // 收货人
                .number(System.currentTimeMillis() + "")                // 订单编号
                .build();
        orderMapper.submitOrder(orders);

        // 三、往order_detail表插入数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for(ShoppingCart shoppingCart : shoppingCartList){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatchOrderDetail(orderDetailList);

        // 四、清空购物车
        shoppingcartMapper.delAllCart(userId);

        // 五、返回OrderSubmitVO
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(ordersSubmitDTO.getAmount())
                .orderTime(LocalDateTime.now())
                .build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Integer userId = BaseContext.getCurrentId();
        User user = userMapper.getUserByid(userId);

        //调用微信支付接口，生成预支付交易单
        /*
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );
        */
        JSONObject jsonObject = new JSONObject();
        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.updateOrder(orders);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO checkOrder(Integer id) {
        // 通过id查询订单
        Orders orders = orderMapper.getById(id);
        Integer orderId = orders.getId();  // 获取订单id
        // 通过订单id查询订单明细
        List<OrderDetail> orderDetailList = orderDetailMapper.getsByOederId(orderId);
        // 封装orderVO
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 历史订单查询
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        Page<OrderVO> page = orderMapper.page(ordersPageQueryDTO);
        List<OrderVO> orderVOList = page.getResult();
        for(OrderVO orderVO : orderVOList){
            Integer orderId = orderVO.getId();
            List<OrderDetail> orderDetailList = orderDetailMapper.getsByOederId(orderId);
            orderVO.setOrderDetailList(orderDetailList);
        }
        return new PageResult(page.getTotal(),orderVOList);
    }

    /**
     * 取消订单
     * @param id
     */
    @Override
    public void cancelOrder(Integer id) {
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .cancelReason("?")
                .build();
        Orders orderDB = orderMapper.getById(id);
        if(orderDB.getPayStatus() == 1){
            // 退款操作
            orders.setPayStatus(Orders.REFUND);
        }
        orderMapper.updateOrder(orders);
    }

    /**
     * 再来一单
     * ----- 重新加入购物车
     * @param id
     */
    @Override
    public void repetition(Integer id) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getsByOederId(id);
        ShoppingCart shoppingCart = new ShoppingCart();
        for(OrderDetail orderDetail : orderDetailList){
            BeanUtils.copyProperties(orderDetail,shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingcartMapper.addCart(shoppingCart);
        }

    }

    // ------------------------管理端-----------------------------------

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    @Transactional
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<OrderVO> page = orderMapper.page(ordersPageQueryDTO);
        List<OrderVO> orderVOList = page.getResult();
        for(OrderVO orderVO : orderVOList){
            Integer orderId = orderVO.getId();
            List<OrderDetail> orderDetailList = orderDetailMapper.getsByOederId(orderId);
            StringBuilder orderDishes = new StringBuilder();
            orderDetailList.forEach(od -> {
                orderDishes.append(od.getName());
            });
            orderVO.setOrderDishes(orderDishes.toString());
        }
        return new PageResult(page.getTotal(),orderVOList);
    }

    @Override
    public OrderStatisticsVO countByOrderStatus() {
        Map<String,Integer> statusMap = orderMapper.countByOrderStatus();
        for (int i = 0; i < 3; i++) {
            statusMap.putIfAbsent(i + "", 0);
        }
        return new OrderStatisticsVO(statusMap.get("0"),statusMap.get("1"),statusMap.get("2"));
    }

    /**
     * 接单
     * @param id
     */
    @Override
    public void comfirmOrder(Integer id) {
        Orders orders = new Orders();
        BeanUtils.copyProperties(orderMapper.getById(id),orders);
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.updateOrder(orders);
    }

    /**
     * 拒单
     * --- 修改订单：拒单理由，订单状态，支付状态
     * --- 只有“待接单”才能 拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = new Orders();
        BeanUtils.copyProperties(orderMapper.getById(ordersRejectionDTO.getId()),orders);
        orders.setStatus(7);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setPayStatus(Orders.REFUND);
        orderMapper.updateOrder(orders);
    }

    /**
     * 派送订单
     * --- 修改订单状态
     * @param id
     */
    @Override
    @Transactional
    public void delivery(Integer id) {
        Orders orders = new Orders();
        BeanUtils.copyProperties(orderMapper.getById(id),orders);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.updateOrder(orders);
    }

    /**
     * 完成订单
     * --- 修改订单状态、送达时间
     * @param id
     */
    @Override
    public void complete(Integer id) {
        Orders orders = new Orders();
        BeanUtils.copyProperties(orderMapper.getById(id),orders);
        orders.setStatus(Orders.COMPLETED);
        orders.setCheckoutTime(LocalDateTime.now());
        orderMapper.updateOrder(orders);
    }

}
