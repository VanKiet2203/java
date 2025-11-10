-- ============================================
-- SEED DATA: Orders và Bill_Exported cho Statistics
-- Tạo dữ liệu mẫu cho các tháng trước tháng 11 (tháng hiện tại)
-- ============================================

USE [QuanLyKho];
GO

-- Kiểm tra và lấy dữ liệu có sẵn
DECLARE @AdminID varchar(20);
DECLARE @CustomerID varchar(50);
DECLARE @ProductID varchar(50);
DECLARE @ProductPrice decimal(10,2);
DECLARE @WarehouseItemID varchar(50);

-- Lấy Admin đầu tiên có sẵn
SELECT TOP 1 @AdminID = Admin_ID FROM dbo.Admin WHERE Status = 'Available';
IF @AdminID IS NULL
BEGIN
    PRINT 'WARNING: Không tìm thấy Admin nào. Vui lòng tạo Admin trước.';
    RETURN;
END

-- Lấy danh sách Customer có sẵn
DECLARE @CustomerCount int;
SELECT @CustomerCount = COUNT(*) FROM dbo.Customer WHERE Record_Status = 'Available';
IF @CustomerCount = 0
BEGIN
    PRINT 'WARNING: Không tìm thấy Customer nào. Vui lòng tạo Customer trước.';
    RETURN;
END

-- Lấy danh sách Product có sẵn
DECLARE @ProductCount int;
SELECT @ProductCount = COUNT(*) FROM dbo.Product WHERE Status = 'Available' AND Quantity > 0;
IF @ProductCount = 0
BEGIN
    PRINT 'WARNING: Không tìm thấy Product nào có sẵn. Vui lòng tạo Product trước.';
    RETURN;
END

PRINT '=== BẮT ĐẦU TẠO DỮ LIỆU MẪU ===';
PRINT 'Admin ID: ' + @AdminID;
PRINT 'Số lượng Customer có sẵn: ' + CAST(@CustomerCount AS varchar);
PRINT 'Số lượng Product có sẵn: ' + CAST(@ProductCount AS varchar);

-- ============================================
-- TẠO DỮ LIỆU CHO THÁNG 9 (September 2024)
-- ============================================
PRINT '';
PRINT '=== TẠO DỮ LIỆU CHO THÁNG 9/2024 ===';

DECLARE @OrderNo varchar(20);
DECLARE @InvoiceNo varchar(50);
DECLARE @DateOrder date;
DECLARE @TimeOrder time(7);
DECLARE @DateExported date;
DECLARE @TimeExported time(7);
DECLARE @TotalQuantity int;
DECLARE @TotalPrice decimal(15,2);
DECLARE @VATPercent decimal(5,2) = 8.00;
DECLARE @VATAmount decimal(15,2);
DECLARE @TotalAmount decimal(15,2);
DECLARE @CartID varchar(50);
DECLARE @Counter int = 1;
DECLARE @WarrantyMonths int;
DECLARE @CurrentQuantity int;

-- Tạo 5-8 orders cho tháng 9
WHILE @Counter <= 8
BEGIN
    -- Lấy Customer ngẫu nhiên (cần lấy trước để tạo mã)
    SELECT TOP 1 @CustomerID = Customer_ID 
    FROM dbo.Customer 
    WHERE Record_Status = 'Available'
    ORDER BY NEWID();
    
    -- Tạo Order_No ngẫu nhiên 8 chữ số (giống code Java)
    SET @OrderNo = FORMAT(ABS(CHECKSUM(NEWID())) % 100000000, '00000000');
    
    -- Tạo Invoice_No ngẫu nhiên 10 chữ số + "-" + Customer_ID (giống code Java)
    SET @InvoiceNo = FORMAT(ABS(CHECKSUM(NEWID())) % 1000000000, '0000000000') + '-' + @CustomerID;
    
    -- Ngày đặt hàng: tháng 9, ngày ngẫu nhiên từ 1-30
    SET @DateOrder = DATEFROMPARTS(2024, 9, 1 + (@Counter * 3) % 30);
    SET @TimeOrder = CAST(DATEADD(MINUTE, (@Counter * 47) % 1440, '00:00:00') AS time(7));
    
    -- Ngày xuất hóa đơn: cùng ngày hoặc ngày sau
    SET @DateExported = DATEADD(DAY, (@Counter % 2), @DateOrder);
    SET @TimeExported = CAST(DATEADD(MINUTE, (@Counter * 37) % 1440, '08:00:00') AS time(7));
    
    -- Tạo Cart_ID: CART- + timestamp + "-" + Customer_ID (giống code Java)
    -- Sử dụng DATEDIFF_BIG để lấy milliseconds từ epoch (1970-01-01) + một số ngẫu nhiên để tránh trùng
    SET @CartID = 'CART-' + CAST(DATEDIFF_BIG(MILLISECOND, '1970-01-01', GETDATE()) + ABS(CHECKSUM(NEWID())) % 1000 AS varchar) + '-' + @CustomerID;
    
    -- Số lượng sản phẩm (1-3)
    SET @TotalQuantity = 1 + (@Counter % 3);
    
    -- Lấy Product ngẫu nhiên có đủ số lượng và có Warehouse_Item_ID
    SELECT TOP 1 
        @ProductID = Product_ID,
        @ProductPrice = ISNULL(Price, 0),
        @WarehouseItemID = Warehouse_Item_ID
    FROM dbo.Product 
    WHERE Status = 'Available' 
        AND Quantity >= @TotalQuantity
        AND Warehouse_Item_ID IS NOT NULL
    ORDER BY NEWID();
    
    -- Nếu không tìm thấy Product đủ số lượng, bỏ qua order này
    IF @ProductID IS NULL
    BEGIN
        SET @Counter = @Counter + 1;
        CONTINUE;
    END
    
    -- Tính tổng tiền
    SET @TotalPrice = @ProductPrice * @TotalQuantity;
    SET @VATAmount = @TotalPrice * @VATPercent / 100.0;
    SET @TotalAmount = @TotalPrice + @VATAmount;
    
    -- 1. Tạo Cart (nếu cần)
    IF NOT EXISTS (SELECT 1 FROM dbo.Cart WHERE Cart_ID = @CartID)
    BEGIN
        INSERT INTO dbo.Cart (Cart_ID, Customer_ID, Product_ID, Quantity, Status)
        VALUES (@CartID, @CustomerID, @ProductID, @TotalQuantity, 'Available');
    END
    
    -- 2. Tạo Order
    IF NOT EXISTS (SELECT 1 FROM dbo.Orders WHERE Order_No = @OrderNo AND Customer_ID = @CustomerID)
    BEGIN
        INSERT INTO dbo.Orders (
            Order_No, Customer_ID, Cart_ID, Total_Quantity_Product, 
            Total_Price, Payment, Date_Order, Time_Order, Status, Record_Status
        )
        VALUES (
            @OrderNo, @CustomerID, @CartID, @TotalQuantity,
            @TotalPrice, 'Cash', @DateOrder, @TimeOrder, 'Confirmed', 'Available'
        );
        
        -- 3. Tạo Order_Details
        INSERT INTO dbo.Orders_Details (
            Order_No, Customer_ID, Product_ID, Price, Sold_Quantity,
            Date_Order, Time_Order, Status, Record_Status
        )
        VALUES (
            @OrderNo, @CustomerID, @ProductID, @ProductPrice, @TotalQuantity,
            @DateOrder, @TimeOrder, 'Confirmed', 'Available'
        );
        
        -- 4. Tạo Bill_Exported
        IF NOT EXISTS (SELECT 1 FROM dbo.Bill_Exported WHERE Invoice_No = @InvoiceNo AND Admin_ID = @AdminID)
        BEGIN
            INSERT INTO dbo.Bill_Exported (
                Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product,
                VAT_Percent, VAT_Amount, Total_Amount, Status
            )
            VALUES (
                @InvoiceNo, @AdminID, @CustomerID, @OrderNo, @TotalQuantity,
                @VATPercent, @VATAmount, @TotalAmount, 'Available'
            );
            
            -- 5. Tạo Bill_Exported_Details
            SELECT @WarrantyMonths = ISNULL(Warranty_Months, 12) 
            FROM dbo.Product WHERE Product_ID = @ProductID;
            
            INSERT INTO dbo.Bill_Exported_Details (
                Invoice_No, Admin_ID, Product_ID,
                Unit_Price_Sell_Before, Unit_Price_Sell_After,
                Sold_Quantity, Discount_Percent,
                Total_Price_Before, Total_Price_After,
                Date_Exported, Time_Exported,
                Start_Date, End_Date, Status
            )
            VALUES (
                @InvoiceNo, @AdminID, @ProductID,
                @ProductPrice, @ProductPrice,  -- Giả sử không có discount
                @TotalQuantity, 0.00,
                @TotalPrice, @TotalPrice,
                @DateExported, @TimeExported,
                @DateExported, DATEADD(MONTH, @WarrantyMonths, @DateExported),
                'Available'
            );
        END
    END
    
    SET @Counter = @Counter + 1;
END

PRINT 'Đã tạo ' + CAST(@Counter - 1 AS varchar) + ' orders cho tháng 9/2024';

-- ============================================
-- TẠO DỮ LIỆU CHO THÁNG 10 (October 2024)
-- ============================================
PRINT '';
PRINT '=== TẠO DỮ LIỆU CHO THÁNG 10/2024 ===';

SET @Counter = 1;

-- Tạo 6-10 orders cho tháng 10
WHILE @Counter <= 10
BEGIN
    -- Lấy Customer ngẫu nhiên (cần lấy trước để tạo mã)
    SELECT TOP 1 @CustomerID = Customer_ID 
    FROM dbo.Customer 
    WHERE Record_Status = 'Available'
    ORDER BY NEWID();
    
    -- Tạo Order_No ngẫu nhiên 8 chữ số (giống code Java)
    SET @OrderNo = FORMAT(ABS(CHECKSUM(NEWID())) % 100000000, '00000000');
    
    -- Tạo Invoice_No ngẫu nhiên 10 chữ số + "-" + Customer_ID (giống code Java)
    SET @InvoiceNo = FORMAT(ABS(CHECKSUM(NEWID())) % 1000000000, '0000000000') + '-' + @CustomerID;
    
    -- Ngày đặt hàng: tháng 10, ngày ngẫu nhiên từ 1-31
    SET @DateOrder = DATEFROMPARTS(2024, 10, 1 + (@Counter * 3) % 31);
    SET @TimeOrder = CAST(DATEADD(MINUTE, (@Counter * 47) % 1440, '00:00:00') AS time(7));
    
    -- Ngày xuất hóa đơn: cùng ngày hoặc ngày sau
    SET @DateExported = DATEADD(DAY, (@Counter % 2), @DateOrder);
    SET @TimeExported = CAST(DATEADD(MINUTE, (@Counter * 37) % 1440, '08:00:00') AS time(7));
    
    -- Tạo Cart_ID: CART- + timestamp + "-" + Customer_ID (giống code Java)
    -- Sử dụng DATEDIFF_BIG để lấy milliseconds từ epoch (1970-01-01) + một số ngẫu nhiên để tránh trùng
    SET @CartID = 'CART-' + CAST(DATEDIFF_BIG(MILLISECOND, '1970-01-01', GETDATE()) + ABS(CHECKSUM(NEWID())) % 1000 AS varchar) + '-' + @CustomerID;
    
    -- Số lượng sản phẩm (1-4)
    SET @TotalQuantity = 1 + (@Counter % 4);
    
    -- Lấy Product ngẫu nhiên có đủ số lượng và có Warehouse_Item_ID
    SELECT TOP 1 
        @ProductID = Product_ID,
        @ProductPrice = ISNULL(Price, 0),
        @WarehouseItemID = Warehouse_Item_ID
    FROM dbo.Product 
    WHERE Status = 'Available' 
        AND Quantity >= @TotalQuantity
        AND Warehouse_Item_ID IS NOT NULL
    ORDER BY NEWID();
    
    -- Kiểm tra Product có đủ số lượng và có Warehouse_Item_ID
    IF @ProductID IS NOT NULL
    BEGIN
        -- Re-check quantity right before use to ensure it's still sufficient
        SELECT @CurrentQuantity = Quantity, @WarehouseItemID = Warehouse_Item_ID
        FROM dbo.Product WHERE Product_ID = @ProductID;
        
        IF @CurrentQuantity < @TotalQuantity OR @WarehouseItemID IS NULL
        BEGIN
            SET @ProductID = NULL;
        END
    END
    
    -- Nếu không tìm thấy Product đủ số lượng, bỏ qua order này
    IF @ProductID IS NULL
    BEGIN
        SET @Counter = @Counter + 1;
        CONTINUE;
    END
    
    -- Tính tổng tiền
    SET @TotalPrice = @ProductPrice * @TotalQuantity;
    SET @VATAmount = @TotalPrice * @VATPercent / 100.0;
    SET @TotalAmount = @TotalPrice + @VATAmount;
    
    -- 1. Tạo Cart (nếu cần)
    IF NOT EXISTS (SELECT 1 FROM dbo.Cart WHERE Cart_ID = @CartID)
    BEGIN
        INSERT INTO dbo.Cart (Cart_ID, Customer_ID, Product_ID, Quantity, Status)
        VALUES (@CartID, @CustomerID, @ProductID, @TotalQuantity, 'Available');
    END
    
    -- 2. Tạo Order
    IF NOT EXISTS (SELECT 1 FROM dbo.Orders WHERE Order_No = @OrderNo AND Customer_ID = @CustomerID)
    BEGIN
        INSERT INTO dbo.Orders (
            Order_No, Customer_ID, Cart_ID, Total_Quantity_Product, 
            Total_Price, Payment, Date_Order, Time_Order, Status, Record_Status
        )
        VALUES (
            @OrderNo, @CustomerID, @CartID, @TotalQuantity,
            @TotalPrice, 'Cash', @DateOrder, @TimeOrder, 'Confirmed', 'Available'
        );
        
        -- 3. Tạo Order_Details
        INSERT INTO dbo.Orders_Details (
            Order_No, Customer_ID, Product_ID, Price, Sold_Quantity,
            Date_Order, Time_Order, Status, Record_Status
        )
        VALUES (
            @OrderNo, @CustomerID, @ProductID, @ProductPrice, @TotalQuantity,
            @DateOrder, @TimeOrder, 'Confirmed', 'Available'
        );
        
        -- 4. Tạo Bill_Exported
        IF NOT EXISTS (SELECT 1 FROM dbo.Bill_Exported WHERE Invoice_No = @InvoiceNo AND Admin_ID = @AdminID)
        BEGIN
            INSERT INTO dbo.Bill_Exported (
                Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product,
                VAT_Percent, VAT_Amount, Total_Amount, Status
            )
            VALUES (
                @InvoiceNo, @AdminID, @CustomerID, @OrderNo, @TotalQuantity,
                @VATPercent, @VATAmount, @TotalAmount, 'Available'
            );
            
            -- 5. Tạo Bill_Exported_Details
            SELECT @WarrantyMonths = ISNULL(Warranty_Months, 12) 
            FROM dbo.Product WHERE Product_ID = @ProductID;
            
            INSERT INTO dbo.Bill_Exported_Details (
                Invoice_No, Admin_ID, Product_ID,
                Unit_Price_Sell_Before, Unit_Price_Sell_After,
                Sold_Quantity, Discount_Percent,
                Total_Price_Before, Total_Price_After,
                Date_Exported, Time_Exported,
                Start_Date, End_Date, Status
            )
            VALUES (
                @InvoiceNo, @AdminID, @ProductID,
                @ProductPrice, @ProductPrice,
                @TotalQuantity, 0.00,
                @TotalPrice, @TotalPrice,
                @DateExported, @TimeExported,
                @DateExported, DATEADD(MONTH, @WarrantyMonths, @DateExported),
                'Available'
            );
        END
    END
    
    SET @Counter = @Counter + 1;
