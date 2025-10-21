IF DB_ID(N'QuanLyKho') IS NULL
BEGIN
    CREATE DATABASE [QuanLyKho];
END;
GO
USE [QuanLyKho];
GO

-- ============================================
-- ADMIN TABLE
-- ============================================
IF OBJECT_ID(N'dbo.Admin', N'U') IS NULL
BEGIN
CREATE TABLE dbo.Admin(
    Admin_ID varchar(20) NOT NULL,
    Admin_Name nvarchar(255) NOT NULL,
    Gender varchar(10) NULL,
    Email varchar(255) NULL,
    Contact varchar(15) NOT NULL,
    Password varchar(255) NOT NULL,
    Image varchar(255) NULL,
    Status varchar(20) NOT NULL CONSTRAINT DF_Admin_Status DEFAULT('Available'),
    CONSTRAINT PK_Admin PRIMARY KEY CLUSTERED (Admin_ID ASC),
    CONSTRAINT UQ_Admin_Email UNIQUE NONCLUSTERED (Email ASC)
);
END;
GO

-- ============================================
-- SUPPLIER TABLE
-- ============================================
IF OBJECT_ID(N'dbo.Supplier', N'U') IS NULL
BEGIN
CREATE TABLE dbo.Supplier(
    Sup_ID varchar(100) NOT NULL,
    Sup_Name nvarchar(250) NOT NULL,
    Address nvarchar(255) NOT NULL,
    Contact varchar(100) NOT NULL,
    Status varchar(20) NOT NULL CONSTRAINT DF_Supplier_Status DEFAULT('Available'),
    CONSTRAINT PK_Supplier PRIMARY KEY CLUSTERED (Sup_ID ASC)
);
END;
GO

-- ============================================
-- CATEGORY TABLE
-- ============================================
IF OBJECT_ID(N'dbo.Category', N'U') IS NULL
BEGIN
CREATE TABLE dbo.Category(
    Category_ID varchar(50) NOT NULL,
    Category_Name nvarchar(225) NOT NULL,
    Sup_ID varchar(100) NOT NULL,
    Status varchar(20) NOT NULL CONSTRAINT DF_Category_Status DEFAULT('Available'),
    CONSTRAINT PK_Category PRIMARY KEY CLUSTERED (Category_ID ASC)
);
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_Category_Supplier')
BEGIN
ALTER TABLE dbo.Category  WITH CHECK
ADD CONSTRAINT FK_Category_Supplier FOREIGN KEY(Sup_ID) REFERENCES dbo.Supplier(Sup_ID);
END;
GO

-- ============================================
-- CUSTOMER TABLE
-- ============================================
IF OBJECT_ID(N'dbo.Customer', N'U') IS NULL
BEGIN
CREATE TABLE dbo.Customer(
    Customer_ID varchar(50) NOT NULL,
    Full_Name nvarchar(255) NOT NULL,
    Gender varchar(10) NULL,
    Date_Of_Birth date NOT NULL,
    Email varchar(255) NULL,
    Contact varchar(15) NOT NULL,
    Address nvarchar(255) NOT NULL,
    Password varchar(255) NOT NULL,
    Status varchar(20) NOT NULL CONSTRAINT DF_Customer_Status DEFAULT('Inactive'),
    Record_Status varchar(20) NOT NULL CONSTRAINT DF_Customer_RecordStatus DEFAULT('Available'),
    CONSTRAINT PK_Customer PRIMARY KEY CLUSTERED (Customer_ID ASC),
    CONSTRAINT UQ_Customer_Email UNIQUE NONCLUSTERED (Email ASC)
);
END;
GO

-- ============================================
-- PRODUCT TABLE
-- ============================================
IF OBJECT_ID(N'dbo.Product', N'U') IS NULL
BEGIN
CREATE TABLE dbo.Product(
    Product_ID varchar(50) NOT NULL,
    Product_Name nvarchar(255) NOT NULL,
    Color nvarchar(50) NULL,
    Speed varchar(50) NULL,
    Battery_Capacity varchar(100) NULL,
    Quantity int NOT NULL CONSTRAINT DF_Product_Quantity DEFAULT(0),
    Category_ID varchar(50) NULL,
    Sup_ID varchar(100) NULL,
    Image varchar(255) NULL,
    Price decimal(10,2) NULL,              
    List_Price_Before decimal(10,2) NULL,   
    List_Price_After  decimal(10,2) NULL,    
    Warehouse_Item_ID varchar(50) NULL,
    Status varchar(20) NOT NULL CONSTRAINT DF_Product_Status DEFAULT('Available'),
    CONSTRAINT PK_Product PRIMARY KEY CLUSTERED (Product_ID ASC)
);
END;
GO

-- Ensure Product has all columns
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('dbo.Product') AND name='List_Price_Before')
    ALTER TABLE dbo.Product ADD List_Price_Before decimal(10,2) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('dbo.Product') AND name='List_Price_After')
    ALTER TABLE dbo.Product ADD List_Price_After decimal(10,2) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('dbo.Product') AND name='Warehouse_Item_ID')
    ALTER TABLE dbo.Product ADD Warehouse_Item_ID varchar(50) NULL;

UPDATE p SET p.List_Price_After = ISNULL(p.List_Price_After, p.Price) FROM dbo.Product p;
UPDATE p SET p.List_Price_Before = ISNULL(p.List_Price_Before, p.List_Price_After) FROM dbo.Product p;
GO

-- ============================================
-- WAREHOUSE STOCK TABLE
-- ============================================
IF OBJECT_ID(N'dbo.Product_Stock', N'U') IS NULL
BEGIN
CREATE TABLE dbo.Product_Stock(
    Warehouse_Item_ID varchar(50) NOT NULL,       
    Product_Name nvarchar(255) NOT NULL,         
    Category_ID varchar(50) NOT NULL,            
    Sup_ID varchar(100) NOT NULL,               
    Quantity_Stock int NOT NULL DEFAULT(0),       
    Unit_Price_Import decimal(18,2) NOT NULL,     
    Created_Date date NOT NULL,                  
    Created_Time time(7) NOT NULL,                
    Is_In_Product bit NOT NULL DEFAULT(0),
    Status varchar(20) NOT NULL CONSTRAINT DF_ProductStock_Status DEFAULT('Available'),
    CONSTRAINT PK_Product_Stock PRIMARY KEY CLUSTERED (Warehouse_Item_ID ASC)
);
END;
GO

-- Foreign Keys cho Product_Stock
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_ProductStock_Category')
BEGIN
    ALTER TABLE dbo.Product_Stock WITH CHECK
    ADD CONSTRAINT FK_ProductStock_Category FOREIGN KEY(Category_ID) REFERENCES dbo.Category(Category_ID);
END;
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_ProductStock_Supplier')
BEGIN
    ALTER TABLE dbo.Product_Stock WITH CHECK
    ADD CONSTRAINT FK_ProductStock_Supplier FOREIGN KEY(Sup_ID) REFERENCES dbo.Supplier(Sup_ID);
END;
GO

-- Foreign Keys cho Product - Product chỉ kế thừa từ Product_Stock
-- Xóa foreign key đến Category và Supplier vì Product chỉ nên kế thừa từ Product_Stock
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_Product_Category')
BEGIN
    ALTER TABLE dbo.Product DROP CONSTRAINT FK_Product_Category;
END;
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_Product_Supplier')
BEGIN
    ALTER TABLE dbo.Product DROP CONSTRAINT FK_Product_Supplier;
END;
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_Product_WarehouseStock')
BEGIN
ALTER TABLE dbo.Product WITH CHECK
ADD CONSTRAINT FK_Product_WarehouseStock FOREIGN KEY(Warehouse_Item_ID) REFERENCES dbo.Product_Stock(Warehouse_Item_ID);
END;
GO

