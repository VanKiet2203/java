package com.Admin.statistics.GUI;

import com.Admin.statistics.BUS.BUS_Chart;
import com.Admin.statistics.Components.FilterPanel;
import com.Admin.statistics.DTO.ChartFilterData;
import com.ComponentandDatabase.Components.MyButton;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.*;

public class Form_Statistics extends JPanel {
    private final JPanel cardPanel = new JPanel(new CardLayout());
    private final BUS_Chart bus = new BUS_Chart();
    private ChartFilterData currentFilter = new ChartFilterData();
    private MyButton btnLine, btnBar, btnPie;
    private String currentCard = "LINE";

    public Form_Statistics() {
        initComponents();
        init();
    }

    private void initComponents() {
        setLayout(null);
        setPreferredSize(new Dimension(1200, 700));
        setBackground(Color.WHITE);
    }

    private void init() {
        // Top filter panel - giảm height để tiết kiệm không gian
        FilterPanel filter = new FilterPanel(this::applyFilter);
        filter.setBounds(10, 5, 1180, 45);
        add(filter);

        // Buttons switch panel - loại bỏ border, giảm height
        JPanel tabBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        tabBtns.setBounds(10, 55, 1180, 38);
        tabBtns.setBackground(Color.WHITE);
        // Loại bỏ border để tiết kiệm không gian

        btnLine = new MyButton("Line Chart", 12);
        btnLine.setPreferredSize(new Dimension(130, 35));
        btnLine.setForeground(Color.WHITE);
        btnLine.setBackgroundColor(new Color(0x2196F3));
        btnLine.setHoverColor(new Color(0x42A5F5));
        btnLine.setPressedColor(new Color(0x1976D2));
        btnLine.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\linechart.png", 
                              20, 20, 6, SwingConstants.LEFT, SwingConstants.CENTER);
        btnLine.addActionListener(e -> switchCard("LINE"));
        
        btnBar = new MyButton("Bar Chart", 12);
        btnBar.setPreferredSize(new Dimension(130, 35));
        btnBar.setForeground(Color.WHITE);
        btnBar.setBackgroundColor(new Color(0x4CAF50));
        btnBar.setHoverColor(new Color(0x66BB6A));
        btnBar.setPressedColor(new Color(0x388E3C));
        btnBar.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\barchart.png", 
                             20, 20, 6, SwingConstants.LEFT, SwingConstants.CENTER);
        btnBar.addActionListener(e -> switchCard("BAR"));
        
        btnPie = new MyButton("Pie Chart", 12);
        btnPie.setPreferredSize(new Dimension(130, 35));
        btnPie.setForeground(Color.WHITE);
        btnPie.setBackgroundColor(new Color(0xFF9800));
        btnPie.setHoverColor(new Color(0xFFB74D));
        btnPie.setPressedColor(new Color(0xF57C00));
        btnPie.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\pie_chart.png", 
                             20, 20, 6, SwingConstants.LEFT, SwingConstants.CENTER);
        btnPie.addActionListener(e -> switchCard("PIE"));

        tabBtns.add(btnLine);
        tabBtns.add(btnBar);
        tabBtns.add(btnPie);
        add(tabBtns);

        // Chart cards panel
        cardPanel.setBounds(10, 105, 1180, 585);
        cardPanel.setBackground(Color.WHITE);
        cardPanel.add(new JPanel(), "LINE");
        cardPanel.add(new JPanel(), "BAR");
        cardPanel.add(new JPanel(), "PIE");
        add(cardPanel);

        // Default load
        applyFilter(new ChartFilterData());
        switchCard("LINE");
        updateButtonStates();
    }

    private void switchCard(String cardName) {
        currentCard = cardName;
        ((CardLayout) cardPanel.getLayout()).show(cardPanel, cardName);
        updateButtonStates();
    }

    private void updateButtonStates() {
        // Reset all buttons
        btnLine.setBackgroundColor(new Color(0x2196F3));
        btnBar.setBackgroundColor(new Color(0x4CAF50));
        btnPie.setBackgroundColor(new Color(0xFF9800));
        
        // Highlight current button
        switch (currentCard) {
            case "LINE":
                btnLine.setBackgroundColor(new Color(0x1976D2));
                break;
            case "BAR":
                btnBar.setBackgroundColor(new Color(0x388E3C));
                break;
            case "PIE":
                btnPie.setBackgroundColor(new Color(0xF57C00));
                break;
        }
    }

    private void applyFilter(ChartFilterData f) {
        currentFilter = f;
        // Build panels
        ChartPanel pLine = bus.getLine(f);
        ChartPanel pBar  = bus.getBar(f);
        ChartPanel pPie  = bus.getPie(f);

        // Clear and replace all cards
        cardPanel.removeAll();
        
        JPanel holdLine = new JPanel(new BorderLayout());
        holdLine.setBackground(Color.WHITE);
        holdLine.add(pLine, BorderLayout.CENTER);
        cardPanel.add(holdLine, "LINE");
        
        JPanel holdBar = new JPanel(new BorderLayout());
        holdBar.setBackground(Color.WHITE);
        holdBar.add(pBar, BorderLayout.CENTER);
        cardPanel.add(holdBar, "BAR");
        
        JPanel holdPie = new JPanel(new BorderLayout());
        holdPie.setBackground(Color.WHITE);
        holdPie.add(pPie, BorderLayout.CENTER);
        cardPanel.add(holdPie, "PIE");
        
        // Show current card
        switchCard(currentCard);
    }
}
