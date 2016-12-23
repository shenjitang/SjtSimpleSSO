/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shenjitang.permission.sso.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shenjitang.permission.dto.AppInfo;
import org.shenjitang.permission.dto.TicketInfo;
import org.shenjitang.permission.sso.IAuthService;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.ws.rs.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author xiaolie
 */
@Path("/auth")  
public class AuthService implements IAuthService{
    private Log logger = LogFactory.getLog(AuthService.class);

    /**
     * <pre>
     *     key : ticket
     *     value : TicketInfo created by createTicket
     * </pre>
     */
    private static Map<String, TicketInfo> ticketMap = new HashMap<String, TicketInfo>();

    /**
     * all register app info
     */
    private static Set<AppInfo> appInfos = new HashSet<AppInfo>();

    /**
     * <pre>
     * key : ticket
     * value : share data
     * </pre>
     */
    private static Map<String, Map<String, String>> ticketSession = new HashMap<String, Map<String, String>>();

    private static Long ticketTimeoutLimit = 30 * 60 * 1000L;

    @GET   
    @Produces("application/xml")  
    public AppInfo helloWorld(){  
         AppInfo appInfo = new AppInfo();
         appInfo.setAppName("hello");
         appInfo.setHost("loalhost");
         appInfo.setHost("/word");
        return appInfo;  
    }     

    @Path("json")  
    @GET   
    @Produces("application/json")  
    public AppInfo helloJson(){  
         AppInfo appInfo = new AppInfo();
         appInfo.setAppName("hello");
         appInfo.setHost("loalhost");
         appInfo.setHost("/word");
        return appInfo;  
    }     

    @Path("array")  
    @GET   
    @Produces("application/json")  
    public Set<Integer> helloArray(){  
        Set<Integer> hosts = new HashSet();
        hosts.add(11);
        hosts.add(22);
        return hosts;  
    }     

    @Path("createTicket")  
    @GET   
    @Produces("application/json")
    @Override
    public String createTicket(@QueryParam("appName") String appName, @QueryParam("host") String host, @QueryParam("path") String path, @QueryParam("user") String user, @QueryParam("sessionId") String sessionId) {
//        String src = user + "|" + System.currentTimeMillis();
//        String ticket = byteArrayToHexString(desCrypt(src, password));
        String ticket = byteArrayToHexString(desCrypt(user, password));

        TicketInfo ticketInfo = new TicketInfo();
        ticketInfo.setSessionId(sessionId);
        ticketInfo.setActiveTime(new Date());
        ticketInfo.setLoginTime(new Date());
        ticketInfo.setTicket(ticket);
        ticketInfo.setUser(user);
        ticketMap.put(ticket, ticketInfo);

        logger.info("App : " + appName + " from " + host + " of " + path + " create ticket : " + ticket + " by sessionId : " + sessionId);
        return ticket;
    }

    @Path("setTTL")
    @GET
    @Override
    public Boolean setTicketTimeoutLimit(@QueryParam("num") long secs) {
        this.ticketTimeoutLimit = secs * 1000;
        return true;
    }

    @Path("setTTLByMin")
    @GET
    @Override
    public Boolean setTicketTimeoutLimitByMinute(@QueryParam("num") int minutes) {
        return setTicketTimeoutLimit(minutes * 60);
    }

    @Path("activeTicket")
    @GET
    @Override
    public Boolean activeTicket(@QueryParam("ticket") String ticket) {
        TicketInfo ticketInfo = ticketMap.get(ticket);
        if (ticketInfo == null) {
            return false;
        }
        ticketInfo.setActiveTime(new Date());
        return true;
    }

    @Path("checkTicket")
    @GET
    @Produces("application/json")
    @Override
    public String checkTicket(@QueryParam("ticket") String ticket) {

        ticket = byteArrayToHexString(desCrypt(ticket, password));

        TicketInfo ticketInfo = ticketMap.get(ticket);
        if (ticketInfo == null) {
            return null;
        }
        if ((new Date().getTime() - ticketInfo.getActiveTime().getTime()) > ticketTimeoutLimit) {
            destroyTicket(ticket);
            return null;
        }
        activeTicket(ticket);
        return ticketInfo.getUser();
    }

