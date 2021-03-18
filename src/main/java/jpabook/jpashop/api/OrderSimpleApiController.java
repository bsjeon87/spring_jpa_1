package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Order
 * Order -> Member (ManyToOne)
 * Order -> Delivery ( OneTo One )
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> orderV1() {
        //단순히 아래방시대로하면 json data 생성시 order<->member 양방향 연관관계에 의해 무한 루프에 빠짐.
        List<Order> all = orderRepository.findAllByQueryDsl(new OrderSearch());
        return all;
    }

}
