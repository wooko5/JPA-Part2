package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class BookForm {
    private Long id;

    @NotBlank(message = "책의 제목은 공백일 수 없습니다.") // @NotBlank는 CharSequence만 지원, 해당 사실을 몰라서 Integer형에 삽질함,,,
    private String name;

    @NotNull(message = "책의 가격은 공백일 수 없습니다.") // @NotEmpty는 CharSequence, Collection, Map, Array만 지원
    private Integer price;

    @NotNull(message = "책의 재고는 공백일 수 없습니다.")
    private Integer stockQuantity;

    @NotBlank(message = "작가는 공백일 수 없습니다.")
    private String author;
    private String isbn;
    private String itemType;
}
