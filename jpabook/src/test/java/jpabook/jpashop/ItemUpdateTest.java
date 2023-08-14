package jpabook.jpashop;

import jpabook.jpashop.domain.item.Book;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ItemUpdateTest {

    @Autowired
    EntityManager em;

    @Test
    public void updateTest() throws Exception{
        Book book = em.find(Book.class, 1L);

        // transaction
        book.setName("asdf");

        // transaction commit
        // -> dirty checking ; 변경 감지(flush할때)
        // -> update query 자동 생성해서 변경된 부분 저장




    }
}