END

PRINT 'Đã tạo ' + CAST(@Counter - 1 AS varchar) + ' orders cho tháng 10/2024';

-- ============================================
-- TẠO DỮ LIỆU CHO THÁNG 8 (August 2024) - Tùy chọn
-- ============================================
PRINT '';
PRINT '=== TẠO DỮ LIỆU CHO THÁNG 8/2024 ===';

SET @Counter = 1;

-- Tạo 5-7 orders cho tháng 8
WHILE @Counter <= 7
BEGIN
    -- Lấy Customer ngẫu nhiên (cần lấy trước để tạo mã)
    SELECT TOP 1 @CustomerID = Customer_ID 
    FROM dbo.Customer 
    WHERE Record_Status = 'Available'
    ORDER BY NEWID();
    
    -- Tạo Order_No ngẫu nhiên 8 chữ số (giống code Java)
    SET @OrderNo = FORMAT(ABS(CHECKSUM(NEWID())) % 100000000, '00000000');
    
    -- Tạo Invoice_No ngẫu nhiên 10 chữ số + "-" + Customer_ID (giống code Java)
    SET @InvoiceNo = FORMAT(ABS(CHECKSUM(NEWID())) % 1000000000, '0000000000') + '-' + @CustomerID;
    
    -- Ngày đặt hàng: tháng 8, ngày ngẫu nhiên từ 1-31
    SET @DateOrder = DATEFROMPARTS(2024, 8, 1 + (@Counter * 4) % 31);
    SET @TimeOrder = CAST(DATEADD(MINUTE, (@Counter * 47) % 1440, '00:00:00') AS time(7));
    
    -- Ngày xuất hóa đơn: cùng ngày hoặc ngày sau
    SET @DateExported = DATEADD(DAY, (@Counter % 2), @DateOrder);
    SET @TimeExported = CAST(DATEADD(MINUTE, (@Counter * 37) % 1440, '08:00:00') AS time(7));
    
    -- Tạo Cart_ID: CART- + timestamp + "-" + Customer_ID (giống code Java)
    -- Sử dụng DATEDIFF_BIG để lấy milliseconds từ epoch (1970-01-01) + một số ngẫu nhiên để tránh trùng
    SET @CartID = 'CART-' + CAST(DATEDIFF_BIG(MILLISECOND, '1970-01-01', GETDATE()) + ABS(CHECKSUM(NEWID())) % 1000 AS varchar) + '-' + @CustomerID;
    
    -- Số lượng sản phẩm (1-3)
    SET @TotalQuantity = 1 + (@Counter % 3);
    
    -- Lấy Product ngẫu nhiên có đủ số lượng và có Warehouse_Item_ID
    SELECT TOP 1 
        @ProductID = Product_ID,
        @ProductPrice = ISNULL(Price, 0),
        @WarehouseItemID = Warehouse_Item_ID
    FROM dbo.Product 
    WHERE Status = 'Available' 
        AND Quantity >= @TotalQuantity
        AND Warehouse_Item_ID IS NOT NULL
    ORDER BY NEWID();
    
    -- Nếu không tìm thấy Product đủ số lượng, bỏ qua order này
    IF @ProductID IS NULL
    BEGIN
        SET @Counter = @Counter + 1;
        CONTINUE;
    END
    
    -- Tính tổng tiền
    SET @TotalPrice = @ProductPrice * @TotalQuantity;
    SET @VATAmount = @TotalPrice * @VATPercent / 100.0;
    SET @TotalAmount = @TotalPrice + @VATAmount;
    
    -- 1. Tạo Cart (nếu cần)
    IF NOT EXISTS (SELECT 1 FROM dbo.Cart WHERE Cart_ID = @CartID)
    BEGIN
        INSERT INTO dbo.Cart (Cart_ID, Customer_ID, Product_ID, Quantity, Status)
        VALUES (@CartID, @CustomerID, @ProductID, @TotalQuantity, 'Available');
    END
    
    -- 2. Tạo Order
    IF NOT EXISTS (SELECT 1 FROM dbo.Orders WHERE Order_No = @OrderNo AND Customer_ID = @CustomerID)
    BEGIN
        INSERT INTO dbo.Orders (
            Order_No, Customer_ID, Cart_ID, Total_Quantity_Product, 
            Total_Price, Payment, Date_Order, Time_Order, Status, Record_Status
        )
        VALUES (
            @OrderNo, @CustomerID, @CartID, @TotalQuantity,
            @TotalPrice, 'Cash', @DateOrder, @TimeOrder, 'Confirmed', 'Available'
        );
        
        -- 3. Tạo Order_Details
        INSERT INTO dbo.Orders_Details (
            Order_No, Customer_ID, Product_ID, Price, Sold_Quantity,
            Date_Order, Time_Order, Status, Record_Status
        )
        VALUES (
            @OrderNo, @CustomerID, @ProductID, @ProductPrice, @TotalQuantity,
            @DateOrder, @TimeOrder, 'Confirmed', 'Available'
        );
        
        -- 4. Tạo Bill_Exported
        IF NOT EXISTS (SELECT 1 FROM dbo.Bill_Exported WHERE Invoice_No = @InvoiceNo AND Admin_ID = @AdminID)
        BEGIN
            INSERT INTO dbo.Bill_Exported (
                Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product,
                VAT_Percent, VAT_Amount, Total_Amount, Status
            )
            VALUES (
                @InvoiceNo, @AdminID, @CustomerID, @OrderNo, @TotalQuantity,
                @VATPercent, @VATAmount, @TotalAmount, 'Available'
            );
            
            -- 5. Tạo Bill_Exported_Details
            SELECT @WarrantyMonths = ISNULL(Warranty_Months, 12) 
            FROM dbo.Product WHERE Product_ID = @ProductID;
            
            INSERT INTO dbo.Bill_Exported_Details (
                Invoice_No, Admin_ID, Product_ID,
                Unit_Price_Sell_Before, Unit_Price_Sell_After,
                Sold_Quantity, Discount_Percent,
                Total_Price_Before, Total_Price_After,
                Date_Exported, Time_Exported,
                Start_Date, End_Date, Status
            )
            VALUES (
                @InvoiceNo, @AdminID, @ProductID,
                @ProductPrice, @ProductPrice,
                @TotalQuantity, 0.00,
                @TotalPrice, @TotalPrice,
                @DateExported, @TimeExported,
                @DateExported, DATEADD(MONTH, @WarrantyMonths, @DateExported),
                'Available'
            );
        END
    END
    
    SET @Counter = @Counter + 1;
END

PRINT 'Đã tạo ' + CAST(@Counter - 1 AS varchar) + ' orders cho tháng 8/2024';

-- ============================================
-- TẠO DỮ LIỆU CHO THÁNG 7 (July 2024) - Tùy chọn
-- ============================================
PRINT '';
PRINT '=== TẠO DỮ LIỆU CHO THÁNG 7/2024 ===';

SET @Counter = 1;

-- Tạo 4-6 orders cho tháng 7
WHILE @Counter <= 6
BEGIN
    -- Lấy Customer ngẫu nhiên (cần lấy trước để tạo mã)
    SELECT TOP 1 @CustomerID = Customer_ID 
    FROM dbo.Customer 
    WHERE Record_Status = 'Available'
    ORDER BY NEWID();
    
    -- Tạo Order_No ngẫu nhiên 8 chữ số (giống code Java)
    SET @OrderNo = FORMAT(ABS(CHECKSUM(NEWID())) % 100000000, '00000000');
    
    -- Tạo Invoice_No ngẫu nhiên 10 chữ số + "-" + Customer_ID (giống code Java)
    SET @InvoiceNo = FORMAT(ABS(CHECKSUM(NEWID())) % 1000000000, '0000000000') + '-' + @CustomerID;
    
    -- Ngày đặt hàng: tháng 7, ngày ngẫu nhiên từ 1-31
    SET @DateOrder = DATEFROMPARTS(2024, 7, 1 + (@Counter * 5) % 31);
    SET @TimeOrder = CAST(DATEADD(MINUTE, (@Counter * 47) % 1440, '00:00:00') AS time(7));
    
    -- Ngày xuất hóa đơn: cùng ngày hoặc ngày sau
    SET @DateExported = DATEADD(DAY, (@Counter % 2), @DateOrder);
    SET @TimeExported = CAST(DATEADD(MINUTE, (@Counter * 37) % 1440, '08:00:00') AS time(7));
    
    -- Tạo Cart_ID: CART- + timestamp + "-" + Customer_ID (giống code Java)
    -- Sử dụng DATEDIFF_BIG để lấy milliseconds từ epoch (1970-01-01) + một số ngẫu nhiên để tránh trùng
    SET @CartID = 'CART-' + CAST(DATEDIFF_BIG(MILLISECOND, '1970-01-01', GETDATE()) + ABS(CHECKSUM(NEWID())) % 1000 AS varchar) + '-' + @CustomerID;
    
    -- Số lượng sản phẩm (1-2)
    SET @TotalQuantity = 1 + (@Counter % 2);
    
    -- Lấy Product ngẫu nhiên có đủ số lượng và có Warehouse_Item_ID
    SELECT TOP 1 
        @ProductID = Product_ID,
        @ProductPrice = ISNULL(Price, 0),
        @WarehouseItemID = Warehouse_Item_ID
    FROM dbo.Product 
    WHERE Status = 'Available' 
        AND Quantity >= @TotalQuantity
        AND Warehouse_Item_ID IS NOT NULL
    ORDER BY NEWID();
    
    -- Kiểm tra Product có đủ số lượng và có Warehouse_Item_ID
    IF @ProductID IS NOT NULL
    BEGIN
        -- Re-check quantity right before use to ensure it's still sufficient
        SELECT @CurrentQuantity = Quantity, @WarehouseItemID = Warehouse_Item_ID
        FROM dbo.Product WHERE Product_ID = @ProductID;
        
        IF @CurrentQuantity < @TotalQuantity OR @WarehouseItemID IS NULL
        BEGIN
            SET @ProductID = NULL;
        END
    END
    
    -- Nếu không tìm thấy Product đủ số lượng, bỏ qua order này
    IF @ProductID IS NULL
    BEGIN
        SET @Counter = @Counter + 1;
        CONTINUE;
    END
    
    -- Tính tổng tiền
    SET @TotalPrice = @ProductPrice * @TotalQuantity;
    SET @VATAmount = @TotalPrice * @VATPercent / 100.0;
    SET @TotalAmount = @TotalPrice + @VATAmount;
    
    -- 1. Tạo Cart (nếu cần)
    IF NOT EXISTS (SELECT 1 FROM dbo.Cart WHERE Cart_ID = @CartID)
    BEGIN
        INSERT INTO dbo.Cart (Cart_ID, Customer_ID, Product_ID, Quantity, Status)
        VALUES (@CartID, @CustomerID, @ProductID, @TotalQuantity, 'Available');
    END
    
    -- 2. Tạo Order
    IF NOT EXISTS (SELECT 1 FROM dbo.Orders WHERE Order_No = @OrderNo AND Customer_ID = @CustomerID)
    BEGIN
        INSERT INTO dbo.Orders (
            Order_No, Customer_ID, Cart_ID, Total_Quantity_Product, 
            Total_Price, Payment, Date_Order, Time_Order, Status, Record_Status
        )
        VALUES (
            @OrderNo, @CustomerID, @CartID, @TotalQuantity,
            @TotalPrice, 'Cash', @DateOrder, @TimeOrder, 'Confirmed', 'Available'
        );
        
        -- 3. Tạo Order_Details
        INSERT INTO dbo.Orders_Details (
            Order_No, Customer_ID, Product_ID, Price, Sold_Quantity,
            Date_Order, Time_Order, Status, Record_Status
        )
        VALUES (
            @OrderNo, @CustomerID, @ProductID, @ProductPrice, @TotalQuantity,
            @DateOrder, @TimeOrder, 'Confirmed', 'Available'
        );
        
        -- 4. Tạo Bill_Exported
        IF NOT EXISTS (SELECT 1 FROM dbo.Bill_Exported WHERE Invoice_No = @InvoiceNo AND Admin_ID = @AdminID)
        BEGIN
            INSERT INTO dbo.Bill_Exported (
                Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product,
                VAT_Percent, VAT_Amount, Total_Amount, Status
            )
            VALUES (
                @InvoiceNo, @AdminID, @CustomerID, @OrderNo, @TotalQuantity,
                @VATPercent, @VATAmount, @TotalAmount, 'Available'
            );
            
            -- 5. Tạo Bill_Exported_Details
            SELECT @WarrantyMonths = ISNULL(Warranty_Months, 12) 
            FROM dbo.Product WHERE Product_ID = @ProductID;
            
            INSERT INTO dbo.Bill_Exported_Details (
                Invoice_No, Admin_ID, Product_ID,
                Unit_Price_Sell_Before, Unit_Price_Sell_After,
                Sold_Quantity, Discount_Percent,
                Total_Price_Before, Total_Price_After,
                Date_Exported, Time_Exported,
                Start_Date, End_Date, Status
            )
            VALUES (
                @InvoiceNo, @AdminID, @ProductID,
                @ProductPrice, @ProductPrice,
                @TotalQuantity, 0.00,
                @TotalPrice, @TotalPrice,
                @DateExported, @TimeExported,
                @DateExported, DATEADD(MONTH, @WarrantyMonths, @DateExported),
                'Available'
            );
        END
    END
    
    SET @Counter = @Counter + 1;
