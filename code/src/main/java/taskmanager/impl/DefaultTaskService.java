package taskmanager.impl;

import java.util.ArrayList;
import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import taskmanager.api.TaskService;
import taskmanager.exception.TaskNotFoundException;
import taskmanager.model.Task;

/**
 * Implementation of the TaskService interface.
 * Technical Pillar 1: Specifications and Error Handling.
 * Technical Pillar 2: Reactive Programming with Flux.
 */
public class DefaultTaskService implements TaskService {
    private final List<Task> storage = new ArrayList<>();

    /**
     * Adds a task to the in-memory storage.
     *
     * @param task the task to persist
     * @return a {@link Mono} that completes when the task is added
     */
    @Override
    public Mono<Void> addTask(Task task) {
        if (task == null) {
            return Mono.error(new IllegalArgumentException("Task cannot be null"));
        }
        storage.add(task);
        return Mono.empty();
    }

    /**
     * Removes a task by its identifier.
     *
     * @param id the ID of the task to remove
     * @return a {@link Mono} that completes when the task is removed
     */
    @Override
    public Mono<Void> removeTask(String id) {
        boolean removed = storage.removeIf(t -> t.id().equals(id));
        if (!removed) {
            return Mono.error(new TaskNotFoundException("Task ID " + id + " not found."));
        }
        return Mono.empty();
    }

    /**
     * Finds a task by its ID.
     *
     * @param taskId the ID of the task to lookup
     * @return a {@link Mono} emitting the task if found, or empty if absent
     */
    @Override
    public Mono<Task> findTaskById(String taskId) {
        return Flux.fromIterable(storage)
                .filter(t -> t.id().equals(taskId))
                .next();
    }

    /**
     * Retrieves all tasks as a reactive stream.
     *
     * @return a {@link Flux} emitting all stored tasks
     */
    @Override
    public Flux<Task> findAllTasks() {
        return Flux.fromIterable(storage);
    }

    /**
     * Retrieves all tasks collected into a list.
     *
     * @return a {@link Mono} emitting the list of stored tasks
     */
    @Override
    public Mono<List<Task>> findAllTasksAsList() {
        return Flux.fromIterable(storage).collectList();
    }
}