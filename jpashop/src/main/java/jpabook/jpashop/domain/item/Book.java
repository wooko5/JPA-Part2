package jpabook.jpashop.domain.item;

import jpabook.jpashop.controller.BookForm;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("B")
@Getter
@Setter
public class Book extends Item {
    private String author;
    private String isbn;

//    private Book(){
//        // new을 통한 생성자를 막기위한 코드
//    }

    /* Book을 위한 정적 팩토리 메서드 */
    public static Book createBook(Long id, String name, int price, int stockQuantity, String author, String isbn) {
        Book book = new Book();
        if (id != null) book.setId(id); //PK가 있다면 준영속 엔티티(Detached Entity)
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        book.setAuthor(author);
        book.setIsbn(isbn);
        return book;
    }
}
