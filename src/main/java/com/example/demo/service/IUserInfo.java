package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.UserInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IUserInfo extends IService<UserInfo> {

    List getUserInfo(String age);
}
