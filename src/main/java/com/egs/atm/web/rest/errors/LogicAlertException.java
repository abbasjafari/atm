package com.egs.atm.web.rest.errors;

/**
 * business wrapper exception
 * */
public class LogicAlertException extends Exception {

    private static final long serialVersionUID = 1L;

    public LogicAlertException(String message) {
        super(message);
    }
    public LogicAlertException(String message,Throwable err) {
        super(message,err);
    }
}
