package com.Admin.home.GUI;

import com.Admin.home.BUS.BUS_Total;
import com.Admin.home.DTO.*;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyButton;
import com.Admin.statistics.BUS.BUS_Chart;
import com.Admin.statistics.DTO.ChartFilterData;
import org.jfree.chart.ChartPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Form_Home extends JPanel {
    private JPanel panelContainer, chartContainer;
    private MyPanel panelTotalCustomer, panelTotalOrder, panelTotalBillExport, panelTotalWarranty, 
            panelTotalSupplier, panelTotalCategory, panelTotalProduct, panelTotalBillImport;
    private JLabel lblTotalCustomer, lblTotalOrder, lblTotalBillExport, lblTotalWarranty, 
            lblTotalSupplier, lblTotalCategory, lblTotalProduct, lblTotalBillImport;
    private MyButton bntRefresh;
    private BUS_Total busTotal;
    private BUS_Chart busChart;
    private com.Admin.dashboard_admin.GUI.Dashboard_ad parentFrame;

    public Form_Home() {
        busTotal = new BUS_Total(); // Khởi tạo BUS
        busChart = new BUS_Chart();
        initComponents();
        updatePanelData(); // Cập nhật dữ liệu từ BUS
        loadChart();
    }
    
    public Form_Home(com.Admin.dashboard_admin.GUI.Dashboard_ad parentFrame) {
        this.parentFrame = parentFrame;
        busTotal = new BUS_Total(); // Khởi tạo BUS
        busChart = new BUS_Chart();
        initComponents();
        updatePanelData(); // Cập nhật dữ liệu từ BUS
        loadChart();
        addClickHandlers(); // Thêm xử lý click cho các card
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.decode("#F5F7FA"));

        panelContainer = new JPanel(new GridLayout(2, 4, 10, 10));
        panelContainer.setBackground(Color.WHITE);
        panelContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); 

        panelTotalCustomer = createInfoPanel("Total Customers", "\uD83D\uDC65", Color.decode("#1CB5E0"), Color.decode("#000046"), lblTotalCustomer = new JLabel());
        panelTotalOrder = createInfoPanel("Total Orders", "\uD83D\uDCDA", Color.decode("#E74C3C"), Color.decode("#641E16"), lblTotalOrder = new JLabel());
        panelTotalBillExport = createInfoPanel("Total Bill Exports", "\uD83D\uDCB8", Color.decode("#28B463"), Color.decode("#145A32"), lblTotalBillExport = new JLabel());
        panelTotalWarranty = createInfoPanel("Total Promotions", "\uD83C\uDF89", Color.decode("#F39C12"), Color.decode("#7E5109"), lblTotalWarranty = new JLabel());
        panelTotalSupplier = createInfoPanel("Total Suppliers", "\uD83C\uDFE2", Color.decode("#8E44AD"), Color.decode("#4A235A"), lblTotalSupplier = new JLabel());
        panelTotalCategory = createInfoPanel("Total Categories", "\uD83D\uDCDD", Color.decode("#D35400"), Color.decode("#641E16"), lblTotalCategory = new JLabel());
        panelTotalProduct = createInfoPanel("Total Products", "\uD83D\uDECD", Color.decode("#1ABC9C"), Color.decode("#0B5345"), lblTotalProduct = new JLabel());
        panelTotalBillImport = createInfoPanel("Total Bill Imports", "\uD83D\uDCB8", Color.decode("#C0392B"), Color.decode("#78281F"), lblTotalBillImport = new JLabel());

        panelContainer.add(panelTotalCustomer);
        panelContainer.add(panelTotalOrder);
        panelContainer.add(panelTotalBillExport);
        panelContainer.add(panelTotalWarranty);
        panelContainer.add(panelTotalSupplier);
        panelContainer.add(panelTotalCategory);
        panelContainer.add(panelTotalProduct);
        panelContainer.add(panelTotalBillImport);
        
        
        // Khởi tạo nút Refresh
        bntRefresh = new MyButton("Refresh", 20);
        bntRefresh.setBackgroundColor(Color.WHITE);
        bntRefresh.setPressedColor(Color.decode("#D3D3D3"));
        bntRefresh.setHoverColor(Color.decode("#EEEEEE"));
        bntRefresh.setFont(new Font("sansserif", Font.BOLD, 16));
        bntRefresh.setForeground(Color.BLACK);
        bntRefresh.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\refresh.png", 
                                   25, 25, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntRefresh.addActionListener(e -> {
            updatePanelData(); // Refresh dữ liệu
            loadChart(); // Refresh chart
        });
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(Color.WHITE);
        topContainer.setBorder(null);
        topContainer.add(panelContainer, BorderLayout.NORTH);
        topContainer.add(bntRefresh, BorderLayout.WEST); // Thêm nút vào góc trên bên phải
        add(topContainer, BorderLayout.NORTH);

        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBackground(Color.WHITE);
        chartContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartContainer.setPreferredSize(new Dimension(900, 450)); // Kích thước phù hợp cho chart
    
        add(chartContainer, BorderLayout.CENTER);
    }

    private MyPanel createInfoPanel(String title, String icon, Color startColor, Color endColor, JLabel lblValue) {
        MyPanel panel = new MyPanel(startColor, endColor, MyPanel.VERTICAL_GRADIENT);
        panel.setLayout(new GridLayout(3, 1)); 
        panel.setPreferredSize(new Dimension(200, 150));
        panel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        JLabel lblIcon = new JLabel(icon, SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28)); 
        lblIcon.setForeground(Color.WHITE);

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        lblValue.setFont(new Font("Arial", Font.BOLD, 22));
        lblValue.setForeground(Color.WHITE);
        lblValue.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(lblIcon);
        panel.add(lblTitle);
        panel.add(lblValue);
        return panel;
    }

    private void updatePanelData() {
        lblTotalCustomer.setText(String.valueOf(busTotal.getTotalCustomers().getStatus()));
        lblTotalOrder.setText(String.valueOf(busTotal.getTotalOrders().getStatus()));
        lblTotalBillExport.setText(String.valueOf(busTotal.getTotalBillExports().getTotalProduct()));
        lblTotalWarranty.setText(String.valueOf(busTotal.getTotalPromotions())); 
        lblTotalSupplier.setText(String.valueOf(busTotal.getTotalSuppliers().getContact()));
        lblTotalCategory.setText(String.valueOf(busTotal.getTotalCategories().getContact()));
        lblTotalProduct.setText(String.valueOf(busTotal.getTotalProducts().getQuantity()));
        lblTotalBillImport.setText(String.valueOf(busTotal.getTotalBillImports().getTotalProduct()));
    }
    
    /**
     * Load chart từ statistics vào Form_Home
     * Sử dụng Bar chart làm mặc định với filter mặc định
     */
    private void loadChart() {
        chartContainer.removeAll();
        
        try {
            // Tạo filter mặc định (không có filter đặc biệt)
            ChartFilterData defaultFilter = new ChartFilterData();
            
            // Lấy Bar chart từ BUS_Chart
            ChartPanel chartPanel = busChart.getBar(defaultFilter);
            
            // Điều chỉnh kích thước chart cho phù hợp với container
            chartPanel.setPreferredSize(new Dimension(870, 420));
            chartPanel.setMinimumSize(new Dimension(800, 350));
            
            // Thêm chart vào container
            chartContainer.add(chartPanel, BorderLayout.CENTER);
            
            chartContainer.revalidate();
            chartContainer.repaint();
        } catch (Exception e) {
            // Nếu có lỗi, hiển thị thông báo
            JLabel errorLabel = new JLabel("Chart could not be loaded", SwingConstants.CENTER);
            errorLabel.setFont(new Font("Arial", Font.BOLD, 16));
            errorLabel.setForeground(Color.GRAY);
            chartContainer.add(errorLabel, BorderLayout.CENTER);
            chartContainer.revalidate();
            chartContainer.repaint();
        }
    }
    
    private void addClickHandlers() {
        // Thêm click handler cho Total Customer -> Customer
        addClickHandler(panelTotalCustomer, "Customer");
        
        // Thêm click handler cho Total Order -> Order
        addClickHandler(panelTotalOrder, "Order");
        
        // Thêm click handler cho Total Bill Export -> Export
        addClickHandler(panelTotalBillExport, "Export");
        
        // Thêm click handler cho Total Warranty -> (có thể là Promotion hoặc Statistics)
        addClickHandler(panelTotalWarranty, "Promotion");
        
        // Thêm click handler cho Total Supplier -> (có thể là Inventory)
        addClickHandler(panelTotalSupplier, "Inventory");
        
        // Thêm click handler cho Total Category -> Category
        addClickHandler(panelTotalCategory, "Category");
        
        // Thêm click handler cho Total Product -> Product
        addClickHandler(panelTotalProduct, "Product");
        
        // Thêm click handler cho Total Bill Import -> Inventory
        addClickHandler(panelTotalBillImport, "Inventory");
    }
    
    private void addClickHandler(MyPanel panel, String formName) {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                navigateToForm(formName);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                panel.setCursor(Cursor.getDefaultCursor());
            }
        });
    }
    
    private void navigateToForm(String formName) {
        if (parentFrame != null) {
            parentFrame.showForm(formName);
        }
    }
}
