package com.game.utils;


import com.game.other.UserDTO;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

public class CurrentUser {
    //创建一个线程池，用于获取当前用户信息
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user){
        tl.set(user);
    }

    public static UserDTO getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }

    public static String getToken() {
        String token = "";
        try {
            HttpServletRequest request = ((ServletRequestAttributes) Objects
                    .requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
            //获取响应头中的token字段
            token = request.getHeader("Authorization");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return token;
    }
}
