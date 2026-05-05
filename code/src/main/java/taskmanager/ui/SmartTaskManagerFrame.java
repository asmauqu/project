package taskmanager.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // أداة تحويل الوقت
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
    private final JTextField taskDescField;     
    private final JTextField taskDeadlineField; 
    private final JCheckBox weatherSensitiveCheck;
    private JTable taskTable;
    private List<Task> loadedTasks;

    // تعريف صيغة الوقت (السنة-الشهر-اليوم الساعة:الدقيقة)
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public SmartTaskManagerFrame(TaskManager taskManager) {
        this.taskManager = taskManager;
        setTitle("Smart Task Manager");
        setSize(950, 450); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        taskTitleField = new JTextField(10);
        taskDescField = new JTextField(12); 
        // وضعنا مثالاً داخل الحقل ليسهل على المستخدم معرفة الصيغة المطلوبة
        taskDeadlineField = new JTextField("2026-05-05 20:00", 12); 
        weatherSensitiveCheck = new JCheckBox("Weather Sensitive");
        
        setupInputPanel();
        
        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Description", "Due Date", "Sensitive", "Status"}, 0);
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
                tasks.forEach(t -> tableModel.addRow(new Object[]{
                    t.id(), 
                    t.title(), 
                    t.description(), 
                    t.dueDateTime().format(formatter), // عرض الوقت بصيغة مرتبة
                    t.weatherSensitive(), 
                    "Pending"
                }));
            }));

        btn.addActionListener(e -> {
            int row = taskTable.getSelectedRow();
            if (row < 0) return;
            
            Task task = loadedTasks.get(row);
            statusLabel.setText(" Analyzing...");
            taskManager.evaluateTasks(task, "Colombo")
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(f -> SwingUtilities.invokeLater(() -> {
                    tableModel.setValueAt("Optimal", row, 5); 
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
     */
    private void setupInputPanel() {
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        inputPanel.add(new JLabel("Title:"));
        inputPanel.add(taskTitleField);
        
        inputPanel.add(new JLabel("Desc:")); 
        inputPanel.add(taskDescField);
        
        inputPanel.add(new JLabel("Time:")); 
        inputPanel.add(taskDeadlineField);
        
        inputPanel.add(weatherSensitiveCheck);
        
        JButton addButton = new JButton("Add Task");
        addButton.addActionListener(e -> {
            try {
                String title = taskTitleField.getText();
                String desc = taskDescField.getText();
                String timeStr = taskDeadlineField.getText(); // قراءة النص من الحقل

                if (title == null || title.isBlank()) {
                    throw new InvalidTaskException("Task title cannot be empty.");
                }

                // الخطوة الجديدة: تحويل النص إلى LocalDateTime
                LocalDateTime dueDateTime;
                try {
                    dueDateTime = LocalDateTime.parse(timeStr, formatter);
                } catch (Exception ex) {
                    throw new InvalidTaskException("Invalid Time format! Use: yyyy-MM-dd HH:mm");
                }
                
                String taskId = "task-" + UUID.randomUUID().toString();
                
                Task newTask = new Task(
                    taskId, 
                    title, 
                    desc.isBlank() ? "User-added task" : desc, 
                    dueDateTime, // نستخدم الوقت الذي أدخله المستخدم
                    weatherSensitiveCheck.isSelected()
                );
                
                Mono.fromCallable(() -> {
                    taskManager.addTask(newTask);
                    return newTask;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(addedTask -> SwingUtilities.invokeLater(() -> {
                    loadedTasks.add(addedTask);
                    tableModel.addRow(new Object[]{
                        addedTask.id(), 
                        addedTask.title(), 
                        addedTask.description(), 
                        addedTask.dueDateTime().format(formatter), 
                        addedTask.weatherSensitive(), 
                        "Pending"
                    });
                    clearFields();
                }));
            } catch (InvalidTaskException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Task", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        inputPanel.add(addButton);
        
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> handleDeleteTask());
        
        inputPanel.add(deleteButton);
        add(inputPanel, BorderLayout.NORTH);
    }

    private void clearFields() {
        taskTitleField.setText("");
        taskDescField.setText("");
        // نعيد الوقت لقيمة افتراضية بعد الإضافة
        taskDeadlineField.setText("2026-05-05 20:00");
        weatherSensitiveCheck.setSelected(false);
        statusLabel.setText(" Ready");
    }

    private void handleDeleteTask() {
        int row = taskTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a task to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Task selectedTask = loadedTasks.get(row);
        Mono.fromCallable(() -> {
            taskManager.removeTask(selectedTask.id());
            return selectedTask.id();
        })
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe(id -> SwingUtilities.invokeLater(() -> {
            tableModel.removeRow(row);
            loadedTasks.remove(row);
            statusLabel.setText(" Deleted: " + selectedTask.title());
        }));
    }
}