    @Path("destroyTicket")
    @GET
    @Override
    public Boolean destroyTicket(@QueryParam("ticket") String ticket) {
        //destroy session data
        ticketSession.remove(ticket);

        //destroy the ticket info
        ticketMap.remove(ticket);
        return true;
    }

    @Path("addToSession")
    @POST
    @Override
    public Boolean addToSession(@FormParam("ticket") String ticket, @FormParam("key") String key, @FormParam("value") String value) {
        Map<String, String> session = ticketSession.get(ticket);
        if (session == null) {
            session = new HashMap<String, String>();
            ticketSession.put(ticket, session);
        }
        session.put(key, value);
        return true;
    }

    @Path("getFromSession")
    @GET
    @Produces("application/json")
    @Override
    public String getFromSession(@QueryParam("ticket") String ticket, @QueryParam("key") String key) {
        Map<String, String> session = ticketSession.get(ticket);
        if (session == null) {
            return null;
        }
        return session.get(key);
    }

    @Path("removeFromSession")
    @GET
    @Override
    public Boolean removeFromSession(@QueryParam("ticket") String ticket, @QueryParam("key") String key) {
        Map<String, String> session = ticketSession.get(ticket);
        if (session == null) {
            return false;
        }
        session.remove(key);
        return true;
    }

    @Path("getHosts")
    @GET
    @Produces("application/json")
    @Override
    public Set<String> getHosts() {
        Set<String> hosts = new HashSet<String>();
        for (AppInfo appInfo : appInfos) {
            hosts.add(appInfo.getHost());
        }
        return hosts;
    }

    @Path("registerApp")
    @POST
    @Override
    public Boolean registerApp(@FormParam("appName") String appName, @FormParam("host") String host, @FormParam("path") String path) {
        AppInfo appInfo = new AppInfo();
        appInfo.setAppName(appName);
        appInfo.setHost(host);
        appInfo.setPath(path);
        appInfos.add(appInfo);
        return true;
    }


    private String password = "com.ri.sjt.chen.sso.info";

    private byte[] desCrypt(String src, String password) {
        // DES算法要求有一个可信任的随机数源
        SecureRandom sr = new SecureRandom();

        // 创建一个密匙工厂，然后用它把DESKeySpec转换成一个SecretKey对象
        SecretKeyFactory keyFactory = null;
        // Cipher对象实际完成加密操作
        Cipher cipher = null;
        try {
            keyFactory = SecretKeyFactory.getInstance("DES");
            cipher = Cipher.getInstance("DES");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("invalid encrypt algorithm:DES");
        } catch (NoSuchPaddingException e) {
            throw new IllegalArgumentException("invalid encrypt algorithm:DES");
        }
        // 从原始密匙数据创建DESKeySpec对象
        DESKeySpec dks = null;
        SecretKey secureKey = null;
        try {
            dks = new DESKeySpec(password.getBytes());
            secureKey = keyFactory.generateSecret(dks);
            // 用密匙初始化Cipher对象
            cipher.init(Cipher.ENCRYPT_MODE, secureKey, sr);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("invalid key size:" + password.getBytes());
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("invalid key size:" + password.getBytes());
        }


        // 执行加密操作
        try {
            return cipher.doFinal(src.getBytes());
        } catch (Exception e) {
            throw new IllegalStateException("do encrypt error" + (e.getMessage() == null ? "" : " : " + e.getMessage()), e);
        }
    }

    public String byteArrayToHexString(byte[] b) {
        StringBuilder resultSb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }
    private static final String[] hexDigits = {
            "0", "1", "2", "3", "4", "5", "6", "7",
            "8", "9", "a", "b", "c", "d", "e", "f"};

    private String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

}
