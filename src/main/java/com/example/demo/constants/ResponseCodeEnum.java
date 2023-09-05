package com.example.demo.constants;

/**
 * @Auther：jinguangshuai
 * @Data：2023/8/3 - 08 - 03 - 10:53
 * @Description:com.example.demo.constants
 * @version:1.0
 */
public enum ResponseCodeEnum {
    SUCCESS("000000", "成功"),
    FAILURE("100000", "失败"),
    RESULT_IS_NULL("100001", "查询结果为空"),
    DEVICE_SAVE_FAILED("100002", "台账保存失败"),
    PARAM_IS_INVALID("200002", "入参json格式化失败"),
    PARAM_ENUM_INVALID("200004", "参数枚举值无效"),
    PARAM_IS_BLANK("200003", "必填字段为空"),
    PARAM_OUTOF_LIMIT("200005", "参数长度超过限制"),
    PARAM_NUMBERFORMAT_FAULT("200006", "数字格式化异常"),
    PARAM_DATEFORMAT_FAULT("200007", "日期格式化异常"),
    DB_OPERATE_FAILED("300000", "数据库操作失败"),
    DB_DUPLICATE_KEY("300001", "数据库主键重复"),
    SQL_PARSE_FAILED("300002", "sql语句解析失败"),
    DB_EXIST_KEY("300003", "对应主键不存在"),
    RPC_CALL_FAILED("400001", "dubbo远程服务调用失败"),
    REST_CALL_FAILED("400002", "REST服务调用失败"),
    DIAGRAM_CALL_FAILED("400003", "图形服务返回失败，请稍后重试");

    private final String type;
    private final String desc;

    private ResponseCodeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public String getValue() {
        return this.type;
    }

    public String getDesc() {
        return this.desc;
    }

    public static ResponseCodeEnum findByValue(String type) {
        ResponseCodeEnum[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            ResponseCodeEnum code = var1[var3];
            if (code.type.equals(type)) {
                return code;
            }
        }

        return null;
    }
}
