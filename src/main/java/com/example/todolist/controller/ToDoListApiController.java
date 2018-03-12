package com.example.todolist.controller;

import com.example.todolist.exceptions.NotFoundException;
import com.example.todolist.model.ToDoEntry;
import com.example.todolist.model.ToDoList;
import com.example.todolist.repository.EntryRepository;
import com.example.todolist.repository.ListRepository;
import java.util.Collection;
import javax.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class ToDoListApiController {

    private final ListRepository listRepository;
    private final EntryRepository entryRepository;

    public ToDoListApiController(ListRepository listRepository,
            EntryRepository entryRepository) {
        this.listRepository = listRepository;
        this.entryRepository = entryRepository;
    }

    /**
     * Returns available lists with code 200
     */
    @GetMapping
    public Collection<ToDoList> getLists() {
        return listRepository.findAll();
    }

    /**
     * Lists all entries in the specified list, 404 if list not found
     */
    @GetMapping("/{listId}")
    public Collection<ToDoEntry> getListEntries(@PathVariable Long listId) {
        return entryRepository.findAllByListId(listId);
    }

    /**
     * Returns 201 and new entity if operation successful or 400 if invalid data supplied.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ToDoList createList(@RequestBody @Valid ToDoList list) {
        return listRepository.save(list);
    }

    /**
     * Returns 201 and new entity if operation successful or 400 if invalid data supplied.
     * Note that creating to do entries with description longer than 16k chars is
     * not allowed!
     */
    @PostMapping("/{listId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void createEntry(@PathVariable Long listId, @RequestBody @Valid ToDoEntry entry) {
        ToDoList list = ensureExists(listRepository.findOne(listId));
        entry.setList(list);
        entryRepository.save(entry);
    }

    /**
     * Returns 200 if successful, 404 if no such list id is found
     */
    @DeleteMapping("/{listid}")
    public void deleteList(@PathVariable Long listId) {
        listRepository.delete(listId);
    }

    /**
     * Deletes given entry if list and entry is valid. Return 404 if ether list or entry id is incorrect.
     * Return 400 if specified entry ID does not belong to the list.
     */
    @DeleteMapping("/{entryId}/{listId}")
    public void deleteEntry(@PathVariable Long listId, @PathVariable Long entryId) {
        ToDoList list = ensureExists(listRepository.findOne(listId));
        ToDoEntry entry = entryRepository.findOne(entryId);
        if (entry.getList() != list) {
            throw new IllegalArgumentException();
        }
        entryRepository.delete(entry);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFound() {
        // No-op, return empty 404
    }

    private static <T> T ensureExists(T object) {
        if (object == null) {
            throw new NotFoundException();
        }
        return object;
    }
}
