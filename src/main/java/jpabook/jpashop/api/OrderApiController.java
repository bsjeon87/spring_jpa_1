package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("api/v1/orders")
    private List<Order> ordersV1() {
        List<Order> orders = orderRepository.findAllByQueryDsl(new OrderSearch());
        for (Order order : orders) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                orderItem.getItem().getName();
            }
        }
        return orders;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
    }

    Long tempId;
    Long tempId2;
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        tempId  = orders.get(0).getId();
        tempId2 = orders.get(1).getId();
        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
    }

    @GetMapping("/api/v3/orders_test")
    public List<OrderDto> ordersV3_test() {
        Order order = orderRepository.findOne(tempId);
        //Order order2 = orderRepository.findOne(tempId2);
        List<OrderDto> newArray = new ArrayList<>();
        OrderDto orderDto = new OrderDto(order);
       // OrderDto orderDto2 = new OrderDto(order2);
        newArray.add(orderDto);
        //newArray.add(orderDto2);
        return newArray;
    }

    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);


        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

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


    //@Data // ???????????? ????????????. toString ???????????????. @Getter??? ??????????????? ????????? ?????? ??????.
    @Getter
    private class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
       // private List<OrderItem> orderItems; // ????????? entity??? ???????????? ??????.
       private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();

           // order.getOrderItems().stream().forEach(i -> i.getItem().getName()); // ?????? ??????.
           // order.getOrderItems().stream().map(i -> i.getItem().getName()); //map ?????? ????????????. ?????? ????????? ????????? ?????? ?????????.
           // order.getOrderItems().stream().map(i -> i.getItem().getName()).collect(Collectors.toList()); // map?????? ?????????.
            orderItems = order.getOrderItems().stream().map(i -> new OrderItemDto(i)).collect(toList());
        }
    }

    @Getter
    static class OrderItemDto {
        //private Long id;
        //private Item item;
        private String itemName;
        private int orderPrice;//????????????
        private int count;//????????????
        public OrderItemDto(OrderItem orderItem) {
            this.orderPrice = orderItem.getOrderPrice();
            this.count = orderItem.getCount();
            this.itemName = orderItem.getItem().getName();
        }
    }
}
