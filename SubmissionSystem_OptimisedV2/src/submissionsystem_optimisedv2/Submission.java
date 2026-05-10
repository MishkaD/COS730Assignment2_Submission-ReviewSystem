package submissionsystem_optimisedv2;

/*
  Unchanged from baseline
  status of ACCEPTED, REJECTED, or REVISION.

 */
public class Submission {

    private String id;
    private String title;
    private String authorId;
    private String content;
    private String format;
    private String status;

    public Submission(String id, String title, String authorId,
                      String content, String format) {
        this.id       = id;
        this.title    = title;
        this.authorId = authorId;
        this.content  = content;
        this.format   = format;
        this.status   = "PENDING";
    }

    public String getId()        { return id; }
    public String getTitle()     { return title; }
    public String getAuthorId()  { return authorId; }
    public String getContent()   { return content; }
    public String getFormat()    { return format; }
    public String getStatus()    { return status; }
    public void   setStatus(String status) { this.status = status; }
}
