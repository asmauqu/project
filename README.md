# Smart Task Manager Project 

## Description of the Project

This project is a task management system developed for our university assignment. It features a modern Java Swing UI and uses reactive programming (Project Reactor) to handle tasks and weather API integration without freezing the user interface.

## How to Run the App

1. Make sure you have **Java JDK** installed on your laptop.
2. Download the project and open it in ** VS Code**.
3. Open the file `MainApp.java` located in the` taskmanager` package.
4. Click **Run** to start the application.

## API Key Configuration

To make the weather-sensitive features work, you need an API key:
*   Go to `MainApp.java`.
*   Find the code where we build the `TaskManager`.
*   Replace `"YOUR_API_KEY_HERE"` with your actual API key from the weather provider.

## Code Example: Adding a Task Reactively

As required by the project instructions, here is an example of how the `TaskManager `is used to add a new task using Mono and **Custom Exceptions**:

``` Java

// Check if the title is empty before adding
if (title.isBlank()) {
    throw new InvalidTaskException("Task title cannot be empty.");
}

// Adding the task reactively to keep the UI smooth
Mono.fromCallable(() -> {
    taskManager.addTask(newTask); 
    return newTask;
})
.subscribeOn(Schedulers.boundedElastic()) // Run in the background
.subscribe(
    addedTask -> SwingUtilities.invokeLater(() -> {
        // Update the table once the task is added
        tableModel.addRow(new Object[]{addedTask.id(), addedTask.title()});
        statusLabel.setText("Task added successfully!");
    }),
    error -> SwingUtilities.invokeLater(() -> 
        statusLabel.setText("Error: " + error.getMessage())
    )
);
