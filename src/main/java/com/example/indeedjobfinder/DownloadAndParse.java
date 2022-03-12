package com.example.indeedjobfinder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The type Download and parse.
 */
@Component
public class DownloadAndParse {
    public final ArrayList<String> qualificationsList = new ArrayList<>();
    public final ArrayList<String> ungradedList = new ArrayList<>();
    public final HashMap<String, JobDetails> masterJobList = new HashMap<>();
    public final HashMap<String, String> qualificationsMap = new HashMap<>();
    WebClient webClient;
    private ConfigLoader myProperties;

    /**
     * Sort by desc value hash map.
     *
     * @param hm the unsorted hash map
     * @return the sorted hash map
     */
    public static HashMap<String, Integer> sortByDescValue(HashMap<String, Integer> hm) {
        return hm.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    /**
     * Sets web client.
     *
     * @param webClient the web client
     */
    @Autowired
    public void setWebClient(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Sets config loader.
     *
     * @param myProperties the my properties
     */
    @Autowired
    public void setConfigLoader(ConfigLoader myProperties) {
        this.myProperties = myProperties;
    }

    /**
     * Run downloader.
     *
     * @throws IOException  the io exception
     * @throws CsvException the csv exception
     */
    @PostConstruct
    public void RunDownloader() throws IOException, CsvException {
        // Load qualifications map
        LoadYesNoMaybe();

        // Search for job listings using specified criteria
        Document htmlDoc = Jsoup.connect("https://www.indeed.com/cmp/" + myProperties.getJobCompany() + "/jobs?q=" + myProperties.getJobQueryText() + "&l=" + myProperties.getJobLocation()).get();

        // Collect all script elements from the results page
        Elements scripts = htmlDoc.getElementsByTag("script");

        // Loop through all script elements until we find the job list JSON Object
        for (Element script : scripts) {
            // Get HTML content from script tag
            String rawHTML = script.html();

            // Create regex pattern to match the job list JSON
            String pattern = "window\\._initialData=JSON\\.parse\\('(.+)'\\);";

            // Create a Pattern object
            Pattern r = Pattern.compile(pattern);

            // Create a Matcher object
            Matcher m = r.matcher(rawHTML);

            // Test for regex match
            if (m.find()) {
                String jsonObject = m.group(1);
                ObjectMapper objectMapper = new ObjectMapper();
                SearchResults results = objectMapper.readValue(jsonObject, SearchResults.class);
                var jobList = results.getJobList().getJobs();
                for (Job job : jobList) {
                    var jobDetails = GetJobDetails(results.getJobSeekerApiUrl(), results.getJobSeekerApiKey(), job.getJobKey());
                    if (jobDetails.getTitle().contains("Senior") || jobDetails.getTitle().contains("Manager") || jobDetails.getTitle().contains("Front") || jobDetails.getTitle().contains("Principal") || jobDetails.getTitle().contains("VP") || jobDetails.getTitle().contains("Sr.")) {
                        jobDetails.setSkippedByTitle(true);
                        masterJobList.putIfAbsent(jobDetails.getKey(), jobDetails);
                    } else {
                        var jobQualifications = ParseJobDescription(jobDetails.getHtml());
                        gradeJobRequirements(jobQualifications, jobDetails);
                        masterJobList.putIfAbsent(jobDetails.getKey(), jobDetails);
                    }
                }
            }
        }
        // Add overall grades to hash map
        HashMap<String, Integer> gradesMap = new HashMap<>();
        for (var jobEntry : masterJobList.entrySet()) {
            gradesMap.putIfAbsent(jobEntry.getKey(), jobEntry.getValue().getOverallGrade());
        }
        // Sort overall grades and export result csv
        Map<String, Integer> sortedGradesMap = sortByDescValue(gradesMap);
        System.out.println("jobURL,jobTitle,overallGrade,yesCount,maybeCount,noCount,ungradedCount");
        for (Map.Entry<String, Integer> en : sortedGradesMap.entrySet()) {
            JobDetails job = masterJobList.get(en.getKey());
            String output = "https://www.indeed.com/viewjob?jk=" + en.getKey() + ",\"" + job.getTitle() + "\"," + job.getOverallGrade() + "," + job.getYesCount() + "," + job.getMaybeCount() + "," + job.getNoCount() + "," + job.getUngradedCount();
            if (job.isSkippedByTitle()) {
                output = "https://www.indeed.com/viewjob?jk=" + en.getKey() + ",\"" + job.getTitle() + " - SKIPPED BY TITLE\",0,0,0,0,0";
            }
            System.out.println(output);
        }
        // Check if we missed any job qualifications
        // This happens when new jobs are added
        if (ungradedList.size() > 0) {
            System.out.println("UNGRADED QUALIFICATIONS LIST - ADD TO LIST AND RE-RUN");
            for (String qualification : ungradedList) {
                System.out.println(qualification);
            }
        }
    }

    /**
     * Get job details.
     *
     * @param jobSeekerApiUrl the job seeker api url
     * @param jobSeekerApiKey the job seeker api key
     * @param jobKey          the job key
     * @return the job details
     */
    public JobDetails GetJobDetails(String jobSeekerApiUrl, String jobSeekerApiKey, String jobKey) {
        WebClient client = webClient.mutate()
                .baseUrl(jobSeekerApiUrl)
                .defaultHeader(HttpHeaders.USER_AGENT, myProperties.getUserAgent())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .defaultHeader("indeed-api-Key", jobSeekerApiKey)
                .defaultUriVariables(Collections.singletonMap("url", jobSeekerApiUrl))
                .build();
        String jsonData = "{\"query\":\"\\n    query CompJobsPageFetchJobData($jobKey: ID!) {\\n        jobData(jobKeys: [$jobKey]) {\\n            results {\\n                job {\\n                    key\\n                    title\\n                    description {\\n                        html\\n                    }\\n                    indeedApply {\\n                        scopes\\n                    }\\n                    location {\\n                        countryCode\\n                    }\\n                }\\n            }\\n        }\\n    }\\n\",\"variables\":{\"jobKey\":\"" + jobKey + "\"}}";
        JobDetailsResponse response = client.post()
                .uri(jobSeekerApiUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)
                .body(BodyInserters.fromValue(jsonData))
                .retrieve()
                .bodyToMono(JobDetailsResponse.class)
                .block();
        assert response != null;
        return response.getJobDetails();
    }

    /**
     * Parse job description list.
     *
     * @param html the html
     * @return the list
     */
    public List<String> ParseJobDescription(String html) {
        List<String> returnList = new ArrayList<>();
        Document htmlDoc = Jsoup.parse(html);
        String currentHeader = "None";
        boolean appendActivated = false;
        StringBuilder appendString = new StringBuilder();
        for (Element e : htmlDoc.getAllElements()) {
            if (e.className().equals("jobSectionHeader")) {
                currentHeader = e.text();
            } else if (e.tagName().equals("li") && currentHeader.equals("Who You Are")) {
                if (e.text().endsWith("following:")) {
                    appendActivated = true;
                    if (appendString.length() == 0) {
                        appendString = new StringBuilder(e.text());
                    } else {
                        appendString.append(" ").append(e.text());
                    }
                }
                if (!appendActivated && !qualificationsList.contains(e.text())) {
                    qualificationsList.add(e.text());
                    returnList.add(e.text());
                } else if (appendActivated) {
                    if (!e.text().endsWith("or")) {
                        qualificationsList.add(appendString.toString());
                        returnList.add(appendString.toString());
                        appendActivated = false;
                        appendString = new StringBuilder();
                    }
                }
            }
        }
        return returnList;
    }

    /**
     * Load qualifications map
     *
     * @throws IOException  the io exception
     * @throws CsvException the csv exception
     */
    public void LoadYesNoMaybe() throws IOException, CsvException {
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(',')
                .withIgnoreQuotations(false)
                .build();

        CSVReader csvReader = new CSVReaderBuilder(new FileReader("src/main/resources/qualifications.csv"))
                .withSkipLines(0)
                .withCSVParser(parser)
                .build();

        List<String[]> myEntries = csvReader.readAll();
        for (String[] entry : myEntries) {
            qualificationsMap.putIfAbsent(entry[0], entry[1]);
        }
    }

    /**
     * Grade job requirements.
     *
     * @param jobQualifications the job qualifications
     * @param jobDetails        the job details
     */
    public void gradeJobRequirements(List<String> jobQualifications, JobDetails jobDetails) {
        for (String qualification : jobQualifications) {
            if (qualificationsMap.containsKey(qualification)) {
                switch (qualificationsMap.get(qualification)) {
                    case "yes":
                        jobDetails.increaseYesCount();
                        break;
                    case "no":
                        jobDetails.increaseNoCount();
                        break;
                    case "maybe":
                        jobDetails.increaseMaybeCount();
                        break;
                    default:
                        jobDetails.increaseUngradedCount();
                        ungradedList.add(qualification);
                        break;
                }
            } else {
                jobDetails.increaseUngradedCount();
                ungradedList.add(qualification);
            }
        }
    }
}