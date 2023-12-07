package jpabook.jpashop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JpashopApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpashopApplication.class, args);
    }

//    @Bean //OrderSimpleApiController의 V1을 굳이 쓸려고 만든 코드, 실제로는 엔티티를 직접 노출해서 API를 설계하지 않는다
//    Hibernate5Module hibernate5Module() {
//        Hibernate5Module hibernate5Module = new Hibernate5Module();
//        hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true); // 강제 지연 로딩 설정
//        return hibernate5Module;
//    }

}
