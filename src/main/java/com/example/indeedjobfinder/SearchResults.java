package com.example.indeedjobfinder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResults {
    @JsonProperty("jobList")
    private JobList jobList;

    @JsonProperty("jobseekerApiKey")
    private String jobSeekerApiKey;

    @JsonProperty("jobseekerApiUrl")
    private String jobSeekerApiUrl;

    public JobList getJobList() {
        return jobList;
    }

    public void setJobList(JobList jobList) {
        this.jobList = jobList;
    }

    public String getJobSeekerApiKey() {
        return jobSeekerApiKey;
    }

    public void setJobSeekerApiKey(String jobSeekerApiKey) {
        this.jobSeekerApiKey = jobSeekerApiKey;
    }

    public String getJobSeekerApiUrl() {
        return jobSeekerApiUrl;
    }

    public void setJobSeekerApiUrl(String jobSeekerApiUrl) {
        this.jobSeekerApiUrl = jobSeekerApiUrl;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class JobList {
    @JsonProperty("jobs")
    private List<Job> jobs;

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Job {
    @JsonProperty("jobKey")
    private String jobKey;

    public String getJobKey() {
        return jobKey;
    }

    public void setJobKey(String jobKey) {
        this.jobKey = jobKey;
    }
}