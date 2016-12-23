/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shenjitang.permission.sso;


import org.shenjitang.permission.dto.AppInfo;
import javax.ws.rs.QueryParam;
import java.util.Set;
/**
 *
 * @author xiaolie
 */
public interface IAuthService {
    public AppInfo helloWorld();
    public String createTicket(String appName, String host, String path, String user, String sessionId);

    /**
     * set ticket timeout limit by second
     * @param secs
     */
    public Boolean setTicketTimeoutLimit(long secs);

    /**
     * set ticket timeout limit by minute
     * @param minutes
     */
    public Boolean setTicketTimeoutLimitByMinute(@QueryParam("minutes") int minutes);

    /**
     * active the ticket by http request every time through the filter
     * @param ticket
     */
    public Boolean activeTicket(String ticket);

    public String checkTicket(String ticket);

    public Boolean destroyTicket(String ticket);

    public Boolean addToSession(String ticket, String key, String value);

    public String getFromSession(String ticket, String key);

    public Boolean removeFromSession(String ticket, String key);

    public Set<String> getHosts();

    public Boolean registerApp(String appName, String host, String path);
}
