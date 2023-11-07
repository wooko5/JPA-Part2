package jpabook.jpashop.domain.item;

import lombok.Data;

@Data
public class ItemType {

    private String code;
    private String name;

    public ItemType(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