-- Uncategorized fallback
IF EXISTS (SELECT 1 FROM dbo.Product WHERE Category_ID IS NULL OR Category_ID='')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM dbo.Supplier WHERE Sup_ID='SUP00')
        INSERT dbo.Supplier(Sup_ID,Sup_Name,Address,Contact) VALUES('SUP00',N'Unknown',N'',N'');
    IF NOT EXISTS (SELECT 1 FROM dbo.Category WHERE Category_ID='UNCAT')
        INSERT dbo.Category(Category_ID,Category_Name,Sup_ID) VALUES('UNCAT',N'Uncategorized','SUP00');
    UPDATE dbo.Product SET Category_ID='UNCAT' WHERE Category_ID IS NULL OR Category_ID='';
END;
GO

-- ============================================
-- CART TABLE
-- ============================================
IF OBJECT_ID(N'dbo.Cart', N'U') IS NULL
BEGIN
CREATE TABLE dbo.Cart(
    Cart_ID varchar(50) NOT NULL,  -- Thêm Cart_ID làm primary key
    Customer_ID varchar(50) NOT NULL,
    Product_ID varchar(50) NOT NULL,
    Quantity int NULL,
    Status varchar(20) NOT NULL CONSTRAINT DF_Cart_Status DEFAULT('Available'),
    CONSTRAINT PK_Cart PRIMARY KEY CLUSTERED (Cart_ID ASC)
);
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_Cart_Customer')
BEGIN
ALTER TABLE dbo.Cart WITH CHECK ADD CONSTRAINT FK_Cart_Customer FOREIGN KEY(Customer_ID) REFERENCES dbo.Customer(Customer_ID);
END;
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_Cart_Product')
BEGIN
ALTER TABLE dbo.Cart WITH CHECK ADD CONSTRAINT FK_Cart_Product FOREIGN KEY(Product_ID) REFERENCES dbo.Product(Product_ID);
END;
GO

-- ============================================
-- BILL_IMPORTED TABLE
-- ============================================
IF OBJECT_ID(N'dbo.Bill_Imported', N'U') IS NULL
BEGIN
CREATE TABLE dbo.Bill_Imported(
    Invoice_No varchar(50) NOT NULL,
    Admin_ID varchar(20) NOT NULL,
    Total_Product int NOT NULL CONSTRAINT DF_BI_TotalProduct DEFAULT(0),
    Total_Price decimal(18,2) NOT NULL CONSTRAINT DF_BI_TotalPrice DEFAULT(0.00),
    Status varchar(20) NOT NULL CONSTRAINT DF_BillImported_Status DEFAULT('Available'),
    CONSTRAINT PK_Bill_Imported PRIMARY KEY CLUSTERED (Invoice_No ASC, Admin_ID ASC)
);
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_BillImported_Admin')
BEGIN
ALTER TABLE dbo.Bill_Imported WITH CHECK
ADD CONSTRAINT FK_BillImported_Admin FOREIGN KEY(Admin_ID) REFERENCES dbo.Admin(Admin_ID) ON DELETE CASCADE;
END;
GO

-- ============================================
-- BILL_IMPORTED_DETAILS TABLE
-- ============================================
IF OBJECT_ID(N'dbo.Bill_Imported_Details', N'U') IS NULL
BEGIN
CREATE TABLE dbo.Bill_Imported_Details(
    Invoice_No varchar(50) NOT NULL,
    Admin_ID varchar(20) NOT NULL,
    Warehouse_Item_ID varchar(50) NOT NULL,       
    Quantity int NOT NULL,
    Unit_Price_Import decimal(18,2) NOT NULL CONSTRAINT DF_BID_UnitPriceImport DEFAULT(0.00),
    Total_Price decimal(18,2) NOT NULL,
    Date_Imported date NOT NULL,
    Time_Imported time(7) NOT NULL,
    Status varchar(20) NOT NULL CONSTRAINT DF_BillImportedDetails_Status DEFAULT('Available'),
    CONSTRAINT PK_Bill_Imported_Details PRIMARY KEY CLUSTERED (Invoice_No ASC, Admin_ID ASC, Warehouse_Item_ID ASC)
);
END;
GO

UPDATE d SET d.Total_Price = d.Quantity * d.Unit_Price_Import
FROM dbo.Bill_Imported_Details d
WHERE ISNULL(d.Total_Price,0)=0;

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_BillImported')
BEGIN
ALTER TABLE dbo.Bill_Imported_Details WITH CHECK
ADD CONSTRAINT FK_BillImported FOREIGN KEY(Invoice_No,Admin_ID) REFERENCES dbo.Bill_Imported(Invoice_No,Admin_ID) ON DELETE CASCADE;
END;

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_BID_WarehouseStock')
BEGIN
ALTER TABLE dbo.Bill_Imported_Details WITH CHECK
ADD CONSTRAINT FK_BID_WarehouseStock FOREIGN KEY(Warehouse_Item_ID) REFERENCES dbo.Product_Stock(Warehouse_Item_ID) ON DELETE CASCADE;
END;
GO

-- ============================================
-- BILL_EXPORTED TABLE
-- ============================================
IF OBJECT_ID(N'dbo.Bill_Exported', N'U') IS NULL
BEGIN
CREATE TABLE dbo.Bill_Exported(
    Invoice_No varchar(50) NOT NULL,
    Admin_ID varchar(20) NOT NULL,
    Customer_ID varchar(50) NULL,
    Order_No varchar(20) NULL,  -- Thêm cột để liên kết với Orders
    Total_Product int NOT NULL CONSTRAINT DF_BE_TotalProduct DEFAULT(0),
    Description varchar(50) NULL,
    Promotion_Code varchar(50) NULL,
    Status varchar(20) NOT NULL CONSTRAINT DF_BillExported_Status DEFAULT('Available'),
    CONSTRAINT PK_Bill_Exported PRIMARY KEY CLUSTERED (Invoice_No ASC, Admin_ID ASC)
);
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('dbo.Bill_Exported') AND name='Promotion_Code')
    ALTER TABLE dbo.Bill_Exported ADD Promotion_Code varchar(50) NULL;

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_BillExported_Admin')
BEGIN
ALTER TABLE dbo.Bill_Exported WITH CHECK
ADD CONSTRAINT FK_BillExported_Admin FOREIGN KEY(Admin_ID) REFERENCES dbo.Admin(Admin_ID) ON DELETE CASCADE;
END;
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_BillExported_Customer')
BEGIN
ALTER TABLE dbo.Bill_Exported WITH CHECK
ADD CONSTRAINT FK_BillExported_Customer FOREIGN KEY(Customer_ID) REFERENCES dbo.Customer(Customer_ID) ON DELETE CASCADE;
END;
-- Foreign key này sẽ được tạo sau khi bảng Orders được tạo
GO

-- ============================================
-- BILL_EXPORTED_DETAILS TABLE
-- ============================================
IF OBJECT_ID(N'dbo.Bill_Exported_Details', N'U') IS NULL
BEGIN
CREATE TABLE dbo.Bill_Exported_Details(
    Invoice_No varchar(50) NOT NULL,
    Admin_ID varchar(20) NOT NULL,
    Customer_ID varchar(50) NULL,
    Product_ID varchar(50) NOT NULL,
    Unit_Price_Sell_Before decimal(10,2) NULL,    -- Giá bán TRƯỚC khuyến mãi
    Unit_Price_Sell_After  decimal(10,2) NOT NULL CONSTRAINT DF_BED_UnitPriceAfter DEFAULT(0.00),  -- Giá bán SAU KM
    Sold_Quantity int NOT NULL, -- Đổi tên từ Quantity thành Sold_Quantity để tránh nhầm lẫn
    Discount_Percent decimal(5,2) NULL,            -- % Khuyến mãi
    Total_Price_Before decimal(15,2) NOT NULL,     -- Tổng tiền trước KM
    Total_Price_After  decimal(15,2) NOT NULL,     -- Tổng tiền sau KM
    Date_Exported date NOT NULL,
    Time_Exported time(7) NOT NULL,
    Status varchar(20) NOT NULL CONSTRAINT DF_BillExportedDetails_Status DEFAULT('Available'),
    CONSTRAINT PK_Bill_Exported_Details PRIMARY KEY CLUSTERED (Invoice_No ASC, Admin_ID ASC, Product_ID ASC)
);
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_BillExportedDetails_Invoice')
BEGIN
ALTER TABLE dbo.Bill_Exported_Details WITH CHECK
ADD CONSTRAINT FK_BillExportedDetails_Invoice FOREIGN KEY(Invoice_No,Admin_ID) REFERENCES dbo.Bill_Exported(Invoice_No,Admin_ID) ON DELETE CASCADE;
END;
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_BillExportedDetails_Product')
BEGIN
ALTER TABLE dbo.Bill_Exported_Details WITH CHECK
ADD CONSTRAINT FK_BillExportedDetails_Product FOREIGN KEY(Product_ID) REFERENCES dbo.Product(Product_ID) ON DELETE CASCADE;
END;
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name=N'CK_BED_SoldQuantity_Positive')
BEGIN
ALTER TABLE dbo.Bill_Exported_Details WITH CHECK
ADD CONSTRAINT CK_BED_SoldQuantity_Positive CHECK (Sold_Quantity>0);
END;

