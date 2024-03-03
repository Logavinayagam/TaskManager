import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.sound.sampled.*;
import java.util.Timer;

public class TaskManagerApp extends JFrame {
    private DefaultListModel<Task> taskListModel;
    private JList<Task> taskList;
    private JTextField taskInput;
    private JComboBox<Integer> yearComboBox;
    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> dayComboBox;
    private JComboBox<Integer> hourComboBox;
    private JComboBox<Integer> minuteComboBox;
    private JButton addButton;
    private JButton doneButton;
    private JButton removeButton;
    private List<Task> tasksWithDueDates;
    private JPanel contentPane;
    private java.util.Timer reminderTimer;
    private Image backgroundImage;

    public TaskManagerApp() {
        setTitle("Task Manager");
        setSize(510, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        // Initialize the tasksWithDueDates list
        tasksWithDueDates = new ArrayList<>();

        // Create a content pane with a gradient background
        contentPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        setContentPane(contentPane);

        // Load the background image
        try {
            backgroundImage = ImageIO.read(new File("img/background2.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create a panel for user input
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        inputPanel.setOpaque(false); // Make the panel background transparent
        //inputPanel.setBackground(new Color(240, 248, 255)); // Light Steel Blue
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Task Description Label and Input
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 10);
        inputPanel.add(new JLabel("Task Description:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        taskInput = new JTextField(20);
        inputPanel.add(taskInput, gbc);



        // Due Date and Time Selection
        // Initialize yearComboBox
        yearComboBox = new JComboBox<>();
        yearComboBox.addItem(2023); // Add your desired years here
        yearComboBox.addItem(2024);
        // Add more years as needed

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        inputPanel.add(new JLabel("Year:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        inputPanel.add(yearComboBox, gbc);

        // Initialize monthComboBox
        monthComboBox = new JComboBox<>();
        String[] months = {"January", "February", "March", "April", "May", "June", "July",
                "August", "September", "October", "November", "December"};
        for (String month : months) {
            monthComboBox.addItem(month);
        }

        gbc.gridx = 2;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Month:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 1;
        inputPanel.add(monthComboBox, gbc);

        // Initialize dayComboBox
        dayComboBox = new JComboBox<>();
        for (int i = 1; i <= 31; i++) {
            dayComboBox.addItem(i);
        }
        gbc.gridx = 4;
        gbc.gridy = 1;

        inputPanel.add(new JLabel("Date:"), gbc);

        gbc.gridx = 5;
        gbc.gridy = 1;
        inputPanel.add(dayComboBox, gbc); // Add the day combo box to the panel

        // Initialize hourComboBox
        hourComboBox = new JComboBox<>();
        for (int i = 0; i <= 23; i++) {
            hourComboBox.addItem(i);
        }

        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Hour:"), gbc);

        // Initialize minuteComboBox
        minuteComboBox = new JComboBox<>();
        for (int i = 0; i <= 59; i++) {
            minuteComboBox.addItem(i);
        }

        gbc.gridx = 1;
        gbc.gridy = 2;
        inputPanel.add(hourComboBox, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Minute:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 2;
        inputPanel.add(minuteComboBox, gbc);

        // Add Task Button
        addButton = new JButton("Add Task");
        addButton.setBackground(new Color(0, 0, 128)); // Navy Blue
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String taskDescription = taskInput.getText().trim();
                int year = (int) yearComboBox.getSelectedItem();
                String month = (String) monthComboBox.getSelectedItem();
                int day = (int) dayComboBox.getSelectedItem();
                int hour = (int) hourComboBox.getSelectedItem();
                int minute = (int) minuteComboBox.getSelectedItem();

                Calendar dueDateCalendar = Calendar.getInstance();
                int selectedYear = (Integer) yearComboBox.getSelectedItem();

                String selectedMonth = (String) monthComboBox.getSelectedItem();
                int selectedDay = (int) dayComboBox.getSelectedItem();
                int selectedHour = (int) hourComboBox.getSelectedItem();
                int selectedMinute = (int) minuteComboBox.getSelectedItem();

                dueDateCalendar.set(selectedYear, Arrays.asList(months).indexOf(selectedMonth), selectedDay, selectedHour, selectedMinute);


                Date dueDate = dueDateCalendar.getTime();

                if (!taskDescription.isEmpty() && isValidDueDate(dueDate)) {
                    Task task = new Task(taskDescription, dueDate);
                    taskListModel.addElement(task);
                    tasksWithDueDates.add(task);
                    taskInput.setText("");
                    scheduleTaskReminder(task); // Schedule the task reminder
                } else {
                    JOptionPane.showMessageDialog(TaskManagerApp.this, "Invalid due date or task description.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        inputPanel.add(addButton, gbc);

        // Create a panel for the task list
        JPanel listPanel = new JPanel();
        listPanel.setOpaque(false);
        //listPanel.setBackground(new Color(240, 248, 255)); // Light Steel Blue
        listPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        listPanel.setLayout(new BorderLayout());

        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setBackground(new Color(240, 248, 255)); // Light Steel Blue
        taskList.setCellRenderer(new TaskListCellRenderer());

        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.getViewport().setBackground(new Color(240, 248, 255)); // Light Steel Blue

        listPanel.add(scrollPane, BorderLayout.CENTER);

        // Create a panel for buttons (Done, Remove, and Mark as Done)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 248, 255)); // Light Steel Blue
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        doneButton = new JButton("Done");
        doneButton.setBackground(new Color(9, 144, 241)); // blue
        doneButton.setForeground(Color.WHITE);
        doneButton.setFocusPainted(false);
        doneButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = taskList.getSelectedIndex();
                if (selectedIndex != -1) {
                    Task selectedTask = taskListModel.getElementAt(selectedIndex);
                    selectedTask.setDone(true);
                    taskList.repaint(); // Repaint the list to reflect the checkbox change
                }
            }
        });

        removeButton = new JButton("Remove Task");
        removeButton.setBackground(new Color(9, 144, 241)); // blue
        removeButton.setForeground(Color.WHITE);
        removeButton.setFocusPainted(false);
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = taskList.getSelectedIndex();
                if (selectedIndex != -1) {
                    taskListModel.remove(selectedIndex);
                    tasksWithDueDates.remove(selectedIndex);
                    taskList.clearSelection(); // Clear the selection
                }
            }
        });

        buttonPanel.add(doneButton);
        buttonPanel.add(removeButton);

        // Create a timer for notifications using java.util.Timer
        reminderTimer = new java.util.Timer(true);
        reminderTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Calendar now = Calendar.getInstance();
                for (Task task : tasksWithDueDates) {
                    if (!task.isDone() && now.after(task.getDueDate())) {
                        showReminderDialog(task); // Show a reminder dialog
                    }
                }
            }
        }, 0, 1000); // Run every 10 sec

        // Add panels to the content pane
        contentPane.setLayout(new BorderLayout());
        contentPane.add(inputPanel, BorderLayout.NORTH);
        contentPane.add(listPanel, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);





    }

    // Custom Task class to hold task description, due date, completion status, snooze time
    private class Task {
        private String description;
        private Date dueDate;
        private boolean done;
        private Date snoozeTime;

        public Task(String description, Date dueDate) {
            this.description = description;
            this.dueDate = dueDate;
            this.done = false;
            this.snoozeTime = null;
        }

        public String getDescription() {
            return description;
        }

        public Date getDueDate() {
            return dueDate;
        }

        public boolean isDone() {
            return done;
        }

        public void setDone(boolean done) {
            this.done = done;
        }

        public Date getSnoozeTime() {
            return snoozeTime;
        }




        public void snooze(int minutes) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.MINUTE, minutes);
            snoozeTime = calendar.getTime();
            updateDueDate(snoozeTime); // Update the due date when snoozing
            updateTaskList();


            // Schedule a new reminder after snoozing for 5 minutes
            TimerTask snoozeReminderTask = new TimerTask() {
                @Override
                public void run() {
                    showReminderDialog(Task.this); // Show a reminder dialog after snoozing
                }
            };

            Timer snoozeReminderTimer = new Timer();
            snoozeReminderTimer.schedule(snoozeReminderTask, minutes * 60 * 1000); // Schedule after 'minutes' minutes
        }

