package com.example.demo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.MetHighRainEsDto;
import com.example.demo.entity.MetHighRain;
import com.example.demo.es.entity.EsQueryCondition;
import com.example.demo.es.entity.EsRange;
import com.example.demo.es.entity.Page;
import com.example.demo.es.utils.EsUtil;
import com.example.demo.hbase.service.IHBaseService;
import com.example.demo.mapper.IMetHighRainMapper;
import com.example.demo.service.IMetHighRainService;
import com.example.demo.vo.EsMetHighRain;
import com.example.demo.vo.MetHighRainDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Auther：jinguangshuai
 * @Data：2023/8/1 - 08 - 01 - 10:01
 * @Description:com.example.demo.service.impl
 * @version:1.0
 */
@Slf4j
@Service
public class IMetHighRainServiceImpl extends ServiceImpl<IMetHighRainMapper, MetHighRain> implements IMetHighRainService {

    static final Integer DEFAULT_PAGE_NO = 1;

    static final Integer DEFAULT_PAGE_SIZE = 10;

    @Autowired
    EsUtil esUtil;

    @Autowired(required = false)
    IHBaseService ihBaseService;

    public Page<MetHighRainDto> queryEsHighRainResult(MetHighRainEsDto dto) {
        long start = System.currentTimeMillis();
        //精确查询
        List<EsQueryCondition> andConditionList = new ArrayList<>();
        if (StrUtil.isNotBlank(dto.getValue())) {
            EsQueryCondition value = new EsQueryCondition();
            value.setFieldName("value");
            value.setFieldValue(dto.getValue());
            andConditionList.add(value);
        }
        if (null != dto.getProvince()) {
            EsQueryCondition province = new EsQueryCondition();
            province.setFieldName("province");
            province.setFieldValue(dto.getProvince());
            andConditionList.add(province);
        }
        if (StrUtil.isNotBlank(dto.getProvinceName())) {
            EsQueryCondition provinceName = new EsQueryCondition();
            provinceName.setFieldName("provinceName");
            provinceName.setFieldValue(dto.getProvinceName());
            andConditionList.add(provinceName);
        }
        if (StrUtil.isNotBlank(dto.getGeom())) {
            EsQueryCondition geom = new EsQueryCondition();
            geom.setFieldName("geom");
            geom.setFieldValue(dto.getGeom());
            andConditionList.add(geom);
        }

        //范围查询
        List<EsRange> multiRangeList = new ArrayList<>();
        if (StringUtils.isNotBlank(dto.getBeginTime())) {
            EsRange minTime = new EsRange();
            minTime.setMinEqualValue(dto.getBeginTime());
            minTime.setMaxEqualValue(dto.getEndTime());
            minTime.setIndexColumn("startTime");
            multiRangeList.add(minTime);
        }
        if (StringUtils.isNotBlank(dto.getEndTime())) {
            EsRange maxTime = new EsRange();
            maxTime.setMinEqualValue(dto.getBeginTime());
            maxTime.setMaxEqualValue(dto.getEndTime());
            maxTime.setIndexColumn("endTime");
            multiRangeList.add(maxTime);
        }

        //分页
        if (null == dto.getPageNo() || null == dto.getPageSize()) {
            dto.setPageNo(DEFAULT_PAGE_NO);
            dto.setPageSize(DEFAULT_PAGE_SIZE);
        }


        Page<EsMetHighRain> esPage = esUtil.conditionSearch("met_high_rain",
                dto.getPageNo(),
                dto.getPageSize(),
                andConditionList,
                null,
                null,
                null,
                multiRangeList,
                EsMetHighRain.class);

        List<EsMetHighRain> records = esPage.getRecords();

        List<String> rowKeys = new ArrayList<>();
        records.forEach(t -> {
            rowKeys.add(t.getRowKey());
        });
        List<String> newRokeys = rowKeys.stream().collect(Collectors.toList());

        long end = System.currentTimeMillis();
        log.info("获取所有es结果耗时 {} 毫秒", end - start);

        List<MetHighRainDto> result = new ArrayList<>();
        long esStart = System.currentTimeMillis();
        Map<String, Map<String, String>> metHighRain = ihBaseService.getListByKeys(newRokeys, "met_high_rain", null, null, null);
        long esEnd = System.currentTimeMillis();
        log.info("获取所有ots结果耗时 {} 毫秒", esEnd - esStart);
        Set<Map.Entry<String, Map<String, String>>> entries = metHighRain.entrySet();
        for (Map.Entry<String, Map<String, String>> entry : entries) {
            Map<String, String> o = entry.getValue();
            MetHighRainDto metHighRainResult = new MetHighRainDto();
            metHighRainResult.setId(o.get("id"));
            metHighRainResult.setStartTime(DateUtil.parse(o.get("startTime"), "yyyy-MM-dd HH:mm:ss"));
            metHighRainResult.setEndTime(DateUtil.parse(o.get("endTime"), "yyyy-MM-dd HH:mm:ss"));
            metHighRainResult.setValue(o.get("value"));
            metHighRainResult.setGeom(o.get("geom"));
            metHighRainResult.setProvince(o.get("province"));
            metHighRainResult.setProvinceName(o.get("provinceName"));
            metHighRainResult.setVersion(o.get("version"));
            metHighRainResult.setCreateTime(DateUtil.parse(o.get("createTime"), "yyyy-MM-dd HH:mm:ss"));
            log.info("hbase中的结果为 {} ", metHighRainResult.toString());
            result.add(metHighRainResult);

        }
        Page<MetHighRainDto> page = new Page<>();
        BeanUtil.copyProperties(esPage, page);

//        page.setTotal(result.size());
//        page.setSize(result.size());
//        page.setCurrent(dto.getPageNo());
//        page.setRecords(result);
//        page.setPages(result.size() == 0? 0:(int) (result.size() % dto.getPageSize() ==0 ? result.size()/dto.getPageSize():(result.size()/dto.getPageSize())+1 ));
        return page;

    }


