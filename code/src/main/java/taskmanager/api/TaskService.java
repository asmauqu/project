package taskmanager.api;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import taskmanager.model.Task;
import taskmanager.exception.InvalidTaskException;
import taskmanager.exception.TaskNotFoundException;

import java.util.List;

/**
 * Service for managing task operations using Reactive Programming.
 * 
 * Technical Pillar 2: Reactive Programming (Flux/Mono).
 * Technical Pillar 5: Documentation and Annotations.
 */
public interface TaskService {

    /**
     * Adds a new task to the system asynchronously.
     * 
     * @param task The task to be added.
     * @return A Mono<Void> that completes when the task is added.
     * @throws InvalidTaskException if the task data is incomplete or invalid.
     */
    Mono<Void> addTask(Task task);

    /**
     * Removes a task from the system by its ID.
     * 
     * @param taskId The unique ID of the task to be removed.
     * @return A Mono<Void> that completes when the task is removed.
     * @throws TaskNotFoundException if no task exists with the given ID.
     */
    Mono<Void> removeTask(String taskId);

    /**
     * Finds a specific task by its unique ID.
     * 
     * @param taskId The ID of the task to search for.
     * @return A Mono containing the task if found, or empty if not.
     */
    Mono<Task> findTaskById(String taskId);

    /**
     * Retrieves all tasks as a reactive stream.
     * 
     * @return A Flux emitting all tasks in the system.
     */
    Flux<Task> findAllTasks();

    /**
     * Retrieves all tasks and collects them into a list.
     * Useful for integration with non-reactive components.
     * 
     * @return A Mono containing a list of all tasks.
     */
    Mono<List<Task>> findAllTasksAsList();
}