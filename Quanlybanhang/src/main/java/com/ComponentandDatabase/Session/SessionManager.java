package com.ComponentandDatabase.Session;

/**
 * SessionManager để quản lý thông tin session của admin hiện tại
 */
public class SessionManager {
    private static SessionManager instance;
    private String currentAdminId;
    private String currentAdminName;
    
    private SessionManager() {
        // Private constructor for singleton
    }
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public void setCurrentAdmin(String adminId, String adminName) {
        this.currentAdminId = adminId;
        this.currentAdminName = adminName;
    }
    
    public String getCurrentAdminId() {
        return currentAdminId;
    }
    
    public String getCurrentAdminName() {
        return currentAdminName;
    }
    
    public void clearSession() {
        this.currentAdminId = null;
        this.currentAdminName = null;
    }
    
    public boolean isLoggedIn() {
        return currentAdminId != null && !currentAdminId.isEmpty();
    }
}
