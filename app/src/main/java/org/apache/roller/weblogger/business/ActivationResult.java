package org.apache.roller.weblogger.business;

import java.util.ArrayList;
import java.util.List;

public class ActivationResult {
    
    private List<String> errors;
    private String status;
    
    public ActivationResult() {
        this.errors = new ArrayList<>();
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
