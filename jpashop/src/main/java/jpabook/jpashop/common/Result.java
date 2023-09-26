package jpabook.jpashop.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 해당 내부 클래스는 JSON 포맷의 확장성을 위해서 만들었다.
 * API 스펙의 유연성과 확장성을 위해 JSON 리스트로 바로 반환하지 말고, 감싸서 반환해야 한다.
 * 예를 들어, { "data" : [{}, {}, ...] }가 아니라 [{}, {}, ...]으로 만들면
 * 고객이 요구사항으로 count같은 파라미터를 넣어달라 했을 때, 확장하기 어렵다.
 * 그래서 API 스펙의 확장성을 위해 "data"로 JSON 리스트를 감싸주는 Result클래스를 생성
 */
@Data
@AllArgsConstructor
public class Result<T> {
    private T data;
}
