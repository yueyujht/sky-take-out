package com.sky.mapper;

import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AddressBookMapper {
    /**
     * 查询当前登录用户的所有地址信息
     * @return
     */
    @Select("select * from sky_take_out.address_book where user_id = #{userId}")
    List<AddressBook> list(Integer userId);

    @Insert("insert into sky_take_out.address_book " +
            "(user_id, consignee, sex, phone, province_code, province_name, city_code, city_name, district_code, district_name, detail, label,is_default) " +
            "VALUES " +
            "(#{userId},#{consignee},#{sex},#{phone},#{provinceCode},#{provinceName},#{cityCode},#{cityName},#{districtCode},#{districtName},#{detail},#{label},#{isDefault})")
    void addAddr(AddressBook addressBook);

    @Select("select * from sky_take_out.address_book where user_id = #{userId} and is_default = 1")
    AddressBook queryDefaultAddr(Integer userId);

    @Select("select * from sky_take_out.address_book where id = #{id}")
    AddressBook getAddrById(Integer id);

    void updateAddr(AddressBook addressBook);

    @Delete("delete from sky_take_out.address_book where id = #{id}")
    void deleteAddrById(Integer id);
}
