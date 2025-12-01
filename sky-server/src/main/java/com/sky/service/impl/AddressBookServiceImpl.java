package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {
    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 查询地址
     * @return
     */
    @Override
    public List<AddressBook> list() {
        return addressBookMapper.list(BaseContext.getCurrentId());
    }

    /**
     * 新增地址
     * @param addressBook
     */
    @Override
    public void addAddr(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        addressBookMapper.addAddr(addressBook);
    }

    /**
     * 查询默认地址
     * @return
     */
    @Override
    public AddressBook queryDefaultAddr() {
        return addressBookMapper.queryDefaultAddr(BaseContext.getCurrentId());
    }

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    @Override
    public AddressBook getAddrById(Integer id) {
        return addressBookMapper.getAddrById(id);
    }

    /**
     * 根据id修改地址
     * @param addressBook
     */
    @Override
    public void updateAddr(AddressBook addressBook) {
        addressBookMapper.updateAddr(addressBook);
    }

    /**
     * 设置默认地址
     * @param id
     */
    @Override
    public void setDefaultAddr(Integer id) {
        AddressBook addressBook = AddressBook.builder()
                .isDefault(1)
                .id(id)
                .build();
        addressBookMapper.updateAddr(addressBook);
    }

    /**
     * 根据id删除地址
     * @param id
     */
    @Override
    public void deleteAddrById(Integer id) {
        addressBookMapper.deleteAddrById(id);
    }
}
