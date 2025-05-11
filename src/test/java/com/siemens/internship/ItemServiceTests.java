package com.siemens.internship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ItemServiceTests {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    private Item savedItem;

    @BeforeEach
    void fillList() {
        savedItem = itemService.save(new Item(null, "New item 1", "Description1", "NEW", "email1@gmail.com"));
        itemService.save(new Item(null, "New item 2", "Description2", "NEW", "email2@gmail.com"));
    }

    @Test
    void findAllTest() {
        List<Item> items = itemService.findAll();

        assertThat(items).isNotNull();
        assertThat(items).isNotEmpty();
    }

    @Test
    void findByIdTest() {
        //try to find item by id
        Item foundItem = itemService.findById(savedItem.getId()).orElse(null);

        //verify
        assertThat(foundItem).isNotNull();
        assertThat(foundItem.getName()).isEqualTo("New item 1");

        //invalid item test
        Item none = itemService.findById(999L).orElse(null);
        assertThat(none).isNull();
    }

    @Test
    void saveTest() {
        //save a new item
        Item newItem = new Item(null, "New item", "Description", "NEW", "email@gmail.com");
        savedItem = itemService.save(newItem);

        //verify
        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getId()).isNotNull();
        assertThat(savedItem.getName()).isEqualTo("New item");
    }

    @Test
    void DeleteByIdTest() {
        //verify if item exists
        assertThat(itemRepository.existsById(savedItem.getId())).isTrue();

        itemService.deleteById(savedItem.getId());

        //verify if item is deleted
        assertThat(itemRepository.existsById(savedItem.getId())).isFalse();

        //delete non-existent item
        itemService.deleteById(999L);

    }

    @Test
    void processItemsAsyncTest() throws Exception{
        CompletableFuture<List<Item>> future = itemService.processItemsAsync();

        //wait for completion
        List<Item> processeditems = future.get();

        //verify
        assertThat(processeditems.get(0).getStatus()).isEqualTo("PROCESSED");
        assertThat(processeditems.get(1).getStatus()).isEqualTo("PROCESSED");
    }
}
