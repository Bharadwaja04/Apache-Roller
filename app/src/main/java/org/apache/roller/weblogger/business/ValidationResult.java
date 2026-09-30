package org.apache.roller.weblogger.business;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    
    private List<String> errors;
    
    public ValidationResult() {
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
}
