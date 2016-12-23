package org.shenjitang.permission.dto;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by yyam on 15-3-23.
 */
@XmlRootElement(name = "AppInfo")
public class AppInfo {
    private String appName;
    private String host;
    private String path;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppInfo appInfo = (AppInfo) o;

        if (appName != null ? !appName.equals(appInfo.appName) : appInfo.appName != null) return false;
        if (host != null ? !host.equals(appInfo.host) : appInfo.host != null) return false;
        if (path != null ? !path.equals(appInfo.path) : appInfo.path != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = appName != null ? appName.hashCode() : 0;
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }
}
