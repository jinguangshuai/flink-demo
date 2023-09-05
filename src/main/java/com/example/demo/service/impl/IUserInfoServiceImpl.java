package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.UserInfo;
import com.example.demo.mapper.IUserInfoMapper;
import com.example.demo.service.IUserInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IUserInfoServiceImpl extends ServiceImpl<IUserInfoMapper, UserInfo> implements IUserInfo {

    public List getUserInfo(String age){
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("age","20");
        List<UserInfo> userInfos = this.baseMapper.selectList(wrapper);
        return userInfos;
    }
}