END

PRINT 'Đã tạo ' + CAST(@Counter - 1 AS varchar) + ' orders cho tháng 7/2024';

-- ============================================
-- KIỂM TRA KẾT QUẢ
-- ============================================
PRINT '';
PRINT '=== KIỂM TRA KẾT QUẢ ===';

SELECT 
    FORMAT(bed.Date_Exported, 'yyyy-MM') AS Month,
    COUNT(DISTINCT bed.Invoice_No) AS Total_Invoices,
    COUNT(bed.Product_ID) AS Total_Products_Sold,
    SUM(bed.Sold_Quantity) AS Total_Quantity,
    SUM(bed.Total_Price_After) AS Total_Revenue
FROM dbo.Bill_Exported_Details bed
WHERE bed.Status = 'Available'
    AND YEAR(bed.Date_Exported) = 2024
    AND MONTH(bed.Date_Exported) IN (7, 8, 9, 10)
GROUP BY FORMAT(bed.Date_Exported, 'yyyy-MM')
ORDER BY Month;

PRINT '';
PRINT '=== HOÀN TẤT TẠO DỮ LIỆU MẪU CHO NĂM 2024 ===';
PRINT 'Dữ liệu đã được tạo cho các tháng: 7, 8, 9, 10/2024';

-- ============================================
-- TẠO DỮ LIỆU CHO NĂM 2025
-- Tạo tương tự như năm 2024
-- ============================================
PRINT '';
PRINT '=== BẮT ĐẦU TẠO DỮ LIỆU CHO NĂM 2025 ===';

-- ============================================
-- TẠO DỮ LIỆU CHO THÁNG 1 (January 2025)
-- ============================================
PRINT '';
PRINT '=== TẠO DỮ LIỆU CHO THÁNG 1/2025 ===';

SET @Counter = 1;

-- Tạo 8 orders cho tháng 1
WHILE @Counter <= 8
BEGIN
    -- Tạo Order_No và Invoice_No
    -- Lấy Customer ngẫu nhiên (cần lấy trước để tạo mã)
    SELECT TOP 1 @CustomerID = Customer_ID 
    FROM dbo.Customer 
    WHERE Record_Status = 'Available'
    ORDER BY NEWID();
    
    -- Tạo Order_No ngẫu nhiên 8 chữ số (giống code Java)
    SET @OrderNo = FORMAT(ABS(CHECKSUM(NEWID())) % 100000000, '00000000');
    
    -- Tạo Invoice_No ngẫu nhiên 10 chữ số + "-" + Customer_ID (giống code Java)
    SET @InvoiceNo = FORMAT(ABS(CHECKSUM(NEWID())) % 1000000000, '0000000000') + '-' + @CustomerID;
    
    -- Ngày đặt hàng: tháng 1, ngày ngẫu nhiên từ 1-31
    SET @DateOrder = DATEFROMPARTS(2025, 1, 1 + (@Counter * 3) % 31);
    SET @TimeOrder = CAST(DATEADD(MINUTE, (@Counter * 47) % 1440, '00:00:00') AS time(7));
    
    -- Ngày xuất hóa đơn: cùng ngày hoặc ngày sau
    SET @DateExported = DATEADD(DAY, (@Counter % 2), @DateOrder);
    SET @TimeExported = CAST(DATEADD(MINUTE, (@Counter * 37) % 1440, '08:00:00') AS time(7));
    
    -- Tạo Cart_ID
    -- Tạo Cart_ID: CART- + timestamp + "-" + Customer_ID (giống code Java)
    -- Sử dụng DATEDIFF_BIG để lấy milliseconds từ epoch (1970-01-01) + một số ngẫu nhiên để tránh trùng
    SET @CartID = 'CART-' + CAST(DATEDIFF_BIG(MILLISECOND, '1970-01-01', GETDATE()) + ABS(CHECKSUM(NEWID())) % 1000 AS varchar) + '-' + @CustomerID;
    
    -- Lấy Customer ngẫu nhiên
    SELECT TOP 1 @CustomerID = Customer_ID 
    FROM dbo.Customer 
    WHERE Record_Status = 'Available'
    ORDER BY NEWID();
    
    -- Số lượng sản phẩm (1-3)
    SET @TotalQuantity = 1 + (@Counter % 3);
    
    -- Lấy Product ngẫu nhiên có đủ số lượng và có Warehouse_Item_ID
    SELECT TOP 1 
        @ProductID = Product_ID,
        @ProductPrice = ISNULL(Price, 0),
        @WarehouseItemID = Warehouse_Item_ID
    FROM dbo.Product 
    WHERE Status = 'Available' 
        AND Quantity >= @TotalQuantity
        AND Warehouse_Item_ID IS NOT NULL
    ORDER BY NEWID();
    
    -- Nếu không tìm thấy Product đủ số lượng, bỏ qua order này
    IF @ProductID IS NULL
    BEGIN
        SET @Counter = @Counter + 1;
        CONTINUE;
    END
    
    -- Tính tổng tiền
    SET @TotalPrice = @ProductPrice * @TotalQuantity;
    SET @VATAmount = @TotalPrice * @VATPercent / 100.0;
    SET @TotalAmount = @TotalPrice + @VATAmount;
    
    -- 1. Tạo Cart (nếu cần)
    IF NOT EXISTS (SELECT 1 FROM dbo.Cart WHERE Cart_ID = @CartID)
    BEGIN
        INSERT INTO dbo.Cart (Cart_ID, Customer_ID, Product_ID, Quantity, Status)
        VALUES (@CartID, @CustomerID, @ProductID, @TotalQuantity, 'Available');
    END
    
    -- 2. Tạo Order
    IF NOT EXISTS (SELECT 1 FROM dbo.Orders WHERE Order_No = @OrderNo AND Customer_ID = @CustomerID)
    BEGIN
        INSERT INTO dbo.Orders (
            Order_No, Customer_ID, Cart_ID, Total_Quantity_Product, 
            Total_Price, Payment, Date_Order, Time_Order, Status, Record_Status
        )
        VALUES (
            @OrderNo, @CustomerID, @CartID, @TotalQuantity,
            @TotalPrice, 'Cash', @DateOrder, @TimeOrder, 'Confirmed', 'Available'
        );
        
        -- 3. Tạo Order_Details
        INSERT INTO dbo.Orders_Details (
            Order_No, Customer_ID, Product_ID, Price, Sold_Quantity,
            Date_Order, Time_Order, Status, Record_Status
        )
        VALUES (
            @OrderNo, @CustomerID, @ProductID, @ProductPrice, @TotalQuantity,
            @DateOrder, @TimeOrder, 'Confirmed', 'Available'
        );
        
        -- 4. Tạo Bill_Exported
        IF NOT EXISTS (SELECT 1 FROM dbo.Bill_Exported WHERE Invoice_No = @InvoiceNo AND Admin_ID = @AdminID)
        BEGIN
            INSERT INTO dbo.Bill_Exported (
                Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product,
                VAT_Percent, VAT_Amount, Total_Amount, Status
            )
            VALUES (
                @InvoiceNo, @AdminID, @CustomerID, @OrderNo, @TotalQuantity,
                @VATPercent, @VATAmount, @TotalAmount, 'Available'
            );
            
            -- 5. Tạo Bill_Exported_Details
            SELECT @WarrantyMonths = ISNULL(Warranty_Months, 12) 
            FROM dbo.Product WHERE Product_ID = @ProductID;
            
            INSERT INTO dbo.Bill_Exported_Details (
                Invoice_No, Admin_ID, Product_ID,
                Unit_Price_Sell_Before, Unit_Price_Sell_After,
                Sold_Quantity, Discount_Percent,
                Total_Price_Before, Total_Price_After,
                Date_Exported, Time_Exported,
                Start_Date, End_Date, Status
            )
            VALUES (
                @InvoiceNo, @AdminID, @ProductID,
                @ProductPrice, @ProductPrice,  -- Giả sử không có discount
                @TotalQuantity, 0.00,
                @TotalPrice, @TotalPrice,
                @DateExported, @TimeExported,
                @DateExported, DATEADD(MONTH, @WarrantyMonths, @DateExported),
                'Available'
            );
        END
    END
    
    SET @Counter = @Counter + 1;
END

PRINT 'Đã tạo ' + CAST(@Counter - 1 AS varchar) + ' orders cho tháng 1/2025';

-- ============================================
-- TẠO DỮ LIỆU CHO THÁNG 2 (February 2025)
-- ============================================
PRINT '';
PRINT '=== TẠO DỮ LIỆU CHO THÁNG 2/2025 ===';

SET @Counter = 1;

-- Tạo 7 orders cho tháng 2
WHILE @Counter <= 7
BEGIN
    -- Tạo Order_No và Invoice_No
    -- Lấy Customer ngẫu nhiên (cần lấy trước để tạo mã)
    SELECT TOP 1 @CustomerID = Customer_ID 
    FROM dbo.Customer 
    WHERE Record_Status = 'Available'
    ORDER BY NEWID();
    
    -- Tạo Order_No ngẫu nhiên 8 chữ số (giống code Java)
    SET @OrderNo = FORMAT(ABS(CHECKSUM(NEWID())) % 100000000, '00000000');
    
    -- Tạo Invoice_No ngẫu nhiên 10 chữ số + "-" + Customer_ID (giống code Java)
    SET @InvoiceNo = FORMAT(ABS(CHECKSUM(NEWID())) % 1000000000, '0000000000') + '-' + @CustomerID;
    
    -- Ngày đặt hàng: tháng 2, ngày ngẫu nhiên từ 1-28
    SET @DateOrder = DATEFROMPARTS(2025, 2, 1 + (@Counter * 4) % 28);
    SET @TimeOrder = CAST(DATEADD(MINUTE, (@Counter * 47) % 1440, '00:00:00') AS time(7));
    
    -- Ngày xuất hóa đơn: cùng ngày hoặc ngày sau
    SET @DateExported = DATEADD(DAY, (@Counter % 2), @DateOrder);
    SET @TimeExported = CAST(DATEADD(MINUTE, (@Counter * 37) % 1440, '08:00:00') AS time(7));
    
    -- Tạo Cart_ID
    -- Tạo Cart_ID: CART- + timestamp + "-" + Customer_ID (giống code Java)
    -- Sử dụng DATEDIFF_BIG để lấy milliseconds từ epoch (1970-01-01) + một số ngẫu nhiên để tránh trùng
    SET @CartID = 'CART-' + CAST(DATEDIFF_BIG(MILLISECOND, '1970-01-01', GETDATE()) + ABS(CHECKSUM(NEWID())) % 1000 AS varchar) + '-' + @CustomerID;
    
    -- Lấy Customer ngẫu nhiên
    SELECT TOP 1 @CustomerID = Customer_ID 
    FROM dbo.Customer 
    WHERE Record_Status = 'Available'
    ORDER BY NEWID();
    
    -- Số lượng sản phẩm (1-3)
    SET @TotalQuantity = 1 + (@Counter % 3);
    
    -- Lấy Product ngẫu nhiên có đủ số lượng và có Warehouse_Item_ID
    SELECT TOP 1 
        @ProductID = Product_ID,
        @ProductPrice = ISNULL(Price, 0),
        @WarehouseItemID = Warehouse_Item_ID
    FROM dbo.Product 
    WHERE Status = 'Available' 
        AND Quantity >= @TotalQuantity
        AND Warehouse_Item_ID IS NOT NULL
    ORDER BY NEWID();
    
    -- Nếu không tìm thấy Product đủ số lượng, bỏ qua order này
    IF @ProductID IS NULL
    BEGIN
        SET @Counter = @Counter + 1;
        CONTINUE;
    END
    
    -- Tính tổng tiền
    SET @TotalPrice = @ProductPrice * @TotalQuantity;
    SET @VATAmount = @TotalPrice * @VATPercent / 100.0;
    SET @TotalAmount = @TotalPrice + @VATAmount;
    
    -- 1. Tạo Cart (nếu cần)
    IF NOT EXISTS (SELECT 1 FROM dbo.Cart WHERE Cart_ID = @CartID)
    BEGIN
        INSERT INTO dbo.Cart (Cart_ID, Customer_ID, Product_ID, Quantity, Status)
        VALUES (@CartID, @CustomerID, @ProductID, @TotalQuantity, 'Available');
    END
    
    -- 2. Tạo Order
    IF NOT EXISTS (SELECT 1 FROM dbo.Orders WHERE Order_No = @OrderNo AND Customer_ID = @CustomerID)
    BEGIN
        INSERT INTO dbo.Orders (
            Order_No, Customer_ID, Cart_ID, Total_Quantity_Product, 
            Total_Price, Payment, Date_Order, Time_Order, Status, Record_Status
        )
        VALUES (
            @OrderNo, @CustomerID, @CartID, @TotalQuantity,
            @TotalPrice, 'Cash', @DateOrder, @TimeOrder, 'Confirmed', 'Available'
        );
        
        -- 3. Tạo Order_Details
        INSERT INTO dbo.Orders_Details (
            Order_No, Customer_ID, Product_ID, Price, Sold_Quantity,
            Date_Order, Time_Order, Status, Record_Status
        )
        VALUES (
            @OrderNo, @CustomerID, @ProductID, @ProductPrice, @TotalQuantity,
            @DateOrder, @TimeOrder, 'Confirmed', 'Available'
        );
        
        -- 4. Tạo Bill_Exported
        IF NOT EXISTS (SELECT 1 FROM dbo.Bill_Exported WHERE Invoice_No = @InvoiceNo AND Admin_ID = @AdminID)
        BEGIN
            INSERT INTO dbo.Bill_Exported (
                Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product,
                VAT_Percent, VAT_Amount, Total_Amount, Status
            )
            VALUES (
                @InvoiceNo, @AdminID, @CustomerID, @OrderNo, @TotalQuantity,
                @VATPercent, @VATAmount, @TotalAmount, 'Available'
            );
            
            -- 5. Tạo Bill_Exported_Details
            SELECT @WarrantyMonths = ISNULL(Warranty_Months, 12) 
            FROM dbo.Product WHERE Product_ID = @ProductID;
            
            INSERT INTO dbo.Bill_Exported_Details (
                Invoice_No, Admin_ID, Product_ID,
                Unit_Price_Sell_Before, Unit_Price_Sell_After,
                Sold_Quantity, Discount_Percent,
                Total_Price_Before, Total_Price_After,
                Date_Exported, Time_Exported,
                Start_Date, End_Date, Status
            )
            VALUES (
                @InvoiceNo, @AdminID, @ProductID,
                @ProductPrice, @ProductPrice,
                @TotalQuantity, 0.00,
                @TotalPrice, @TotalPrice,
                @DateExported, @TimeExported,
                @DateExported, DATEADD(MONTH, @WarrantyMonths, @DateExported),
                'Available'
            );
        END
    END
    
    SET @Counter = @Counter + 1;
