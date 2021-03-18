package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

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
                .collect(Collectors.toList());
    }

    //@Data // 여러개를 만들어줌. toString 이런것등등. @Getter만 만들어줘도 문제가 되지 않음.
    @Getter
    private class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItem> orderItems; // 여전히 entity가 노출되어 있음.

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();

            order.getOrderItems().stream().forEach(i -> i.getItem().getName()); // 단순 처리.
           // order.getOrderItems().stream().map(i -> i.getItem().getName()); //map 내용 수행안됨. 뒤에 연결된 함수에 따라 호출됨.
           // order.getOrderItems().stream().map(i -> i.getItem().getName()).collect(Collectors.toList()); // map내용 처리됨.
            orderItems = order.getOrderItems();
        }
    }
}
