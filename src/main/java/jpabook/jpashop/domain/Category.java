package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Category {

    @Id @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private String name;

    @ManyToMany(fetch = FetchType.LAZY)                      //예제차원으로 사용. 실제 쓰면안됨.
    @JoinTable(name = "category_item", //다대다 개념이 db에없음.
        joinColumns = @JoinColumn(name = "category_id"), //현재 객체를 매핑할 컬럼
        inverseJoinColumns = @JoinColumn(name = "item_id") //반대편에 조인될 컬럼
    )
    private List<Item> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY) //부모와의 관계 (바로 아래에 child와 관계를 맺음) - 부모의 id만 db에서는 가지고 있으면 child와 parent 각 각 조회가능.
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY) //바로위에 parent를 가르킴.
    private List<Category> child = new ArrayList<>();

    //연관관계 메서드
    public void addChildCategory(Category child) {
        this.child.add(child);
        child.setParent(this);
    }

}
