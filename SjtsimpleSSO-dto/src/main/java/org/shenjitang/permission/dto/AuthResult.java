package org.shenjitang.permission.dto;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by yyam on 15-3-23.
 */
@XmlRootElement(name = "AuthResult")
public class AuthResult {
    private Boolean success;
    private String appName;
    private String user;
    private String errMessage;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }
}
