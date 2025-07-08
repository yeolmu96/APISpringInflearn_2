package jpabook.jpashop.service.query;

import jpabook.jpashop.domain.OrderItem;
import lombok.Data;

@Data
public class OrderItemDto{
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