package com.ComponentandDatabase.Session;

public class SessionManager {
    private static SessionManager instance;
    private String currentAdminId;
    private String currentAdminName;
    private boolean loggedIn = false;
    
    private SessionManager() {}
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public void setCurrentAdmin(String adminId, String adminName) {
        this.currentAdminId = adminId;
        this.currentAdminName = adminName;
        this.loggedIn = true;
    }
    
    public String getCurrentAdminId() {
        return currentAdminId;
    }
    
    public String getCurrentAdminName() {
        return currentAdminName;
    }
    
    public boolean isLoggedIn() {
        return loggedIn;
    }
    
    public void logout() {
        this.currentAdminId = null;
        this.currentAdminName = null;
        this.loggedIn = false;
    }
}
