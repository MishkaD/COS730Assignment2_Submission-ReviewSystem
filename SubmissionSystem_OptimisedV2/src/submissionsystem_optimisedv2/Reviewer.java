package submissionsystem_optimisedv2;
/*

 Unchanged from baseline

 */
public class Reviewer {

    private String id;
    private String name;
    private String expertise;
    private int    workload;
    private double score;

    public Reviewer(String id, String name, String expertise) {
        this.id        = id;
        this.name      = name;
        this.expertise = expertise;
        this.workload  = 0;
        this.score     = 0.0;
    }

    

     //Sequence diagram: loop [each reviewer] -> Reviewer : submitScore(score)
      //reviewer evaluates the paper and returns their score
    
    public double submitScore(double scoreValue) {
        this.score = scoreValue;
        return this.score;
    }

    public String getId()        { return id; }
    public String getName()      { return name; }
    public String getExpertise() { return expertise; }
    public int    getWorkload()  { return workload; }
    public double getScore()     { return score; }

    public void setWorkload(int workload) { this.workload = workload; }
    public void incrementWorkload()       { this.workload++; }
}
