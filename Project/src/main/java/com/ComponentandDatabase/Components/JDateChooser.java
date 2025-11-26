package com.ComponentandDatabase.Components;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Custom Date Chooser Component
 */
public class JDateChooser extends JPanel {
    private JTextField dateField;
    private JButton calendarButton;
    private Date selectedDate;
    private SimpleDateFormat dateFormat;
    private String dateFormatString = "dd/MM/yyyy";
    
    public JDateChooser() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        dateFormat = new SimpleDateFormat(dateFormatString);
        
        // Text field to display date
        dateField = new JTextField();
        dateField.setEditable(false);
        dateField.setFont(new Font("Arial", Font.PLAIN, 13));
        dateField.setBackground(Color.WHITE);
        add(dateField, BorderLayout.CENTER);
        
        // Calendar button
        calendarButton = new JButton("📅");
        calendarButton.setPreferredSize(new Dimension(40, 25));
        calendarButton.setFocusPainted(false);
        calendarButton.addActionListener(e -> showCalendar());
        add(calendarButton, BorderLayout.EAST);
    }
    
    public void setDate(Date date) {
        this.selectedDate = date;
        if (date != null) {
            dateField.setText(dateFormat.format(date));
        } else {
            dateField.setText("");
        }
    }
    
    public Date getDate() {
        return selectedDate;
    }
    
    public void setDateFormatString(String format) {
        this.dateFormatString = format;
        this.dateFormat = new SimpleDateFormat(format);
        if (selectedDate != null) {
            dateField.setText(dateFormat.format(selectedDate));
        }
    }
    
    public String getDateFormatString() {
        return dateFormatString;
    }
    
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (dateField != null) {
            dateField.setFont(font);
        }
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (dateField != null) {
            dateField.setEnabled(enabled);
        }
        if (calendarButton != null) {
            calendarButton.setEnabled(enabled);
        }
    }
    
    private void showCalendar() {
        // Create calendar dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chọn ngày", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(320, 280);
        dialog.setLocationRelativeTo(this);
        
        // Calendar panel
        JPanel calendarPanel = new JPanel(new GridLayout(7, 7, 2, 2));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Month/Year selector
        JPanel headerPanel = new JPanel(new FlowLayout());
        Calendar cal = Calendar.getInstance();
        if (selectedDate != null) {
            cal.setTime(selectedDate);
        }
        
        // Initialize calendar
        final int[] currentYear = {cal.get(Calendar.YEAR)};
        final int[] currentMonth = {cal.get(Calendar.MONTH)};
        
        JButton prevMonth = new JButton("<");
        JButton nextMonth = new JButton(">");
        JLabel monthYearLabel = new JLabel();
        
        prevMonth.addActionListener(e -> {
            currentMonth[0]--;
            if (currentMonth[0] < 0) {
                currentMonth[0] = 11;
                currentYear[0]--;
            }
            updateCalendar(calendarPanel, currentYear[0], currentMonth[0], dialog);
            updateMonthYearLabel(monthYearLabel, currentYear[0], currentMonth[0]);
        });
        
        nextMonth.addActionListener(e -> {
            currentMonth[0]++;
            if (currentMonth[0] > 11) {
                currentMonth[0] = 0;
                currentYear[0]++;
            }
            updateCalendar(calendarPanel, currentYear[0], currentMonth[0], dialog);
            updateMonthYearLabel(monthYearLabel, currentYear[0], currentMonth[0]);
        });
        
        headerPanel.add(prevMonth);
        headerPanel.add(monthYearLabel);
        headerPanel.add(nextMonth);
        
        updateMonthYearLabel(monthYearLabel, currentYear[0], currentMonth[0]);
        updateCalendar(calendarPanel, currentYear[0], currentMonth[0], dialog);
        
        // Bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton todayButton = new JButton("Hôm nay");
        JButton clearButton = new JButton("Xóa");
        
        todayButton.addActionListener(e -> {
            setDate(new Date());
            dialog.dispose();
        });
        
        clearButton.addActionListener(e -> {
            setDate(null);
            dialog.dispose();
        });
        
        bottomPanel.add(todayButton);
        bottomPanel.add(clearButton);
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(calendarPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void updateMonthYearLabel(JLabel label, int year, int month) {
        String[] months = {"Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                          "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
        label.setText(months[month] + " " + year);
    }
    
    private void updateCalendar(JPanel panel, int year, int month, JDialog dialog) {
        panel.removeAll();
        
        // Day headers
        String[] days = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
        for (String day : days) {
            JLabel label = new JLabel(day, SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 11));
            label.setForeground(Color.BLUE);
            panel.add(label);
        }
        
        // Calculate first day of month
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // Add empty cells before first day
        for (int i = 0; i < firstDayOfWeek; i++) {
            panel.add(new JLabel(""));
        }
        
        // Add day buttons
        for (int day = 1; day <= daysInMonth; day++) {
            final int selectedDay = day;
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(new Font("Arial", Font.PLAIN, 11));
            dayButton.setBackground(Color.WHITE);
            dayButton.setFocusPainted(false);
            
            // Highlight today
            Calendar today = Calendar.getInstance();
            if (year == today.get(Calendar.YEAR) && 
                month == today.get(Calendar.MONTH) && 
                day == today.get(Calendar.DAY_OF_MONTH)) {
                dayButton.setBackground(new Color(173, 216, 230));
            }
            
            // Highlight selected date
            if (selectedDate != null) {
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.setTime(selectedDate);
                if (year == selectedCal.get(Calendar.YEAR) && 
                    month == selectedCal.get(Calendar.MONTH) && 
                    day == selectedCal.get(Calendar.DAY_OF_MONTH)) {
                    dayButton.setBackground(new Color(144, 238, 144));
                }
            }
            
            dayButton.addActionListener(e -> {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, month, selectedDay, 0, 0, 0);
                newDate.set(Calendar.MILLISECOND, 0);
                setDate(newDate.getTime());
                dialog.dispose();
            });
            
            panel.add(dayButton);
        }
        
        panel.revalidate();
        panel.repaint();
    }
}

