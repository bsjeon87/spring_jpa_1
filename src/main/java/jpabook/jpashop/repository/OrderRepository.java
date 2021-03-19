package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    /**
     * 유지보수성 떨어지고 / 버그 만들기 쉬움( string이므로)
     * @param orderSearch
     * @return
     */
    public List<Order> findAllByString(OrderSearch orderSearch) {
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    /**
     * JPA Criteria를 활용하여 동적쿼리 사용. => 유지보수성이 떨어짐.
     * @param orderSearch
     * @return
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" +
                            orderSearch.getMemberName() + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
        return query.getResultList();
    }

    /**
     * using querydsl
     * @param orderSearch
     * @return
     */
    public List<Order> findAllByQueryDsl(OrderSearch orderSearch) {
        JPAQueryFactory query = new JPAQueryFactory(em);
        QOrder order = QOrder.order;
        QMember member = QMember.member;


        //System.out.println("process:" + ProcessHandle.current().pid() + "thread:" + Thread.currentThread().getId());

        return query.select(order)
                    .from(order)
                    .join(order.member, member)
                    .where(statusEq(orderSearch.getOrderStatus()), statusLike(orderSearch.getMemberName()))
                    .limit(1000)
                    .fetch();
    }

    private BooleanExpression statusEq(OrderStatus statusCond) {
        if (statusCond == null) {
            return null;
        }
        return QOrder.order.status.eq(statusCond);
    }

    private BooleanExpression statusLike(String memberName) {
        if(!StringUtils.hasText(memberName)) {
            return null;
        }
        return QMember.member.name.like(memberName);
    }

    public List<Order> findAllWithMemberDelivery() {
        //join fetch이므로 해당 객체에 모든 정보를 퍼올림.
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member" +
                        " join fetch o.delivery d", Order.class).getResultList();

    }

    public List<OrderSimpleQueryDto> findOrderDtos() {
        // join fetch가 아니기 때문에 연관된 객체 중에 select 문에 포함되어 있는 정보만 뽑도록 쿼리를 날림.(좀더 최적화됨)
        return em.createQuery(
                "select new jpabook.jpashop.repository.OrderSimpleQueryDto(o.id, o.member.name, o.orderDate, o.status, d.address)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderSimpleQueryDto.class).getResultList();
    }

    public List<Order> findAllWithItem() {
        //똑같은 객체가 다 관계 만큼 들어감.
        // ( 일대다 관계( order:orderItem) - 쿼리를 하면  늘어난 상태로 db에서 데이터를 가지고 오고
        //   jpa는 객체간의 관계를 정리하여 return함. -> 정리하는 과정에서 일대다 관계때문에 늘어난 부분은 컬렉션으로 변경됨.
        //   변경은 되었지만 쿼리에서 가지고 온 데이터 갯수는 뻥튀기된 컬럼갯수이므로 그만큼 채워서 리턴하게됨. )
        /* return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item i", Order.class)
                .getResultList(); */
        //distinct를 추가해서 order객체 기준으로 중복을 없앰.(db distinct 처리(모든 컬럼이 똑같을때만 없앰) +
        //                                              jpa layer에서 Order 객체단위 중복 제거.) => ** 페이징 불가능함.**
        // 페이징은 일대다 + 패치 조인 인 경우, db에서 페이징작업을 하지 않고, 몽땅 메모리에 올려서 페이징하여 결과를 리턴함.
        // 애플리케이션에서 메모리를 다 잡아 놓고 처러히게됨. 주의해야함..( 페이지 기준이 app/db 사이에 차이가 있음.)
        // 일대다 패치조인이 아닌경우, 페이징 처리를 db에서 할수있음.
        // **DB상 페이징이 안되므로 메모리상에 하여 위험하므로 사용하면 안됨.***
        // ** 일대다 에 추가로 일대다 쿼리를 처리하면 안됨(컬렉션 둘 이상에 패치 조인을 사용한경우, JPA에서 처리하는게 부정확함).
        return em.createQuery(
                "select distinct  o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item i", Order.class)
                .getResultList();

    }
}