UPDATE d
SET d.Unit_Price_Sell_Before = COALESCE(d.Unit_Price_Sell_Before, p.List_Price_Before, d.Unit_Price_Sell_After)
FROM dbo.Bill_Exported_Details d
JOIN dbo.Product p ON p.Product_ID=d.Product_ID;
UPDATE d SET d.Total_Price_Before = d.Unit_Price_Sell_Before * d.Sold_Quantity
FROM dbo.Bill_Exported_Details d
WHERE ISNULL(d.Total_Price_Before,0)=0;
UPDATE d SET d.Total_Price_After  = d.Unit_Price_Sell_After * d.Sold_Quantity * (1 - ISNULL(d.Discount_Percent,0)/100.0)
FROM dbo.Bill_Exported_Details d
WHERE ISNULL(d.Total_Price_After,0)=0;
GO

-- ============================================
-- ORDERS TABLE
-- ============================================
IF OBJECT_ID(N'dbo.Orders', N'U') IS NULL
BEGIN
CREATE TABLE dbo.Orders(
    Order_No varchar(20) NOT NULL,
    Customer_ID varchar(50) NOT NULL,
    Cart_ID varchar(50) NULL,  -- Thêm cột để liên kết với Cart
    Total_Quantity_Product int NOT NULL CONSTRAINT DF_Orders_TotalQty DEFAULT(0),
    Total_Price decimal(15,2) NOT NULL CONSTRAINT DF_Orders_TotalPrice DEFAULT(0.00),
    Payment varchar(20) NOT NULL CONSTRAINT DF_Orders_Payment DEFAULT('Cash'),
    Date_Order date NULL,
    Time_Order time(7) NULL,
    Status varchar(20) NOT NULL CONSTRAINT DF_Orders_Status DEFAULT('Waiting'),
    Record_Status varchar(20) NOT NULL CONSTRAINT DF_Orders_RecordStatus DEFAULT('Available'),
    CONSTRAINT PK_Orders PRIMARY KEY CLUSTERED (Order_No ASC, Customer_ID ASC)
);
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_Orders_Customer')
BEGIN
ALTER TABLE dbo.Orders WITH CHECK
ADD CONSTRAINT FK_Orders_Customer FOREIGN KEY(Customer_ID) REFERENCES dbo.Customer(Customer_ID) ON DELETE CASCADE;
END;
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_Orders_Cart')
BEGIN
ALTER TABLE dbo.Orders WITH CHECK
ADD CONSTRAINT FK_Orders_Cart FOREIGN KEY(Cart_ID) REFERENCES dbo.Cart(Cart_ID) ON DELETE NO ACTION ON UPDATE NO ACTION;
END;

-- Tạo foreign key cho Bill_Exported sau khi Orders đã được tạo
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_BillExported_Orders')
BEGIN
ALTER TABLE dbo.Bill_Exported WITH CHECK
ADD CONSTRAINT FK_BillExported_Orders FOREIGN KEY(Order_No, Customer_ID) REFERENCES dbo.Orders(Order_No, Customer_ID) ON DELETE NO ACTION ON UPDATE NO ACTION;
END;
GO

-- ============================================
-- ORDERS_DETAILS TABLE
-- ============================================
IF OBJECT_ID(N'dbo.Orders_Details', N'U') IS NULL
BEGIN
CREATE TABLE dbo.Orders_Details(
    Order_No varchar(20) NOT NULL,
    Customer_ID varchar(50) NOT NULL,
    Product_ID varchar(50) NOT NULL,
    Price decimal(10,2) NOT NULL,
    Sold_Quantity int NOT NULL, -- Đổi tên từ Quantity thành Sold_Quantity để tránh nhầm lẫn
    Date_Order date NOT NULL,
    Time_Order time(7) NOT NULL,
    Status nvarchar(50) NOT NULL,
    Record_Status varchar(20) NOT NULL CONSTRAINT DF_OrdersDetails_RecordStatus DEFAULT('Available'),
    CONSTRAINT PK_Orders_Details PRIMARY KEY CLUSTERED (Order_No ASC, Customer_ID ASC, Product_ID ASC)
);
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_OrdersDetails_Orders')
BEGIN
ALTER TABLE dbo.Orders_Details WITH CHECK
ADD CONSTRAINT FK_OrdersDetails_Orders FOREIGN KEY(Order_No,Customer_ID) REFERENCES dbo.Orders(Order_No,Customer_ID) ON DELETE CASCADE;
END;
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_OrdersDetails_Product')
BEGIN
ALTER TABLE dbo.Orders_Details WITH CHECK
ADD CONSTRAINT FK_OrdersDetails_Product FOREIGN KEY(Product_ID) REFERENCES dbo.Product(Product_ID) ON DELETE CASCADE;
END;
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name=N'CK_Orders_Details_SoldQuantity_Positive')
BEGIN
ALTER TABLE dbo.Orders_Details WITH CHECK
ADD CONSTRAINT CK_Orders_Details_SoldQuantity_Positive CHECK (Sold_Quantity>0);
END;
GO

-- ============================================
-- INSURANCE TABLE 
-- ============================================
IF OBJECT_ID(N'dbo.Insurance', N'U') IS NULL
BEGIN
CREATE TABLE dbo.Insurance(
    Insurance_No varchar(50) NOT NULL,
    Admin_ID varchar(20) NOT NULL,
    Customer_ID varchar(50) NULL,
    Invoice_No varchar(50) NULL,              
    Describle_customer varchar(50) NULL,
    Start_Date_Insurance date NOT NULL,
    End_Date_Insurance date NOT NULL,
    Status varchar(20) NOT NULL CONSTRAINT DF_Insurance_Status DEFAULT('Available'),
    CONSTRAINT PK_Insurance PRIMARY KEY CLUSTERED (Insurance_No ASC, Admin_ID ASC)
);
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_Insurance_Admin')
BEGIN
ALTER TABLE dbo.Insurance WITH CHECK
ADD CONSTRAINT FK_Insurance_Admin FOREIGN KEY(Admin_ID) REFERENCES dbo.Admin(Admin_ID) ON UPDATE CASCADE ON DELETE CASCADE;
END;
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_Insurance_Customer')
BEGIN
ALTER TABLE dbo.Insurance WITH CHECK
ADD CONSTRAINT FK_Insurance_Customer FOREIGN KEY(Customer_ID) REFERENCES dbo.Customer(Customer_ID) ON UPDATE CASCADE ON DELETE CASCADE;
END;
-- Remove old foreign key constraint if exists
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_Insurance_BillExported')
BEGIN
    ALTER TABLE dbo.Insurance DROP CONSTRAINT FK_Insurance_BillExported;
END;

-- Add new foreign key constraint to Bill_Exported_Details

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='UQ_BillExportedDetails_InvoiceAdmin' AND object_id=OBJECT_ID('dbo.Bill_Exported_Details'))
BEGIN
    CREATE UNIQUE NONCLUSTERED INDEX UQ_BillExportedDetails_InvoiceAdmin 
    ON dbo.Bill_Exported_Details(Invoice_No, Admin_ID);