END

PRINT 'Đã tạo ' + CAST(@Counter - 1 AS varchar) + ' orders cho tháng 2/2025';

-- ============================================
-- TẠO DỮ LIỆU CHO THÁNG 3 (March 2025)
-- ============================================
PRINT '';
PRINT '=== TẠO DỮ LIỆU CHO THÁNG 3/2025 ===';

SET @Counter = 1;

-- Tạo 9 orders cho tháng 3
WHILE @Counter <= 9
BEGIN
    -- Tạo Order_No và Invoice_No
    -- Lấy Customer ngẫu nhiên (cần lấy trước để tạo mã)
    SELECT TOP 1 @CustomerID = Customer_ID 
    FROM dbo.Customer 
    WHERE Record_Status = 'Available'
    ORDER BY NEWID();
    
    -- Tạo Order_No ngẫu nhiên 8 chữ số (giống code Java)
    SET @OrderNo = FORMAT(ABS(CHECKSUM(NEWID())) % 100000000, '00000000');
    
    -- Tạo Invoice_No ngẫu nhiên 10 chữ số + "-" + Customer_ID (giống code Java)
    SET @InvoiceNo = FORMAT(ABS(CHECKSUM(NEWID())) % 1000000000, '0000000000') + '-' + @CustomerID;
    
    -- Ngày đặt hàng: tháng 3, ngày ngẫu nhiên từ 1-31
    SET @DateOrder = DATEFROMPARTS(2025, 3, 1 + (@Counter * 3) % 31);
    SET @TimeOrder = CAST(DATEADD(MINUTE, (@Counter * 47) % 1440, '00:00:00') AS time(7));
    
    -- Ngày xuất hóa đơn: cùng ngày hoặc ngày sau
    SET @DateExported = DATEADD(DAY, (@Counter % 2), @DateOrder);
    SET @TimeExported = CAST(DATEADD(MINUTE, (@Counter * 37) % 1440, '08:00:00') AS time(7));
    
    -- Tạo Cart_ID
    -- Tạo Cart_ID: CART- + timestamp + "-" + Customer_ID (giống code Java)
    -- Sử dụng DATEDIFF_BIG để lấy milliseconds từ epoch (1970-01-01) + một số ngẫu nhiên để tránh trùng
    SET @CartID = 'CART-' + CAST(DATEDIFF_BIG(MILLISECOND, '1970-01-01', GETDATE()) + ABS(CHECKSUM(NEWID())) % 1000 AS varchar) + '-' + @CustomerID;
    
    -- Lấy Customer ngẫu nhiên
    SELECT TOP 1 @CustomerID = Customer_ID 
    FROM dbo.Customer 
    WHERE Record_Status = 'Available'
    ORDER BY NEWID();
    
    -- Số lượng sản phẩm (1-4)
    SET @TotalQuantity = 1 + (@Counter % 4);
    
    -- Lấy Product ngẫu nhiên có đủ số lượng và có Warehouse_Item_ID
    SELECT TOP 1 
        @ProductID = Product_ID,
        @ProductPrice = ISNULL(Price, 0),
        @WarehouseItemID = Warehouse_Item_ID
    FROM dbo.Product 
    WHERE Status = 'Available' 
        AND Quantity >= @TotalQuantity
        AND Warehouse_Item_ID IS NOT NULL
    ORDER BY NEWID();
    
    -- Kiểm tra Product có đủ số lượng và có Warehouse_Item_ID
    IF @ProductID IS NOT NULL
    BEGIN
        -- Re-check quantity right before use to ensure it's still sufficient
        SELECT @CurrentQuantity = Quantity, @WarehouseItemID = Warehouse_Item_ID
        FROM dbo.Product WHERE Product_ID = @ProductID;
        
        IF @CurrentQuantity < @TotalQuantity OR @WarehouseItemID IS NULL
        BEGIN
            SET @ProductID = NULL;
        END
    END
    
    -- Nếu không tìm thấy Product đủ số lượng, bỏ qua order này
    IF @ProductID IS NULL
    BEGIN
        SET @Counter = @Counter + 1;
        CONTINUE;
    END
    
    -- Tính tổng tiền
    SET @TotalPrice = @ProductPrice * @TotalQuantity;
    SET @VATAmount = @TotalPrice * @VATPercent / 100.0;
    SET @TotalAmount = @TotalPrice + @VATAmount;
    
    -- 1. Tạo Cart (nếu cần)
    IF NOT EXISTS (SELECT 1 FROM dbo.Cart WHERE Cart_ID = @CartID)
    BEGIN
        INSERT INTO dbo.Cart (Cart_ID, Customer_ID, Product_ID, Quantity, Status)
        VALUES (@CartID, @CustomerID, @ProductID, @TotalQuantity, 'Available');
    END
    
    -- 2. Tạo Order
    IF NOT EXISTS (SELECT 1 FROM dbo.Orders WHERE Order_No = @OrderNo AND Customer_ID = @CustomerID)
    BEGIN
        INSERT INTO dbo.Orders (
            Order_No, Customer_ID, Cart_ID, Total_Quantity_Product, 
            Total_Price, Payment, Date_Order, Time_Order, Status, Record_Status
        )
        VALUES (
            @OrderNo, @CustomerID, @CartID, @TotalQuantity,
            @TotalPrice, 'Cash', @DateOrder, @TimeOrder, 'Confirmed', 'Available'
        );
        
        -- 3. Tạo Order_Details
        INSERT INTO dbo.Orders_Details (
            Order_No, Customer_ID, Product_ID, Price, Sold_Quantity,
            Date_Order, Time_Order, Status, Record_Status
        )
        VALUES (
            @OrderNo, @CustomerID, @ProductID, @ProductPrice, @TotalQuantity,
            @DateOrder, @TimeOrder, 'Confirmed', 'Available'
        );
        
        -- 4. Tạo Bill_Exported
        IF NOT EXISTS (SELECT 1 FROM dbo.Bill_Exported WHERE Invoice_No = @InvoiceNo AND Admin_ID = @AdminID)
        BEGIN
            INSERT INTO dbo.Bill_Exported (
                Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product,
                VAT_Percent, VAT_Amount, Total_Amount, Status
            )
            VALUES (
                @InvoiceNo, @AdminID, @CustomerID, @OrderNo, @TotalQuantity,
                @VATPercent, @VATAmount, @TotalAmount, 'Available'
            );
            
            -- 5. Tạo Bill_Exported_Details
            SELECT @WarrantyMonths = ISNULL(Warranty_Months, 12) 
            FROM dbo.Product WHERE Product_ID = @ProductID;
            
            INSERT INTO dbo.Bill_Exported_Details (
                Invoice_No, Admin_ID, Product_ID,
                Unit_Price_Sell_Before, Unit_Price_Sell_After,
                Sold_Quantity, Discount_Percent,
                Total_Price_Before, Total_Price_After,
                Date_Exported, Time_Exported,
                Start_Date, End_Date, Status
            )
            VALUES (
                @InvoiceNo, @AdminID, @ProductID,
                @ProductPrice, @ProductPrice,
                @TotalQuantity, 0.00,
                @TotalPrice, @TotalPrice,
                @DateExported, @TimeExported,
                @DateExported, DATEADD(MONTH, @WarrantyMonths, @DateExported),
                'Available'
            );
        END
    END
    
    SET @Counter = @Counter + 1;
END

PRINT 'Đã tạo ' + CAST(@Counter - 1 AS varchar) + ' orders cho tháng 3/2025';

-- ============================================
-- TẠO DỮ LIỆU CHO THÁNG 4 (April 2025)
-- ============================================
PRINT '';
PRINT '=== TẠO DỮ LIỆU CHO THÁNG 4/2025 ===';

SET @Counter = 1;

-- Tạo 8 orders cho tháng 4
WHILE @Counter <= 8
BEGIN
    -- Tạo Order_No và Invoice_No
    -- Lấy Customer ngẫu nhiên (cần lấy trước để tạo mã)
    SELECT TOP 1 @CustomerID = Customer_ID 
    FROM dbo.Customer 
    WHERE Record_Status = 'Available'
    ORDER BY NEWID();
    
    -- Tạo Order_No ngẫu nhiên 8 chữ số (giống code Java)
    SET @OrderNo = FORMAT(ABS(CHECKSUM(NEWID())) % 100000000, '00000000');
    
    -- Tạo Invoice_No ngẫu nhiên 10 chữ số + "-" + Customer_ID (giống code Java)
    SET @InvoiceNo = FORMAT(ABS(CHECKSUM(NEWID())) % 1000000000, '0000000000') + '-' + @CustomerID;
    
    -- Ngày đặt hàng: tháng 4, ngày ngẫu nhiên từ 1-30
    SET @DateOrder = DATEFROMPARTS(2025, 4, 1 + (@Counter * 3) % 30);
    SET @TimeOrder = CAST(DATEADD(MINUTE, (@Counter * 47) % 1440, '00:00:00') AS time(7));
    
    -- Ngày xuất hóa đơn: cùng ngày hoặc ngày sau
    SET @DateExported = DATEADD(DAY, (@Counter % 2), @DateOrder);
    SET @TimeExported = CAST(DATEADD(MINUTE, (@Counter * 37) % 1440, '08:00:00') AS time(7));
    
    -- Tạo Cart_ID
    -- Tạo Cart_ID: CART- + timestamp + "-" + Customer_ID (giống code Java)
    -- Sử dụng DATEDIFF_BIG để lấy milliseconds từ epoch (1970-01-01) + một số ngẫu nhiên để tránh trùng
    SET @CartID = 'CART-' + CAST(DATEDIFF_BIG(MILLISECOND, '1970-01-01', GETDATE()) + ABS(CHECKSUM(NEWID())) % 1000 AS varchar) + '-' + @CustomerID;
    
    -- Lấy Customer ngẫu nhiên
    SELECT TOP 1 @CustomerID = Customer_ID 
    FROM dbo.Customer 
    WHERE Record_Status = 'Available'
    ORDER BY NEWID();
    
    -- Số lượng sản phẩm (1-3)
    SET @TotalQuantity = 1 + (@Counter % 3);
    
    -- Lấy Product ngẫu nhiên có đủ số lượng và có Warehouse_Item_ID
    SELECT TOP 1 
        @ProductID = Product_ID,
        @ProductPrice = ISNULL(Price, 0),
        @WarehouseItemID = Warehouse_Item_ID
    FROM dbo.Product 
    WHERE Status = 'Available' 
        AND Quantity >= @TotalQuantity
        AND Warehouse_Item_ID IS NOT NULL
    ORDER BY NEWID();
    
    -- Nếu không tìm thấy Product đủ số lượng, bỏ qua order này
    IF @ProductID IS NULL
    BEGIN
        SET @Counter = @Counter + 1;
        CONTINUE;
    END
    
    -- Tính tổng tiền
    SET @TotalPrice = @ProductPrice * @TotalQuantity;
    SET @VATAmount = @TotalPrice * @VATPercent / 100.0;
    SET @TotalAmount = @TotalPrice + @VATAmount;
    
    -- 1. Tạo Cart (nếu cần)
    IF NOT EXISTS (SELECT 1 FROM dbo.Cart WHERE Cart_ID = @CartID)
    BEGIN
        INSERT INTO dbo.Cart (Cart_ID, Customer_ID, Product_ID, Quantity, Status)
        VALUES (@CartID, @CustomerID, @ProductID, @TotalQuantity, 'Available');
    END
    
    -- 2. Tạo Order
    IF NOT EXISTS (SELECT 1 FROM dbo.Orders WHERE Order_No = @OrderNo AND Customer_ID = @CustomerID)
    BEGIN
        INSERT INTO dbo.Orders (
            Order_No, Customer_ID, Cart_ID, Total_Quantity_Product, 
            Total_Price, Payment, Date_Order, Time_Order, Status, Record_Status
        )
        VALUES (
            @OrderNo, @CustomerID, @CartID, @TotalQuantity,
            @TotalPrice, 'Cash', @DateOrder, @TimeOrder, 'Confirmed', 'Available'
        );
        
        -- 3. Tạo Order_Details
        INSERT INTO dbo.Orders_Details (
            Order_No, Customer_ID, Product_ID, Price, Sold_Quantity,
            Date_Order, Time_Order, Status, Record_Status
        )
        VALUES (
            @OrderNo, @CustomerID, @ProductID, @ProductPrice, @TotalQuantity,
            @DateOrder, @TimeOrder, 'Confirmed', 'Available'
        );
        
        -- 4. Tạo Bill_Exported
        IF NOT EXISTS (SELECT 1 FROM dbo.Bill_Exported WHERE Invoice_No = @InvoiceNo AND Admin_ID = @AdminID)
        BEGIN
            INSERT INTO dbo.Bill_Exported (
                Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product,
                VAT_Percent, VAT_Amount, Total_Amount, Status
            )
            VALUES (
                @InvoiceNo, @AdminID, @CustomerID, @OrderNo, @TotalQuantity,
                @VATPercent, @VATAmount, @TotalAmount, 'Available'
            );
            
            -- 5. Tạo Bill_Exported_Details
            SELECT @WarrantyMonths = ISNULL(Warranty_Months, 12) 
            FROM dbo.Product WHERE Product_ID = @ProductID;
            
            INSERT INTO dbo.Bill_Exported_Details (
                Invoice_No, Admin_ID, Product_ID,
                Unit_Price_Sell_Before, Unit_Price_Sell_After,
                Sold_Quantity, Discount_Percent,
                Total_Price_Before, Total_Price_After,
                Date_Exported, Time_Exported,
                Start_Date, End_Date, Status
            )
            VALUES (
                @InvoiceNo, @AdminID, @ProductID,
                @ProductPrice, @ProductPrice,
                @TotalQuantity, 0.00,
                @TotalPrice, @TotalPrice,
                @DateExported, @TimeExported,
                @DateExported, DATEADD(MONTH, @WarrantyMonths, @DateExported),
                'Available'
            );
        END
    END
    
    SET @Counter = @Counter + 1;
