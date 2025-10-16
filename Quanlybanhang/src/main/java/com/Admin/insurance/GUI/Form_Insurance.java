
package com.Admin.insurance.GUI;

import com.Admin.dashboard_admin.GUI.Dashboard_ad;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Components.MyPanel;
import com.ComponentandDatabase.Components.MyTextField;
import com.toedter.calendar.JDateChooser;
import com.Admin.insurance.BUS.BUS_ProductInfo;
import com.Admin.insurance.BUS.BUS_CustomerInfo;
import com.Admin.insurance.BUS.BUS_Warranty;
import com.Admin.insurance.DTO.DTO_CustomerInfo;
import com.Admin.insurance.DTO.DTOProductInfo;
import com.Admin.insurance.DTO.DTO_Insurance;
import com.Admin.insurance.DTO.DTO_InsuranceDetails;
import com.ComponentandDatabase.Components.CustomDialog;
import javax.swing.JPanel;
import java.text.SimpleDateFormat; // Định dạng ngày tháng
import java.util.Random;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;
import java.time.LocalDate;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import javax.swing.*;
import java.util.Date;
import java.awt.*;
import java.io.File;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import static com.ComponentandDatabase.Components.UIConstants.*;

public class Form_Insurance extends JPanel {
    private JPanel panel, panelSearch, billBody;
    private MyPanel panelBill, panelTitle;
    private JLabel lblAdminID, lblAdminName, lblInvoice, lblProductID, lblProductName, lblCateID, lblBrand
        ,lblWarranty, lblDescription, lblStartDate, lblEndDate;
    private MyButton bntExportFile, bntDetails, bntRefresh, bntAddBill, bntExport;
    private MyTextField txtAdminID, txtAdminName, txtProductID, txtProductName, txtCateID, txtBrand, txtWarranty; 
    private JTextArea txtDescription;
    private JDateChooser startDate, endDate;
    private BUS_ProductInfo busProductInfo;
    private BUS_CustomerInfo busCustomerInfo;
    private BUS_Warranty busWarranty;
    private String warrantyNo;
    public Form_Insurance() {
        initComponents();
        init();
       
    }

    private void initComponents() {
        setLayout(null);
        setPreferredSize(new Dimension(1530, 860)); // Giữ kích thước nhưng không ép buộc vị trí
        setBackground(Color.WHITE); // Kiểm tra hiển thị
    }

