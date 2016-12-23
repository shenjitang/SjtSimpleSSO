package org.shenjitang.sjtsimplesso.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by gang.xie on 2015/3/26.
 */
public class JerseyRequest {

    protected Log logger = LogFactory.getLog(this.getClass());

    protected static String ssoServiceHost = "";
    protected static String ssoServicePort = "";

    protected Object getRequest(String httpPath, Class toBean, Map<String, String> paramsMap) {
        Object result = null;
        try {
            Client client = Client.create();
            WebResource service = client.resource(httpPath);
            if (paramsMap != null && !paramsMap.isEmpty()) {
                MultivaluedMapImpl params = new MultivaluedMapImpl();
                for (Map.Entry<String, String> param : paramsMap.entrySet()) {
                    params.add(param.getKey(), param.getValue());
                }
                result = service.queryParams(params).get(String.class);
            } else {
                result = service.get(String.class);
            }
            if (toBean != null) {
                JSONObject jsonObject = JSONObject.fromObject(result);
                return JSONObject.toBean(jsonObject, toBean);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error("request error:"+httpPath);
            return null;
        }
        return result;
    }

    protected Object getListRequest(String httpPath, Class toBean) {
        Object result = null;
        try {
            Client client = Client.create();
            WebResource service = client.resource(httpPath);
            result = service.get(String.class);
            if (toBean != null) {
                JSONArray jsonArr = JSONArray.fromObject(result);
                List list = new ArrayList();
                for (int i = 0; i < jsonArr.size(); i++) {
                    list.add(JSONObject.toBean(jsonArr.getJSONObject(i), toBean));
                }
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    protected void postRequest(String httpPath, Map<String, String> paramsMap) {
        try {
            Client client = Client.create();
            WebResource service = client.resource(httpPath);
            MultivaluedMapImpl params = new MultivaluedMapImpl();
            for (Map.Entry<String, String> param : paramsMap.entrySet()) {
                params.add(param.getKey(), param.getValue());
            }
            service.post(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected String getUrl(String requestUrl) {
        return "http://" + (ssoServiceHost + ":" + ssoServicePort + requestUrl).replaceAll("//", "/");
    }

}