END

PRINT 'Đã tạo ' + CAST(@Counter - 1 AS varchar) + ' orders cho tháng 4/2025';

-- ============================================
-- TẠO DỮ LIỆU CHO THÁNG 5 (May 2025)
-- ============================================
PRINT '';
PRINT '=== TẠO DỮ LIỆU CHO THÁNG 5/2025 ===';

SET @Counter = 1;

-- Tạo 10 orders cho tháng 5
WHILE @Counter <= 10
BEGIN
    -- Tạo Order_No và Invoice_No
    -- Lấy Customer ngẫu nhiên (cần lấy trước để tạo mã)
    SELECT TOP 1 @CustomerID = Customer_ID 
    FROM dbo.Customer 
    WHERE Record_Status = 'Available'
    ORDER BY NEWID();
    
    -- Tạo Order_No ngẫu nhiên 8 chữ số (giống code Java)
    SET @OrderNo = FORMAT(ABS(CHECKSUM(NEWID())) % 100000000, '00000000');
    
    -- Tạo Invoice_No ngẫu nhiên 10 chữ số + "-" + Customer_ID (giống code Java)
    SET @InvoiceNo = FORMAT(ABS(CHECKSUM(NEWID())) % 1000000000, '0000000000') + '-' + @CustomerID;
    
    -- Ngày đặt hàng: tháng 5, ngày ngẫu nhiên từ 1-31
    SET @DateOrder = DATEFROMPARTS(2025, 5, 1 + (@Counter * 3) % 31);
    SET @TimeOrder = CAST(DATEADD(MINUTE, (@Counter * 47) % 1440, '00:00:00') AS time(7));
    
    -- Ngày xuất hóa đơn: cùng ngày hoặc ngày sau
    SET @DateExported = DATEADD(DAY, (@Counter % 2), @DateOrder);
    SET @TimeExported = CAST(DATEADD(MINUTE, (@Counter * 37) % 1440, '08:00:00') AS time(7));
    
    -- Tạo Cart_ID
    -- Tạo Cart_ID: CART- + timestamp + "-" + Customer_ID (giống code Java)
    -- Sử dụng DATEDIFF_BIG để lấy milliseconds từ epoch (1970-01-01) + một số ngẫu nhiên để tránh trùng
    SET @CartID = 'CART-' + CAST(DATEDIFF_BIG(MILLISECOND, '1970-01-01', GETDATE()) + ABS(CHECKSUM(NEWID())) % 1000 AS varchar) + '-' + @CustomerID;
    
    -- Lấy Customer ngẫu nhiên
    SELECT TOP 1 @CustomerID = Customer_ID 
    FROM dbo.Customer 
    WHERE Record_Status = 'Available'
    ORDER BY NEWID();
    
    -- Số lượng sản phẩm (1-4)
    SET @TotalQuantity = 1 + (@Counter % 4);
    
    -- Lấy Product ngẫu nhiên có đủ số lượng và có Warehouse_Item_ID
    SELECT TOP 1 
        @ProductID = Product_ID,
        @ProductPrice = ISNULL(Price, 0),
        @WarehouseItemID = Warehouse_Item_ID
    FROM dbo.Product 
    WHERE Status = 'Available' 
        AND Quantity >= @TotalQuantity
        AND Warehouse_Item_ID IS NOT NULL
    ORDER BY NEWID();
    
    -- Kiểm tra Product có đủ số lượng và có Warehouse_Item_ID
    IF @ProductID IS NOT NULL
    BEGIN
        -- Re-check quantity right before use to ensure it's still sufficient
        SELECT @CurrentQuantity = Quantity, @WarehouseItemID = Warehouse_Item_ID
        FROM dbo.Product WHERE Product_ID = @ProductID;
        
        IF @CurrentQuantity < @TotalQuantity OR @WarehouseItemID IS NULL
        BEGIN
            SET @ProductID = NULL;
        END
    END
    
    -- Nếu không tìm thấy Product đủ số lượng, bỏ qua order này
    IF @ProductID IS NULL
    BEGIN
        SET @Counter = @Counter + 1;
        CONTINUE;
    END
    
    -- Tính tổng tiền
    SET @TotalPrice = @ProductPrice * @TotalQuantity;
    SET @VATAmount = @TotalPrice * @VATPercent / 100.0;
    SET @TotalAmount = @TotalPrice + @VATAmount;
    
    -- 1. Tạo Cart (nếu cần)
    IF NOT EXISTS (SELECT 1 FROM dbo.Cart WHERE Cart_ID = @CartID)
    BEGIN
        INSERT INTO dbo.Cart (Cart_ID, Customer_ID, Product_ID, Quantity, Status)
        VALUES (@CartID, @CustomerID, @ProductID, @TotalQuantity, 'Available');
    END
    
    -- 2. Tạo Order
    IF NOT EXISTS (SELECT 1 FROM dbo.Orders WHERE Order_No = @OrderNo AND Customer_ID = @CustomerID)
    BEGIN
        INSERT INTO dbo.Orders (
            Order_No, Customer_ID, Cart_ID, Total_Quantity_Product, 
            Total_Price, Payment, Date_Order, Time_Order, Status, Record_Status
        )
        VALUES (
            @OrderNo, @CustomerID, @CartID, @TotalQuantity,
            @TotalPrice, 'Cash', @DateOrder, @TimeOrder, 'Confirmed', 'Available'
        );
        
        -- 3. Tạo Order_Details
        INSERT INTO dbo.Orders_Details (
            Order_No, Customer_ID, Product_ID, Price, Sold_Quantity,
            Date_Order, Time_Order, Status, Record_Status
        )
        VALUES (
            @OrderNo, @CustomerID, @ProductID, @ProductPrice, @TotalQuantity,
            @DateOrder, @TimeOrder, 'Confirmed', 'Available'
        );
        
        -- 4. Tạo Bill_Exported
        IF NOT EXISTS (SELECT 1 FROM dbo.Bill_Exported WHERE Invoice_No = @InvoiceNo AND Admin_ID = @AdminID)
        BEGIN
            INSERT INTO dbo.Bill_Exported (
                Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product,
                VAT_Percent, VAT_Amount, Total_Amount, Status
            )
            VALUES (
                @InvoiceNo, @AdminID, @CustomerID, @OrderNo, @TotalQuantity,
                @VATPercent, @VATAmount, @TotalAmount, 'Available'
            );
            
            -- 5. Tạo Bill_Exported_Details
            SELECT @WarrantyMonths = ISNULL(Warranty_Months, 12) 
            FROM dbo.Product WHERE Product_ID = @ProductID;
            
            INSERT INTO dbo.Bill_Exported_Details (
                Invoice_No, Admin_ID, Product_ID,
                Unit_Price_Sell_Before, Unit_Price_Sell_After,
                Sold_Quantity, Discount_Percent,
                Total_Price_Before, Total_Price_After,
                Date_Exported, Time_Exported,
                Start_Date, End_Date, Status
            )
            VALUES (
                @InvoiceNo, @AdminID, @ProductID,
                @ProductPrice, @ProductPrice,
                @TotalQuantity, 0.00,
                @TotalPrice, @TotalPrice,
                @DateExported, @TimeExported,
                @DateExported, DATEADD(MONTH, @WarrantyMonths, @DateExported),
                'Available'
            );
        END
    END
    
    SET @Counter = @Counter + 1;
END

PRINT 'Đã tạo ' + CAST(@Counter - 1 AS varchar) + ' orders cho tháng 5/2025';

PRINT '';
PRINT '=== HOÀN TẤT TẠO DỮ LIỆU CHO NĂM 2025 (Tháng 1-5) ===';

-- ============================================
-- TẠO DỮ LIỆU CHO THÁNG 6 (June 2025)
-- ============================================
PRINT '';
PRINT '=== TẠO DỮ LIỆU CHO THÁNG 6/2025 ===';

SET @Counter = 1;

-- Tạo 9 orders cho tháng 6
WHILE @Counter <= 9
BEGIN
    -- Lấy Customer ngẫu nhiên (cần lấy trước để tạo mã)
    SELECT TOP 1 @CustomerID = Customer_ID 
    FROM dbo.Customer 
    WHERE Record_Status = 'Available'
    ORDER BY NEWID();
    
    -- Tạo Order_No ngẫu nhiên 8 chữ số (giống code Java)
    SET @OrderNo = FORMAT(ABS(CHECKSUM(NEWID())) % 100000000, '00000000');
    
    -- Tạo Invoice_No ngẫu nhiên 10 chữ số + "-" + Customer_ID (giống code Java)
    SET @InvoiceNo = FORMAT(ABS(CHECKSUM(NEWID())) % 1000000000, '0000000000') + '-' + @CustomerID;
    SET @DateOrder = DATEFROMPARTS(2025, 6, 1 + (@Counter * 3) % 30);
    SET @TimeOrder = CAST(DATEADD(MINUTE, (@Counter * 47) % 1440, '00:00:00') AS time(7));
    SET @DateExported = DATEADD(DAY, (@Counter % 2), @DateOrder);
    SET @TimeExported = CAST(DATEADD(MINUTE, (@Counter * 37) % 1440, '08:00:00') AS time(7));
    -- Tạo Cart_ID: CART- + timestamp + "-" + Customer_ID (giống code Java)
    -- Sử dụng DATEDIFF_BIG để lấy milliseconds từ epoch (1970-01-01) + một số ngẫu nhiên để tránh trùng
    SET @CartID = 'CART-' + CAST(DATEDIFF_BIG(MILLISECOND, '1970-01-01', GETDATE()) + ABS(CHECKSUM(NEWID())) % 1000 AS varchar) + '-' + @CustomerID;
    
    SET @TotalQuantity = 1 + (@Counter % 3);
    
    SELECT TOP 1 @ProductID = Product_ID, @ProductPrice = ISNULL(Price, 0), @WarehouseItemID = Warehouse_Item_ID
    FROM dbo.Product 
    WHERE Status = 'Available' 
        AND Quantity >= @TotalQuantity
        AND Warehouse_Item_ID IS NOT NULL
    ORDER BY NEWID();
    
    -- Kiểm tra Product có đủ số lượng
    IF @ProductID IS NOT NULL
    BEGIN
        SELECT @CurrentQuantity = Quantity, @WarehouseItemID = Warehouse_Item_ID
        FROM dbo.Product WHERE Product_ID = @ProductID;
        
        IF @CurrentQuantity < @TotalQuantity OR @WarehouseItemID IS NULL
        BEGIN
            SET @ProductID = NULL;
        END
    END
    
    -- Nếu không tìm thấy Product đủ số lượng, bỏ qua order này
    IF @ProductID IS NULL
    BEGIN
        SET @Counter = @Counter + 1;
        CONTINUE;
    END
    
    SET @TotalPrice = @ProductPrice * @TotalQuantity;
    SET @VATAmount = @TotalPrice * @VATPercent / 100.0;
    SET @TotalAmount = @TotalPrice + @VATAmount;
    
    IF NOT EXISTS (SELECT 1 FROM dbo.Cart WHERE Cart_ID = @CartID)
    BEGIN
        INSERT INTO dbo.Cart (Cart_ID, Customer_ID, Product_ID, Quantity, Status)
        VALUES (@CartID, @CustomerID, @ProductID, @TotalQuantity, 'Available');
    END
    
    IF NOT EXISTS (SELECT 1 FROM dbo.Orders WHERE Order_No = @OrderNo AND Customer_ID = @CustomerID)
    BEGIN
        INSERT INTO dbo.Orders (Order_No, Customer_ID, Cart_ID, Total_Quantity_Product, Total_Price, Payment, Date_Order, Time_Order, Status, Record_Status)
        VALUES (@OrderNo, @CustomerID, @CartID, @TotalQuantity, @TotalPrice, 'Cash', @DateOrder, @TimeOrder, 'Confirmed', 'Available');
        
        INSERT INTO dbo.Orders_Details (Order_No, Customer_ID, Product_ID, Price, Sold_Quantity, Date_Order, Time_Order, Status, Record_Status)
        VALUES (@OrderNo, @CustomerID, @ProductID, @ProductPrice, @TotalQuantity, @DateOrder, @TimeOrder, 'Confirmed', 'Available');
        
        IF NOT EXISTS (SELECT 1 FROM dbo.Bill_Exported WHERE Invoice_No = @InvoiceNo AND Admin_ID = @AdminID)
        BEGIN
            INSERT INTO dbo.Bill_Exported (Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product, VAT_Percent, VAT_Amount, Total_Amount, Status)
            VALUES (@InvoiceNo, @AdminID, @CustomerID, @OrderNo, @TotalQuantity, @VATPercent, @VATAmount, @TotalAmount, 'Available');
            
            SELECT @WarrantyMonths = ISNULL(Warranty_Months, 12) FROM dbo.Product WHERE Product_ID = @ProductID;
            
            INSERT INTO dbo.Bill_Exported_Details (Invoice_No, Admin_ID, Product_ID, Unit_Price_Sell_Before, Unit_Price_Sell_After, Sold_Quantity, Discount_Percent, Total_Price_Before, Total_Price_After, Date_Exported, Time_Exported, Start_Date, End_Date, Status)
            VALUES (@InvoiceNo, @AdminID, @ProductID, @ProductPrice, @ProductPrice, @TotalQuantity, 0.00, @TotalPrice, @TotalPrice, @DateExported, @TimeExported, @DateExported, DATEADD(MONTH, @WarrantyMonths, @DateExported), 'Available');
        END
    END
    
    SET @Counter = @Counter + 1;
