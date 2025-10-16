package com.Admin.insurance.BUS;

import com.Admin.insurance.DAO.DAOCustomerInfo;
import com.Admin.insurance.DTO.DTO_CustomerInfo;
import java.sql.SQLException;

public class BUS_CustomerInfo {
    private DAOCustomerInfo daoCustomerInfo;

    // Constructor để khởi tạo DAO
    public BUS_CustomerInfo() {
        daoCustomerInfo = new DAOCustomerInfo();
    }
}
