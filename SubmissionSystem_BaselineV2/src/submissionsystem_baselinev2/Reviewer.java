package submissionsystem_baselinev2;
/**

 - single reviewer in the system that evaluates submitted research papers
 -Sequence diagram lifeline: "Reviewer"
 The diagram shows: Reviewer --> EvaluationManager : submitScore(score)
 This is implemented as the submitScore() method below

 */
public class Reviewer {

    private String id;         // Unique reviewer ID e.g. "R001"
    private String name;       // Full name e.g. "Dr. Mrshall"
    private String expertise;  // Research area e.g. "Artificial Intelligence"
    private int    workload;   // no# papers they are currently reviewing
    private double score;      // The score they gave for the current submission

    public Reviewer(String id, String name, String expertise) {
        this.id        = id;
        this.name      = name;
        this.expertise = expertise;
        this.workload  = 0;    // New reviewers start with no assigned papers
        this.score     = 0.0;
    }

    /*
    
     Reviewer --> EvaluationManager : submitScore(score)
     The reviewer evaluates the paper and provides a score between 0 and 10
      collected by the EvaluationManager to calculate the overall result
     */
    public double submitScore(double scoreValue) {
        this.score = scoreValue;
        return this.score;
    }

    // Getters
    public String getId()        { return id; }
    public String getName()      { return name; }
    public String getExpertise() { return expertise; }
    public int    getWorkload()  { return workload; }
    public double getScore()     { return score; }

    public void setWorkload(int workload)  { this.workload = workload; }
    public void incrementWorkload()        { this.workload++; }
}
