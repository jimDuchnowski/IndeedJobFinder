package com.example.indeedjobfinder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The type Job details response.
 */
public class JobDetailsResponse {
    @JsonProperty("data")
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public JobDetails getJobDetails() {
        return new JobDetails(
                this.getData().getJobData().getResults().get(0).getJob().getKey(),
                this.getData().getJobData().getResults().get(0).getJob().getTitle(),
                this.getData().getJobData().getResults().get(0).getJob().getDescription().getHtml()
        );
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Data {
    @JsonProperty("jobData")
    private JobData jobData;

    public JobData getJobData() {
        return jobData;
    }

    public void setJobData(JobData jobData) {
        this.jobData = jobData;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class JobData {
    @JsonProperty("results")
    private List<Result> results;

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Result {
    @JsonProperty("job")
    private ThisJob job;

    public ThisJob getJob() {
        return job;
    }

    public void setJob(ThisJob job) {
        this.job = job;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class ThisJob {
    @JsonProperty("key")
    private String key;
    @JsonProperty("title")
    private String title;
    @JsonProperty("description")
    private Description description;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Description {
    @JsonProperty("html")
    private String html;

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
}