    private void init() {
        panel = new JPanel();
        panel.setLayout(null);
        panel.setBounds(0, 0, 1530, 860); // Giữ nguyên layout của các thành phần
        panel.setBackground(Color.WHITE);
        add(panel);

        // Title
        JLabel lblTitle = new JLabel("QUẢN LÝ BẢO HÀNH");
        lblTitle.setFont(FONT_TITLE_LARGE);
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setBounds(20, 10, 400, 40);
        panel.add(lblTitle);
         
        bntRefresh = new MyButton("Làm mới", 20);
        styleInfoButton(bntRefresh);
        bntRefresh.setBounds(10, 60, 140, 35);
        bntRefresh.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\refresh.png", 25, 25, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntRefresh.addActionListener((e) -> {
            Refresh();
        });
        panel.add(bntRefresh);
        
        bntExportFile = new MyButton("Excel", 0);
        styleInfoButton(bntExportFile);
        bntExportFile.setBounds(160, 60, 130, 35);
        bntExportFile.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\Excel.png", 40, 40, 10, SwingConstants.RIGHT, SwingConstants.CENTER);
        bntExportFile.addActionListener(e -> {
            busWarranty= new BUS_Warranty();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Excel File");
            fileChooser.setSelectedFile(new File("Warranty_Report.xlsx"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String filePath = fileToSave.getAbsolutePath();

                // Đảm bảo có phần mở rộng ".xlsx"
                if (!filePath.endsWith(".xlsx")) {
                    filePath += ".xlsx";
                }

                boolean success = busWarranty.exportToExcel(filePath);

                if (success) {
                   CustomDialog.showSuccess("File exported successfully !");
                } else {
                   
                }
            }
        });
        
        
        panel.add(bntExportFile);
        
        bntDetails = new MyButton("Chi tiết", 20);
        styleInfoButton(bntDetails);
        bntDetails.setButtonIcon("src\\main\\resources\\Icons\\Admin_icon\\bill_export.png", 25, 25, 5, SwingConstants.RIGHT, SwingConstants.CENTER);    
        bntDetails.setBounds(300, 60, 140, 35);
        bntDetails.addActionListener((e) -> {
            WarrantyDetails details= new WarrantyDetails();
            details.setVisible(true);
            
        });
        panel.add(bntDetails);
       
         lblAdminID= new JLabel("Admin.ID");
         lblAdminID.setFont(new Font("Arial", Font.PLAIN, 16));
         lblAdminID.setForeground(Color.BLACK);
         lblAdminID.setBounds(430, 5, 100, 35);
         panel.add(lblAdminID);
       
          txtAdminID = new MyTextField();
          txtAdminID.setBorder(BorderFactory.createLineBorder(Color.GRAY));
          txtAdminID.setTextColor(Color.RED); // Đặt màu mong muốn
          txtAdminID.setLocked(true); // Gọi sau cũng không sao
          txtAdminID.setTextFont(new Font("Times New Roman", Font.BOLD, 16));
          txtAdminID.setBackgroundColor(Color.WHITE);
          txtAdminID.setBounds(400, 40, 130, 35);
          txtAdminID.setText(Dashboard_ad.adminID);
          panel.add(txtAdminID);
          
           
         lblAdminName= new JLabel("Admin Name");
         lblAdminName.setFont(new Font("Arial", Font.PLAIN, 16));
         lblAdminName.setForeground(Color.BLACK);
         lblAdminName.setBounds(610, 5, 100, 35);
         panel.add(lblAdminName);
         
         txtAdminName = new MyTextField();
         txtAdminName.setBorder(BorderFactory.createLineBorder(Color.GRAY));
         txtAdminName.setTextColor(Color.BLUE); // Đặt màu mong muốn
         txtAdminName.setLocked(true); // Gọi sau cũng không sao
         txtAdminName.setTextFont(new Font("Times New Roman", Font.BOLD, 16));
         txtAdminName.setBackgroundColor(Color.WHITE);
         txtAdminName.setBounds(570, 40, 160, 35);
         txtAdminName.setText(Dashboard_ad.getAdminName(txtAdminID.getText().strip()));
         panel.add(txtAdminName);
       
        
        lblProductID= new JLabel("Product.ID");
        lblProductID.setFont(new Font("Arial", Font.BOLD, 18));
        lblProductID.setForeground(Color.BLACK);
        lblProductID.setBounds(80,180, 100, 35);
        panel.add(lblProductID);
        
        lblProductName= new JLabel("Product Name");
        lblProductName.setFont(new Font("Arial", Font.BOLD, 18));
        lblProductName.setForeground(Color.BLACK);
        lblProductName.setBounds(80, 260, 130, 35);
        panel.add(lblProductName);
        
        lblCateID= new JLabel("Category.ID");
        lblCateID.setFont(new Font("Arial", Font.BOLD, 18));
        lblCateID.setForeground(Color.BLACK);
        lblCateID.setBounds(80, 340, 130, 35);
        panel.add(lblCateID);
        
        lblBrand= new JLabel("Brand");
        lblBrand.setFont(new Font("Arial", Font.BOLD, 18));
        lblBrand.setForeground(Color.BLACK);
        lblBrand.setBounds(570, 260, 100, 35);
        panel.add(lblBrand);
        
        lblWarranty= new JLabel("Warranty Period");
        lblWarranty.setFont(new Font("Arial", Font.BOLD, 18));
        lblWarranty.setForeground(Color.BLACK);
        lblWarranty.setBounds(510, 340, 150, 35);
        panel.add(lblWarranty);
        
        // IMEI UI removed
        
        lblDescription= new JLabel("Description");
        lblDescription.setFont(new Font("Arial", Font.BOLD, 18));
        lblDescription.setForeground(Color.BLACK);
        lblDescription.setBounds(80, 500, 130, 35);
        panel.add(lblDescription);
          
        lblStartDate= new JLabel("Start Date");
        lblStartDate.setFont(new Font("Arial", Font.BOLD, 18));
        lblStartDate.setForeground(Color.BLACK);
        lblStartDate.setBounds(80, 600, 130, 35);
        panel.add(lblStartDate);
          
        lblEndDate= new JLabel("End Date");
        lblEndDate.setFont(new Font("Arial", Font.BOLD, 18));
        lblEndDate.setForeground(Color.BLACK);
        lblEndDate.setBounds(80, 680, 130, 35);
        panel.add(lblEndDate);
        
        txtProductID = new MyTextField();
        txtProductID.setBorder(null);
        txtProductID.setTextColor(Color.BLACK); // Đặt màu mong muốn
        txtProductID.setLocked(true); // Gọi sau cũng không sao
        txtProductID.setTextFont(new Font("Times New Roman", Font.BOLD | Font.ITALIC, 18));
        txtProductID.setBackgroundColor(Color.WHITE);
        txtProductID.setBounds(180,180, 100, 35);
        panel.add(txtProductID);
        
        
        txtProductName = new MyTextField();
        txtProductName.setBorder(null);
        txtProductName.setTextColor(Color.BLACK); // Đặt màu mong muốn
        txtProductName.setLocked(true); // Gọi sau cũng không sao
        txtProductName.setTextFont(new Font("Times New Roman", Font.PLAIN, 18));
        txtProductName.setBackgroundColor(Color.WHITE);
        txtProductName.setBounds(190,260, 350, 35);
        panel.add(txtProductName);
             
        txtCateID = new MyTextField();
        txtCateID.setBorder(null);
        txtCateID.setTextColor(Color.BLACK); // Đặt màu mong muốn
        txtCateID.setLocked(true); // Gọi sau cũng không sao
        txtCateID.setTextFont(new Font("Times New Roman", Font.PLAIN, 18));
        txtCateID.setBackgroundColor(Color.WHITE);
        txtCateID.setBounds(190,340, 120, 35);
        panel.add(txtCateID);
        
        txtBrand = new MyTextField();
        txtBrand.setBorder(null);
        txtBrand.setTextColor(Color.BLACK); // Đặt màu mong muốn
        txtBrand.setLocked(true); // Gọi sau cũng không sao
        txtBrand.setTextFont(new Font("Times New Roman", Font.PLAIN, 18));
        txtBrand.setBackgroundColor(Color.WHITE);
        txtBrand.setBounds(620,260, 120, 35);
        panel.add(txtBrand);
        
        txtWarranty = new MyTextField();
        txtWarranty.setBorder(null);
        txtWarranty.setTextColor(Color.BLACK); // Đặt màu mong muốn
        txtWarranty.setLocked(true); // Gọi sau cũng không sao
        txtWarranty.setTextFont(new Font("Times New Roman", Font.PLAIN, 18));
        txtWarranty.setBackgroundColor(Color.WHITE);
        txtWarranty.setBounds(640,340, 140, 35);
        panel.add(txtWarranty);
                
        
        panelBill = new MyPanel();
        panelBill.setLayout(new BorderLayout());
        panelBill.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1)); // Chỉ 1 viền chính
        panelBill.setBackground(Color.WHITE);
        panelBill.setBounds(820, 100, 450, 680);
        panel.add(panelBill);

       // Tạo panel title "Bill For Insurance" (không thêm border riêng)
       JPanel paneltitle = new JPanel();
       paneltitle.setBackground(Color.RED);
       paneltitle.setPreferredSize(new Dimension(450, 30)); // Fixed height
       JLabel lblBillTitle = new JLabel("BILL FOR INSURANCE", JLabel.CENTER);
       lblBillTitle.setFont(new Font("Arial", Font.BOLD, 16));
       lblBillTitle.setForeground(Color.WHITE);
       paneltitle.add(lblBillTitle);
       panelBill.add(paneltitle, BorderLayout.NORTH);

       // Tạo panel content chính với scroll (không border)
       JPanel billContent = new JPanel();
       billContent.setLayout(new BorderLayout());
       billContent.setBackground(Color.WHITE);

       // Các panel con (không border)
       billBody = new JPanel();
       billBody.setLayout(new BoxLayout(billBody, BoxLayout.Y_AXIS));
       billBody.setBackground(Color.WHITE);

       // Scroll pane chính (không border)
       JScrollPane mainScrollPane = new JScrollPane(billBody);
       mainScrollPane.setBorder(null); // Loại bỏ border mặc định của scroll pane
       mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
       mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

       // Thêm vào billContent
       billContent.add(mainScrollPane, BorderLayout.CENTER);
       panelBill.add(billContent, BorderLayout.CENTER);
      
       panel.add(panelBill);
       
        // IMEI UI removed
       
        txtDescription = new JTextArea();
        txtDescription.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        txtDescription.setLineWrap(true); // Tự động xuống dòng khi hết chiều rộng
        txtDescription.setWrapStyleWord(true); // Xuống dòng theo từ
        txtDescription.setBackground(Color.WHITE); // Nền trắng
        txtDescription.setBorder(new LineBorder(Color.GRAY)); // Viền đơn màu xám

        JScrollPane scrollDescription= new JScrollPane(txtDescription);
        scrollDescription.setBorder(new LineBorder(Color.GRAY)); // Viền cho JScrollPane
       scrollDescription.setBounds(200, 480, 360, 88); // Thay đổi tọa độ và kích thước nếu cần
        panel.add(scrollDescription);
       
        startDate = new JDateChooser();
        startDate.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        startDate.setDateFormatString("dd/MM/yyyy");  // Định dạng ngày theo kiểu dd/MM/yyyy
        startDate.setBounds(200, 600, 160, 35);
        startDate.setBackground(Color.WHITE);
        panel.add(startDate);
        
        endDate = new JDateChooser();
        endDate.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        endDate.setDateFormatString("dd/MM/yyyy");  // Định dạng ngày theo kiểu dd/MM/yyyy
        endDate.setBounds(200, 680, 160, 35);
        endDate.setBackground(Color.WHITE);
        panel.add(endDate);
       
          
        bntAddBill = new MyButton("Thêm Bill", 20);
        stylePrimaryButton(bntAddBill);
        bntAddBill.setBounds(610, 732, 110, 35);
        bntAddBill.addActionListener((e) -> {
            if (!validateFields()) {
                return; // Nếu dữ liệu không hợp lệ, dừng lại
            }

            // IMEI logic removed
        });

        panel.add(bntAddBill);
        
        bntExport = new MyButton("💾 Lưu Bill", 20);
        stylePrimaryButton(bntExport);
        bntExport.setFont(FONT_BUTTON_LARGE);
        bntExport.setBounds(950, 720, 200, 60);
        bntExport.addActionListener((e) -> {
            // Kiểm tra các trường nhập liệu trước khi xuất
            if (!validateFields()) {
                return; // Dừng thực thi nếu có trường không hợp lệ
            }
            boolean confirm = CustomDialog.showOptionPane(
                "Confirm Exportation",
                "Are you sure want to export bill?",
                UIManager.getIcon("OptionPane.questionIcon"),
                Color.decode("#FF6666")
            );

          if(confirm){
             try {
                // Khởi tạo BUS để xử lý nghiệp vụ
                busCustomerInfo = new BUS_CustomerInfo();
                busProductInfo = new BUS_ProductInfo();
                busWarranty = new BUS_Warranty();

                String adminID = txtAdminID.getText().strip();
                String adminName = txtAdminName.getText().strip();
                String productID = txtProductID.getText().strip();
                String customerID = null; // optional in DB
                Date startDateValue = startDate.getDate();
                Date endDateValue = endDate.getDate();
                String description = txtDescription.getText();

                // Kiểm tra khách hàng
                DTO_CustomerInfo customer = null; // optional

                // Kiểm tra sản phẩm (simple: must have Product ID text)
                DTOProductInfo productInfo = null;
                if (productID.isEmpty()) {
                    CustomDialog.showError("Please select a product ID!");
                    return;
                }

                // Kiểm tra ngày hợp lệ
                if (endDateValue.before(startDateValue)) {
                    CustomDialog.showError("End date must be after start date!");
                    return;
                }

                // Tạo DTO cho hóa đơn bảo hành
                DTO_Insurance insurance = new DTO_Insurance(
                    warrantyNo,  // Sử dụng biến toàn cục thay vì tạo mới
                    adminID,
                    customerID,
                    description, // Thêm describleCustomer
                    startDateValue.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                    endDateValue.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                );

                // Tạo DTO cho chi tiết hóa đơn bảo hành
                DTO_InsuranceDetails insuranceDetails = new DTO_InsuranceDetails(
                    warrantyNo, // Sử dụng biến toàn cục thay vì tạo mới
                    adminID,
                    customerID,
                    productID,
                    description,
                    LocalDate.now(), 
                    java.time.LocalTime.now()
                );

                // Thêm hóa đơn bảo hành vào database
                boolean warrantyInserted = busWarranty.insertBillWarranty(insurance);
                boolean detailsInserted = busWarranty.insertBillWarrantyDetails(insuranceDetails);

                if (warrantyInserted && detailsInserted) {
                    CustomDialog.showSuccess("Warranty bill exported successfully and saved in database !");
                } else {
                    CustomDialog.showError("Failed to save warranty invoice!");
                    return;
                }

                // Xuất PDF
                PDF_Insurance pdfExporter = new PDF_Insurance(
                    panelBill, 
                    adminID, 
                    adminName, 
                    customer, 
                    productInfo, 
                    startDateValue, 
                    endDateValue, 
                    description
                );
                pdfExporter.exportToPDF();

                // Xóa dữ liệu hiển thị trên giao diện
                billBody.removeAll();
                billBody.revalidate();
                billBody.repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
                CustomDialog.showError("Error while exporting warranty invoice: " + ex.getMessage());
            }
         }
          
        });

        panel.add(bntExport);  
   }
    
    public void Refresh(){
        txtProductID.setText(null);
        txtProductName.setText(null);
        txtCateID.setText(null);
        txtWarranty.setText(null);
        txtDescription.setText(null);
        txtBrand.setText(null);
        billBody.removeAll();
        billBody.revalidate();
        billBody.repaint();
    }
    
    
    private void loadProductInfo(String imei) {
        busProductInfo = new BUS_ProductInfo(); // Khởi tạo BUS
// [REMOVED IMEI]         DTOProductInfo productInfo = busProductInfo.getProductInfoByIMEI(imei); // Lấy thông tin sản phẩm theo IMEI

        // IMEI lookup removed
    }
    private JLabel createSeparator() {
           JLabel separator = new JLabel("===================================================");
           separator.setForeground(Color.GRAY);
           separator.setAlignmentX(Component.CENTER_ALIGNMENT);
           return separator;
       }

    // Hàm thêm dòng thông tin
    private void addInfoRow(JPanel panel, String label, String value) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rowPanel.setBackground(Color.WHITE);
        
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.PLAIN, 12));
        
        rowPanel.add(lblLabel);
        rowPanel.add(lblValue);
        panel.add(rowPanel);
    }

    private void createWarrantyBill(String imei) {
        try {
            busCustomerInfo = new BUS_CustomerInfo();
            busProductInfo = new BUS_ProductInfo();
            warrantyNo= new String();
            billBody = getBillBody();
            if (billBody == null) {
                CustomDialog.showError("Bill display area not found!");
                return;
            }

            // Clear existing components
            billBody.removeAll();
            billBody.setLayout(new BoxLayout(billBody, BoxLayout.Y_AXIS));
            billBody.setBackground(Color.WHITE);

            // ===== 0. Warranty Invoice No =====
            warrantyNo = String.format("%010d", new Random().nextInt(1_000_000_000));

            lblInvoice = new JLabel("WARRANTY INVOICE No: " + warrantyNo, SwingConstants.CENTER);
            lblInvoice.setFont(new Font("Arial", Font.BOLD, 16));
            lblInvoice.setAlignmentX(Component.CENTER_ALIGNMENT);
            billBody.add(lblInvoice);
            addVerticalSpace(15); // Sử dụng phương thức helper

            // ===== 1. Admin Information =====
            String adminID = txtAdminID.getText().strip();
            String adminName = txtAdminName.getText().strip();

            JPanel adminPanel = createSectionPanel("ADMIN INFORMATION");
            addInfoRow(adminPanel, "Admin ID:", adminID);
            addInfoRow(adminPanel, "Admin Name:", adminName);
            billBody.add(adminPanel);
            addSeparatorWithSpace();

            // ===== 2. Customer Information =====
// [REMOVED IMEI]             DTO_CustomerInfo customer = busCustomerInfo.getCustomerInfoByIMEI(imei);
            // Customer section optional; removed IMEI-based retrieval

            // ===== 3. Product Information =====
// [REMOVED IMEI]             DTOProductInfo productInfo = busProductInfo.getProductInfoByIMEI(imei);
            // Product section removed (no IMEI lookup in this form)

            // ===== 4. Warranty Details =====
            JPanel warrantyPanel = createSectionPanel("WARRANTY DETAILS");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            // Start Date
            String startDateStr = (startDate != null && startDate.getDate() != null) ? 
                                 dateFormat.format(startDate.getDate()) : "N/A";
            addInfoRow(warrantyPanel, "Start Date:", startDateStr);

            // End Date
            String endDateStr = (endDate != null && endDate.getDate() != null) ? 
                               dateFormat.format(endDate.getDate()) : "N/A";
            addInfoRow(warrantyPanel, "End Date:", endDateStr);

            // Issue Description
            String description = txtDescription != null ? txtDescription.getText() : "";
            addInfoRow(warrantyPanel, "Issue Description:", description);

            billBody.add(warrantyPanel);
            addSeparatorWithSpace();

            // ===== 5. Terms & Conditions =====
            JPanel termsPanel = createSectionPanel("TERMS & CONDITIONS");
            JTextArea termsArea = new JTextArea(
                "1. Warranty covers manufacturing defects only.\n" +
                "2. Warranty does not cover physical damage or liquid damage.\n" +
                "3. ID card must be presented for warranty claims.\n" +
                "4. Warranty is non-transferable."
            );
            termsArea.setEditable(false);
            termsArea.setFont(new Font("Arial", Font.PLAIN, 12));
            termsArea.setBackground(Color.WHITE);
            termsArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            // Đảm bảo termsArea được thêm vào termsPanel chứ không phải warrantyPanel
            termsPanel.add(termsArea);
            billBody.add(termsPanel);

            // Refresh UI
            billBody.revalidate();
            billBody.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("Error creating warranty bill: " + e.getMessage());
        }
    }

    // Helper methods để tránh lặp code và đảm bảo thêm component đúng cách
    private void addVerticalSpace(int height) {
        billBody.add(Box.createVerticalStrut(height));
    }

    private void addSeparatorWithSpace() {
        billBody.add(createSeparator());
        addVerticalSpace(10);
    }
  
    private JPanel createSectionPanel(String title) {
        JPanel panelcreate = new JPanel();
        panelcreate.setLayout(new BoxLayout(panelcreate, BoxLayout.Y_AXIS));
        panelcreate.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelcreate.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        panelcreate.add(titleLabel);
        panelcreate.add(Box.createVerticalStrut(5));
        panelcreate.add(separator);
        panelcreate.add(Box.createVerticalStrut(5));

        return panelcreate;
    }
    
     private JPanel getBillBody() {
            // Kiểm tra cấu trúc panel theo đúng cách bạn đã thiết kế
            if (panelBill.getComponentCount() > 0) {
                Component northComp = panelBill.getComponent(0); // Panel tiêu đề (NORTH)

                // Lấy component CENTER (index 1 nếu có cả NORTH và CENTER)
                if (panelBill.getComponentCount() > 1) {
                    Component centerComp = panelBill.getComponent(1);
                    if (centerComp instanceof JPanel) {
                        JPanel billContent = (JPanel) centerComp;
                        if (billContent.getComponentCount() > 0) {
                            Component scrollComp = billContent.getComponent(0);
                            if (scrollComp instanceof JScrollPane) {
                                JScrollPane scrollPane = (JScrollPane) scrollComp;
                                return (JPanel) scrollPane.getViewport().getView();
                            }
                        }
                    }
                }
            }

            // Fallback: tạo mới nếu không tìm thấy (đảm bảo không bao giờ null)
            JPanel newBody = new JPanel();
            newBody.setLayout(new BoxLayout(newBody, BoxLayout.Y_AXIS));
            newBody.setBackground(Color.WHITE);

            // Tạo lại cấu trúc scroll pane nếu cần
            JScrollPane scrollPane = new JScrollPane(newBody);
            scrollPane.setBorder(null);

            // Tạo lại cấu trúc billContent
            JPanel billContent = new JPanel(new BorderLayout());
            billContent.add(scrollPane, BorderLayout.CENTER);

            // Cập nhật lại panelBill
            panelBill.removeAll();
            panelBill.add(panelTitle, BorderLayout.NORTH);
            panelBill.add(billContent, BorderLayout.CENTER);
            panelBill.revalidate();

            return newBody;
         }
    
     private boolean validateFields() {
        if (txtDescription.getText().strip().isEmpty()) {
            CustomDialog.showError("Please enter a warranty description!");
            return false;
        }
        if (startDate.getDate() == null) {
            CustomDialog.showError("Please select a start date for the warranty!");
            return false;
        }
        if (endDate.getDate() == null) {
            CustomDialog.showError("Please select an end date for the warranty!");
            return false;
        }
         return true; // Trả về `true` nếu tất cả các trường hợp hợp lệ
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

     private void styleInfoButton(MyButton btn) {
         btn.setBackgroundColor(INFO_COLOR);
         btn.setHoverColor(INFO_HOVER);
         btn.setPressedColor(INFO_HOVER.darker());
         btn.setFont(FONT_BUTTON_MEDIUM);
         btn.setForeground(Color.WHITE);
     }
}
