package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class MemberForm {
    @NotBlank(message = "회원 이름은 공백일 수 없습니다.")
    private String name;
    private String city;
    private String street;
    private String zipcode;
}
