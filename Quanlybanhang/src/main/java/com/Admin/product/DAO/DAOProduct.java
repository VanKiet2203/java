package com.Admin.product.DAO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.ComponentandDatabase.Components.CustomDialog;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.sql.*;
import java.math.BigDecimal;
import java.awt.Image;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import com.ComponentandDatabase.Components.MyButton;
import com.ComponentandDatabase.Database_Connection.DatabaseConnection;
import com.Admin.category.DTO.DTOCategory;
import com.Admin.product.DTO.DTOProduct;
import java.sql.Connection;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JLabel;

public class DAOProduct {

    private BufferedImage uploadedImage;
    private String imagePath;
     private Connection getConnection() throws SQLException {
        return DatabaseConnection.connect();
    }
    
   public String handleUploadButton(MyButton bntupload, JPanel panelUpload) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Choose an image");
    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Images", "jpg", "jpeg", "png"));

    int result = fileChooser.showOpenDialog(null);
    if (result == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();

        try {
            BufferedImage originalImage = ImageIO.read(selectedFile);
            if (originalImage != null) {
                // Resize ảnh
                Image scaledImage = originalImage.getScaledInstance(230, 230, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaledImage);
                String imagePath = selectedFile.getAbsolutePath(); // <-- Khai báo ở đây

                // Lưu ảnh vào thư mục nội bộ
                File destFolder = new File("D:" + File.separator + "Image_Data");

                if (!destFolder.exists()) destFolder.mkdirs(); // Tạo thư mục nếu chưa có

                // Kiểm tra nếu ảnh đã tồn tại trong thư mục (so sánh tên đầy đủ bao gồm phần mở rộng)
                File destFile = new File(destFolder, selectedFile.getName());
                if (destFile.exists()) {
                    // Nếu file đã tồn tại, sử dụng tên gốc của file
                    imagePath = destFile.getAbsolutePath(); // Dùng đường dẫn của file đã tồn tại
                    // Hiển thị ảnh lên GUI
                    JLabel imageLabel = new JLabel(icon);
                    panelUpload.removeAll();
                    panelUpload.add(imageLabel, "center");
                    panelUpload.revalidate();
                    panelUpload.repaint();
                    panelUpload.setVisible(true);
                } else {
                    // Nếu file chưa tồn tại, tạo bản sao mới với tên duy nhất
                    String newFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                    destFile = new File(destFolder, newFileName);

                    // Kiểm tra nếu file với tên mới đã tồn tại, sẽ tiếp tục tạo tên mới
                    while (destFile.exists()) {
                        newFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                        destFile = new File(destFolder, newFileName);
                    }

                    // Sao chép ảnh vào thư mục
                    Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    imagePath = destFile.getAbsolutePath(); // Cập nhật đường dẫn file mới

                    // Hiển thị ảnh lên GUI
                    JLabel imageLabel = new JLabel(icon);
                    panelUpload.removeAll();
                    panelUpload.add(imageLabel, "center");
                    panelUpload.revalidate();
                    panelUpload.repaint();
                    panelUpload.setVisible(true);
                }

                return imagePath; // ✅ Trả về đường dẫn ảnh đã lưu

            } else {
                CustomDialog.showError("Invalid image!");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            CustomDialog.showError("Cannot upload this image!");
        }
    }
    return null; // Trường hợp không chọn ảnh hoặc xảy ra lỗi
}

    
    public List<DTOCategory> getAllCategoriesWithSupplier() {
        List<DTOCategory> list = new ArrayList<>();
        String sql = """
            SELECT c.Category_ID, c.Category_Name, s.Sup_ID, s.Sup_Name, s.Address, s.Contact
            FROM Category c 
            JOIN Supplier s ON c.Sup_ID = s.Sup_ID
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                DTOCategory dto = new DTOCategory();
                dto.setCategoryID(rs.getString("Category_ID"));
                dto.setCategoryName(rs.getString("Category_Name"));
                dto.setSupID(rs.getString("Sup_ID"));
                dto.setSupName(rs.getString("Sup_Name"));
                dto.setAddress(rs.getString("Address"));
                dto.setContact(rs.getString("Contact"));

                list.add(dto);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            CustomDialog dialog = new CustomDialog();
            dialog.showError("Lỗi khi truy vấn dữ liệu danh mục!");
        }

        return list;
    }
     
     public boolean isProductIDExists(String productID) {
        String sql = "SELECT 1 FROM Product WHERE Product_ID = ? AND Status = 'Available'";
        
        try (Connection conn = getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, productID);
            ResultSet rs = stmt.executeQuery();
            
            return rs.next(); // Nếu có dòng trả về, tức là đã tồn tại Product_ID
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
     
    public void saveProduct(DTOProduct product) {
    // Kiểm tra xem Product_ID đã tồn tại chưa
    if (isProductIDExists(product.getProductId())) {
        CustomDialog.showError("Product ID already exists! Please use another one.");
        return;
    }

    // Nếu Product_ID không tồn tại, thực hiện lưu sản phẩm
    // Quantity sẽ được trigger tự động tính toán, không cần set = 0
    String sql = "INSERT INTO Product(Product_ID, Product_Name, Color, Speed, " +
                "Battery_Capacity, Quantity, Category_ID, Sup_ID, Image, Price, List_Price_Before, List_Price_After, Warehouse_Item_ID, Warranty_Months) " +
                "VALUES (?, ?, ?, ?, ?, 0, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, product.getProductId());
        stmt.setString(2, product.getProductName());
        stmt.setString(3, product.getColor());
        stmt.setString(4, product.getSpeed());
        stmt.setString(5, product.getBatteryCapacity());
        // Set Quantity = 0, trigger sẽ tự động tính toán
        stmt.setString(6, product.getCategoryId());
        stmt.setString(7, product.getSupId());
        stmt.setString(8, product.getImage());
        stmt.setBigDecimal(9, product.getPrice());
        stmt.setBigDecimal(10, product.getListPriceBefore());
        stmt.setBigDecimal(11, product.getListPriceAfter());
        stmt.setString(12, product.getProductId()); // Warehouse_Item_ID = Product_ID (same ID)
        stmt.setInt(13, product.getWarrantyMonths());

        stmt.executeUpdate();
        CustomDialog.showSuccess("Product saved successfully!");
    } catch (SQLException e) {
        e.printStackTrace();
        CustomDialog.showError("Product save failed");
    }
}
     
     
    public void uploadProductToTable(DefaultTableModel model) {
        model.setRowCount(0);
        
        // Hiển thị sản phẩm với 3 loại số lượng: Tổng nhập, Tồn kho, Đã bán
        String sql = """
            SELECT p.Product_ID, p.Product_Name, p.Color, p.Speed, 
                   p.Battery_Capacity, p.Warranty_Months,
                   ISNULL(ps.Quantity_Stock, 0) AS Total_Imported,    -- Số lượng nhập (từ Inventory)
                   p.Quantity AS Current_Stock,                        -- Số lượng tồn kho (Product)
                   ISNULL(ps.Quantity_Stock, 0) - p.Quantity AS Sold_Quantity, -- Số lượng đã bán (Nhập - Tồn)
                   ISNULL(p.Price, 0) AS Price,                        -- Giá bán (đảm bảo không null)
                   c.Category_ID, c.Category_Name,
                   CASE 
                       WHEN ISNULL(ps.Quantity_Stock, 0) = p.Quantity + (ISNULL(ps.Quantity_Stock, 0) - p.Quantity)
                       THEN N'✓ Cân bằng'
                       ELSE N'✗ Lệch'
                   END AS Balance_Status
            FROM Product p 
            JOIN Category c ON p.Category_ID = c.Category_ID
            LEFT JOIN Product_Stock ps ON p.Warehouse_Item_ID = ps.Warehouse_Item_ID
            WHERE p.Status = 'Available' 
                AND c.Status = 'Available'
                AND (ps.Status = 'Available' OR ps.Status IS NULL)
            ORDER BY p.Product_ID
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                java.math.BigDecimal price = rs.getBigDecimal("Price");
                // Format price để tránh scientific notation
                String formattedPrice = "0.00";
                if (price != null && price.compareTo(java.math.BigDecimal.ZERO) > 0) {
                    formattedPrice = String.format("%.2f", price.doubleValue());
                }

                Object[] row = new Object[]{
                    rs.getString("Product_ID"),
                    rs.getString("Product_Name"), 
                    rs.getString("Color"),
                    rs.getString("Speed"),
                    rs.getString("Battery_Capacity"),
                    rs.getInt("Warranty_Months"),
                    rs.getInt("Total_Imported"),      // Tổng số lượng đã nhập
                    rs.getInt("Current_Stock"),       // Số lượng tồn kho hiện tại
                    rs.getInt("Sold_Quantity"),       // Số lượng đã bán
                    formattedPrice,
                    rs.getString("Category_ID"),
                    rs.getString("Category_Name")
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            CustomDialog.showError("Error loading products: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("Unexpected error loading products: " + e.getMessage());
        }
    }
    public DTOProduct getProductById(String productId) {
    String sql = "SELECT Product_ID, Product_Name, Color, Speed, " +
                "Battery_Capacity, Quantity, Category_ID, Sup_ID, Image, Price, List_Price_Before, List_Price_After, Warehouse_Item_ID, Warranty_Months " +
                "FROM Product WHERE Product_ID = ?";

    try (Connection conn = getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, productId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            DTOProduct product = new DTOProduct(
                rs.getString("Product_ID"),
                rs.getString("Product_Name"),
                rs.getString("Color"),
                rs.getString("Speed"),
                rs.getString("Battery_Capacity"),
                rs.getInt("Quantity"),
                rs.getString("Category_ID"),
                rs.getString("Sup_ID"),
                rs.getString("Image"),
                rs.getBigDecimal("Price"),
                rs.getBigDecimal("List_Price_Before"),
                rs.getBigDecimal("List_Price_After"),
                rs.getInt("Warranty_Months")
            );
            // Set Warehouse_Item_ID if available
            String warehouseItemId = rs.getString("Warehouse_Item_ID");
            if (warehouseItemId != null) {
                // Assuming DTOProduct has a method to set warehouse item ID
                // product.setWarehouseItemId(warehouseItemId);
            }
            return product;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}
    public boolean updateProduct(DTOProduct product) {
        String sql = "UPDATE Product SET Product_Name = ?, Color = ?, Speed = ?, " +
                    "Battery_Capacity = ?, Category_ID = ?, Sup_ID = ?, " +
                    "Image = ?, Price = ?, List_Price_Before = ?, List_Price_After = ?, Warehouse_Item_ID = ? WHERE Product_ID = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getColor());
            stmt.setString(3, product.getSpeed());
            stmt.setString(4, product.getBatteryCapacity());
            // KHÔNG cập nhật Quantity ở đây vì trigger sẽ tự động tính toán
            stmt.setString(5, product.getCategoryId());
            stmt.setString(6, product.getSupId());
            stmt.setString(7, product.getImage());
            stmt.setBigDecimal(8, product.getPrice());
            stmt.setBigDecimal(9, product.getListPriceBefore());
            stmt.setBigDecimal(10, product.getListPriceAfter());
            stmt.setString(11, product.getProductId()); // Warehouse_Item_ID = Product_ID (same ID)
            stmt.setString(12, product.getProductId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

   public boolean deleteProduct(String productId) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction

            // Soft delete: Chỉ thay đổi Status thành 'Unavailable' thay vì xóa thật
            String sql = "UPDATE Product SET Status = 'Unavailable' WHERE Product_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, productId);
                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    conn.commit(); // Commit nếu thành công
                    return true;
                }
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback nếu lỗi
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset lại autocommit
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private boolean hasRelatedRecords(String productId) {
        String sql = "SELECT 1 FROM Orders_Details WHERE Product_ID = ? AND Record_Status = 'Available'";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, productId);
            return stmt.executeQuery().next();

        } catch (SQLException e) {
            e.printStackTrace();
            return true; // Trả về true nếu có lỗi để ngăn xóa nhầm
        }
    }
    
    public void searchProduct(String keyword, String selected, DefaultTableModel model) {
        model.setRowCount(0);  // Xóa dữ liệu cũ trong bảng

        String sql = """
            SELECT 
                p.Product_ID, 
                p.Product_Name, 
                p.Price, 
                p.Quantity, 
                CASE 
                    WHEN p.Quantity = 0 THEN 'Unavailable' 
                    ELSE 'Available' 
                END AS Status, 
                c.Category_ID, 
                c.Category_Name, 
                s.Sup_ID AS Brand_ID, 
                s.Sup_Name AS Brand_Name, 
                s.Contact
            FROM 
                Product p
            JOIN Category c ON p.Category_ID = c.Category_ID
            JOIN Supplier s ON c.Sup_ID = s.Sup_ID
            WHERE 
        """;

        boolean needParameter = true;

        switch (selected) {
            case "Product.ID" -> sql += "p.Product_ID = ?";
            case "Product Name" -> sql += "p.Product_Name LIKE ?";
            case "Brand.ID" -> sql += "s.Sup_ID LIKE ?";
            case "Available" -> {
                sql += "p.Quantity > 0";
                needParameter = false;
            }
            case "Unavailable" -> {
                sql += "p.Quantity = 0";
                needParameter = false;
            }
            default -> {
                return;  // Không làm gì nếu không khớp
            }
        }

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (needParameter) {
                stmt.setString(1, selected.equals("Product.ID") ? keyword : "%" + keyword + "%");
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    java.math.BigDecimal price = rs.getBigDecimal("Price");
                    String formattedPrice = price != null ? 
                        String.format("%.2f", price.doubleValue()) : "0.00";
                        
                    Object[] row = new Object[]{
                        rs.getString("Product_ID"),
                        rs.getString("Product_Name"),
                        formattedPrice,
                        rs.getInt("Quantity"),
                        rs.getString("Status"),
                        rs.getString("Category_ID"),
                        rs.getString("Category_Name"),
                        rs.getString("Brand_ID"),
                        rs.getString("Brand_Name"),
                        rs.getString("Contact")
                    };
                    model.addRow(row);  // Đổ vào bảng
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            CustomDialog.showError("Lỗi khi tìm kiếm sản phẩm!");
        }
    }

    public void exportProductToExcel(String filePath) {
        String sql = "SELECT * FROM Product WHERE Status = 'Available'";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Products");

            // Header
            String[] headers = {
                "Product ID", "Product Name", "Color", "Speed", "Battery Capacity",
                "Price", "Quantity", "Category ID", "Image"
            };

            XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Data
            int rowIndex = 1;
            while (rs.next()) {
                XSSFRow row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(rs.getString("Product_ID"));
                row.createCell(1).setCellValue(rs.getString("Product_Name"));
                row.createCell(2).setCellValue(rs.getString("Color"));
                row.createCell(3).setCellValue(rs.getString("Speed"));
                row.createCell(4).setCellValue(rs.getString("Battery_Capacity"));
                row.createCell(5).setCellValue(rs.getBigDecimal("Price").toString());
                row.createCell(6).setCellValue(rs.getBigDecimal("List_Price_Before").toString());
                row.createCell(7).setCellValue(rs.getBigDecimal("List_Price_After").toString());
                row.createCell(8).setCellValue(rs.getInt("Quantity"));
                row.createCell(9).setCellValue(rs.getString("Category_ID"));
                row.createCell(10).setCellValue(rs.getString("Sup_ID"));
                row.createCell(11).setCellValue(rs.getString("Image"));
            }

            // Autosize columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                workbook.write(out);
            }

            workbook.close();
            CustomDialog.showSuccess("File exported successfully !");

        } catch (Exception e) {
            e.printStackTrace();
            CustomDialog.showError("Lỗi khi xuất dữ liệu sản phẩm ra Excel!");
        }
    }
    public boolean addProduct(DTOProduct product) {
        String sql = "INSERT INTO Product(Product_ID, Product_Name, Color, Speed, " +
                    "Battery_Capacity, Quantity, Category_ID, Sup_ID, Image, Price, List_Price_Before, List_Price_After, Warehouse_Item_ID, Warranty_Months) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getProductId());
            ps.setString(2, product.getProductName());
            ps.setString(3, product.getColor());
            ps.setString(4, product.getSpeed());
            ps.setString(5, product.getBatteryCapacity());
            ps.setInt(6, product.getQuantity());
            ps.setString(7, product.getCategoryId());
            ps.setString(8, product.getSupId());
            ps.setString(9, product.getImage());
            ps.setBigDecimal(10, product.getPrice());
            ps.setBigDecimal(11, product.getListPriceBefore());
            ps.setBigDecimal(12, product.getListPriceAfter());
            ps.setString(13, product.getProductId()); // Warehouse_Item_ID = Product_ID (same ID)
            ps.setInt(14, product.getWarrantyMonths());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Lấy thông tin sản phẩm từ kho (Product_Stock) để tạo Product
    public DTOProduct getProductFromWarehouse(String warehouseItemId) {
        String sql = """
            SELECT ps.Warehouse_Item_ID, ps.Product_Name, ps.Category_ID, ps.Sup_ID, 
                   ps.Quantity_Stock, ps.Unit_Price_Import, ps.Created_Date, ps.Created_Time
            FROM Product_Stock ps
            WHERE ps.Warehouse_Item_ID = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, warehouseItemId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Tạo DTOProduct từ dữ liệu kho
                DTOProduct product = new DTOProduct();
                product.setProductId(rs.getString("Warehouse_Item_ID"));
                product.setProductName(rs.getString("Product_Name"));
                product.setCategoryId(rs.getString("Category_ID"));
                product.setSupId(rs.getString("Sup_ID"));
                product.setQuantity(rs.getInt("Quantity_Stock"));
                // Color, Speed, Battery_Capacity không có trong kho mới - set null
                product.setColor(null);
                product.setSpeed(null);
                product.setBatteryCapacity(null);
                // Giá bán để null để admin nhập
                product.setPrice(null);
                product.setListPriceBefore(null);
                product.setListPriceAfter(null);
                
                return product;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Lấy danh sách Warehouse Items chưa được tạo Product
    public void loadAvailableWarehouseItems(DefaultTableModel model) {
        String sql = """
            SELECT ps.Warehouse_Item_ID, ps.Product_Name, c.Category_Name, s.Sup_Name, 
                   ps.Quantity_Stock, ps.Unit_Price_Import, ps.Created_Date
            FROM Product_Stock ps
            LEFT JOIN Category c ON ps.Category_ID = c.Category_ID
            LEFT JOIN Supplier s ON ps.Sup_ID = s.Sup_ID
            WHERE ps.Warehouse_Item_ID NOT IN (
                SELECT DISTINCT Warehouse_Item_ID 
                FROM Product 
                WHERE Warehouse_Item_ID IS NOT NULL
            )
                AND ps.Status = 'Available'
                AND c.Status = 'Available'
                AND s.Status = 'Available'
            ORDER BY ps.Created_Date DESC
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            model.setRowCount(0);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("Warehouse_Item_ID"),
                    rs.getString("Product_Name"),
                    rs.getString("Category_Name"),
                    rs.getString("Sup_Name"),
                    rs.getInt("Quantity_Stock"),
                    rs.getBigDecimal("Unit_Price_Import"),
                    rs.getDate("Created_Date")
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            CustomDialog.showError("Error loading warehouse items: " + e.getMessage());
        }
    }
    
    // Tạo Product từ Warehouse Item
    public boolean createProductFromWarehouse(String warehouseItemId, String color, String speed, 
                                            String batteryCapacity, BigDecimal price) {
        String insertSQL = """
            INSERT INTO Product (Product_ID, Product_Name, Color, Speed, Battery_Capacity, 
                               Quantity, Category_ID, Sup_ID, Image, Price, List_Price_Before, 
                               List_Price_After, Warehouse_Item_ID)
            SELECT ?, Product_Name, ?, ?, ?, Quantity_Stock, Category_ID, Sup_ID, 
                   NULL, ?, ?, ?, Warehouse_Item_ID
            FROM Product_Stock 
            WHERE Warehouse_Item_ID = ? AND Status = 'Available'
        """;
        
        String updateWarehouseSQL = """
            UPDATE Product_Stock 
            SET Is_In_Product = 1 
            WHERE Warehouse_Item_ID = ?
        """;
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL);
                 PreparedStatement updateStmt = conn.prepareStatement(updateWarehouseSQL)) {
                
                insertStmt.setString(1, warehouseItemId); // Product_ID = Warehouse_Item_ID
                insertStmt.setString(2, color);
                insertStmt.setString(3, speed);
                insertStmt.setString(4, batteryCapacity);
                insertStmt.setBigDecimal(5, price);
                insertStmt.setBigDecimal(6, price.multiply(new BigDecimal("0.9"))); // List_Price_Before
                insertStmt.setBigDecimal(7, price.multiply(new BigDecimal("0.8"))); // List_Price_After
                insertStmt.setString(8, warehouseItemId);
                
                int rowsAffected = insertStmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    // Update Is_In_Product flag
                    updateStmt.setString(1, warehouseItemId);
                    updateStmt.executeUpdate();
                    
                    // Trigger sẽ tự động cập nhật Product.Quantity = Quantity_Stock
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Lấy danh sách Warehouse Items có thể tạo Product
    public List<String> getAvailableWarehouseItems() {
        List<String> warehouseItems = new ArrayList<>();
        String sql = """
            SELECT Warehouse_Item_ID, Product_Name 
            FROM Product_Stock 
            WHERE Status = 'Available' 
            AND Is_In_Product = 0
            ORDER BY Warehouse_Item_ID
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String warehouseId = rs.getString("Warehouse_Item_ID");
                String productName = rs.getString("Product_Name");
                warehouseItems.add(warehouseId + " - " + productName);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return warehouseItems;
    }
    
    // Import products from Excel file
    public void importProductFromExcel(File excelFile) {
        String insertSQL = "INSERT INTO Product (Product_ID, Product_Name, Color, Speed, Battery_Capacity, Quantity, Category_ID, Sup_ID, Image, Price, List_Price_Before, List_Price_After, Warehouse_Item_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String checkCategorySQL = "SELECT COUNT(*) FROM Category WHERE Category_ID = ? AND Status = 'Available'";
        String checkProductSQL = "SELECT COUNT(*) FROM Product WHERE Product_ID = ? AND Status = 'Available'";

        int successCount = 0;
        int errorCount = 0;
        StringBuilder errors = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(excelFile);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) {
                rowIterator.next(); // Skip header
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                int rowNum = row.getRowNum() + 1;

                if (row.getPhysicalNumberOfCells() >= 9) {
                    String productID = getCellValueAsString(row.getCell(0));
                    String productName = getCellValueAsString(row.getCell(1));
                    String color = getCellValueAsString(row.getCell(2));
                    String speed = getCellValueAsString(row.getCell(3));
                    String batteryCapacity = getCellValueAsString(row.getCell(4));
                    int quantity = (int) getCellValueAsNumber(row.getCell(5));
                    String categoryID = getCellValueAsString(row.getCell(6));
                    String supID = getCellValueAsString(row.getCell(7));
                    double price = getCellValueAsNumber(row.getCell(8));

                    if (productID == null || productID.trim().isEmpty() ||
                        productName == null || productName.trim().isEmpty() ||
                        categoryID == null || categoryID.trim().isEmpty() ||
                        supID == null || supID.trim().isEmpty() ||
                        quantity <= 0 || price <= 0) {
                        errors.append("Row ").append(rowNum).append(": Missing or invalid data\n");
                        errorCount++;
                        continue;
                    }

                    try (Connection conn = getConnection()) {
                        conn.setAutoCommit(false);

                        try (PreparedStatement checkCategoryStmt = conn.prepareStatement(checkCategorySQL);
                             PreparedStatement checkProductStmt = conn.prepareStatement(checkProductSQL);
                             PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {

                            // Check if Category exists
                            checkCategoryStmt.setString(1, categoryID);
                            try (ResultSet categoryRs = checkCategoryStmt.executeQuery()) {
                                if (!categoryRs.next() || categoryRs.getInt(1) == 0) {
                                    errors.append("Row ").append(rowNum).append(": Category ID '").append(categoryID).append("' does not exist\n");
                                    errorCount++;
                                    continue;
                                }
                            }

                            // Check if Product already exists
                            checkProductStmt.setString(1, productID);
                            try (ResultSet productRs = checkProductStmt.executeQuery()) {
                                if (productRs.next() && productRs.getInt(1) > 0) {
                                    errors.append("Row ").append(rowNum).append(": Product ID '").append(productID).append("' already exists\n");
                                    errorCount++;
                                    continue;
                                }
                            }

                            // Insert product
                            insertStmt.setString(1, productID);
                            insertStmt.setString(2, productName);
                            insertStmt.setString(3, color);
                            insertStmt.setString(4, speed);
                            insertStmt.setString(5, batteryCapacity);
                            insertStmt.setInt(6, quantity);
                            insertStmt.setString(7, categoryID);
                            insertStmt.setString(8, supID);
                            insertStmt.setString(9, null); // Image
                            insertStmt.setBigDecimal(10, BigDecimal.valueOf(price));
                            insertStmt.setBigDecimal(11, BigDecimal.valueOf(price * 0.9)); // List_Price_Before
                            insertStmt.setBigDecimal(12, BigDecimal.valueOf(price * 0.8)); // List_Price_After
                            insertStmt.setString(13, productID); // Warehouse_Item_ID = Product_ID

                            insertStmt.executeUpdate();
                            conn.commit();
                            successCount++;

                        } catch (SQLException e) {
                            conn.rollback();
                            errors.append("Row ").append(rowNum).append(": ").append(e.getMessage()).append("\n");
                            errorCount++;
                        } finally {
                            conn.setAutoCommit(true);
                        }

                    } catch (SQLException e) {
                        errors.append("Row ").append(rowNum).append(": Database connection error - ").append(e.getMessage()).append("\n");
                        errorCount++;
                    }
                } else {
                    errors.append("Row ").append(rowNum).append(": Insufficient data (need at least 9 columns)\n");
                    errorCount++;
                }
            }

            // Show results
            String message = String.format("Import completed!\nSuccess: %d products\nErrors: %d rows", 
                                         successCount, errorCount);
            if (errorCount > 0) {
                message += "\n\nErrors:\n" + errors.toString();
            }
            
            if (errorCount == 0) {
                CustomDialog.showSuccess(message);
            } else if (successCount > 0) {
                CustomDialog.showError(message);
            } else {
                CustomDialog.showError("Import failed!\n\n" + errors.toString());
            }
            
        } catch (IOException e) {
            CustomDialog.showError("Error reading Excel file: " + e.getMessage());
        } catch (Exception e) {
            CustomDialog.showError("Unexpected error during import: " + e.getMessage());
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private double getCellValueAsNumber(Cell cell) {
        if (cell == null) {
            return 0.0;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            default:
                return 0.0;
        }
    }
    
    // Method để đồng bộ lại tất cả số lượng Product
    public void syncAllProductQuantities() {
        String syncSQL = "EXEC sp_SyncAllProductQuantities";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(syncSQL)) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Sync result: " + rs.getString("Result") + " - " + rs.getString("Message"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error syncing product quantities: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Method để kiểm tra tính đúng đắn của số lượng
    public void checkQuantityBalance() {
        String checkSQL = "EXEC sp_CheckQuantityBalance";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSQL)) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("=== QUANTITY BALANCE CHECK ===");
                System.out.printf("%-15s %-30s %-10s %-10s %-10s %-15s %-10s%n", 
                    "Product_ID", "Product_Name", "Imported", "Current", "Sold", "Balance_Status", "Difference");
                System.out.println("-".repeat(100));
                
                while (rs.next()) {
                    System.out.printf("%-15s %-30s %-10d %-10d %-10d %-15s %-10d%n",
                        rs.getString("Product_ID"),
                        rs.getString("Product_Name").substring(0, Math.min(30, rs.getString("Product_Name").length())),
                        rs.getInt("Total_Imported"),
                        rs.getInt("Current_Stock"),
                        rs.getInt("Total_Sold"),
                        rs.getString("Balance_Status"),
                        rs.getInt("Difference")
                    );
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking quantity balance: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Method để sửa dữ liệu hiện tại
    public void fixCurrentData() {
        String fixSQL = """
            -- Cập nhật Is_In_Product = 1 cho các Product đã tồn tại
            UPDATE Product_Stock 
            SET Is_In_Product = 1 
            WHERE Warehouse_Item_ID IN (
                SELECT Warehouse_Item_ID 
                FROM Product 
                WHERE Warehouse_Item_ID IS NOT NULL
            )
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(fixSQL)) {
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Fixed Is_In_Product flag for " + rowsAffected + " warehouse items");
            
            // Đồng bộ số lượng
            syncAllProductQuantities();
            
        } catch (SQLException e) {
            System.err.println("Error fixing current data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
}
