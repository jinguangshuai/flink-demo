package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @Auther：jinguangshuai
 * @Data：2023/6/30 - 06 - 30 - 11:06
 * @Description:com.example.demo.entity
 * @version:1.0
 */
@Data
@ApiModel("测试")
@TableName("user_info")
public class UserInfo {
    public String id;
    public String age;
    public String name;
    public String sex;

    public UserInfo() {
    }

    public UserInfo(String id, String age, String name, String sex) {
        this.id = id;
        this.age = age;
        this.name = name;
        this.sex = sex;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}