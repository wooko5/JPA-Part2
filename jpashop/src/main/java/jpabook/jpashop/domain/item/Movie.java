package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("M") // 싱글 테이블 전략이기때문에 저장할 때 구분할 수 있는 value를 설정하는 것!
@Getter @Setter
public class Movie extends Item{
    private String director;
    private String actor;
}
