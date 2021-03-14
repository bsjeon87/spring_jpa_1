package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.exception.NotEnoughStockException;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@RunWith(SpringRunner.class) //spring이랑 엮어서 함께 테스트하겠다.
@SpringBootTest //spring boot를 띄운 상태로 테스트하겠다(없으면 autowired가 실패함.). 일반적으로 독립적으로 테스트하는게 더좋음( 더 빠르고 간결하므로)
@Transactional // spring의 transactional은  test에서 commit 하지 않고 rollback을 함.
public class OrderServiceTest {

    @Autowired EntityManager em;
    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    private Member createMember() {
        Member member =new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울",  "경기", "123-123"));
        em.persist(member);
        return member;
    }

    private Item createBook(String name, int price, int stockQuantity) {
        Item book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    @Test
    public void 상품주문() throws Exception {
        //given
        Member member = createMember();
        Item book = createBook("시골 jpa", 10000, 10);

        int orderCount = 2;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order order = orderRepository.findOne(orderId); //만약 영속성 컨텍스트에 조건에 맞는게 없다면 실제 select 쿼리를 날림.(rollback되더라도..)

        Assert.assertEquals("상품주문시 상태는 ORDER", OrderStatus.ORDER, order.getStatus());
        Assert.assertEquals("주문한 상품 수가 정확해야함", 1, order.getOrderItems().size());
        Assert.assertEquals("주문 가격은 가격 * 수량이다",  10000 * orderCount, order.getTotalPrice());
        Assert.assertEquals("주문 수량만큼 재고가 줄어야한다", 8, book.getStockQuantity());
    }

    @Test(expected = NotEnoughStockException.class)
    public void 상품주문_재고수량초과() throws Exception {
        //given
        Member member = createMember();
        Item book = createBook("시골 jpa", 10000, 10);

        int orderCount = 11;

        //when
        orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Assert.fail("제고 수량 부족 예외가 발생해야 한다");
    }

    @Test
    public void 주문취소() throws Exception {
        //given
        Member member = createMember();
        Item book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        Assert.assertEquals("주문 취소시 상태는 CANCEL이다", OrderStatus.CANCEL, getOrder.getStatus());
        Assert.assertEquals("주문이 취소된 상태는 그만큼 재고가 증가해야한다", 10, book.getStockQuantity());
    }


}