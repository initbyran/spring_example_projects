package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /**
     * 상품 등록 폼
     * @param model
     * @return
     */
    @GetMapping(value = "/items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }

    /**
     * 상품 등록 저장
     * @param form
     * @return
     */
    @PostMapping(value = "/items/new")
    public String create(BookForm form) {

        // CREATE BOOK 같은 메소드를 만드는 것이 더 좋음
        // -> SETTER 사용 지양
        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());
        itemService.saveItem(book);
        return "redirect:/items";
    }

    /**
     * 상품 목록 조회
     * @param model
     * @return
     */
    @GetMapping(value = "/items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    /**
     * 상품 수정 폼
     * @param itemId
     * @param model
     * @return
     */
    @GetMapping(value = "/items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {

        Book item = (Book) itemService.findOne(itemId);

        BookForm form = new BookForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());
        model.addAttribute("form", form);
        return "items/updateItemForm";
    }

    /**
     * 상품 수정
     * -> 권한 설정이 필요하다
     * @param form
     * @return
     */
    @PostMapping(value = "/items/{itemId}/edit")
    public String updateItem(@PathVariable Long itemId, @ModelAttribute("form") BookForm form) {

        // 준영속 엔티티 : id가 이미 셋팅된 객체
        // -> 데이터베이스에 이미 식별자가 있었던 것이므로 영속성 컨텍스트가 더이상 관리하지않는 엔티티
        // -> JPA가 관리하지 않으므로 dirty checking이 되지 않는다
        // -> 데이터베이스 업데이트가 발생하지 않는다
        // -> 해결 : 변경 감지 기능 사용 / 병합(merge) 사용

        // 트랜잭션이 있는 서비스에서 데이터 변경 직접 할 것
//        Book book = new Book();
//        book.setId(form.getId());
//        book.setName(form.getName());
//        book.setPrice(form.getPrice());
//        book.setStockQuantity(form.getStockQuantity());
//        book.setAuthor(form.getAuthor());
//        book.setIsbn(form.getIsbn());
//        itemService.saveItem(book);

        // update할 내용이 많을 때는 DTO 별도 생성
        itemService.updateItem(itemId, form.getName(), form.getPrice(), form.getStockQuantity());
        return "redirect:/items";
    }
}