END

PRINT 'Đã tạo ' + CAST(@Counter - 1 AS varchar) + ' orders cho tháng 6/2025';

-- ============================================
-- TẠO DỮ LIỆU CHO THÁNG 7 (July 2025)
-- ============================================
PRINT '';
PRINT '=== TẠO DỮ LIỆU CHO THÁNG 7/2025 ===';

SET @Counter = 1;

-- Tạo 8 orders cho tháng 7
WHILE @Counter <= 8
BEGIN
    -- Lấy Customer ngẫu nhiên (cần lấy trước để tạo mã)
    SELECT TOP 1 @CustomerID = Customer_ID 
    FROM dbo.Customer 
    WHERE Record_Status = 'Available'
    ORDER BY NEWID();
    
    -- Tạo Order_No ngẫu nhiên 8 chữ số (giống code Java)
    SET @OrderNo = FORMAT(ABS(CHECKSUM(NEWID())) % 100000000, '00000000');
    
    -- Tạo Invoice_No ngẫu nhiên 10 chữ số + "-" + Customer_ID (giống code Java)
    SET @InvoiceNo = FORMAT(ABS(CHECKSUM(NEWID())) % 1000000000, '0000000000') + '-' + @CustomerID;
    SET @DateOrder = DATEFROMPARTS(2025, 7, 1 + (@Counter * 4) % 31);
    SET @TimeOrder = CAST(DATEADD(MINUTE, (@Counter * 47) % 1440, '00:00:00') AS time(7));
    SET @DateExported = DATEADD(DAY, (@Counter % 2), @DateOrder);
    SET @TimeExported = CAST(DATEADD(MINUTE, (@Counter * 37) % 1440, '08:00:00') AS time(7));
    -- Tạo Cart_ID: CART- + timestamp + "-" + Customer_ID (giống code Java)
    -- Sử dụng DATEDIFF_BIG để lấy milliseconds từ epoch (1970-01-01) + một số ngẫu nhiên để tránh trùng
    SET @CartID = 'CART-' + CAST(DATEDIFF_BIG(MILLISECOND, '1970-01-01', GETDATE()) + ABS(CHECKSUM(NEWID())) % 1000 AS varchar) + '-' + @CustomerID;
    
    SET @TotalQuantity = 1 + (@Counter % 3);
    
    SELECT TOP 1 @ProductID = Product_ID, @ProductPrice = ISNULL(Price, 0), @WarehouseItemID = Warehouse_Item_ID
    FROM dbo.Product 
    WHERE Status = 'Available' 
        AND Quantity >= @TotalQuantity
        AND Warehouse_Item_ID IS NOT NULL
    ORDER BY NEWID();
    
    -- Kiểm tra Product có đủ số lượng
    IF @ProductID IS NOT NULL
    BEGIN
        SELECT @CurrentQuantity = Quantity, @WarehouseItemID = Warehouse_Item_ID
        FROM dbo.Product WHERE Product_ID = @ProductID;
        
        IF @CurrentQuantity < @TotalQuantity OR @WarehouseItemID IS NULL
        BEGIN
            SET @ProductID = NULL;
        END
    END
    
    -- Nếu không tìm thấy Product đủ số lượng, bỏ qua order này
    IF @ProductID IS NULL
    BEGIN
        SET @Counter = @Counter + 1;
        CONTINUE;
    END
    
    SET @TotalPrice = @ProductPrice * @TotalQuantity;
    SET @VATAmount = @TotalPrice * @VATPercent / 100.0;
    SET @TotalAmount = @TotalPrice + @VATAmount;
    
    IF NOT EXISTS (SELECT 1 FROM dbo.Cart WHERE Cart_ID = @CartID)
    BEGIN
        INSERT INTO dbo.Cart (Cart_ID, Customer_ID, Product_ID, Quantity, Status)
        VALUES (@CartID, @CustomerID, @ProductID, @TotalQuantity, 'Available');
    END
    
    IF NOT EXISTS (SELECT 1 FROM dbo.Orders WHERE Order_No = @OrderNo AND Customer_ID = @CustomerID)
    BEGIN
        INSERT INTO dbo.Orders (Order_No, Customer_ID, Cart_ID, Total_Quantity_Product, Total_Price, Payment, Date_Order, Time_Order, Status, Record_Status)
        VALUES (@OrderNo, @CustomerID, @CartID, @TotalQuantity, @TotalPrice, 'Cash', @DateOrder, @TimeOrder, 'Confirmed', 'Available');
        
        INSERT INTO dbo.Orders_Details (Order_No, Customer_ID, Product_ID, Price, Sold_Quantity, Date_Order, Time_Order, Status, Record_Status)
        VALUES (@OrderNo, @CustomerID, @ProductID, @ProductPrice, @TotalQuantity, @DateOrder, @TimeOrder, 'Confirmed', 'Available');
        
        IF NOT EXISTS (SELECT 1 FROM dbo.Bill_Exported WHERE Invoice_No = @InvoiceNo AND Admin_ID = @AdminID)
        BEGIN
            INSERT INTO dbo.Bill_Exported (Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product, VAT_Percent, VAT_Amount, Total_Amount, Status)
            VALUES (@InvoiceNo, @AdminID, @CustomerID, @OrderNo, @TotalQuantity, @VATPercent, @VATAmount, @TotalAmount, 'Available');
            
            SELECT @WarrantyMonths = ISNULL(Warranty_Months, 12) FROM dbo.Product WHERE Product_ID = @ProductID;
            
            INSERT INTO dbo.Bill_Exported_Details (Invoice_No, Admin_ID, Product_ID, Unit_Price_Sell_Before, Unit_Price_Sell_After, Sold_Quantity, Discount_Percent, Total_Price_Before, Total_Price_After, Date_Exported, Time_Exported, Start_Date, End_Date, Status)
            VALUES (@InvoiceNo, @AdminID, @ProductID, @ProductPrice, @ProductPrice, @TotalQuantity, 0.00, @TotalPrice, @TotalPrice, @DateExported, @TimeExported, @DateExported, DATEADD(MONTH, @WarrantyMonths, @DateExported), 'Available');
        END
    END
    
    SET @Counter = @Counter + 1;
END

PRINT 'Đã tạo ' + CAST(@Counter - 1 AS varchar) + ' orders cho tháng 7/2025';

-- ============================================
-- TẠO DỮ LIỆU CHO THÁNG 8 (August 2025)
-- ============================================
PRINT '';
PRINT '=== TẠO DỮ LIỆU CHO THÁNG 8/2025 ===';

SET @Counter = 1;

-- Tạo 7 orders cho tháng 8
WHILE @Counter <= 7
BEGIN
    -- Lấy Customer ngẫu nhiên (cần lấy trước để tạo mã)
    SELECT TOP 1 @CustomerID = Customer_ID 
    FROM dbo.Customer 
    WHERE Record_Status = 'Available'
    ORDER BY NEWID();
    
    -- Tạo Order_No ngẫu nhiên 8 chữ số (giống code Java)
    SET @OrderNo = FORMAT(ABS(CHECKSUM(NEWID())) % 100000000, '00000000');
    
    -- Tạo Invoice_No ngẫu nhiên 10 chữ số + "-" + Customer_ID (giống code Java)
    SET @InvoiceNo = FORMAT(ABS(CHECKSUM(NEWID())) % 1000000000, '0000000000') + '-' + @CustomerID;
    SET @DateOrder = DATEFROMPARTS(2025, 8, 1 + (@Counter * 4) % 31);
    SET @TimeOrder = CAST(DATEADD(MINUTE, (@Counter * 47) % 1440, '00:00:00') AS time(7));
    SET @DateExported = DATEADD(DAY, (@Counter % 2), @DateOrder);
    SET @TimeExported = CAST(DATEADD(MINUTE, (@Counter * 37) % 1440, '08:00:00') AS time(7));
    -- Tạo Cart_ID: CART- + timestamp + "-" + Customer_ID (giống code Java)
    -- Sử dụng DATEDIFF_BIG để lấy milliseconds từ epoch (1970-01-01) + một số ngẫu nhiên để tránh trùng
    SET @CartID = 'CART-' + CAST(DATEDIFF_BIG(MILLISECOND, '1970-01-01', GETDATE()) + ABS(CHECKSUM(NEWID())) % 1000 AS varchar) + '-' + @CustomerID;
    
    SET @TotalQuantity = 1 + (@Counter % 3);
    
    SELECT TOP 1 @ProductID = Product_ID, @ProductPrice = ISNULL(Price, 0), @WarehouseItemID = Warehouse_Item_ID
    FROM dbo.Product 
    WHERE Status = 'Available' 
        AND Quantity >= @TotalQuantity
        AND Warehouse_Item_ID IS NOT NULL
    ORDER BY NEWID();
    
    -- Kiểm tra Product có đủ số lượng
    IF @ProductID IS NOT NULL
    BEGIN
        SELECT @CurrentQuantity = Quantity, @WarehouseItemID = Warehouse_Item_ID
        FROM dbo.Product WHERE Product_ID = @ProductID;
        
        IF @CurrentQuantity < @TotalQuantity OR @WarehouseItemID IS NULL
        BEGIN
            SET @ProductID = NULL;
        END
    END
    
    -- Nếu không tìm thấy Product đủ số lượng, bỏ qua order này
    IF @ProductID IS NULL
    BEGIN
        SET @Counter = @Counter + 1;
        CONTINUE;
    END
    
    SET @TotalPrice = @ProductPrice * @TotalQuantity;
    SET @VATAmount = @TotalPrice * @VATPercent / 100.0;
    SET @TotalAmount = @TotalPrice + @VATAmount;
    
    IF NOT EXISTS (SELECT 1 FROM dbo.Cart WHERE Cart_ID = @CartID)
    BEGIN
        INSERT INTO dbo.Cart (Cart_ID, Customer_ID, Product_ID, Quantity, Status)
        VALUES (@CartID, @CustomerID, @ProductID, @TotalQuantity, 'Available');
    END
    
    IF NOT EXISTS (SELECT 1 FROM dbo.Orders WHERE Order_No = @OrderNo AND Customer_ID = @CustomerID)
    BEGIN
        INSERT INTO dbo.Orders (Order_No, Customer_ID, Cart_ID, Total_Quantity_Product, Total_Price, Payment, Date_Order, Time_Order, Status, Record_Status)
        VALUES (@OrderNo, @CustomerID, @CartID, @TotalQuantity, @TotalPrice, 'Cash', @DateOrder, @TimeOrder, 'Confirmed', 'Available');
        
        INSERT INTO dbo.Orders_Details (Order_No, Customer_ID, Product_ID, Price, Sold_Quantity, Date_Order, Time_Order, Status, Record_Status)
        VALUES (@OrderNo, @CustomerID, @ProductID, @ProductPrice, @TotalQuantity, @DateOrder, @TimeOrder, 'Confirmed', 'Available');
        
        IF NOT EXISTS (SELECT 1 FROM dbo.Bill_Exported WHERE Invoice_No = @InvoiceNo AND Admin_ID = @AdminID)
        BEGIN
            INSERT INTO dbo.Bill_Exported (Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product, VAT_Percent, VAT_Amount, Total_Amount, Status)
            VALUES (@InvoiceNo, @AdminID, @CustomerID, @OrderNo, @TotalQuantity, @VATPercent, @VATAmount, @TotalAmount, 'Available');
            
            SELECT @WarrantyMonths = ISNULL(Warranty_Months, 12) FROM dbo.Product WHERE Product_ID = @ProductID;
            
            INSERT INTO dbo.Bill_Exported_Details (Invoice_No, Admin_ID, Product_ID, Unit_Price_Sell_Before, Unit_Price_Sell_After, Sold_Quantity, Discount_Percent, Total_Price_Before, Total_Price_After, Date_Exported, Time_Exported, Start_Date, End_Date, Status)
            VALUES (@InvoiceNo, @AdminID, @ProductID, @ProductPrice, @ProductPrice, @TotalQuantity, 0.00, @TotalPrice, @TotalPrice, @DateExported, @TimeExported, @DateExported, DATEADD(MONTH, @WarrantyMonths, @DateExported), 'Available');
        END
    END
    
    SET @Counter = @Counter + 1;
