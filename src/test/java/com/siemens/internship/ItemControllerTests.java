package com.siemens.internship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ItemController.class)
public class ItemControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    private Item savedItem;

    @BeforeEach
    void fillList() {
        savedItem = new Item(1L, "New item 1", "Description1", "NEW", "email1@gmail.com");
    }

    @Test
    void getAllItemsTest() throws Exception {
        List<Item> items = List.of(savedItem);

        Mockito.when(itemService.findAll()).thenReturn(items);

        //GET request and verify
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("New item 1"));
    }

    @Test
    void getItemByIdTest() throws Exception {
        Item item = new Item(1L, "New item 1", "Description1", "NEW", "email1@gmail.com");

        Mockito.when(itemService.findById(1L)).thenReturn(java.util.Optional.of(item));

        //GET request and verify
        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New item 1"));
    }

    @Test
    void getItemByIdNotFoundTest() throws Exception {
       Mockito.when(itemService.findById(2L)).thenReturn(java.util.Optional.empty());

        //GET request and verify
        mockMvc.perform(get("/api/items/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createItemTest() throws Exception {
        Item savedItem = new Item(2L, "New item 2", "Description 2", "NEW", "email2@gmail.com");

        Mockito.when(itemService.save(Mockito.any(Item.class))).thenReturn(savedItem);

        //POST request and verify
        mockMvc.perform(post("/api/items")
                .contentType("application/json")
                .content("{\"name\":\"New item 2\",\"description\":\"Description 2\",\"status\":\"NEW\",\"email\":\"email2@gmail.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New item 2"))
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void updateItemTest() throws Exception {
        Item updatedItem = new Item(savedItem.getId(), "Updated item", "Updated description", "UPDATED", "email1_updated@gmail.com");

        Mockito.when(itemService.findById(savedItem.getId())).thenReturn(java.util.Optional.of(savedItem));
        Mockito.when(itemService.save(Mockito.any(Item.class))).thenReturn(updatedItem);

        mockMvc.perform(put("/api/items/" + savedItem.getId())
                        .contentType("application/json")
                        .content("{\"name\":\"Updated item\",\"description\":\"Updated description\",\"status\":\"UPDATED\",\"email\":\"email1_updated@gmail.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated item"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void updateItemNotFoundTest() throws Exception {
        Mockito.when(itemService.findById(2L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(put("/api/items/2")
                        .contentType("application/json")
                        .content("{\"name\":\"Non-existent item\",\"description\":\"Non-existent description\",\"status\":\"NEW\",\"email\":\"nonexistent@gmail.com\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteItemTest() throws Exception {
        Mockito.when(itemService.findById(savedItem.getId())).thenReturn(java.util.Optional.of(savedItem));

        mockMvc.perform(delete("/api/items/" + savedItem.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteItemNotFoundTest() throws Exception {
        Mockito.when(itemService.findById(savedItem.getId())).thenReturn(java.util.Optional.empty());

        mockMvc.perform(delete("/api/items/999"))
                .andExpect(status().isNotFound());
    }
}