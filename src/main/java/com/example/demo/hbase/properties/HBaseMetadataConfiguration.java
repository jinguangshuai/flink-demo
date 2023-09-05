package com.example.demo.hbase.properties;

import com.example.demo.hbase.dto.RowKeyConfigDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @ClassName : HbaseMetadataConfiguration  //类名
 * @Description : hbase元数据配置类  //描述
 * @Author : MingMaster  //作者
 * @Date: 2020-09-05 //时间
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "hbase.table-config")
@RefreshScope
@Component
public class HBaseMetadataConfiguration {

    private Map<String, RowKeyConfigDTO> emsSearchCondition;

    private Map<String, RowKeyConfigDTO> dmsSearchCondition;

    private Map<String, RowKeyConfigDTO> cmsSearchCondition;

}
