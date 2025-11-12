package com.sky.exception;

/**
 * 套餐修改失败
 *      处于启售状态
 */
public class SetmealUpdateFailedException extends BaseException{
    public SetmealUpdateFailedException() {
    }

    public SetmealUpdateFailedException(String msg) {
        super(msg);
    }
}
