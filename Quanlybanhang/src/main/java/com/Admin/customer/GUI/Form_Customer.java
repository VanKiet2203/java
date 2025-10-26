package com.Admin.customer.GUI;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;
import java.text.SimpleDateFormat;
import java.util.List;
import java.awt.event.ActionEvent;
import com.Admin.customer.BUS.BusCustomer;
import com.Admin.customer.DTO.DTOCustomer;
import com.ComponentandDatabase.Components.MyTable;
import com.ComponentandDatabase.Components.MyCombobox;
import com.ComponentandDatabase.Components.MyTextField;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.CustomDialog;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import static com.ComponentandDatabase.Components.UIConstants.*;

public class Form_Customer extends JPanel {
    private JPanel panel;
    private JLabel lblStatus; 
    private MyTable Customner;
    private MyTextField txtSearch;
    private MyCombobox<String> cmbSearch, cmbStatus;
    private JPanel panelSearch;
    private MyButton bntSearch, bntRefresh, bntDelete, bntExportfile, bntUpdate;
    private BusCustomer busCustomer;
    private DefaultTableModel model;

    public Form_Customer() {
        initComponents();
        init();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1200, 700)); // Giảm kích thước cho màn hình nhỏ
        setBackground(Color.WHITE);
    }

    private void init() {
        // Tạo main panel với scroll
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setPreferredSize(new Dimension(1200, 900)); // Kích thước lớn hơn để scroll
        mainPanel.setBackground(Color.WHITE);
        
        // Tạo scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        
        add(scrollPane, BorderLayout.CENTER);
        panel = mainPanel; // Gán panel để sử dụng trong các method khác

        // 1️⃣ Tên cột
        String[] columnNames = {
            "Customer.ID", "Full Name", "Gender", "Date Of Birth", 
            "Email", "Contact", "Address", "Status"
        };

        // 2️⃣ Tạo model
        model = new DefaultTableModel(columnNames, 0);


        // 5️⃣ Tạo bảng với style chuẩn
        Customner = createStyledTable(model);
        Customner.setRowHeight(30);

        JScrollPane tableScrollPane = MyTable.createScrollPane(Customner, 20, 220, 1160, 400);

        // 7️⃣ Tùy chỉnh thanh cuộn
        tableScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(15, Integer.MAX_VALUE));
        tableScrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(Integer.MAX_VALUE, 15));

        // 8️⃣ Thêm tableScrollPane vào panel
        panel.add(tableScrollPane);

        // 9️⃣ Repaint panel sau khi thêm
        panel.revalidate();
        panel.repaint();

        // 🔟 Load dữ liệu vào model sau khi GUI sẵn sàng
        SwingUtilities.invokeLater(() -> {
            loadCustomerData(model);
            expandTableColumns(); // 👈 Gọi method mới để mở rộng cột
        });

           // Bắt sự kiện chọn dòng trong bảng để set giá trị vào combobox State
       Customner.addMouseListener(new MouseAdapter() {
           @Override
           public void mouseClicked(MouseEvent e) {
               int selectedRow = Customner.getSelectedRow();
               if (selectedRow != -1) {
                   String stateValue = Customner.getValueAt(selectedRow, 7).toString();

                   // Đưa giá trị lên combobox
                   cmbStatus.setSelectedItem(stateValue);
               }
           }
       });
      
    
            // Title
            JLabel lblTitle = new JLabel("MANAGE CUSTOMER");
            lblTitle.setFont(FONT_TITLE_LARGE);
            lblTitle.setForeground(PRIMARY_COLOR);
            lblTitle.setBounds(20, 10, 400, 40);
            panel.add(lblTitle);
            
            // Tạo panelSearch với màu nền trắng
            panelSearch = new MyPanel(Color.WHITE);
            panelSearch.setLayout(null);
            panelSearch.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                "Search",
                0, 0,
                FONT_TITLE_SMALL,
                PRIMARY_COLOR
            ));
            panelSearch.setBounds(20, 60, 1160, 100);
            
            // ComboBox search
            String[] items = {"Customer.ID", "Customer Name", "Email", "Contact"};
            cmbSearch = new MyCombobox<>(items);
            cmbSearch.setBounds(20, 30, 150, 35);
            cmbSearch.setCustomFont(FONT_CONTENT_MEDIUM);
            cmbSearch.setCustomColors(Color.WHITE, Color.GRAY, Color.BLACK);
            panelSearch.add(cmbSearch);
            
            // TextField search - THÊM VIỀN
            txtSearch = new MyTextField();
            txtSearch.setHint("Search something...");
            txtSearch.setBounds(180, 30, 300, 35);
            txtSearch.setTextFont(FONT_CONTENT_MEDIUM);
            txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            panelSearch.add(txtSearch);

            // Button Search trong panelSearch
            bntSearch = new MyButton("Search", 20);
            stylePrimaryButton(bntSearch);
            bntSearch.setBounds(500, 30, 120, 35);
            bntSearch.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\search.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);
            bntSearch.addActionListener(e -> {
                try {
                    String selectedColumn = cmbSearch.getSelectedItem().toString();
                    String keyword = txtSearch.getText().trim();
                    String statusFilter = cmbStatus.getSelectedItem().toString();
                    busCustomer = new BusCustomer();
                    // Gọi BUS để lấy danh sách khách hàng đã lọc
                    List<DTOCustomer> resultList = busCustomer.searchCustomer(selectedColumn, keyword, statusFilter);

                    // Xóa dữ liệu cũ
                    model.setRowCount(0); 

                    // Hiển thị dữ liệu mới
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                    for (DTOCustomer customer : resultList) {
                        model.addRow(new Object[] {
                            customer.getCustomerID(),
                            customer.getFullName(),
                            customer.getGender(),
                            customer.getDateOfBirth() != null ? sdf.format(customer.getDateOfBirth()) : "",
                            customer.getEmail(),
                            customer.getContact(),
                            customer.getAddress(),
                            customer.getStatus()
                        });
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Lỗi khi tìm kiếm khách hàng: " + ex.getMessage());
                }
            });

            panelSearch.add(bntSearch);
            panel.add(panelSearch);
            // Status dropdown - đặt ngang hàng với search box
            lblStatus = new JLabel("Status");
            lblStatus.setFont(new Font("Arial", Font.PLAIN, 16));
            lblStatus.setForeground(Color.BLACK);
            lblStatus.setBounds(650, 5, 100, 35);
            panelSearch.add(lblStatus);

            String[] items_status = {"Active", "Inactive"};
            cmbStatus = new MyCombobox<>(items_status);
            cmbStatus.setBounds(650, 30, 110, 35);
            cmbStatus.setCustomFont(new Font("Times New Roman", Font.PLAIN, 15));
            cmbStatus.setCustomColors(Color.WHITE, Color.GRAY, Color.BLACK);
            panelSearch.add(cmbStatus);            
            // Sắp xếp lại các button theo hàng ngang, tránh chồng chéo
            // Button Refresh
            bntRefresh = new MyButton("Refresh", 30);
            styleInfoButton(bntRefresh);
            bntRefresh.setBounds(20, 170, 120, 35);
            bntRefresh.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\refresh.png", 25, 25, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
            bntRefresh.addActionListener((ActionEvent e) -> {
                refreshForm();
            });
            panel.add(bntRefresh);
            
            // Button Delete
            bntDelete = new MyButton("Delete", 20);
            // Sử dụng màu đỏ đậm để tránh trùng với icon
            bntDelete.setBackgroundColor(Color.decode("#DC3545"));
            bntDelete.setHoverColor(Color.decode("#C82333"));
            bntDelete.setPressedColor(Color.decode("#BD2130"));
            bntDelete.setFont(FONT_BUTTON_MEDIUM);
            bntDelete.setForeground(Color.WHITE);
            bntDelete.setBounds(150, 170, 120, 35);
            bntDelete.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\delete.png", 25, 25, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
            bntDelete.addActionListener(e -> {
                int selectedRow = Customner.getSelectedRow();
                if (selectedRow == -1) {
                    CustomDialog.showError("Please choose a customer to deactivate!");
                    return;
                }

                busCustomer = new BusCustomer();
                String customerID = Customner.getValueAt(selectedRow, 0).toString(); // Cột 0 là Customer.ID
                String fullName = busCustomer.getCustomerNameByID(customerID);

                boolean confirm = CustomDialog.showOptionPane(
                        "Confirm Deactivation",
                        "Are you sure you want to deactivate customer: " + fullName + "?",
                        UIManager.getIcon("OptionPane.questionIcon"),
                        Color.decode("#FF6666")
                );

                if (confirm) {
                    try {
                        busCustomer = new BusCustomer();
                        boolean isDeleted = busCustomer.delete(customerID);

                        if (isDeleted) {
                            model.setRowCount(0);
                            loadCustomerData(model);
                            expandTableColumns();
                            CustomDialog.showSuccess("Successfully deactivated customer " + fullName + "!");
                        } else {
                            System.out.println("DEBUG: Deactivation failed, customer might not exist.");
                            CustomDialog.showError("Deactivation failure! Customer may not exist or an issue occurred.");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        CustomDialog.showError("Error while deactivating customer: " + ex.getMessage());
                    }
                }
            });
            panel.add(bntDelete);
            
            // Button Update
            bntUpdate = new MyButton("Update", 20);
            styleWarningButton(bntUpdate);
            bntUpdate.setBounds(280, 170, 120, 35);
            bntUpdate.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\update.png", 25, 25, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
            bntUpdate.addActionListener(e -> {
                busCustomer = new BusCustomer();
                busCustomer.update(Customner, cmbStatus);

                model.setRowCount(0);  // 👈 Xóa dữ liệu cũ khỏi bảng
                loadCustomerData(model);  // 👈 Tải lại dữ liệu mới
                expandTableColumns(); // 👈 Cập nhật lại chiều rộng cột
            });
            panel.add(bntUpdate);
            
            // Button Export Excel
            bntExportfile = new MyButton("Export", 20);
            bntExportfile.setBackgroundColor(Color.WHITE); // Màu nền
            bntExportfile.setPressedColor(Color.decode("#D3D3D3")); // Màu khi nhấn
            bntExportfile.setHoverColor(Color.decode("#EEEEEE")); // Màu khi rê chuột vào
            bntExportfile.setBounds(410, 170, 120, 35); // Điều chỉnh vị trí và kích thước
            bntExportfile.setFont(new Font("sansserif", Font.BOLD, 14));
            bntExportfile.setForeground(Color.BLACK);
            bntExportfile.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\Excel.png", 30, 30, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
            bntExportfile.addActionListener((ActionEvent e) -> {
                busCustomer = new BusCustomer();
                busCustomer.exportFile(Customner);
            });
            panel.add(bntExportfile);
           panelSearch.repaint();
           panelSearch.revalidate();
            
           this.repaint();
           this.revalidate();          
     
   }
 
        // 3️⃣ Load dữ liệu từ BUS vào model
     private void loadCustomerData(DefaultTableModel model) {
         BusCustomer bus = new BusCustomer(); // Khởi tạo lớp BUS
         List<DTOCustomer> customerList = bus.getAllCustomers(); // Lấy danh sách khách hàng

         SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

         for (DTOCustomer customer : customerList) {
             Object[] row = new Object[]{
                 customer.getCustomerID(),
                 customer.getFullName(),
                 customer.getGender(),
                 customer.getDateOfBirth() != null ? sdf.format(customer.getDateOfBirth()) : "",
                 customer.getEmail(),
                 customer.getContact(),
                 customer.getAddress(),
                 customer.getStatus()
             };
             model.addRow(row); // Thêm dòng vào model
         }
     }
     public void refreshForm() {
        if (model != null) {
            model.setRowCount(0); // Xóa dữ liệu cũ trong bảng
            loadCustomerData(model); // Load lại dữ liệu từ DB
        } else {
            System.err.println("Model is null - chưa được khởi tạo");
        }

        // Reset các combo box
        if (cmbStatus != null) cmbStatus.setSelectedIndex(0);
        if (cmbSearch != null) cmbSearch.setSelectedIndex(0);
    }
    
    /**
     * Mở rộng các cột của table để sử dụng hết không gian có sẵn
     */
    private void expandTableColumns() {
        if (Customner == null) return;
        
        TableColumnModel columnModel = Customner.getColumnModel();
        int totalWidth = 1160; // Chiều rộng tổng của table (tối ưu cho màn hình nhỏ)
        int columnCount = Customner.getColumnCount();
        
        // Định nghĩa tỷ lệ chiều rộng cho từng cột (tối ưu cho màn hình nhỏ)
        double[] columnRatios = {
            0.10,  // Customer.ID - 10%
            0.18,  // Full Name - 18%
            0.08,  // Gender - 8%
            0.12,  // Date Of Birth - 12%
            0.20,  // Email - 20%
            0.12,  // Contact - 12%
            0.15,  // Address - 15%
            0.05   // Status - 5%
        };
        
        // Áp dụng tỷ lệ cho từng cột
        for (int i = 0; i < columnCount && i < columnRatios.length; i++) {
            TableColumn column = columnModel.getColumn(i);
            int columnWidth = (int) (totalWidth * columnRatios[i]);
            column.setPreferredWidth(columnWidth);
            column.setWidth(columnWidth);
        }
        
        // Đảm bảo table sử dụng hết không gian
        Customner.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        Customner.revalidate();
        Customner.repaint();
    }

    // ============================================
    // HELPER METHODS FOR UI STYLING
    // ============================================

    private void stylePrimaryButton(MyButton btn) {
        btn.setBackgroundColor(PRIMARY_COLOR);
        btn.setHoverColor(PRIMARY_HOVER);
        btn.setPressedColor(PRIMARY_HOVER.darker());
        btn.setFont(FONT_BUTTON_MEDIUM);
        btn.setForeground(Color.WHITE);
    }


    private void styleWarningButton(MyButton btn) {
        btn.setBackgroundColor(WARNING_COLOR);
        btn.setHoverColor(WARNING_HOVER);
        btn.setPressedColor(WARNING_HOVER.darker());
        btn.setFont(FONT_BUTTON_MEDIUM);
        btn.setForeground(Color.WHITE);
    }

    private void styleInfoButton(MyButton btn) {
        btn.setBackgroundColor(INFO_COLOR);
        btn.setHoverColor(INFO_HOVER);
        btn.setPressedColor(INFO_HOVER.darker());
        btn.setFont(FONT_BUTTON_MEDIUM);
        btn.setForeground(Color.WHITE);
    }

    private MyTable createStyledTable(DefaultTableModel model) {
        return new MyTable(
            model,
            Color.WHITE,                    // Nền bảng
            TEXT_PRIMARY,                   // Chữ bảng
            Color.decode("#E8F5E9"),        // Nền dòng chọn
            Color.BLACK,                    // Chữ dòng chọn
            PRIMARY_COLOR,                  // Nền tiêu đề
            Color.WHITE,                    // Chữ tiêu đề
            FONT_TABLE_CONTENT,             // Font nội dung
            FONT_TABLE_HEADER               // Font tiêu đề
        );
    }
   
}


