package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    // use thread-safe collection
    private List<Item> processedItems = new CopyOnWriteArrayList<>();
    private AtomicInteger processedCount = new AtomicInteger(0);


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */

    /* Explanation of what was wrong with the code before:
        -processItemsAsync() method is annotated with @Async but doesn't return
         a Future, just a plain List
        -processedItems & processedCount are not thread-safe
        -main thread returns processedItems before the async tasks completes(
        it may still be empty or incomplete bc async tasks still running)
        -the error msg is not clear enough
    */

    //changed the return type bc @Async requires it
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {

        List<Long> itemIds = itemRepository.findAllIds();

        // for processing items asynchronously
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Long id : itemIds) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(100);

                    Item item = itemRepository.findById(id).orElse(null);
                    if (item == null) {
                        return;
                    }

                    item.setStatus("PROCESSED");
                    itemRepository.save(item);
                    processedItems.add(item);

                    processedCount.incrementAndGet();

                } catch (InterruptedException e) {
                    System.out.println("Error processing item with id " + id + ": " + e.getMessage());
                }
            }, executor);

            futures.add(future);
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        return allOf.thenApply(v -> processedItems);
    }
}