END;

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_Insurance_BillExportedDetails')
BEGIN
ALTER TABLE dbo.Insurance WITH CHECK
ADD CONSTRAINT FK_Insurance_BillExportedDetails 
FOREIGN KEY(Invoice_No, Admin_ID) 
REFERENCES dbo.Bill_Exported_Details(Invoice_No, Admin_ID) 
ON UPDATE NO ACTION ON DELETE NO ACTION;
END;
GO

-- ============================================
-- INSURANCE_DETAILS TABLE
-- ============================================
IF OBJECT_ID(N'dbo.Insurance_Details', N'U') IS NULL
BEGIN
CREATE TABLE dbo.Insurance_Details(
    Insurance_No varchar(50) NOT NULL,
    Admin_ID varchar(20) NOT NULL,
    Customer_ID varchar(50) NULL,
    Invoice_No varchar(50) NULL,            
    Product_ID varchar(50) NOT NULL,
    Description nvarchar(255) NULL,
    Date_Insurance date NOT NULL,
    Time_Insurance time(7) NOT NULL,
    Status varchar(20) NOT NULL CONSTRAINT DF_InsuranceDetails_Status DEFAULT('Available'),
    CONSTRAINT PK_Insurance_Details PRIMARY KEY CLUSTERED (Insurance_No ASC, Admin_ID ASC, Product_ID ASC)
);
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_InsuranceDetails_Insurance')
BEGIN
ALTER TABLE dbo.Insurance_Details WITH CHECK
ADD CONSTRAINT FK_InsuranceDetails_Insurance FOREIGN KEY(Insurance_No,Admin_ID) REFERENCES dbo.Insurance(Insurance_No,Admin_ID) ON DELETE CASCADE;
END;
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_InsuranceDetails_Product')
BEGIN
ALTER TABLE dbo.Insurance_Details WITH CHECK
ADD CONSTRAINT FK_InsuranceDetails_Product FOREIGN KEY(Product_ID) REFERENCES dbo.Product(Product_ID) ON DELETE CASCADE;
END;
-- Remove old foreign key constraint if exists
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_InsuranceDetails_BillExported')
BEGIN
    ALTER TABLE dbo.Insurance_Details DROP CONSTRAINT FK_InsuranceDetails_BillExported;
END;

-- Insurance_Details không cần nối trực tiếp với Bill_Exported_Details
-- Chỉ Insurance nối với Bill_Exported_Details thông qua unique index
GO

-- ============================================
-- PROMOTION TABLE
-- ============================================
IF OBJECT_ID(N'dbo.Promotion', N'U') IS NULL
BEGIN
CREATE TABLE dbo.Promotion(
    Promotion_Code varchar(50) NOT NULL,
    Promotion_Name nvarchar(255) NOT NULL,
    Start_Date date NOT NULL,
    End_Date date NOT NULL,
    Discount_Percent decimal(5,2) NOT NULL,
    Status varchar(20) NOT NULL CONSTRAINT DF_Promotion_Status DEFAULT('Available'),
    CONSTRAINT PK_Promotion PRIMARY KEY CLUSTERED (Promotion_Code ASC)
);
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_BillExported_Promotion')
BEGIN
    ALTER TABLE dbo.Bill_Exported WITH CHECK
    ADD CONSTRAINT FK_BillExported_Promotion FOREIGN KEY(Promotion_Code)
        REFERENCES dbo.Promotion(Promotion_Code);
END;
GO

-- ============================================
-- SEED DATA
-- ============================================
MERGE dbo.Supplier AS t
USING (VALUES
('NIJIA', N'CÔNG TY NIJIA',  N'QL1A, Quất Động, Thường Tín, Hà Nội', '0936281080'),
('TAILG', N'CÔNG TY TAILG',  N'Số 8 KCN Minh Quang, P, Thị Xã Mã Hào, Hưng Yên', '0972155557'),
('YADEA', N'CÔNG TY YADEA',  N'Khu công nghiệp Quang Châu, Phường Nếnh, Tỉnh Bắc Ninh', '0920462988'),
('VINFAST', N'CÔNG TY Vinfast',N'Khu Kinh tế Đình Vũ, TP. Hải Phòng', '0972155337')
) AS s(Sup_ID,Sup_Name,Address,Contact)
ON (t.Sup_ID=s.Sup_ID)
WHEN NOT MATCHED THEN
  INSERT (Sup_ID,Sup_Name,Address,Contact) VALUES (s.Sup_ID,s.Sup_Name,s.Address,s.Contact);
GO

-- ============================================
-- VIEWS
-- ============================================
-- View 1: Tổng hợp số lượng nhập, bán, tồn
IF OBJECT_ID(N'dbo.v_Product_Quantity_InOut', N'V') IS NOT NULL DROP VIEW dbo.v_Product_Quantity_InOut;
GO
CREATE VIEW dbo.v_Product_Quantity_InOut AS
WITH Imported AS (
    SELECT ps.Warehouse_Item_ID, SUM(CAST(d.Quantity AS bigint)) AS Imported_Qty
    FROM dbo.Bill_Imported_Details d
    JOIN dbo.Product_Stock ps ON d.Warehouse_Item_ID = ps.Warehouse_Item_ID
    WHERE d.Status = 'Available'
    GROUP BY ps.Warehouse_Item_ID
),
Sold AS (
    SELECT p.Warehouse_Item_ID, SUM(CAST(d.Sold_Quantity AS bigint)) AS Sold_Qty -- Sử dụng Sold_Quantity
    FROM dbo.Bill_Exported_Details d
    JOIN dbo.Product p ON d.Product_ID = p.Product_ID
    WHERE p.Warehouse_Item_ID IS NOT NULL AND d.Status = 'Available'
    GROUP BY p.Warehouse_Item_ID
),
Stock AS (
    SELECT ps.Warehouse_Item_ID, CAST(ps.Quantity_Stock AS bigint) AS Stock_Qty
    FROM dbo.Product_Stock ps
    WHERE ps.Status = 'Available'
)
SELECT ps.Warehouse_Item_ID, ps.Product_Name,
       COALESCE(i.Imported_Qty,0) AS Imported_Qty,
       COALESCE(s.Sold_Qty,0)     AS Sold_Qty,
       COALESCE(k.Stock_Qty,0)    AS Stock_Qty,
       CASE WHEN COALESCE(k.Stock_Qty,0)+COALESCE(s.Sold_Qty,0)=COALESCE(i.Imported_Qty,0) THEN 1 ELSE 0 END AS IsBalanced
FROM dbo.Product_Stock ps
LEFT JOIN Imported i ON i.Warehouse_Item_ID = ps.Warehouse_Item_ID
LEFT JOIN Sold s ON s.Warehouse_Item_ID = ps.Warehouse_Item_ID
LEFT JOIN Stock k ON k.Warehouse_Item_ID = ps.Warehouse_Item_ID
WHERE ps.Status = 'Available';
GO

-- View 2: In hóa đơn nhập
IF OBJECT_ID(N'dbo.v_Bill_Imported_Print', N'V') IS NOT NULL DROP VIEW dbo.v_Bill_Imported_Print;
GO
CREATE VIEW dbo.v_Bill_Imported_Print AS
SELECT h.Invoice_No, h.Admin_ID, h.Total_Product, h.Total_Price,
       d.Warehouse_Item_ID, ps.Product_Name,
       d.Quantity, d.Unit_Price_Import,
       d.Total_Price AS Line_Total,
       d.Date_Imported, d.Time_Imported
FROM dbo.Bill_Imported h
JOIN dbo.Bill_Imported_Details d ON d.Invoice_No=h.Invoice_No AND d.Admin_ID=h.Admin_ID
JOIN dbo.Product_Stock ps ON ps.Warehouse_Item_ID=d.Warehouse_Item_ID
WHERE h.Status = 'Available' AND d.Status = 'Available' AND ps.Status = 'Available';
GO

