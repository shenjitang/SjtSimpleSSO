package org.shenjitang.sjtsimplesso.client.listener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shenjitang.permission.dto.AppInfo;
import org.shenjitang.sjtsimplesso.client.JerseyRequest;
import org.shenjitang.sjtsimplesso.client.WebConstants;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.*;
import java.util.*;

/**
 * 作用是监听session变化。负责将session中的内容和sso service同步。监听App启动事件，
 * 当app启动时在SSOService里注册App的名称和host的关系。SSOService会根据这个关系来设置Cookie(Cookie.setDomsin())
 * Created by  gang.xie on 2015/3/24.
 */
public class SsoSessionListener extends JerseyRequest implements ServletContextListener, HttpSessionListener, HttpSessionAttributeListener {


    private static Boolean authed =false;
    protected static final Log logger = LogFactory.getLog(SsoSessionListener.class);

    /**
     * ServletContextListener
     *
     * @param servletContextEvent
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.debug("SsoSessionListener.contextInitialize start");

        super.ssoServiceHost = servletContextEvent.getServletContext().getInitParameter("sso_service_host");
        super.ssoServicePort = servletContextEvent.getServletContext().getInitParameter("sso_service_port");

        if (StringUtils.isBlank(super.ssoServiceHost) || StringUtils.isBlank(super.ssoServicePort)) {
            logger.error("not config ssoServiceHost and ssoServicePort");
        }

        String host = servletContextEvent.getServletContext().getInitParameter("app_server_host");
        if (StringUtils.isBlank(host)) {
            host = servletContextEvent.getServletContext().getVirtualServerName();
        }
        String path = servletContextEvent.getServletContext().getContextPath();
        String appName = servletContextEvent.getServletContext().getInitParameter("app_server_name");

        //regist app
        Map<String, String> params = new HashMap<>();
        params.put("appName", appName);
        params.put("host", host);
        params.put("path", path);
        String registAppUrl = super.getUrl(WebConstants.REGIST_APP_URL);
        postRequest(registAppUrl, params);

        //get app infos
        String getHostsUrl = super.getUrl(WebConstants.GET_HOSTS_URL);
        String hosts = (String) super.getRequest(getHostsUrl, null, null);

        servletContextEvent.getServletContext().setAttribute(WebConstants.KEY_APP_HOSTS, hosts);
        logger.debug("SsoSessionListener.contextInitialize end");
    }

    /**
     * ServletContextListener
     *
     * @param servletContextEvent
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("ServletContextListener.contextDestroyed");
    }

    /**
     * HttpSessionListener
     *
     * @param httpSessionEvent
     */
    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        System.out.println("HttpSessionListener.sessionCreated");
    }

    /**
     * HttpSessionListener
     *
     * @param httpSessionEvent
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        System.out.println("HttpSessionListener.sessionDestroyed");
    }

    /**
     * HttpSessionAttributeListener
     *
     * @param httpSessionBindingEvent
     */
    @Override
    public void attributeAdded(HttpSessionBindingEvent httpSessionBindingEvent) {
        HttpSession session = httpSessionBindingEvent.getSession();
        if (WebConstants.KEY_LOGIN_USER.equals(httpSessionBindingEvent.getName())) {
            String sessionTicket = (String) session.getAttribute(WebConstants.KEY_LOGIN_TICKET);
            if (StringUtils.isBlank(sessionTicket)) {

                String host = session.getServletContext().getInitParameter("app_server_host");
                if (StringUtils.isBlank(host)) {
                    host = session.getServletContext().getVirtualServerName();
                }
                String path = session.getServletContext().getContextPath();
                String appName = session.getServletContext().getInitParameter("app_server_name");


                Map<String, String> params = new HashMap<>();
                params.put("appName", appName);
                params.put("host", host);
                params.put("path", path);
                params.put("user", httpSessionBindingEvent.getValue() + "");
                params.put("sessionId", session.getId());

                //create Ticket
                String createTicketUrl = super.getUrl(WebConstants.CREATE_TICKET_URL);
                String ticket = (String) getRequest(createTicketUrl, null, params);
                if (StringUtils.isBlank(ticket)) {
                    logger.debug("SsoService CreateTicket Get Ticket Is NULL");
                    return;
                }
                session.setAttribute(WebConstants.KEY_LOGIN_TICKET, ticket);
                authed = true;
            }
        }
        if (authed) {
            String needSaveToServer = session.getServletContext().getInitParameter("sjtSsoNeedSaveToServer");
            if (isSynchronizeServer(needSaveToServer, httpSessionBindingEvent.getName())) {
                String ticket = (String) session.getAttribute(WebConstants.KEY_LOGIN_TICKET);
                String addToSessionUrl = super.getUrl(WebConstants.ADD_TO_SESSION_URL);
                Map<String, String> params = new HashMap<>();
                params.put("ticket", ticket);
                params.put("key", httpSessionBindingEvent.getName());
                params.put("value", httpSessionBindingEvent.getValue() + "");
                postRequest(addToSessionUrl, params);
            }
        }
    }

    private boolean isSynchronizeServer(String needSaveToServer, String sessionKey) {
        if (StringUtils.isBlank(needSaveToServer)) {
            return false;
        }
        if ("FALSE".equals(needSaveToServer.toUpperCase())) {
            return false;
        }
        Set<String> needSaves = new HashSet<>();
        String[] needSavesSp = needSaveToServer.split(",");
        for (String needSave : needSavesSp) {
            needSaves.add(needSave.toUpperCase());
        }
        if (needSaves.contains("TRUE") || needSaves.contains(sessionKey.toUpperCase())) {
            return true;
        }
        return false;
    }

    /**
     * HttpSessionAttributeListener
     *
     * @param httpSessionBindingEvent
     */
    @Override
    public void attributeRemoved(HttpSessionBindingEvent httpSessionBindingEvent) {
        if (WebConstants.KEY_LOGIN_USER.equals(httpSessionBindingEvent.getName())) {
            String ticket = (String) httpSessionBindingEvent.getValue();
            String destoryTicket = super.getUrl(WebConstants.DESTROY_TICKET_URL);
            //destory ticket
            Map<String, String> params = new HashMap<>();
            params.put("ticket", ticket);
            getRequest(destoryTicket, null, params);
        } else {
            HttpSession session = httpSessionBindingEvent.getSession();
            String needSaveToServer = session.getServletContext().getInitParameter("sjtSsoNeedSaveToServer");
            if (isSynchronizeServer(needSaveToServer, httpSessionBindingEvent.getName())) {
                String ticket = (String) session.getAttribute(WebConstants.KEY_LOGIN_TICKET);
                String removeFromSessionUrl = super.getUrl(WebConstants.REMOVE_FROM_SESSION_URL);
                Map<String, String> params = new HashMap<>();
                params.put("ticket", ticket);
                params.put("key", httpSessionBindingEvent.getName());
                //remove from session
                getRequest(removeFromSessionUrl, null, params);
            }
        }
    }

    /**
     * HttpSessionAttributeListener
     *
     * @param httpSessionBindingEvent
     */
    @Override
    public void attributeReplaced(HttpSessionBindingEvent httpSessionBindingEvent) {
        System.out.println("HttpSessionAttributeListener.attributeReplaced");
    }

}
