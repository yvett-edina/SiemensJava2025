package com.siemens.internship;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    // changed the return type to Object so I can return the error messages
    // or the Item upon successful creation
    @PostMapping
    public ResponseEntity<Object> createItem(@Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            List<String> errorMsg = result.getAllErrors().stream().map(ObjectError::getDefaultMessage).toList();

            // returns 400 Bad Request with validation error instead of returning 201 Created
            return new ResponseEntity<>(errorMsg, HttpStatus.BAD_REQUEST);
        }
        // returns 201 Created with validation error instead of returning 400 Bad Request
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            item.setId(id);
            return new ResponseEntity<>(itemService.save(item), HttpStatus.OK);
        } else {
            // changed it so we know the item was not found
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteById(id);
        // changed it so we get more appropriate message
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/process")
    public CompletableFuture<ResponseEntity<List<Item>>> processItems() {
        return itemService.processItemsAsync().thenApply(items -> new ResponseEntity<>(items, HttpStatus.OK));
    }
}