END

PRINT 'Đã tạo ' + CAST(@Counter - 1 AS varchar) + ' orders cho tháng 8/2025';

-- ============================================
-- TẠO DỮ LIỆU CHO THÁNG 9 (September 2025)
-- ============================================
PRINT '';
PRINT '=== TẠO DỮ LIỆU CHO THÁNG 9/2025 ===';

SET @Counter = 1;

-- Tạo 8 orders cho tháng 9
WHILE @Counter <= 8
BEGIN
    -- Lấy Customer ngẫu nhiên (cần lấy trước để tạo mã)
    SELECT TOP 1 @CustomerID = Customer_ID 
    FROM dbo.Customer 
    WHERE Record_Status = 'Available'
    ORDER BY NEWID();
    
    -- Tạo Order_No ngẫu nhiên 8 chữ số (giống code Java)
    SET @OrderNo = FORMAT(ABS(CHECKSUM(NEWID())) % 100000000, '00000000');
    
    -- Tạo Invoice_No ngẫu nhiên 10 chữ số + "-" + Customer_ID (giống code Java)
    SET @InvoiceNo = FORMAT(ABS(CHECKSUM(NEWID())) % 1000000000, '0000000000') + '-' + @CustomerID;
    SET @DateOrder = DATEFROMPARTS(2025, 9, 1 + (@Counter * 3) % 30);
    SET @TimeOrder = CAST(DATEADD(MINUTE, (@Counter * 47) % 1440, '00:00:00') AS time(7));
    SET @DateExported = DATEADD(DAY, (@Counter % 2), @DateOrder);
    SET @TimeExported = CAST(DATEADD(MINUTE, (@Counter * 37) % 1440, '08:00:00') AS time(7));
    -- Tạo Cart_ID: CART- + timestamp + "-" + Customer_ID (giống code Java)
    -- Sử dụng DATEDIFF_BIG để lấy milliseconds từ epoch (1970-01-01) + một số ngẫu nhiên để tránh trùng
    SET @CartID = 'CART-' + CAST(DATEDIFF_BIG(MILLISECOND, '1970-01-01', GETDATE()) + ABS(CHECKSUM(NEWID())) % 1000 AS varchar) + '-' + @CustomerID;
    
    SET @TotalQuantity = 1 + (@Counter % 3);
    
    SELECT TOP 1 @ProductID = Product_ID, @ProductPrice = ISNULL(Price, 0), @WarehouseItemID = Warehouse_Item_ID
    FROM dbo.Product 
    WHERE Status = 'Available' 
        AND Quantity >= @TotalQuantity
        AND Warehouse_Item_ID IS NOT NULL
    ORDER BY NEWID();
    
    -- Kiểm tra Product có đủ số lượng
    IF @ProductID IS NOT NULL
    BEGIN
        SELECT @CurrentQuantity = Quantity, @WarehouseItemID = Warehouse_Item_ID
        FROM dbo.Product WHERE Product_ID = @ProductID;
        
        IF @CurrentQuantity < @TotalQuantity OR @WarehouseItemID IS NULL
        BEGIN
            SET @ProductID = NULL;
        END
    END
    
    -- Nếu không tìm thấy Product đủ số lượng, bỏ qua order này
    IF @ProductID IS NULL
    BEGIN
        SET @Counter = @Counter + 1;
        CONTINUE;
    END
    
    SET @TotalPrice = @ProductPrice * @TotalQuantity;
    SET @VATAmount = @TotalPrice * @VATPercent / 100.0;
    SET @TotalAmount = @TotalPrice + @VATAmount;
    
    IF NOT EXISTS (SELECT 1 FROM dbo.Cart WHERE Cart_ID = @CartID)
    BEGIN
        INSERT INTO dbo.Cart (Cart_ID, Customer_ID, Product_ID, Quantity, Status)
        VALUES (@CartID, @CustomerID, @ProductID, @TotalQuantity, 'Available');
    END
    
    IF NOT EXISTS (SELECT 1 FROM dbo.Orders WHERE Order_No = @OrderNo AND Customer_ID = @CustomerID)
    BEGIN
        INSERT INTO dbo.Orders (Order_No, Customer_ID, Cart_ID, Total_Quantity_Product, Total_Price, Payment, Date_Order, Time_Order, Status, Record_Status)
        VALUES (@OrderNo, @CustomerID, @CartID, @TotalQuantity, @TotalPrice, 'Cash', @DateOrder, @TimeOrder, 'Confirmed', 'Available');
        
        INSERT INTO dbo.Orders_Details (Order_No, Customer_ID, Product_ID, Price, Sold_Quantity, Date_Order, Time_Order, Status, Record_Status)
        VALUES (@OrderNo, @CustomerID, @ProductID, @ProductPrice, @TotalQuantity, @DateOrder, @TimeOrder, 'Confirmed', 'Available');
        
        IF NOT EXISTS (SELECT 1 FROM dbo.Bill_Exported WHERE Invoice_No = @InvoiceNo AND Admin_ID = @AdminID)
        BEGIN
            INSERT INTO dbo.Bill_Exported (Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product, VAT_Percent, VAT_Amount, Total_Amount, Status)
            VALUES (@InvoiceNo, @AdminID, @CustomerID, @OrderNo, @TotalQuantity, @VATPercent, @VATAmount, @TotalAmount, 'Available');
            
            SELECT @WarrantyMonths = ISNULL(Warranty_Months, 12) FROM dbo.Product WHERE Product_ID = @ProductID;
            
            INSERT INTO dbo.Bill_Exported_Details (Invoice_No, Admin_ID, Product_ID, Unit_Price_Sell_Before, Unit_Price_Sell_After, Sold_Quantity, Discount_Percent, Total_Price_Before, Total_Price_After, Date_Exported, Time_Exported, Start_Date, End_Date, Status)
            VALUES (@InvoiceNo, @AdminID, @ProductID, @ProductPrice, @ProductPrice, @TotalQuantity, 0.00, @TotalPrice, @TotalPrice, @DateExported, @TimeExported, @DateExported, DATEADD(MONTH, @WarrantyMonths, @DateExported), 'Available');
        END
    END
    
    SET @Counter = @Counter + 1;
END

PRINT 'Đã tạo ' + CAST(@Counter - 1 AS varchar) + ' orders cho tháng 9/2025';

-- ============================================
-- TẠO DỮ LIỆU CHO THÁNG 10 (October 2025)
-- ============================================
PRINT '';
PRINT '=== TẠO DỮ LIỆU CHO THÁNG 10/2025 ===';

SET @Counter = 1;

-- Tạo 10 orders cho tháng 10
WHILE @Counter <= 10
BEGIN
    -- Lấy Customer ngẫu nhiên (cần lấy trước để tạo mã)
    SELECT TOP 1 @CustomerID = Customer_ID 
    FROM dbo.Customer 
    WHERE Record_Status = 'Available'
    ORDER BY NEWID();
    
    -- Tạo Order_No ngẫu nhiên 8 chữ số (giống code Java)
    SET @OrderNo = FORMAT(ABS(CHECKSUM(NEWID())) % 100000000, '00000000');
    
    -- Tạo Invoice_No ngẫu nhiên 10 chữ số + "-" + Customer_ID (giống code Java)
    SET @InvoiceNo = FORMAT(ABS(CHECKSUM(NEWID())) % 1000000000, '0000000000') + '-' + @CustomerID;
    SET @DateOrder = DATEFROMPARTS(2025, 10, 1 + (@Counter * 3) % 31);
    SET @TimeOrder = CAST(DATEADD(MINUTE, (@Counter * 47) % 1440, '00:00:00') AS time(7));
    SET @DateExported = DATEADD(DAY, (@Counter % 2), @DateOrder);
    SET @TimeExported = CAST(DATEADD(MINUTE, (@Counter * 37) % 1440, '08:00:00') AS time(7));
    -- Tạo Cart_ID: CART- + timestamp + "-" + Customer_ID (giống code Java)
    -- Sử dụng DATEDIFF_BIG để lấy milliseconds từ epoch (1970-01-01) + một số ngẫu nhiên để tránh trùng
    SET @CartID = 'CART-' + CAST(DATEDIFF_BIG(MILLISECOND, '1970-01-01', GETDATE()) + ABS(CHECKSUM(NEWID())) % 1000 AS varchar) + '-' + @CustomerID;
    
    SET @TotalQuantity = 1 + (@Counter % 4);
    
    SELECT TOP 1 @ProductID = Product_ID, @ProductPrice = ISNULL(Price, 0), @WarehouseItemID = Warehouse_Item_ID
    FROM dbo.Product 
    WHERE Status = 'Available' 
        AND Quantity >= @TotalQuantity
        AND Warehouse_Item_ID IS NOT NULL
    ORDER BY NEWID();
    
    -- Kiểm tra Product có đủ số lượng
    IF @ProductID IS NOT NULL
    BEGIN
        SELECT @CurrentQuantity = Quantity, @WarehouseItemID = Warehouse_Item_ID
        FROM dbo.Product WHERE Product_ID = @ProductID;
        
        IF @CurrentQuantity < @TotalQuantity OR @WarehouseItemID IS NULL
        BEGIN
            SET @ProductID = NULL;
        END
    END
    
    -- Nếu không tìm thấy Product đủ số lượng, bỏ qua order này
    IF @ProductID IS NULL
    BEGIN
        SET @Counter = @Counter + 1;
        CONTINUE;
    END
    
    SET @TotalPrice = @ProductPrice * @TotalQuantity;
    SET @VATAmount = @TotalPrice * @VATPercent / 100.0;
    SET @TotalAmount = @TotalPrice + @VATAmount;
    
    IF NOT EXISTS (SELECT 1 FROM dbo.Cart WHERE Cart_ID = @CartID)
    BEGIN
        INSERT INTO dbo.Cart (Cart_ID, Customer_ID, Product_ID, Quantity, Status)
        VALUES (@CartID, @CustomerID, @ProductID, @TotalQuantity, 'Available');
    END
    
    IF NOT EXISTS (SELECT 1 FROM dbo.Orders WHERE Order_No = @OrderNo AND Customer_ID = @CustomerID)
    BEGIN
        INSERT INTO dbo.Orders (Order_No, Customer_ID, Cart_ID, Total_Quantity_Product, Total_Price, Payment, Date_Order, Time_Order, Status, Record_Status)
        VALUES (@OrderNo, @CustomerID, @CartID, @TotalQuantity, @TotalPrice, 'Cash', @DateOrder, @TimeOrder, 'Confirmed', 'Available');
        
        INSERT INTO dbo.Orders_Details (Order_No, Customer_ID, Product_ID, Price, Sold_Quantity, Date_Order, Time_Order, Status, Record_Status)
        VALUES (@OrderNo, @CustomerID, @ProductID, @ProductPrice, @TotalQuantity, @DateOrder, @TimeOrder, 'Confirmed', 'Available');
        
        IF NOT EXISTS (SELECT 1 FROM dbo.Bill_Exported WHERE Invoice_No = @InvoiceNo AND Admin_ID = @AdminID)
        BEGIN
            INSERT INTO dbo.Bill_Exported (Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product, VAT_Percent, VAT_Amount, Total_Amount, Status)
            VALUES (@InvoiceNo, @AdminID, @CustomerID, @OrderNo, @TotalQuantity, @VATPercent, @VATAmount, @TotalAmount, 'Available');
            
            SELECT @WarrantyMonths = ISNULL(Warranty_Months, 12) FROM dbo.Product WHERE Product_ID = @ProductID;
            
            INSERT INTO dbo.Bill_Exported_Details (Invoice_No, Admin_ID, Product_ID, Unit_Price_Sell_Before, Unit_Price_Sell_After, Sold_Quantity, Discount_Percent, Total_Price_Before, Total_Price_After, Date_Exported, Time_Exported, Start_Date, End_Date, Status)
            VALUES (@InvoiceNo, @AdminID, @ProductID, @ProductPrice, @ProductPrice, @TotalQuantity, 0.00, @TotalPrice, @TotalPrice, @DateExported, @TimeExported, @DateExported, DATEADD(MONTH, @WarrantyMonths, @DateExported), 'Available');
        END
    END
    
    SET @Counter = @Counter + 1;
END

PRINT 'Đã tạo ' + CAST(@Counter - 1 AS varchar) + ' orders cho tháng 10/2025';

-- ============================================
-- TẠO DỮ LIỆU CHO THÁNG 11 (November 2025)
-- ============================================
PRINT '';
PRINT '=== TẠO DỮ LIỆU CHO THÁNG 11/2025 ===';

SET @Counter = 1;

