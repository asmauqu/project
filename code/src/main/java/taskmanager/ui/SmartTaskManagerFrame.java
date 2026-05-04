package taskmanager.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import taskmanager.api.TaskManager;
import taskmanager.exception.InvalidTaskException;
import taskmanager.exception.TaskNotFoundException;
import taskmanager.model.Task;

/**
 * Technical Pillar 2 & 3: Reactive UI for Task Management.
 * Provides a comprehensive GUI for managing tasks with weather-sensitive scheduling.
 */
public class SmartTaskManagerFrame extends JFrame {
    private final TaskManager taskManager;
    private final DefaultTableModel tableModel;
    private final JLabel statusLabel = new JLabel(" Ready");
    private final JTextField taskTitleField;
    private final JCheckBox weatherSensitiveCheck;
    private JTable taskTable;
    private List<Task> loadedTasks;

    public SmartTaskManagerFrame(TaskManager taskManager) {
        this.taskManager = taskManager;
        setTitle("Smart Task Manager");
        setSize(700, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Initialize input panel components
        taskTitleField = new JTextField(20);
        weatherSensitiveCheck = new JCheckBox("Weather Sensitive");
        
        // Setup UI Layout
        setupInputPanel();
        
        // UI Components
        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Sensitive", "Status"}, 0);
        taskTable = new JTable(tableModel);
        JButton btn = new JButton("Evaluate Weather");

        add(new JScrollPane(taskTable), BorderLayout.CENTER);
        JPanel p = new JPanel(new BorderLayout());
        p.add(btn, BorderLayout.CENTER); p.add(statusLabel, BorderLayout.SOUTH);
        add(p, BorderLayout.SOUTH);

        // Load tasks reactively [Pillar 3]
        Mono.fromCallable(taskManager::getTasks)
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(tasks -> SwingUtilities.invokeLater(() -> {
                this.loadedTasks = tasks;
                tasks.forEach(t -> tableModel.addRow(new Object[]{t.id(), t.title(), t.weatherSensitive(), "Pending"}));
            }));

        // Button Logic [Pillar 2]
        btn.addActionListener(e -> {
            int row = taskTable.getSelectedRow();
            if (row < 0) return;
            
            Task task = loadedTasks.get(row);
            statusLabel.setText(" Analyzing...");
            taskManager.evaluateTasks(task, "Colombo")
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(f -> SwingUtilities.invokeLater(() -> {
                    tableModel.setValueAt("Optimal", row, 3);
                    statusLabel.setText(" Complete: " + f.condition());
                }));
        });
    }

    /**
     * Sets up the input panel at the NORTH position of the frame.
     * 
     * Technical Pillar 1 (Exceptions): Uses try-catch to validate task input and throw InvalidTaskException.
     * Technical Pillar 2 (Reactive): Calls taskManager.addTask() using Mono to handle task addition reactively.
     * Technical Pillar 5 (Documentation): Provides comprehensive Javadoc for UI setup.
     * 
     * The input panel contains:
     * - A label "Task Title:"
     * - A JTextField for entering task title
     * - A JCheckBox for marking weather sensitivity
     * - An "Add Task" button to add the task reactively
     * 
     * When the "Add Task" button is clicked:
     * 1. Validates that the task title is not empty
     * 2. Creates a new Task with random UUID and current time + 1 hour as due date
     * 3. Adds the task reactively using Mono
     * 4. Updates the JTable automatically
     * 5. Shows error dialog if validation fails (empty title)
     */
    private void setupInputPanel() {
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Add label
        inputPanel.add(new JLabel("Task Title:"));
        
        // Add title text field
        inputPanel.add(taskTitleField);
        
        // Add weather sensitivity checkbox
        inputPanel.add(weatherSensitiveCheck);
        
        // Add Task button with reactive listener
        JButton addButton = new JButton("Add Task");
        addButton.addActionListener(e -> {
            try {
                // Pillar 1: Validation with custom exception
                String title = taskTitleField.getText();
                if (title == null || title.isBlank()) {
                    throw new InvalidTaskException("Task title cannot be empty.");
                }
                
                // Generate random ID and set due date to 1 hour from now
                String taskId = "task-" + UUID.randomUUID().toString();
                LocalDateTime dueDateTime = LocalDateTime.now().plusHours(1);
                boolean weatherSensitive = weatherSensitiveCheck.isSelected();
                
                // Create new task
                Task newTask = new Task(
                    taskId,
                    title,
                    "User-added task",
                    dueDateTime,
                    weatherSensitive
                );
                
                // Pillar 2: Reactive task addition using Mono
                Mono.fromCallable(() -> {
                    taskManager.addTask(newTask);
                    return newTask;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                    addedTask -> SwingUtilities.invokeLater(() -> {
                        // Update table with new task
                        loadedTasks.add(addedTask);
                        tableModel.addRow(new Object[]{
                            addedTask.id(),
                            addedTask.title(),
                            addedTask.weatherSensitive(),
                            "Pending"
                        });
                        // Clear input fields
                        taskTitleField.setText("");
                        weatherSensitiveCheck.setSelected(false);
                        statusLabel.setText(" Task added: " + addedTask.title());
                    }),
                    error -> SwingUtilities.invokeLater(() -> 
                        statusLabel.setText(" Error: " + error.getMessage())
                    )
                );
                
            } catch (InvalidTaskException ex) {
                // Pillar 1: Show error dialog on validation failure
                JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Invalid Task",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
        
        inputPanel.add(addButton);
        
        // Add Delete button with reactive listener
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> handleDeleteTask());
        
        inputPanel.add(deleteButton);
        
        // Add input panel at the NORTH position
        add(inputPanel, BorderLayout.NORTH);
    }

    /**
     * Handles the deletion of a selected task from the table.
     * 
     * Technical Pillar 1 (Exceptions): Handles TaskNotFoundException for failed deletions.
     * Technical Pillar 2 (Reactive): Uses Mono to delete the task reactively from the task manager.
     * Technical Pillar 5 (Documentation): Comprehensive Javadoc explaining the deletion workflow.
     * 
     * When the "Delete Selected" button is clicked:
     * 1. Validates that a task is selected in the table (shows warning if not)
     * 2. Retrieves the task ID from the selected row
     * 3. Deletes the task reactively using Mono
     * 4. Removes the task from the JTable automatically
     * 5. Removes the task from the loadedTasks list to keep in sync
     * 6. Shows error message if TaskNotFoundException occurs
     */
    private void handleDeleteTask() {
        int row = taskTable.getSelectedRow();
        
        // Pillar 1: Check if a row is selected
        if (row < 0) {
            JOptionPane.showMessageDialog(
                this,
                "Please select a task to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        Task selectedTask = loadedTasks.get(row);
        String taskId = selectedTask.id();
        
        // Pillar 2: Reactive task deletion using Mono
        Mono.fromCallable(() -> {
            taskManager.removeTask(taskId);
            return taskId;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe(
            deletedTaskId -> SwingUtilities.invokeLater(() -> {
                // Remove from table
                tableModel.removeRow(row);
                // Remove from loadedTasks list to keep in sync
                loadedTasks.remove(row);
                statusLabel.setText(" Task deleted: " + selectedTask.title());
            }),
            error -> SwingUtilities.invokeLater(() -> {
                // Pillar 1: Handle TaskNotFoundException
                if (error instanceof TaskNotFoundException) {
                    JOptionPane.showMessageDialog(
                        this,
                        error.getMessage(),
                        "Task Not Found",
                        JOptionPane.ERROR_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "Error deleting task: " + error.getMessage(),
                        "Deletion Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
                statusLabel.setText(" Error: " + error.getMessage());
            })
        );
    }
}