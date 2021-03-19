package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

//엔티티가 아닌 특정화면에 핏한 repository 부분을 떼어냄(핵심 비즈니스로직만 남겨두기위해).
@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;


    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders();

        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });
        return result;
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id =  :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();

    }

    private List<OrderQueryDto> findOrders() {
        //OrderQueryDto의 list collection은 new로 생성할 수 없음. query의 new operation은 db의 select문과 대응 되는데, 컬렉션은
        //db 쿼리 한줄에 결과를 표현할 수 없으므로. collection을 제외하고 우선 만듬.
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }
}