-- View 3: In hóa đơn xuất
IF OBJECT_ID(N'dbo.v_Bill_Exported_Print', N'V') IS NOT NULL DROP VIEW dbo.v_Bill_Exported_Print;
GO
CREATE VIEW dbo.v_Bill_Exported_Print AS
SELECT h.Invoice_No, h.Admin_ID, h.Customer_ID, h.Total_Product, h.Description,
       d.Product_ID, p.Product_Name,
       d.Sold_Quantity, -- Sử dụng Sold_Quantity
       d.Unit_Price_Sell_Before, d.Unit_Price_Sell_After, d.Discount_Percent,
       d.Total_Price_Before, d.Total_Price_After,
       d.Date_Exported, d.Time_Exported
FROM dbo.Bill_Exported h
JOIN dbo.Bill_Exported_Details d ON d.Invoice_No=h.Invoice_No AND d.Admin_ID=h.Admin_ID
JOIN dbo.Product p ON p.Product_ID=d.Product_ID
WHERE h.Status = 'Available' AND d.Status = 'Available' AND p.Status = 'Available';
GO

-- View 4: Báo cáo tồn kho
IF OBJECT_ID(N'dbo.v_Inventory_Report', N'V') IS NOT NULL DROP VIEW dbo.v_Inventory_Report;
GO
CREATE VIEW dbo.v_Inventory_Report AS
SELECT q.Warehouse_Item_ID, q.Product_Name,
       ISNULL(q.Imported_Qty,0) AS Imported_Qty,
       ISNULL(q.Sold_Qty,0)     AS Sold_Qty,
       ISNULL(q.Stock_Qty,0)    AS Stock_Qty,
       CASE WHEN ISNULL(q.Stock_Qty,0)+ISNULL(q.Sold_Qty,0)=ISNULL(q.Imported_Qty,0) THEN N'Hợp lệ' ELSE N'Lệch' END AS CheckBalance
FROM dbo.v_Product_Quantity_InOut q;
GO

-- View 5: Thông tin đầy đủ Product (bao gồm số lượng và giá)
IF OBJECT_ID(N'dbo.v_Product_Full_Info', N'V') IS NOT NULL DROP VIEW dbo.v_Product_Full_Info;
GO
CREATE VIEW dbo.v_Product_Full_Info AS
SELECT 
    p.Product_ID,
    p.Product_Name,
    p.Image,
    p.Category_ID,
    p.Sup_ID,
    p.Color,
    p.Speed,
    p.Battery_Capacity,
    
    -- ========== SỐ LƯỢNG ==========
    ISNULL(ps.Quantity_Stock, 0) AS Total_Imported,         -- Số lượng nhập (từ Inventory)
    p.Quantity AS Current_Stock,                             -- Số lượng tồn kho (Product)
    ISNULL(ps.Quantity_Stock, 0) - p.Quantity AS Total_Sold, -- Số lượng đã bán (Nhập - Tồn)
    
    -- ========== GIÁ CẢ ==========
    ps.Unit_Price_Import AS Import_Price,                   
    p.Price AS Current_Selling_Price,                       
    p.List_Price_Before AS Original_Price,                 
    p.List_Price_After AS Promotional_Price,              
    
    -- ========== LINK ==========
    p.Warehouse_Item_ID,
    ISNULL(ps.Is_In_Product, 0) AS Is_In_Product,
    ps.Created_Date AS Stock_Created_Date,
    
    -- ========== KIỂM TRA CÂN BẰNG ==========
    CASE 
        WHEN ISNULL(ps.Quantity_Stock, 0) = p.Quantity + (ISNULL(ps.Quantity_Stock, 0) - p.Quantity)
        THEN N'✓ Cân bằng'
        ELSE N'✗ Lệch'
    END AS Balance_Status
    
FROM dbo.Product p
LEFT JOIN dbo.Product_Stock ps ON p.Warehouse_Item_ID = ps.Warehouse_Item_ID
WHERE p.Status = 'Available' AND (ps.Status = 'Available' OR ps.Status IS NULL);
GO

-- View 6: Kiểm tra cân bằng số lượng (Nhập = Bán + Tồn)
IF OBJECT_ID(N'dbo.v_Product_Quantity_Check', N'V') IS NOT NULL DROP VIEW dbo.v_Product_Quantity_Check;
GO
CREATE VIEW dbo.v_Product_Quantity_Check AS
SELECT 
    p.Product_ID,
    p.Product_Name,
    ISNULL(ps.Quantity_Stock, 0) AS Total_Imported,         -- Số lượng nhập
    p.Quantity AS Current_Stock,                             -- Số lượng tồn kho
    ISNULL(ps.Quantity_Stock, 0) - p.Quantity AS Total_Sold, -- Số lượng đã bán
    
    -- Kiểm tra: Nhập = Bán + Tồn
    CASE 
        WHEN ISNULL(ps.Quantity_Stock, 0) = p.Quantity + (ISNULL(ps.Quantity_Stock, 0) - p.Quantity)
        THEN N'✓ Cân bằng'
        ELSE N'✗ Lệch'
    END AS Balance_Status,
    
    -- Hiển thị công thức
    CAST(ISNULL(ps.Quantity_Stock, 0) AS nvarchar) + ' = ' + 
    CAST((ISNULL(ps.Quantity_Stock, 0) - p.Quantity) AS nvarchar) + ' + ' + 
    CAST(p.Quantity AS nvarchar) AS Formula
    
FROM dbo.Product p
LEFT JOIN dbo.Product_Stock ps ON p.Warehouse_Item_ID = ps.Warehouse_Item_ID
WHERE p.Status = 'Available' AND (ps.Status = 'Available' OR ps.Status IS NULL);
GO

-- View 7: Insurance với thông tin Export Bill Details
IF OBJECT_ID(N'dbo.v_Insurance_With_Export', N'V') IS NOT NULL DROP VIEW dbo.v_Insurance_With_Export;
GO
CREATE VIEW dbo.v_Insurance_With_Export AS
SELECT 
    i.Insurance_No,
    i.Admin_ID,
    i.Customer_ID,
    i.Invoice_No,
    i.Describle_customer,
    i.Start_Date_Insurance,
    i.End_Date_Insurance,
    bed.Product_ID,
    p.Product_Name,
    bed.Sold_Quantity, -- Sử dụng Sold_Quantity
    bed.Unit_Price_Sell_After,
    bed.Date_Exported,
    c.Full_Name AS Customer_Name,
    c.Contact AS Customer_Contact,
    c.Address AS Customer_Address
FROM dbo.Insurance i
LEFT JOIN dbo.Bill_Exported_Details bed ON i.Invoice_No = bed.Invoice_No AND i.Admin_ID = bed.Admin_ID
LEFT JOIN dbo.Product p ON bed.Product_ID = p.Product_ID
LEFT JOIN dbo.Customer c ON i.Customer_ID = c.Customer_ID
WHERE i.Status = 'Available' AND bed.Status = 'Available' AND p.Status = 'Available' AND c.Record_Status = 'Available';
GO

-- View 8: Insurance Details với thông tin Product
IF OBJECT_ID(N'dbo.v_Insurance_Details_With_Product', N'V') IS NOT NULL DROP VIEW dbo.v_Insurance_Details_With_Product;
GO
CREATE VIEW dbo.v_Insurance_Details_With_Product AS
SELECT 
    id.Insurance_No,
    id.Admin_ID,
    id.Customer_ID,
    id.Invoice_No,
    id.Product_ID,
    id.Description,
    id.Date_Insurance,
    id.Time_Insurance,
    p.Product_Name,
    p.Color,
    p.Speed,
    p.Battery_Capacity,
    p.Price,
    bed.Sold_Quantity AS Sold_Quantity,
    bed.Unit_Price_Sell_After AS Sold_Price,
    bed.Date_Exported AS Sale_Date
FROM dbo.Insurance_Details id
LEFT JOIN dbo.Product p ON id.Product_ID = p.Product_ID
LEFT JOIN dbo.Bill_Exported_Details bed ON id.Invoice_No = bed.Invoice_No 
    AND id.Admin_ID = bed.Admin_ID 
    AND id.Product_ID = bed.Product_ID
