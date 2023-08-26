package jpabook.jpashop;

import lombok.Data;

@Data
public class Jaeuk {
    private String name;
    private Long socialNumber;
    private int age;
    private String basicAddress;
    private String detailAddress;

    @Override
    public String toString() {
        return "Jaeuk{" +
                "name='" + name + '\'' +
                ", socialNumber=" + socialNumber +
                ", age=" + age +
                ", basicAddress='" + basicAddress + '\'' +
                ", detailAddress='" + detailAddress + '\'' +
                '}';
    }
}