package com.devit.devitcertificationservice.rabbitMQ;

import com.devit.devitcertificationservice.rabbitMQ.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * RabbitMQ에 메시지를 보내는 서비스 클래스
 */
@Service
@Slf4j
public class RabbitMqSender {
    // RabbitTemplate 클래스를 사용하면 RabbitMQ로 메시지를 보내고 받을 수 있다.
    private RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitMqSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // yml 파일에서 가져옴
    // 메시지를 다른 대기열로 라우팅하는 역할을 하는 RabbitMQ 교환을 정의RabbitMQConfig
    @Value("${spring.rabbitmq.exchange}")
    private String exchange;
    // 교환 유형에 따라 메시지를 큐로 라우팅하는 방법을 정의
    @Value("${spring.rabbitmq.routingkey}")
    private String routingkey;
    // yml 파일의 message 를 가져옴
    @Value("${app.message}")
    private String message;

    public void send(UserDto userDto) {
        rabbitTemplate.convertAndSend(exchange, routingkey, userDto);
        log.info(message);
    }
}