WHERE id.Status = 'Available' AND p.Status = 'Available' AND bed.Status = 'Available';
GO

-- View 9: Available Export Bill Details for Insurance
IF OBJECT_ID(N'dbo.v_Available_Export_Bills_For_Insurance', N'V') IS NOT NULL DROP VIEW dbo.v_Available_Export_Bills_For_Insurance;
GO
CREATE VIEW dbo.v_Available_Export_Bills_For_Insurance AS
SELECT 
    bed.Invoice_No,
    bed.Admin_ID,
    bed.Customer_ID,
    bed.Product_ID,
    p.Product_Name,
    bed.Sold_Quantity, -- Sử dụng Sold_Quantity
    bed.Unit_Price_Sell_After,
    bed.Date_Exported,
    c.Full_Name AS Customer_Name,
    c.Contact AS Customer_Contact,
    c.Address AS Customer_Address,
    CASE 
        WHEN i.Insurance_No IS NULL THEN 'Available for Insurance'
        ELSE 'Already Insured'
    END AS Insurance_Status,
    i.Insurance_No AS Existing_Insurance_No
FROM dbo.Bill_Exported_Details bed
LEFT JOIN dbo.Product p ON bed.Product_ID = p.Product_ID
LEFT JOIN dbo.Customer c ON bed.Customer_ID = c.Customer_ID
LEFT JOIN dbo.Insurance i ON bed.Invoice_No = i.Invoice_No AND bed.Admin_ID = i.Admin_ID
WHERE bed.Invoice_No IS NOT NULL 
    AND bed.Status = 'Available' 
    AND p.Status = 'Available' 
    AND c.Record_Status = 'Available';
GO

-- ============================================
-- TRIGGERS FOR PRODUCT_STOCK
-- ============================================

-- Trigger 1: Tăng stock khi nhập hàng
-- TRIGGER NÀY ĐÃ BỊ VÔ HIỆU HÓA VÌ MERGE ĐÃ TỰ ĐỘNG CẬP NHẬT SỐ LƯỢNG
-- Không cần trigger này nữa để tránh nhân đôi số lượng
/*
CREATE TRIGGER dbo.trg_BID_AI_Stock ON dbo.Bill_Imported_Details AFTER INSERT AS
BEGIN
    SET NOCOUNT ON;
    UPDATE ps
    SET ps.Quantity_Stock = ps.Quantity_Stock + x.TotalQty
    FROM dbo.Product_Stock ps
    JOIN (SELECT Warehouse_Item_ID, SUM(Quantity) AS TotalQty FROM inserted GROUP BY Warehouse_Item_ID) x
      ON x.Warehouse_Item_ID = ps.Warehouse_Item_ID;
END;
*/
GO

-- Trigger 2: Cập nhật stock khi sửa bill nhập
-- TRIGGER NÀY ĐÃ BỊ VÔ HIỆU HÓA VÌ MERGE ĐÃ TỰ ĐỘNG CẬP NHẬT SỐ LƯỢNG
-- Không cần trigger này nữa để tránh nhân đôi số lượng
/*
CREATE TRIGGER dbo.trg_BID_AU_Stock ON dbo.Bill_Imported_Details AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    WITH d AS (SELECT Warehouse_Item_ID, SUM(Quantity) AS QtyDel FROM deleted GROUP BY Warehouse_Item_ID),
         i AS (SELECT Warehouse_Item_ID, SUM(Quantity) AS QtyIns FROM inserted GROUP BY Warehouse_Item_ID)
    UPDATE ps
    SET ps.Quantity_Stock = ps.Quantity_Stock - ISNULL(d.QtyDel,0) + ISNULL(i.QtyIns,0)
    FROM dbo.Product_Stock ps
    LEFT JOIN d ON d.Warehouse_Item_ID = ps.Warehouse_Item_ID
    LEFT JOIN i ON i.Warehouse_Item_ID = ps.Warehouse_Item_ID
    WHERE d.Warehouse_Item_ID IS NOT NULL OR i.Warehouse_Item_ID IS NOT NULL;
END;
*/
GO

-- Trigger 3: Giảm stock khi xóa bill nhập
-- TRIGGER NÀY ĐÃ BỊ VÔ HIỆU HÓA VÌ MERGE ĐÃ TỰ ĐỘNG CẬP NHẬT SỐ LƯỢNG
-- Không cần trigger này nữa để tránh nhân đôi số lượng
/*
CREATE TRIGGER dbo.trg_BID_AD_Stock ON dbo.Bill_Imported_Details AFTER DELETE AS
BEGIN
    SET NOCOUNT ON;
    UPDATE ps
    SET ps.Quantity_Stock = ps.Quantity_Stock - x.TotalQty
    FROM dbo.Product_Stock ps
    JOIN (SELECT Warehouse_Item_ID, SUM(Quantity) AS TotalQty FROM deleted GROUP BY Warehouse_Item_ID) x
      ON x.Warehouse_Item_ID = ps.Warehouse_Item_ID;
END;
*/
GO

-- Trigger 4: Giảm stock khi xuất hàng
CREATE TRIGGER dbo.trg_BED_AI_Stock ON dbo.Bill_Exported_Details AFTER INSERT AS
BEGIN
    SET NOCOUNT ON;
    UPDATE ps
    SET ps.Quantity_Stock = ps.Quantity_Stock - x.TotalQty
    FROM dbo.Product_Stock ps
    JOIN dbo.Product p ON p.Warehouse_Item_ID = ps.Warehouse_Item_ID
    JOIN (SELECT Product_ID, SUM(Sold_Quantity) AS TotalQty FROM inserted GROUP BY Product_ID) x
      ON x.Product_ID = p.Product_ID
    WHERE p.Warehouse_Item_ID IS NOT NULL;
END;
GO

-- Trigger 5: Cập nhật stock khi sửa bill xuất
CREATE TRIGGER dbo.trg_BED_AU_Stock ON dbo.Bill_Exported_Details AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    WITH d AS (SELECT p.Warehouse_Item_ID, SUM(del.Sold_Quantity) AS QtyDel 
               FROM deleted del
               JOIN dbo.Product p ON del.Product_ID = p.Product_ID
               WHERE p.Warehouse_Item_ID IS NOT NULL
               GROUP BY p.Warehouse_Item_ID),
         i AS (SELECT p.Warehouse_Item_ID, SUM(ins.Sold_Quantity) AS QtyIns 
               FROM inserted ins
               JOIN dbo.Product p ON ins.Product_ID = p.Product_ID
               WHERE p.Warehouse_Item_ID IS NOT NULL
               GROUP BY p.Warehouse_Item_ID)
    UPDATE ps
    SET ps.Quantity_Stock = ps.Quantity_Stock + ISNULL(d.QtyDel,0) - ISNULL(i.QtyIns,0)
    FROM dbo.Product_Stock ps
    LEFT JOIN d ON d.Warehouse_Item_ID = ps.Warehouse_Item_ID
    LEFT JOIN i ON i.Warehouse_Item_ID = ps.Warehouse_Item_ID
    WHERE d.Warehouse_Item_ID IS NOT NULL OR i.Warehouse_Item_ID IS NOT NULL;
END;
GO

-- Trigger 6: Tăng stock khi xóa bill xuất (trả lại hàng)
CREATE TRIGGER dbo.trg_BED_AD_Stock ON dbo.Bill_Exported_Details AFTER DELETE AS
BEGIN
    SET NOCOUNT ON;
    UPDATE ps
    SET ps.Quantity_Stock = ps.Quantity_Stock + x.TotalQty
    FROM dbo.Product_Stock ps
    JOIN dbo.Product p ON p.Warehouse_Item_ID = ps.Warehouse_Item_ID
    JOIN (SELECT Product_ID, SUM(Sold_Quantity) AS TotalQty FROM deleted GROUP BY Product_ID) x
      ON x.Product_ID = p.Product_ID
    WHERE p.Warehouse_Item_ID IS NOT NULL;
END;
GO

-- ============================================
-- TRIGGERS FOR QUANTITY MANAGEMENT
-- ============================================

