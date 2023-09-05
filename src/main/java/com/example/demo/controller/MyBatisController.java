package com.example.demo.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.example.demo.constants.SimpleResponseBody;
import com.example.demo.dto.MetHighRainEsDto;
import com.example.demo.dto.UserInfoDto;
import com.example.demo.entity.UserInfo;
import com.example.demo.service.IMetHighRainService;
import com.example.demo.service.IUserInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.demo.utils.DateUtil.isDateHasTime;

@RestController
@RequestMapping("/test")
@Api(value = "测试服务", tags = {"测试服务"})
public class MyBatisController {

    @Autowired
    IUserInfo iUserInfo;

    @Autowired
    IMetHighRainService iMetHighRainService;

    @PostMapping("/queryTestUser")
    @ApiOperation(value = "测试服务", notes = "测试服务")
    public List<UserInfo> queryDanceWarningFile(@RequestBody @Valid @ApiParam(value = "测试服务查询对象") UserInfoDto dto) {
        List<UserInfo> userInfo = iUserInfo.getUserInfo(dto.getAge());
        return userInfo;
    }

    @PostMapping("/es/queryHighRainResult")
    @ApiOperation(value = "查询暴雨原始解析结果", notes = "提供雨原始解析结果的查询服务能力。支持通过预警开始时间，预警结束时间，省份名称，省份编码等条件对暴雨原始解析结果进行自定义分页查询，查询结果包括符合筛选条件的所有暴雨原始解析结果。", nickname = "pgp-007-2305-20585")
    public SimpleResponseBody<Object> queryLightningWarningEffectDevice(@RequestBody @Valid @ApiParam(value = "暴雨原始解析结果查询对象") MetHighRainEsDto dto) {
        //验证时间是否在合法范围
        String startTime = dto.getBeginTime();
        String endTime = dto.getEndTime();
        if(StrUtil.isNotBlank(startTime) && StrUtil.isNotBlank(endTime)){
            if(!isDateHasTime(startTime) && !isDateHasTime(endTime)){
                throw new DateTimeException("日期格式化异常，请检查入参");
            }
        }
        LocalDateTime start = DateUtil.parse(dto.getBeginTime(), "yyyy-MM-dd HH:mm:ss").toLocalDateTime();
        LocalDateTime end = DateUtil.parse(dto.getEndTime(), "yyyy-MM-dd HH:mm:ss").toLocalDateTime();
        if (start.isAfter(end)) {
            return SimpleResponseBody.fail("开始日期不能大于结束日期！");
        }
        return SimpleResponseBody.success(iMetHighRainService.queryEsHighRainResult(dto));
    }

    @PostMapping("/es/queryHighRainResultMap")
    @ApiOperation(value = "新查询暴雨原始解析结果", notes = "提供雨原始解析结果的查询服务能力。支持通过预警开始时间，预警结束时间，省份名称，省份编码等条件对暴雨原始解析结果进行自定义分页查询，查询结果包括符合筛选条件的所有暴雨原始解析结果。", nickname = "pgp-007-2305-20585")
    public SimpleResponseBody<Object> queryLightningWarningEffectDeviceMap(@RequestBody @Valid @ApiParam(value = "暴雨原始解析结果查询对象") MetHighRainEsDto dto) {
        //验证时间是否在合法范围
        String startTime = dto.getBeginTime();
        String endTime = dto.getEndTime();
        if(StrUtil.isNotBlank(startTime) && StrUtil.isNotBlank(endTime)){
            if(!isDateHasTime(startTime) && !isDateHasTime(endTime)){
                throw new DateTimeException("日期格式化异常，请检查入参");
            }
        }
        LocalDateTime start = DateUtil.parse(dto.getBeginTime(), "yyyy-MM-dd HH:mm:ss").toLocalDateTime();
        LocalDateTime end = DateUtil.parse(dto.getEndTime(), "yyyy-MM-dd HH:mm:ss").toLocalDateTime();
        if (start.isAfter(end)) {
            return SimpleResponseBody.fail("开始日期不能大于结束日期！");
        }
        return SimpleResponseBody.success(iMetHighRainService.queryEsHighRainResultMap(dto));
    }

}
