package com.example.indeedjobfinder;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "my")
public class ConfigLoader {
    private String jobCompany;
    private String jobQueryText;
    private String jobLocation;
    private String userAgent;

    public String getJobCompany() {
        return jobCompany;
    }

    public void setJobCompany(String jobCompany) {
        this.jobCompany = jobCompany;
    }

    public String getJobQueryText() {
        return jobQueryText;
    }

    public void setJobQueryText(String jobQueryText) {
        this.jobQueryText = jobQueryText;
    }

    public String getJobLocation() {
        return jobLocation;
    }

    public void setJobLocation(String jobLocation) {
        this.jobLocation = jobLocation;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
