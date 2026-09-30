package org.apache.roller.weblogger.business;

import java.util.ArrayList;
import java.util.List;

public class RegistrationResult {
    
    private List<String> errors;
    private boolean success;
    private String activationStatus;
    
    public RegistrationResult() {
        this.errors = new ArrayList<>();
        this.success = false;
    }
    
    public void addError(String error) {
        errors.add(error);
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getActivationStatus() {
        return activationStatus;
    }
    
    public void setActivationStatus(String activationStatus) {
        this.activationStatus = activationStatus;
    }
}
