package com.example.demo.hbase.util;

public enum Module {
    STORM("StormClient"), KAFKA("KafkaClient"), ZOOKEEPER("Client"), HDFS("HDFSClient");

    private String name;

    private Module(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
