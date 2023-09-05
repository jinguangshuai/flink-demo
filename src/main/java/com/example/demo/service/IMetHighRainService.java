package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.dto.MetHighRainEsDto;
import com.example.demo.entity.MetHighRain;
import com.example.demo.es.entity.Page;
import com.example.demo.vo.MetHighRainDto;

/**
 * @Auther：jinguangshuai
 * @Data：2023/8/3 - 08 - 03 - 17:14
 * @Description:com.example.demo.service
 * @version:1.0
 */
public interface IMetHighRainService extends IService<MetHighRain> {
    //ES查询
    Page<MetHighRainDto> queryEsHighRainResult(MetHighRainEsDto dto);

    //ES查询
    Page<MetHighRainDto> queryEsHighRainResultMap(MetHighRainEsDto dto);
}
