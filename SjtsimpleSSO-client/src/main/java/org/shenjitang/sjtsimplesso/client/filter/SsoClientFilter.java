package org.shenjitang.sjtsimplesso.client.filter;

import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shenjitang.sjtsimplesso.client.JerseyRequest;
import org.shenjitang.sjtsimplesso.client.WebConstants;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 拦截用户访问，做登录验证。
 * Created by gang.xie on 2015/3/24.
 */
public class SsoClientFilter extends JerseyRequest implements Filter {

    protected static final Log logger = LogFactory.getLog(SsoClientFilter.class);

    private String redirectURL;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.redirectURL = filterConfig.getInitParameter("redirectURL") == null ? "null" : filterConfig.getInitParameter("redirectURL").toString();
        logger.info("redirectUrl======" + redirectURL);
        logger.debug("SsoClientFilterInitFilter..........");
    }

    private String getTicketByCookie(Cookie[] cookies) {
        String ticket = null;
        if (cookies != null && cookies.length != 0) {
            for (Cookie cookie : cookies) {
                if (WebConstants.KEY_LOGIN_TICKET.equals(cookie.getName())) {
                    ticket = cookie.getValue();
                }
            }
        }
        return ticket;
    }


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        logger.debug("SsoClientFilter.doFilter()");
        String cookieTicket = null;
        String host = servletRequest.getServletContext().getInitParameter("login_service_host");
        String post = servletRequest.getServletContext().getInitParameter("login_service_post");
        String name = servletRequest.getServletContext().getInitParameter("login_service_name");
        String loalHost = "http://" + host + ":" + post + "/" + name;

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession();
        String seesionUser = (String) session.getAttribute(WebConstants.KEY_LOGIN_USER);
        //获取当次请求的url
        String url = request.getRequestURI();
        //获取某项目不需要进行验证的url组
        String[] redirectUrls = getRedirectURLs();
        //进行判断是否是需要进行验证的url
        Boolean needValidate = isNeedValidate(redirectUrls, url);
        logger.info("needValiDate ,,,, url,,,, redirectUrl  ==== " + needValidate + ",,,,,," + url + ",,,," + redirectURL);
        if(needValidate){
            if (StringUtils.isBlank(seesionUser)) {//may be not login or app transfer
                cookieTicket = getTicketByCookie(request.getCookies());
                if (StringUtils.isBlank(cookieTicket)) {//may be not login
                    String sessionTicket = (String) session.getAttribute(WebConstants.KEY_LOGIN_TICKET);
                    if (StringUtils.isBlank(sessionTicket)) {//is not login
                        response.sendRedirect(loalHost + "/?sourceurl=" + request.getRequestURL());
                        return;
                    }
                } else {//app transfer
                    Map<String, String> params = new HashMap<>();
                    params.put("ticket", cookieTicket);
                    String checkTicketUrl = super.getUrl(WebConstants.CHECK_TICKET_URL);
                    String user = (String) getRequest(checkTicketUrl, null, params);
                    if (StringUtils.isBlank(user)) {
                        logger.warn("sso server has no ticket for " + cookieTicket);
                        response.sendRedirect(loalHost + "/?sourceurl=" + request.getRequestURL());
                        return;
                    }
                    session.setAttribute(WebConstants.KEY_LOGIN_TICKET, cookieTicket);
                    session.setAttribute(WebConstants.KEY_LOGIN_USER, user);
                    response.addHeader(WebConstants.KEY_LOGIN_TICKET, cookieTicket);
                    //get app infos
                    String appHosts = getAppsHost();
                    setHosts(appHosts, cookieTicket, response);
                }
            } else {//is logins

                String headerTicket = request.getHeader(WebConstants.KEY_LOGIN_TICKET);
                String sessionTicket = (String) session.getAttribute(WebConstants.KEY_LOGIN_TICKET);
                cookieTicket = getTicketByCookie(request.getCookies());
                if(!sessionTicket.equals(cookieTicket)){
                    sessionTicket=cookieTicket;
                    session.setAttribute(WebConstants.KEY_LOGIN_TICKET,sessionTicket);
                }
                if (StringUtils.isBlank(headerTicket) && StringUtils.isNotBlank(sessionTicket)) {
                    response.addHeader(WebConstants.KEY_LOGIN_TICKET, sessionTicket);
                    //get app infos
                    String appHosts = getAppsHost();
                    setHosts(appHosts, sessionTicket, response);
                }
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private String[] getRedirectURLs() {
        String[] redirectUrls;
        redirectUrls = redirectURL.split(",");
        return redirectUrls;
    }

    private Boolean isNeedValidate(String[] redirectUrls, String url) {
        Boolean needValidate = true;
        for(String redirectUrl : redirectUrls){
            if(redirectUrl.equals(url)){
                needValidate = false;
            }
        }
        return needValidate;
    }

    private String getAppsHost() {
        String getHostsUrl = super.getUrl(WebConstants.GET_HOSTS_URL);
        String appHosts = (String) super.getRequest(getHostsUrl, null, null);
        return appHosts;
    }

    private void setHosts(String appHosts, String sessionTicket, HttpServletResponse response) {
        JSONArray jsonArr = JSONArray.fromObject(appHosts);
        for (int i = 0; i < jsonArr.size(); i++) {
            Cookie cookie = new Cookie(WebConstants.KEY_LOGIN_TICKET, sessionTicket);
            cookie.setPath("/");

            response.addCookie(cookie);
        }
    }

    @Override
    public void destroy() {
        logger.debug("SsoClientFilterDestroyFilter..........");
    }

}