package com.sky.controller.user;

import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/addressBook")
@Slf4j
@Api(tags = "地址簿相关接口")
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    /**
     * 查询当前登录用户的所有地址信息
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查询当前登录用户的所有地址信息")
    public Result list(){
        log.info("查询当前登录用户的所有地址信息");
        List<AddressBook> addressBookList = addressBookService.list();
        return Result.success(addressBookList);
    }

    /**
     * 查询默认地址
     * @return
     */
    @GetMapping("/default")
    @ApiOperation("查询默认地址")
    public Result queryDefaultAddr(){
        log.info("查询默认地址");
        AddressBook addressBook = addressBookService.queryDefaultAddr();
        return Result.success(addressBook);
    }

    /**
     * 新增地址
     * @param addressBook
     * @return
     */
    @PostMapping
    @ApiOperation("新增地址")
    public Result addAddr(@RequestBody AddressBook addressBook){
        log.info("新增地址：{}",addressBook);
        addressBookService.addAddr(addressBook);
        return Result.success();
    }

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询地址")
    public Result getAddrById(@PathVariable Integer id){
        log.info("根据id查询地址:{}",id);
        AddressBook addressBook = addressBookService.getAddrById(id);
        return Result.success(addressBook);
    }

    /**
     * 根据id修改地址
     * @param addressBook
     * @return
     */
    @PutMapping
    @ApiOperation("根据id修改地址")
    public Result updateAddr(@RequestBody AddressBook addressBook){
        log.info("根据id修改地址");
        addressBookService.updateAddr(addressBook);
        return Result.success();
    }

    /**
     * 设置默认地址
     * @param idMap
     * @return
     */
    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result setDefaultAddr(@RequestBody Map<String,Integer> idMap){
        log.info("设置默认地址:{}",idMap);
        addressBookService.setDefaultAddr(idMap.get("id"));
        return Result.success();
    }

    /**
     * 根据id删除地址
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据id删除地址")
    public Result deleteAddrById(Integer id){
        log.info("根据id删除地址");
        addressBookService.deleteAddrById(id);
        return Result.success();
    }
}
