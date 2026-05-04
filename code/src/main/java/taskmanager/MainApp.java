package taskmanager;

import java.time.LocalDateTime;

import javax.swing.SwingUtilities;

import taskmanager.api.TaskManager;
import taskmanager.model.Task;
import taskmanager.ui.SmartTaskManagerFrame;

/**
 * Main entry point for the Smart Task Manager application.
 * Demonstrates Reactive Programming and API integration.
 */
public class MainApp {

    /**
     * Application starter.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        
        /** 
         * Technical Pillar 1: Using Builder pattern for decoupled service initialization.
         */
        TaskManager tm = TaskManager.builder()
                .withWeatherApiKey("3295492b9a69850c9502ec1820780859")
                .build();

        /** 
         * Technical Pillar 1: Leveraging Java Records for immutable data modeling.
         */
        tm.addTask(new Task(
                "task-001", "Morning run", "Outdoor exercise",
                LocalDateTime.now().plusHours(2), true
        ));
        
        tm.addTask(new Task(
                "task-002", "Coding session", "Java development",
                LocalDateTime.now().plusHours(4), false
        ));

        /** 
         * Technical Pillar 3: Thread-safe GUI launching on the Event Dispatch Thread (EDT).
         */
        SwingUtilities.invokeLater(() -> {
            SmartTaskManagerFrame frame = new SmartTaskManagerFrame(tm);
            frame.setLocationRelativeTo(null); 
            frame.setVisible(true);
        });
    }
}