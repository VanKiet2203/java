# SmartSales Manager

SmartSales Manager is a Java Swing desktop application designed to support basic sales and inventory management for a retail shop.  
The project follows a layered structure (DTO → DAO → BUS → GUI) and uses Microsoft SQL Server as its primary database.

---

## 📌 Overview

The application provides two separate interfaces:

- **Admin Panel** – used for managing store operations  
- **Customer UI** – used by customers to browse and purchase products  

The system supports product management, customer and employee management, inventory tracking, order processing, and basic promotions.

---

## 🚀 Main Features

### **Admin Panel**
- Login & role-based access  
- Product & category management  
- Customer & employee management  
- Supplier management  
- Inventory and stock control  
- Import & export bills  
- Order management  
- Promotion management  
- Basic reporting & dashboard

### **Customer UI**
- Login / registration  
- Product browsing & search  
- Product detail preview  
- Shopping cart  
- Order creation  
- Order history tracking  
- User profile management  

---

## 🛠 Technologies Used
- **Java 8+**
- **Java Swing**
- **Microsoft SQL Server**
- **JDBC**
- **Layered architecture (DTO / DAO / BUS / GUI)**

---

## 📁 Project Structure
```text
src/
└─ main/java/com/
  ├─ Admin/ # Admin-side modules
  ├─ User/ # Customer-facing modules
  ├─ DTO/ # Data transfer objects
  ├─ DAO/ # Database access layer
  ├─ BUS/ # Business logic layer
  └─ ComponentandDatabase/ # Shared components + DB connection
resources/
├─ Icons/
├─ Profile_Image/
├─ Sound/
└─ Bill_Exported/
Database/
├─ image-product/
└─ excel-import/
```
## ▶️ How to Run

1. Import the project into your IDE (IntelliJ, Eclipse, or NetBeans).  
2. Restore/create the SQL Server database using the provided scripts.  
3. Update database connection info inside: ComponentandDatabase/Database_Connection
4. Add required `.jar` files from the `lib/` folder.  
5. Run the application through:
- **Admin Login:** `com.Admin.login.GUI.Login`  
- **Customer Login:** `com.User.login_user.GUI.Login_User`  

---
## 🎯 Purpose

This project is built for study and practice in:

- Java desktop programming with Swing  
- SQL Server database integration  
- Multi-layer application design  
- Retail shop management workflows  
