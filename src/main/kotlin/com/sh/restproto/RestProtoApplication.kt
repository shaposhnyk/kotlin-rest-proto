package com.sh.restproto

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter
import org.springframework.http.converter.protobuf.ProtobufJsonFormatHttpMessageConverter

@SpringBootApplication
class RestProtoApplication {
    @Bean
    fun protoConv() = ProtobufHttpMessageConverter()

    @Bean
    fun jsonConv() = ProtobufJsonFormatHttpMessageConverter()

    @Bean
    fun repo() = CustomerRepo()
}

fun main(args: Array<String>) {
    runApplication<RestProtoApplication>(*args)
}
