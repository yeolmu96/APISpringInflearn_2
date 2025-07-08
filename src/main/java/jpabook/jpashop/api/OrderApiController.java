package jpabook.jpashop.api;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import jpabook.jpashop.service.query.OrderQueryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/*
<<컬렉션 패치 조인>>
1:n 관계에서 fetch join -> 페이징 불가능/1개만 사용 가능
 */

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    //플랫 데이터 최적화 : JOIN 결과 조회 후 원하는 모양으로 직접 변환
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats =  orderQueryRepository.findAllByDto_flat();

        //주문(Order)에 대한 데이터를 한 번의 쿼리로 모두 가져와서 OrderFlatDto라는 DTO 객체에 담아 반환
        //“평탄화”란, 주문 1건에 여러 주문상품이 있으면 주문정보가 중복으로 반복되며 한 행씩 쭉 나오는 것
        //페이징 불가능
        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),
                                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                        e.getKey().getAddress(), e.getValue()))
            .collect(toList());
    }

    //1:N관계는 IN절 활용으로 메모리에 미리 조회해서 최적화
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    //DTO 직접 조회
    //ToOne 먼저 조회
    //ToMany(컬렉션) 별도 처리(row 수 증가하기 때문에)
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4(){
        return orderQueryRepository.findOrderQueryDto();
    }

    //컬렉션 페이징 한계 돌파하기
    //XToOne 관계는 fetch join으로 쿼리 수 최적화
    //컬렉션은 지연 로딩 유지하고 yaml 파일 hibernate 설정(fetch_size)으로 최적화, 페이징 처리
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> odersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {

        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit); //XToOne -> fetch join

        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    private final OrderQueryService orderQueryService;

    //fetch join으로 쿼리 수 최적화
    @GetMapping("/api/v3/orders")
    public List<jpabook.jpashop.service.query.OrderDto> odersV3() {

        return orderQueryService.ordersV3();

//        List<Order> orders = orderRepository.findAllWithItem();
//
//        List<OrderDto> result = orders.stream()
//                .map(o -> new OrderDto(o))
//                .collect(Collectors.toList());
//
//        return result;
    }
    
    //객체 조회 후 DTO 변환
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(OrderDto::new)
//                .map(o -> new OrderDto(o))
                .collect(toList());
        return collect;
    }

    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems; //OrderItem으로 받으면 안 되고 Dto로 맵핑(객체 직접 노출 방지)

        //생성자
        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(toList());

//            order.getOrderItems().stream().forEach(o -> o.getItem().getName());
//            orderItems = order.getOrderItems();
        }
    }

    @Data
    static class OrderItemDto{
        private String itemName;
        private int orderPrice;
        private int count;

        //생성자
        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName(); //강제 초기화
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

    //객체 조회 후 그대로 반환
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();

            //프록시 강제 초기화(lazy loading)
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

}
