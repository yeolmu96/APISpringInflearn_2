package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
xToOne(ManyToOne, OneToOne)

order
order -> memeber
order -> delivery
 */

@Slf4j
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1(){
        log.info("OrderSimpleApiController.ordersV1");
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        log.info("OrderSimpleApiController.ordersV1-2");
        return all;
    }
}