-- Tạo 9 orders cho tháng 11
WHILE @Counter <= 9
BEGIN
    -- Lấy Customer ngẫu nhiên (cần lấy trước để tạo mã)
    SELECT TOP 1 @CustomerID = Customer_ID 
    FROM dbo.Customer 
    WHERE Record_Status = 'Available'
    ORDER BY NEWID();
    
    -- Tạo Order_No ngẫu nhiên 8 chữ số (giống code Java)
    SET @OrderNo = FORMAT(ABS(CHECKSUM(NEWID())) % 100000000, '00000000');
    
    -- Tạo Invoice_No ngẫu nhiên 10 chữ số + "-" + Customer_ID (giống code Java)
    SET @InvoiceNo = FORMAT(ABS(CHECKSUM(NEWID())) % 1000000000, '0000000000') + '-' + @CustomerID;
    SET @DateOrder = DATEFROMPARTS(2025, 11, 1 + (@Counter * 3) % 30);
    SET @TimeOrder = CAST(DATEADD(MINUTE, (@Counter * 47) % 1440, '00:00:00') AS time(7));
    SET @DateExported = DATEADD(DAY, (@Counter % 2), @DateOrder);
    SET @TimeExported = CAST(DATEADD(MINUTE, (@Counter * 37) % 1440, '08:00:00') AS time(7));
    -- Tạo Cart_ID: CART- + timestamp + "-" + Customer_ID (giống code Java)
    -- Sử dụng DATEDIFF_BIG để lấy milliseconds từ epoch (1970-01-01) + một số ngẫu nhiên để tránh trùng
    SET @CartID = 'CART-' + CAST(DATEDIFF_BIG(MILLISECOND, '1970-01-01', GETDATE()) + ABS(CHECKSUM(NEWID())) % 1000 AS varchar) + '-' + @CustomerID;
    
    SET @TotalQuantity = 1 + (@Counter % 3);
    
    SELECT TOP 1 @ProductID = Product_ID, @ProductPrice = ISNULL(Price, 0), @WarehouseItemID = Warehouse_Item_ID
    FROM dbo.Product 
    WHERE Status = 'Available' 
        AND Quantity >= @TotalQuantity
        AND Warehouse_Item_ID IS NOT NULL
    ORDER BY NEWID();
    
    -- Kiểm tra Product có đủ số lượng
    IF @ProductID IS NOT NULL
    BEGIN
        SELECT @CurrentQuantity = Quantity, @WarehouseItemID = Warehouse_Item_ID
        FROM dbo.Product WHERE Product_ID = @ProductID;
        
        IF @CurrentQuantity < @TotalQuantity OR @WarehouseItemID IS NULL
        BEGIN
            SET @ProductID = NULL;
        END
    END
    
    -- Nếu không tìm thấy Product đủ số lượng, bỏ qua order này
    IF @ProductID IS NULL
    BEGIN
        SET @Counter = @Counter + 1;
        CONTINUE;
    END
    
    SET @TotalPrice = @ProductPrice * @TotalQuantity;
    SET @VATAmount = @TotalPrice * @VATPercent / 100.0;
    SET @TotalAmount = @TotalPrice + @VATAmount;
    
    IF NOT EXISTS (SELECT 1 FROM dbo.Cart WHERE Cart_ID = @CartID)
    BEGIN
        INSERT INTO dbo.Cart (Cart_ID, Customer_ID, Product_ID, Quantity, Status)
        VALUES (@CartID, @CustomerID, @ProductID, @TotalQuantity, 'Available');
    END
    
    IF NOT EXISTS (SELECT 1 FROM dbo.Orders WHERE Order_No = @OrderNo AND Customer_ID = @CustomerID)
    BEGIN
        INSERT INTO dbo.Orders (Order_No, Customer_ID, Cart_ID, Total_Quantity_Product, Total_Price, Payment, Date_Order, Time_Order, Status, Record_Status)
        VALUES (@OrderNo, @CustomerID, @CartID, @TotalQuantity, @TotalPrice, 'Cash', @DateOrder, @TimeOrder, 'Confirmed', 'Available');
        
        INSERT INTO dbo.Orders_Details (Order_No, Customer_ID, Product_ID, Price, Sold_Quantity, Date_Order, Time_Order, Status, Record_Status)
        VALUES (@OrderNo, @CustomerID, @ProductID, @ProductPrice, @TotalQuantity, @DateOrder, @TimeOrder, 'Confirmed', 'Available');
        
        IF NOT EXISTS (SELECT 1 FROM dbo.Bill_Exported WHERE Invoice_No = @InvoiceNo AND Admin_ID = @AdminID)
        BEGIN
            INSERT INTO dbo.Bill_Exported (Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product, VAT_Percent, VAT_Amount, Total_Amount, Status)
            VALUES (@InvoiceNo, @AdminID, @CustomerID, @OrderNo, @TotalQuantity, @VATPercent, @VATAmount, @TotalAmount, 'Available');
            
            SELECT @WarrantyMonths = ISNULL(Warranty_Months, 12) FROM dbo.Product WHERE Product_ID = @ProductID;
            
            INSERT INTO dbo.Bill_Exported_Details (Invoice_No, Admin_ID, Product_ID, Unit_Price_Sell_Before, Unit_Price_Sell_After, Sold_Quantity, Discount_Percent, Total_Price_Before, Total_Price_After, Date_Exported, Time_Exported, Start_Date, End_Date, Status)
            VALUES (@InvoiceNo, @AdminID, @ProductID, @ProductPrice, @ProductPrice, @TotalQuantity, 0.00, @TotalPrice, @TotalPrice, @DateExported, @TimeExported, @DateExported, DATEADD(MONTH, @WarrantyMonths, @DateExported), 'Available');
        END
    END
    
    SET @Counter = @Counter + 1;
END

PRINT 'Đã tạo ' + CAST(@Counter - 1 AS varchar) + ' orders cho tháng 11/2025';

-- ============================================
-- TẠO DỮ LIỆU CHO THÁNG 12 (December 2025)
-- ============================================
PRINT '';
PRINT '=== TẠO DỮ LIỆU CHO THÁNG 12/2025 ===';

SET @Counter = 1;

-- Tạo 12 orders cho tháng 12 (mùa cao điểm)
WHILE @Counter <= 12
BEGIN
    -- Lấy Customer ngẫu nhiên (cần lấy trước để tạo mã)
    SELECT TOP 1 @CustomerID = Customer_ID 
    FROM dbo.Customer 
    WHERE Record_Status = 'Available'
    ORDER BY NEWID();
    
    -- Tạo Order_No ngẫu nhiên 8 chữ số (giống code Java)
    SET @OrderNo = FORMAT(ABS(CHECKSUM(NEWID())) % 100000000, '00000000');
    
    -- Tạo Invoice_No ngẫu nhiên 10 chữ số + "-" + Customer_ID (giống code Java)
    SET @InvoiceNo = FORMAT(ABS(CHECKSUM(NEWID())) % 1000000000, '0000000000') + '-' + @CustomerID;
    SET @DateOrder = DATEFROMPARTS(2025, 12, 1 + (@Counter * 2) % 31);
    SET @TimeOrder = CAST(DATEADD(MINUTE, (@Counter * 47) % 1440, '00:00:00') AS time(7));
    SET @DateExported = DATEADD(DAY, (@Counter % 2), @DateOrder);
    SET @TimeExported = CAST(DATEADD(MINUTE, (@Counter * 37) % 1440, '08:00:00') AS time(7));
    -- Tạo Cart_ID: CART- + timestamp + "-" + Customer_ID (giống code Java)
    -- Sử dụng DATEDIFF_BIG để lấy milliseconds từ epoch (1970-01-01) + một số ngẫu nhiên để tránh trùng
    SET @CartID = 'CART-' + CAST(DATEDIFF_BIG(MILLISECOND, '1970-01-01', GETDATE()) + ABS(CHECKSUM(NEWID())) % 1000 AS varchar) + '-' + @CustomerID;
    
    SET @TotalQuantity = 1 + (@Counter % 4);
    
    SELECT TOP 1 @ProductID = Product_ID, @ProductPrice = ISNULL(Price, 0), @WarehouseItemID = Warehouse_Item_ID
    FROM dbo.Product 
    WHERE Status = 'Available' 
        AND Quantity >= @TotalQuantity
        AND Warehouse_Item_ID IS NOT NULL
    ORDER BY NEWID();
    
    -- Kiểm tra Product có đủ số lượng
    IF @ProductID IS NOT NULL
    BEGIN
        SELECT @CurrentQuantity = Quantity, @WarehouseItemID = Warehouse_Item_ID
        FROM dbo.Product WHERE Product_ID = @ProductID;
        
        IF @CurrentQuantity < @TotalQuantity OR @WarehouseItemID IS NULL
        BEGIN
            SET @ProductID = NULL;
        END
    END
    
    -- Nếu không tìm thấy Product đủ số lượng, bỏ qua order này
    IF @ProductID IS NULL
    BEGIN
        SET @Counter = @Counter + 1;
        CONTINUE;
    END
    
    SET @TotalPrice = @ProductPrice * @TotalQuantity;
    SET @VATAmount = @TotalPrice * @VATPercent / 100.0;
    SET @TotalAmount = @TotalPrice + @VATAmount;
    
    IF NOT EXISTS (SELECT 1 FROM dbo.Cart WHERE Cart_ID = @CartID)
    BEGIN
        INSERT INTO dbo.Cart (Cart_ID, Customer_ID, Product_ID, Quantity, Status)
        VALUES (@CartID, @CustomerID, @ProductID, @TotalQuantity, 'Available');
    END
    
    IF NOT EXISTS (SELECT 1 FROM dbo.Orders WHERE Order_No = @OrderNo AND Customer_ID = @CustomerID)
    BEGIN
        INSERT INTO dbo.Orders (Order_No, Customer_ID, Cart_ID, Total_Quantity_Product, Total_Price, Payment, Date_Order, Time_Order, Status, Record_Status)
        VALUES (@OrderNo, @CustomerID, @CartID, @TotalQuantity, @TotalPrice, 'Cash', @DateOrder, @TimeOrder, 'Confirmed', 'Available');
        
        INSERT INTO dbo.Orders_Details (Order_No, Customer_ID, Product_ID, Price, Sold_Quantity, Date_Order, Time_Order, Status, Record_Status)
        VALUES (@OrderNo, @CustomerID, @ProductID, @ProductPrice, @TotalQuantity, @DateOrder, @TimeOrder, 'Confirmed', 'Available');
        
        IF NOT EXISTS (SELECT 1 FROM dbo.Bill_Exported WHERE Invoice_No = @InvoiceNo AND Admin_ID = @AdminID)
        BEGIN
            INSERT INTO dbo.Bill_Exported (Invoice_No, Admin_ID, Customer_ID, Order_No, Total_Product, VAT_Percent, VAT_Amount, Total_Amount, Status)
            VALUES (@InvoiceNo, @AdminID, @CustomerID, @OrderNo, @TotalQuantity, @VATPercent, @VATAmount, @TotalAmount, 'Available');
            
            SELECT @WarrantyMonths = ISNULL(Warranty_Months, 12) FROM dbo.Product WHERE Product_ID = @ProductID;
            
            INSERT INTO dbo.Bill_Exported_Details (Invoice_No, Admin_ID, Product_ID, Unit_Price_Sell_Before, Unit_Price_Sell_After, Sold_Quantity, Discount_Percent, Total_Price_Before, Total_Price_After, Date_Exported, Time_Exported, Start_Date, End_Date, Status)
            VALUES (@InvoiceNo, @AdminID, @ProductID, @ProductPrice, @ProductPrice, @TotalQuantity, 0.00, @TotalPrice, @TotalPrice, @DateExported, @TimeExported, @DateExported, DATEADD(MONTH, @WarrantyMonths, @DateExported), 'Available');
        END
    END
    
    SET @Counter = @Counter + 1;
END

PRINT 'Đã tạo ' + CAST(@Counter - 1 AS varchar) + ' orders cho tháng 12/2025';

PRINT '';
PRINT '=== HOÀN TẤT TẠO DỮ LIỆU CHO NĂM 2025 (Tất cả 12 tháng) ===';

-- ============================================
-- KIỂM TRA KẾT QUẢ TỔNG HỢP
-- ============================================
PRINT '';
PRINT '=== KIỂM TRA KẾT QUẢ TỔNG HỢP ===';

-- Thống kê theo năm
SELECT 
    YEAR(bed.Date_Exported) AS Year,
    COUNT(DISTINCT bed.Invoice_No) AS Total_Invoices,
    COUNT(bed.Product_ID) AS Total_Products_Sold,
    SUM(bed.Sold_Quantity) AS Total_Quantity,
    SUM(bed.Total_Price_After) AS Total_Revenue
FROM dbo.Bill_Exported_Details bed
WHERE bed.Status = 'Available'
    AND YEAR(bed.Date_Exported) IN (2024, 2025)
GROUP BY YEAR(bed.Date_Exported)
ORDER BY Year;

-- Thống kê theo tháng trong năm 2025
SELECT 
    FORMAT(bed.Date_Exported, 'yyyy-MM') AS Month,
    COUNT(DISTINCT bed.Invoice_No) AS Total_Invoices,
    COUNT(bed.Product_ID) AS Total_Products_Sold,
    SUM(bed.Sold_Quantity) AS Total_Quantity,
    SUM(bed.Total_Price_After) AS Total_Revenue
FROM dbo.Bill_Exported_Details bed
WHERE bed.Status = 'Available'
    AND YEAR(bed.Date_Exported) = 2025
GROUP BY FORMAT(bed.Date_Exported, 'yyyy-MM')
ORDER BY Month;

PRINT '';
PRINT '=== HOÀN TẤT TẠO DỮ LIỆU MẪU ===';
PRINT 'Dữ liệu đã được tạo cho:';
PRINT '  - Năm 2024: Tháng 7, 8, 9, 10';
PRINT '  - Năm 2025: Tất cả 12 tháng';
PRINT 'Bạn có thể kiểm tra dữ liệu trong phần Statistics.';
GO

