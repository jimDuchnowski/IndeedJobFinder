package com.example.indeedjobfinder;

/**
 * The type Job details.
 */
public class JobDetails {
    private String key;
    private String title;
    private String html;
    private int yesCount = 0;
    private int noCount = 0;
    private int maybeCount = 0;
    private int ungradedCount = 0;
    private boolean skippedByTitle = false;

    /**
     * Instantiates a new Job details object
     *
     * @param key   the job key
     * @param title the job title
     * @param html  the raw html of the job description
     */
    public JobDetails(String key, String title, String html) {
        this.key = key;
        this.title = title;
        this.html = html;
    }

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

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public int getYesCount() {
        return yesCount;
    }

    public void increaseYesCount() {
        this.yesCount++;
    }

    public int getNoCount() {
        return noCount;
    }

    public void increaseNoCount() {
        this.noCount++;
    }

    public int getMaybeCount() {
        return maybeCount;
    }

    public void increaseMaybeCount() {
        this.maybeCount++;
    }

    public int getUngradedCount() {
        return ungradedCount;
    }

    public void increaseUngradedCount() {
        this.ungradedCount++;
    }

    public boolean isSkippedByTitle() {
        return skippedByTitle;
    }

    public void setSkippedByTitle(boolean skippedByTitle) {
        this.skippedByTitle = skippedByTitle;
    }

    public int getOverallGrade() {
        return ((this.yesCount*2) + (this.maybeCount) + (this.ungradedCount*-1) + (this.noCount*-2));
    }
}
