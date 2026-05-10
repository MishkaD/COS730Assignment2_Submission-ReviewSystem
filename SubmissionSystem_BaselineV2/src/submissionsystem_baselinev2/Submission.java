package submissionsystem_baselinev2;

/**
 - Represents a research output that a Researcher wants to submit for review

-This object is created when the Researcher clicks "submit" on the UI
-travels through the entire system from validation, to reviewer
  assignment, to evaluation — and ends with a final status of
  ACCEPTED, REJECTED, or REVISION
 
 - Sequence diagram lifeline: not a lifeline itself, but the "data" object
-passed along every arrow in the diagram ...the data in submit(data),
 - saveSubmission(data), validateFormat(data))

 */
public class Submission {

    private String id;        // Unique submission identifier e.g. "SUB001"
    private String title;     //  Title of the research paper
    private String authorId;  // Id of the researcher who submitted it
    private String content;   // The actual paper content
    private String format;    // File format e.g. "PDF", "DOCX", "TXT"
    private String status;    // Current status - starts as "PENDING"

    public Submission(String id, String title, String authorId, String content, String format) {
        this.id       = id;
        this.title    = title;
        this.authorId = authorId;
        this.content  = content;
        this.format   = format;
        this.status   = "PENDING"; // start
    }

    // Getters
    public String getId()        { return id; }
    public String getTitle()     { return title; }
    public String getAuthorId()  { return authorId; }
    public String getContent()   { return content; }
    public String getFormat()    { return format; }
    public String getStatus()    { return status; }

     //updated at the end of the process by EvaluationManager
     public void setStatus(String status) { this.status = status; }
}
