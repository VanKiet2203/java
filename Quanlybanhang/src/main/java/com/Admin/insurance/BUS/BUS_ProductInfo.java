package com.Admin.insurance.BUS;

import com.Admin.insurance.DAO.DAOProductInfo;
import com.Admin.insurance.DTO.DTOProductInfo;
import java.sql.SQLException;

public class BUS_ProductInfo {
    private DAOProductInfo daoProductInfo;

    // Constructor để khởi tạo DAOProductInfo
    public BUS_ProductInfo() {
        daoProductInfo = new DAOProductInfo();
    }
}
