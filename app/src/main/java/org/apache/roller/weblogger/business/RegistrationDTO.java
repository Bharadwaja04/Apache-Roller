package org.apache.roller.weblogger.business;

import java.util.Date;

public class RegistrationDTO {
    
    private String userName;
    private String screenName;
    private String fullName;
    private String emailAddress;
    private String locale;
    private String timeZone;
    private String openIdUrl;
    private String passwordText;
    private String passwordConfirm;
    private Date dateCreated;
    private Boolean enabled;
    private String activationCode;
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getScreenName() {
        return screenName;
    }
    
    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmailAddress() {
        return emailAddress;
    }
    
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    
    public String getLocale() {
        return locale;
    }
    
    public void setLocale(String locale) {
        this.locale = locale;
    }
    
    public String getTimeZone() {
        return timeZone;
    }
    
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
    
    public String getOpenIdUrl() {
        return openIdUrl;
    }
    
    public void setOpenIdUrl(String openIdUrl) {
        this.openIdUrl = openIdUrl;
    }
    
    public String getPasswordText() {
        return passwordText;
    }
    
    public void setPasswordText(String passwordText) {
        this.passwordText = passwordText;
    }
    
    public String getPasswordConfirm() {
        return passwordConfirm;
    }
    
    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }
    
    public Date getDateCreated() {
        return dateCreated;
    }
    
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getActivationCode() {
        return activationCode;
    }
    
    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }
}