-- Trigger 1: Cập nhật Product.Quantity khi nhập hàng (tăng tồn kho)
CREATE TRIGGER dbo.trg_Update_Product_Stock_On_Import
ON dbo.Bill_Imported_Details
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Cập nhật Product.Quantity = Tổng nhập - Tổng đã bán (CHỈ qua Export, KHÔNG qua Orders)
    -- CHỈ cập nhật các Product đã tồn tại (có Product_ID)
    UPDATE p
    SET p.Quantity = (
        SELECT 
            ISNULL(SUM(bid.Quantity), 0) - 
            ISNULL(SUM(bed.Sold_Quantity), 0) -- Sử dụng Sold_Quantity thay vì Quantity
        FROM dbo.Bill_Imported_Details bid
        LEFT JOIN dbo.Bill_Exported_Details bed ON bed.Product_ID = p.Product_ID AND bed.Status = 'Available'
        WHERE bid.Warehouse_Item_ID = p.Warehouse_Item_ID
    )
    FROM dbo.Product p
    WHERE p.Warehouse_Item_ID IN (
        SELECT DISTINCT Warehouse_Item_ID FROM inserted
        UNION
        SELECT DISTINCT Warehouse_Item_ID FROM deleted
    )
    AND p.Product_ID IS NOT NULL; -- CHỈ cập nhật Product đã tồn tại
END;
GO

-- Trigger 2: Cập nhật Product.Quantity khi bán hàng qua Export (giảm tồn kho)
CREATE TRIGGER dbo.trg_Update_Product_Stock_On_Export
ON dbo.Bill_Exported_Details
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Cập nhật Product.Quantity = Tổng nhập - Tổng đã bán (CHỈ qua Export, KHÔNG qua Orders)
    UPDATE p
    SET p.Quantity = (
        SELECT 
            ISNULL(SUM(bid.Quantity), 0) - 
            ISNULL(SUM(bed.Sold_Quantity), 0) -- Sử dụng Sold_Quantity thay vì Quantity
        FROM dbo.Bill_Imported_Details bid
        LEFT JOIN dbo.Bill_Exported_Details bed ON bed.Product_ID = p.Product_ID AND bed.Status = 'Available'
        WHERE bid.Warehouse_Item_ID = p.Warehouse_Item_ID
    )
    FROM dbo.Product p
    WHERE p.Product_ID IN (
        SELECT DISTINCT Product_ID FROM inserted
        UNION
        SELECT DISTINCT Product_ID FROM deleted
    );
END;
GO

-- Trigger 3: KHÔNG cập nhật Product.Quantity khi có Order (chỉ kiểm tra tồn kho)
-- Orders chỉ dự trữ hàng, không trừ số lượng cho đến khi xuất hóa đơn
-- Trigger này được giữ lại để tương lai có thể cần logic khác
CREATE TRIGGER dbo.trg_Update_Product_Stock_On_Order
ON dbo.Orders_Details
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    
    
END;
GO

-- Trigger 4: Set Product.Quantity khi tạo Product mới từ Warehouse
-- CHỈ set Quantity cho Product mới tạo từ Warehouse VÀ chưa có dữ liệu Bill_Imported_Details
CREATE TRIGGER dbo.trg_Set_Product_Quantity_On_Create
ON dbo.Product
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    
    -- CHỈ set Quantity cho Product mới tạo từ Warehouse
    -- VÀ chưa có dữ liệu Bill_Imported_Details
    UPDATE p
    SET p.Quantity = (
        SELECT ISNULL(ps.Quantity_Stock, 0)
        FROM dbo.Product_Stock ps
        WHERE ps.Warehouse_Item_ID = p.Warehouse_Item_ID
    )
    FROM dbo.Product p
    INNER JOIN inserted i ON p.Product_ID = i.Product_ID
    WHERE p.Warehouse_Item_ID IS NOT NULL 
    AND NOT EXISTS (
        SELECT 1 FROM dbo.Bill_Imported_Details bid 
        WHERE bid.Warehouse_Item_ID = p.Warehouse_Item_ID
    ); -- CHỈ set khi chưa có dữ liệu nhập
END;
GO

-- ============================================
-- STORED PROCEDURES FOR QUANTITY SYNCHRONIZATION
-- ============================================

-- Stored Procedure: Đồng bộ tất cả số lượng Product
CREATE PROCEDURE dbo.sp_SyncAllProductQuantities
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Cập nhật Product.Quantity với logic đúng:
    -- Quantity = Tổng nhập - Tổng đã bán (CHỈ qua Export, KHÔNG qua Orders)
    -- CHỈ cập nhật các Product đã tồn tại và có dữ liệu nhập
    UPDATE p
    SET p.Quantity = (
        SELECT 
            ISNULL(SUM(bid.Quantity), 0) - 
            ISNULL(SUM(bed.Sold_Quantity), 0) -- Sử dụng Sold_Quantity thay vì Quantity
        FROM dbo.Bill_Imported_Details bid
        LEFT JOIN dbo.Bill_Exported_Details bed ON bed.Product_ID = p.Product_ID AND bed.Status = 'Available'
        WHERE bid.Warehouse_Item_ID = p.Warehouse_Item_ID
    )
    FROM dbo.Product p
    WHERE p.Warehouse_Item_ID IS NOT NULL
    AND p.Product_ID IS NOT NULL -- CHỈ cập nhật Product đã tồn tại
    AND EXISTS (
        SELECT 1 FROM dbo.Bill_Imported_Details bid 
        WHERE bid.Warehouse_Item_ID = p.Warehouse_Item_ID
    ); -- CHỈ cập nhật khi có dữ liệu nhập
    
    SELECT 'SUCCESS' AS Result, 'All product quantities synchronized' AS Message;
END;
GO

-- Stored Procedure: Kiểm tra tính đúng đắn của số lượng
CREATE PROCEDURE dbo.sp_CheckQuantityBalance
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        p.Product_ID,
        p.Product_Name,
        ISNULL(ps.Quantity_Stock, 0) AS Total_Imported,
        p.Quantity AS Current_Stock,
        ISNULL(sold.Sold_Quantity, 0) AS Total_Sold,
        CASE 
            WHEN ISNULL(ps.Quantity_Stock, 0) = p.Quantity + ISNULL(sold.Sold_Quantity, 0)
            THEN N'✓ Cân bằng'
            ELSE N'✗ Lệch'
        END AS Balance_Status,
        ISNULL(ps.Quantity_Stock, 0) - p.Quantity - ISNULL(sold.Sold_Quantity, 0) AS Difference
    FROM dbo.Product p
    LEFT JOIN dbo.Product_Stock ps ON p.Warehouse_Item_ID = ps.Warehouse_Item_ID
    LEFT JOIN (
        SELECT p.Warehouse_Item_ID, 
               ISNULL(SUM(bed.Sold_Quantity), 0) + ISNULL(SUM(od.Sold_Quantity), 0) AS Sold_Quantity -- Sử dụng Sold_Quantity
        FROM dbo.Product p
        LEFT JOIN dbo.Bill_Exported_Details bed ON p.Product_ID = bed.Product_ID AND bed.Status = 'Available'
        LEFT JOIN dbo.Orders_Details od ON p.Product_ID = od.Product_ID AND od.Record_Status = 'Available'
        WHERE p.Warehouse_Item_ID IS NOT NULL
        GROUP BY p.Warehouse_Item_ID
    ) sold ON ps.Warehouse_Item_ID = sold.Warehouse_Item_ID
    WHERE p.Status = 'Available' AND (ps.Status = 'Available' OR ps.Status IS NULL)
    ORDER BY Balance_Status DESC, ABS(ISNULL(ps.Quantity_Stock, 0) - p.Quantity - ISNULL(sold.Sold_Quantity, 0)) DESC;
END;
GO

-- ============================================
-- STORED PROCEDURES FOR INSURANCE
-- ============================================

-- Stored Procedure: Get Export Bill Details for Insurance

