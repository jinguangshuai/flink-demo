package com.example.demo.constants;

/**
 * @Auther：jinguangshuai
 * @Data：2023/8/2 - 08 - 02 - 10:47
 * @Description:com.example.demo.constants
 * @version:1.0
 */
public enum EsCreateIndexEnum {

    MET_HIGH_RAIN("暴雨NC解析原始结果","met_high_rain","{\n" +
            "    \"settings\":{\n" +
            "        \"number_of_shards\":5,\n" +
            "        \"max_result_window\":5000000\n" +
            "    },\n" +
            "    \"mappings\":{\n" +
            "        \"rowKey\":{\n" +
            "            \"type\":\"keyword\"\n" +
            "        },\n" +
            "        \"id\":{\n" +
            "            \"type\":\"keyword\"\n" +
            "        },\n" +
            "        \"startTime\":{\n" +
            "            \"type\":\"date\",\n" +
            "            \"format\":\"yyyy-MM-dd HH:mm:ss ||yyyy-MM-dd||epoch_millis\"\n" +
            "        },\n" +
            "        \"endTime\":{\n" +
            "            \"type\":\"date\",\n" +
            "            \"format\":\"yyyy-MM-dd HH:mm:ss ||yyyy-MM-dd||epoch_millis\"\n" +
            "        },\n" +
            "        \"value\":{\n" +
            "            \"type\":\"keyword\"\n" +
            "        },\n" +
            "        \"geom\":{\n" +
            "            \"type\":\"text\"\n" +
            "        },\n" +
            "        \"province\":{\n" +
            "            \"type\":\"keyword\"\n" +
            "        },\n" +
            "        \"provinceName\":{\n" +
            "            \"type\":\"keyword\"\n" +
            "        },\n" +
            "        \"version\":{\n" +
            "            \"type\":\"keyword\"\n" +
            "        },\n" +
            "        \"createTime\":{\n" +
            "            \"type\":\"date\",\n" +
            "            \"format\":\"yyyy-MM-dd HH:mm:ss ||yyyy-MM-dd||epoch_millis\"\n" +
            "        },\n" +
            "    }\n" +
            "}"),
    MET_HIGH_RAIN_INDEX("暴雨解析结果","met_high_rain","{\n" +
            "     \"settings\": {\n" +
            "          \"number_of_shards\": 3,\n" +
            "          \"max_result_window\": 5000000\n" +
            "     },\n" +
            "     \"mappings\": {\n" +
            "          \"properties\": {\n" +
            "               \"rowKey\": {\n" +
            "                    \"type\": \"keyword\"\n" +
            "               },\n" +
            "               \"id\": {\n" +
            "                    \"type\": \"keyword\"\n" +
            "               },\n" +
            "               \"startTime\": {\n" +
            "                    \"type\": \"date\",\n" +
            "                    \"format\": \"yyyy-mm-dd HH:mm:ss||yyyy-mm-dd||epoch_millis\"\n" +
            "               },\n" +
            "               \"endTime\": {\n" +
            "                    \"type\": \"date\",\n" +
            "                    \"format\": \"yyyy-mm-dd HH:mm:ss||yyyy-mm-dd||epoch_millis\"\n" +
            "               },\n" +
            "               \"value\": {\n" +
            "                    \"type\": \"keyword\"\n" +
            "               },\n" +
            "               \"geom\": {\n" +
            "                    \"type\": \"keyword\"\n" +
            "               },\n" +
            "               \"province\": {\n" +
            "                    \"type\": \"keyword\"\n" +
            "               },\n" +
            "               \"provinceName\": {\n" +
            "                    \"type\": \"keyword\",\n" +
            "               },\n" +
            "               \"version\": {\n" +
            "                    \"type\": \"keyword\"\n" +
            "               },\n" +
            "               \"createTime\": {\n" +
            "                    \"type\": \"date\",\n" +
            "                    \"format\": \"yyyy-mm-dd HH:mm:ss||yyyy-mm-dd||epoch_millis\"\n" +
            "               }\n" +
            "          }\n" +
            "     }\n" +
            "}");

    private String name;
    private String tableName;
    private String dsl;

    EsCreateIndexEnum(String name, String tableName, String dsl) {
        this.name = name;
        this.tableName = tableName;
        this.dsl = dsl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDsl() {
        return dsl;
    }

    public void setDsl(String dsl) {
        this.dsl = dsl;
    }
}