        private void updateTaskList() {
            taskListModel.clear();
            for (Task task : tasksWithDueDates) {
                taskListModel.addElement(task);
            }
            taskList.repaint(); // Repaint the task list to reflect changes
        }


        public void updateDueDate(Date newDueDate) {
            dueDate = newDueDate;
            snoozeTime = null; // Remove snooze time when due date is updated
        }


        @Override
        public String toString() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String status = done ? " (Done)" : "";
            return description + status + " (Due: " + dateFormat.format(dueDate) + ")";
        }
    }

    // Custom cell renderer to display checkboxes, task description, and due date
    private class TaskListCellRenderer extends JPanel implements ListCellRenderer<Task> {
        private JCheckBox checkBox;
        private JLabel descriptionLabel;
        private JLabel dueDateLabel;

        public TaskListCellRenderer() {
            setLayout(new BorderLayout());
            checkBox = new JCheckBox();
            descriptionLabel = new JLabel();
            dueDateLabel = new JLabel();

            JPanel textPanel = new JPanel(new BorderLayout());
            textPanel.add(checkBox, BorderLayout.WEST); // Place the checkbox on the left
            textPanel.add(descriptionLabel, BorderLayout.CENTER);
            textPanel.add(dueDateLabel, BorderLayout.EAST);

            add(textPanel, BorderLayout.CENTER); // Place the text panel in the center
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Task> list, Task task, int index, boolean isSelected, boolean cellHasFocus) {
            checkBox.setSelected(task.isDone());
            descriptionLabel.setText(task.getDescription());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            dueDateLabel.setText("Due: " + dateFormat.format(task.getDueDate()));
            checkBox.setEnabled(!task.isDone()); // Disable the checkbox if the task is done
            setForeground(isSelected ? list.getSelectionForeground():list.getForeground());
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground()); // Use sky blue for selected items
            return this;
        }
    }

    private boolean isValidDueDate(Date dueDate) {
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dueDate);

        // Check if the due date is in the future
        return !calendar.before(now);
    }

    private void scheduleTaskReminder(Task task) {
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        Calendar dueDateCalendar = Calendar.getInstance();
        dueDateCalendar.setTime(task.getDueDate());

        long delay = dueDateCalendar.getTimeInMillis() - now.getTimeInMillis();
        if (delay > 0) {
            TimerTask reminderTask = new TimerTask() {
                @Override
                public void run() {
                    showReminderDialog(task); // Show a reminder dialog
                }
            };
            reminderTimer.schedule(reminderTask, delay);
        }
    }

    private void showReminderDialog(Task task) {
        if (!task.isDone() && tasksWithDueDates.contains(task)) { // Check if the task is not already done
            // Create a reminder dialog
            JDialog dialog = new JDialog(TaskManagerApp.this, "Task Reminder", true);
            dialog.setSize(400, 150);
            dialog.setLayout(new BorderLayout());

            JPanel messagePanel = new JPanel();
            messagePanel.setLayout(new BorderLayout());
            messagePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            messagePanel.setBackground(Color.WHITE);

            JLabel messageLabel = new JLabel("Task Reminder: " + task.getDescription() + " (Due: " + formatDueDate(task.getDueDate()) + ")");
            messagePanel.add(messageLabel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(Color.WHITE);

            JButton snoozeButton = new JButton("Snooze (5 min)");
            snoozeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    task.snooze(5); // Snooze the task for 5 minutes
                    dialog.dispose();
                }
            });

            JButton dismissButton = new JButton("Dismiss (Mark as Done)");
            dismissButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    task.setDone(true); // Mark the task as done
                    dialog.dispose();
                    updateTaskList(); // Update the task list to reflect the task completion
                }
            });

            buttonPanel.add(snoozeButton);
            buttonPanel.add(dismissButton);

            dialog.add(messagePanel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            // Play a  (you can replace the path with the sound file you want)
            try {
                String soundPath = "sound/file_example_WAV_1MG.wav";
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(soundPath).getAbsoluteFile());
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            dialog.setVisible(true);
        }
    }


    private String formatDueDate(Date dueDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return dateFormat.format(dueDate);
    }

    private void updateTaskList() {
        taskListModel.clear();
        for (Task task : tasksWithDueDates) {
            taskListModel.addElement(task);
        }
        taskList.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TaskManagerApp app = new TaskManagerApp();
                app.setVisible(true);
            }
        });
    }
}
