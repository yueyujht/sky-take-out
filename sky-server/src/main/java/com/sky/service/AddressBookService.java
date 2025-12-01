package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressBookService {
    /**
     * 查询当前登录用户的所有地址信息
     * @return
     */
    List<AddressBook> list();

    /**
     * 新增地址
     * @param addressBook
     */
    void addAddr(AddressBook addressBook);

    /**
     * 查询默认地址
     * @return
     */
    AddressBook queryDefaultAddr();

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    AddressBook getAddrById(Integer id);

    /**
     * 根据id修改地址
     * @param addressBook
     */
    void updateAddr(AddressBook addressBook);

    /**
     * 设置默认地址
     * @param id
     */
    void setDefaultAddr(Integer id);

    /**
     * 根据id删除地址
     * @param id
     */
    void deleteAddrById(Integer id);
}