    public Page<MetHighRainDto> queryEsHighRainResultMap(MetHighRainEsDto dto) {
        long start = System.currentTimeMillis();
        Page<MetHighRainDto> page = new Page<>();
        //分页
        if (null == dto.getPageNo() || null == dto.getPageSize()) {
            dto.setPageNo(DEFAULT_PAGE_NO);
            dto.setPageSize(DEFAULT_PAGE_SIZE);
        }
        //精确查询
        Map<String, Object> andMap = createHighRainAndParam(dto);
        //范围查询
        Map<String, Object> rangeUpMap = new HashMap<>();
        rangeUpMap.put("startTime", dto.getBeginTime());
        Map<String, Object> rangeDownMap = new HashMap<>();
        rangeDownMap.put("endTime", dto.getEndTime());
        try {
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<EsMetHighRain> esPage = esUtil.mapConditionSearch("met_high_rain",
                    dto.getPageNo(),
                    dto.getPageSize(),
                    andMap,
                    null,
                    null,
                    null,
                    rangeUpMap,
                    rangeDownMap,
                    null,
                    EsMetHighRain.class);

            List<EsMetHighRain> records = esPage.getRecords();
            List<String> rowKeys = new ArrayList<>();
            records.forEach(t -> {
                rowKeys.add(t.getRowKey());
            });
            List<String> newRokeys = rowKeys.stream().collect(Collectors.toList());

            long esEnd = System.currentTimeMillis();
            log.info("获取所有es结果耗时 {} 毫秒", esEnd - start);

            List<MetHighRainDto> result = new ArrayList<>();
            long esStart = System.currentTimeMillis();
            Map<String, Map<String, String>> metHighRain = ihBaseService.getListByKeys(newRokeys, "met_high_rain", null, null, null);
            long otsEnd = System.currentTimeMillis();
            log.info("获取所有ots结果耗时 {} 毫秒", otsEnd - esStart);
            Set<Map.Entry<String, Map<String, String>>> entries = metHighRain.entrySet();
            for (Map.Entry<String, Map<String, String>> entry : entries) {
                Map<String, String> o = entry.getValue();
                MetHighRainDto metHighRainResult = new MetHighRainDto();
                metHighRainResult.setId(o.get("id"));
                metHighRainResult.setStartTime(DateUtil.parse(o.get("startTime"), "yyyy-MM-dd HH:mm:ss"));
                metHighRainResult.setEndTime(DateUtil.parse(o.get("endTime"), "yyyy-MM-dd HH:mm:ss"));
                metHighRainResult.setValue(o.get("value"));
                metHighRainResult.setGeom(o.get("geom"));
                metHighRainResult.setProvince(o.get("province"));
                metHighRainResult.setProvinceName(o.get("provinceName"));
                metHighRainResult.setVersion(o.get("version"));
                metHighRainResult.setCreateTime(DateUtil.parse(o.get("createTime"), "yyyy-MM-dd HH:mm:ss"));
                log.info("hbase中的结果为 {} ", metHighRainResult.toString());
                result.add(metHighRainResult);
            }

            BeanUtil.copyProperties(esPage, page);
//            page.setTotal(result.size());
//            page.setSize(result.size());
//            page.setCurrent(dto.getPageNo());
//            page.setRecords(result);
//            page.setPages(result.size() == 0? 0:(int) (result.size() % dto.getPageSize() ==0 ? result.size()/dto.getPageSize():(result.size()/dto.getPageSize())+1 ));
        } catch (Exception e) {
            log.error("查询结果失败！" + e);
        }
        return page;
    }

    public Map<String, Object> createHighRainAndParam(MetHighRainEsDto dto) {
        Map<String, Object> andMap = new HashMap<>();
        if (StringUtils.isNotBlank(dto.getValue())) {
            andMap.put("value", dto.getValue());
        }
        if (StringUtils.isNotBlank(dto.getProvince())) {
            andMap.put("province", dto.getProvince());
        }
        if (StringUtils.isNotBlank(dto.getProvinceName())) {
            andMap.put("provinceName", dto.getProvinceName());
        }
        if (StringUtils.isNotBlank(dto.getValue())) {
            andMap.put("geom", dto.getGeom());
        }
        return andMap;
    }
}
