package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if (item.getId() == null) {
            em.persist(item);
        } else {
            Item mergedItem = em.merge(item); //update와 유사. (merged Item이 영속성 컨텍스트에서 관리함. 인자로 들어온 item은 영속객체는 아님.
                                              //merge 기능의 경우 모든 필드를 인자로 넘어온 item 필드로 변경함. (merge안쓰는게 좋음.)
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }
}
