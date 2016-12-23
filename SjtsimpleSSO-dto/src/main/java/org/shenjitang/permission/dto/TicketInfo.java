package org.shenjitang.permission.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by yyam on 15-3-23.
 */
@XmlRootElement(name = "AuthResult")
public class TicketInfo {
    private String ticket;
    private String sessionId;
    private Date loginTime;
    private Date activeTime;
    private String user;
    private Map sessionMap = new HashMap();

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

    public Date getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(Date activeTime) {
        this.activeTime = activeTime;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Map getSessionMap() {
        return sessionMap;
    }

    public void setSessionMap(Map sessionMap) {
        this.sessionMap = sessionMap;
    }
}
