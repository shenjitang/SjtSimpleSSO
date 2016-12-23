/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shenjitang.sjtsimplesso.standalone;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author xiaolie
 */
public class Main {
    public static void main(String[] args) throws IOException {  
        URI ServerURI=UriBuilder.fromUri("http://localhost/").port(8881).build();
        startServer(ServerURI);  
        System.out.println("服务已启动，请访问："+ServerURI);  
    }      
      
    protected static SelectorThread startServer(URI serverURI) throws IOException {  
        final Map<String, String> initParams = new HashMap<String, String>();  
        initParams.put("com.sun.jersey.config.property.packages","org.shenjitang.permission.sso.impl");  
        System.out.println("Grizzly 启动中...");  
        SelectorThread threadSelector = GrizzlyWebContainerFactory.create(serverURI, initParams);       
        return threadSelector;  
    }      
    
}
