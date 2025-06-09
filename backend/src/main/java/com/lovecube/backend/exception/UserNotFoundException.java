package com.lovecube.backend.exception;

/**
 * @author Admin
 * 用于处理用户不存在的情况
 */
public class UserNotFoundException extends Exception
{
    public UserNotFoundException(String message)
    {
        super(message);
    }
}