CREATE PROCEDURE dbo.sp_GetExportBillDetailsForInsurance
    @InvoiceNo varchar(50),
    @AdminID varchar(20)
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        bed.Invoice_No,
        bed.Admin_ID,
        bed.Customer_ID,
        bed.Product_ID,
        p.Product_Name,
        p.Color,
        p.Speed,
        p.Battery_Capacity,
        p.Price,
        bed.Sold_Quantity, -- Sử dụng Sold_Quantity
        bed.Unit_Price_Sell_After AS Sold_Price,
        bed.Date_Exported AS Sale_Date,
        bed.Time_Exported AS Sale_Time,
        c.Full_Name AS Customer_Name,
        c.Contact AS Customer_Contact,
        c.Address AS Customer_Address,
        c.Email AS Customer_Email
    FROM dbo.Bill_Exported_Details bed
    LEFT JOIN dbo.Product p ON bed.Product_ID = p.Product_ID
    LEFT JOIN dbo.Customer c ON bed.Customer_ID = c.Customer_ID
    WHERE bed.Invoice_No = @InvoiceNo 
        AND bed.Admin_ID = @AdminID 
        AND bed.Status = 'Available' 
        AND p.Status = 'Available' 
        AND c.Record_Status = 'Available';
END;
GO

-- Stored Procedure: Create Insurance from Export Bill Details

CREATE PROCEDURE dbo.sp_CreateInsuranceFromExportBill
    @InsuranceNo varchar(50),
    @InvoiceNo varchar(50),
    @AdminID varchar(20),
    @CustomerID varchar(50),
    @Description nvarchar(255),
    @StartDate date,
    @EndDate date,
    @ProductIDs varchar(MAX) -- Comma-separated list of Product_IDs to insure
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    
    BEGIN TRY
        -- Insert into Insurance table
        INSERT INTO dbo.Insurance (
            Insurance_No, Admin_ID, Customer_ID, Invoice_No, 
            Describle_customer, Start_Date_Insurance, End_Date_Insurance, Status
        )
        VALUES (
            @InsuranceNo, @AdminID, @CustomerID, @InvoiceNo,
            @Description, @StartDate, @EndDate, 'Available'
        );
        
        -- Insert into Insurance_Details table for selected products
        INSERT INTO dbo.Insurance_Details (
            Insurance_No, Admin_ID, Customer_ID, Invoice_No, Product_ID,
            Description, Date_Insurance, Time_Insurance, Status
        )
        SELECT 
            @InsuranceNo, @AdminID, @CustomerID, @InvoiceNo, bed.Product_ID,
            @Description, GETDATE(), GETDATE(), 'Available'
        FROM dbo.Bill_Exported_Details bed
        WHERE bed.Invoice_No = @InvoiceNo 
            AND bed.Admin_ID = @AdminID 
            AND bed.Status = 'Available'
            AND (@ProductIDs IS NULL OR bed.Product_ID IN (
                SELECT value FROM STRING_SPLIT(@ProductIDs, ',')
            ));
        
        COMMIT TRANSACTION;
        SELECT 'SUCCESS' AS Result, 'Insurance created successfully' AS Message;
        
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SELECT 'ERROR' AS Result, ERROR_MESSAGE() AS Message;
    END CATCH;
END;
GO

-- ============================================
-- INDEXES FOR PERFORMANCE
-- ============================================

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='IX_BID_WarehouseItem' AND object_id=OBJECT_ID('dbo.Bill_Imported_Details'))
    CREATE INDEX IX_BID_WarehouseItem ON dbo.Bill_Imported_Details(Warehouse_Item_ID);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='IX_BED_Product' AND object_id=OBJECT_ID('dbo.Bill_Exported_Details'))
    CREATE INDEX IX_BED_Product ON dbo.Bill_Exported_Details(Product_ID);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='IX_Product_WarehouseItem' AND object_id=OBJECT_ID('dbo.Product'))
    CREATE INDEX IX_Product_WarehouseItem ON dbo.Product(Warehouse_Item_ID);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='IX_PS_CategorySup' AND object_id=OBJECT_ID('dbo.Product_Stock'))
    CREATE INDEX IX_PS_CategorySup ON dbo.Product_Stock(Category_ID, Sup_ID);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='IX_Insurance_InvoiceNo' AND object_id=OBJECT_ID('dbo.Insurance'))
    CREATE INDEX IX_Insurance_InvoiceNo ON dbo.Insurance(Invoice_No, Admin_ID);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='IX_InsuranceDetails_InvoiceNo' AND object_id=OBJECT_ID('dbo.Insurance_Details'))
    CREATE INDEX IX_InsuranceDetails_InvoiceNo ON dbo.Insurance_Details(Invoice_No, Admin_ID);
GO


-- ============================================
-- FINAL UPDATES
-- ============================================

UPDATE p SET p.Price = p.List_Price_After FROM dbo.Product p;
GO

UPDATE d
SET d.Discount_Percent = COALESCE(d.Discount_Percent, pr.Discount_Percent)
FROM dbo.Bill_Exported_Details d
JOIN dbo.Bill_Exported h ON h.Invoice_No=d.Invoice_No AND h.Admin_ID=d.Admin_ID
LEFT JOIN dbo.Promotion pr ON pr.Promotion_Code = h.Promotion_Code
WHERE pr.Promotion_Code IS NOT NULL;

UPDATE d SET d.Total_Price_After = d.Unit_Price_Sell_After * d.Sold_Quantity * (1 - ISNULL(d.Discount_Percent,0)/100.0)
FROM dbo.Bill_Exported_Details d;
GO



-- 2.1) Trigger cập nhật tồn Product khi có INSERT/UPDATE/DELETE trên Bill_Exported_Details
IF OBJECT_ID('dbo.trg_Update_Product_Stock_On_Export', 'TR') IS NOT NULL
    DROP TRIGGER dbo.trg_Update_Product_Stock_On_Export;
GO

CREATE TRIGGER dbo.trg_Update_Product_Stock_On_Export
ON dbo.Bill_Exported_Details
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    -- Chỉ tính: Product.Quantity = Tổng nhập - Tổng đã bán (CHỈ qua Export, KHÔNG qua Orders)
    UPDATE p
    SET p.Quantity = (
        SELECT 
            ISNULL(SUM(bid.Quantity), 0) 
            - ISNULL((
                SELECT SUM(bed.Sold_Quantity) -- Sử dụng Sold_Quantity
                FROM dbo.Bill_Exported_Details bed 
                WHERE bed.Product_ID = p.Product_ID
                  AND bed.Status = 'Available'
            ), 0)
        FROM dbo.Bill_Imported_Details bid
        WHERE bid.Warehouse_Item_ID = p.Warehouse_Item_ID
    )
    FROM dbo.Product p
    WHERE p.Product_ID IN (
        SELECT DISTINCT Product_ID FROM inserted
        UNION
        SELECT DISTINCT Product_ID FROM deleted
    );
END;
GO

-- 2.2) Trigger "recalc all" (nếu bạn có trigger/tác vụ tính lại hàng loạt)
IF OBJECT_ID('dbo.trg_Recalc_Product_Quantities', 'TR') IS NOT NULL
    DROP TRIGGER dbo.trg_Recalc_Product_Quantities;
GO

CREATE TRIGGER dbo.trg_Recalc_Product_Quantities
ON dbo.Product
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Chỉ tính: Product.Quantity = Tổng nhập - Tổng đã bán (CHỈ qua Export, KHÔNG qua Orders)
    UPDATE p
    SET p.Quantity = (
        SELECT 
            ISNULL(SUM(bid.Quantity), 0) 
            - ISNULL((
                SELECT SUM(bed.Sold_Quantity) -- Sử dụng Sold_Quantity
                FROM dbo.Bill_Exported_Details bed 
                WHERE bed.Product_ID = p.Product_ID
                  AND bed.Status = 'Available'
            ), 0)
        FROM dbo.Bill_Imported_Details bid
        WHERE bid.Warehouse_Item_ID = p.Warehouse_Item_ID
    )
    FROM dbo.Product p
    WHERE p.Warehouse_Item_ID IS NOT NULL;
END;
GO
