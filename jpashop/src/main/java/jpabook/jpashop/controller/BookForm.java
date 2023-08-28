package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class BookForm {
    //TODO: 검증(validation 넣어보기)
    private Long id;
    @NotBlank(message = "책의 제목은 공백일 수 없습니다.")
    private String name;
    @NotBlank(message = "책의 가격은 공백일 수 없습니다.")
    private int price;
    @NotBlank(message = "책의 재고는 공백일 수 없습니다.")
    private int stockQuantity;

    @NotBlank(message = "작가는 공백일 수 없습니다.")
    private String author;
    private String isbn;
}
