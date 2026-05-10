package submissionsystem_optimisedv2;
import java.util.ArrayList;
import java.util.List;

/**

 Changed three-method chain from the baseline (getAvailableReviewers -> filterConflicts -> checkWorkload -> assignReview loop)
 is replaced by assignBestReviewers(data).
 
Baseline flow (driven by SubmissionController):
  SC -> RM.getAvailableReviewers()
  RM -> RM.filterConflicts()     [loop 1 over reviewer list]
  RM -> RM.checkWorkload()       [loop 2 over same list]
  SC -> RM.assignReview() x3      [controller ran the assignment loop]

Optimised flow (driven internally):
  SC -> RM.assignBestReviewers(data)
  RM -> DB.fetchReviewers()     [get list]
   RM -> internal single pass    [check eligibility via DecisionTable]
   RM -> Reviewer.assignReview() [assign internally, not by controller]
   RM -> SC : success              [returns assigned list]

 * Sequence diagram
 SubmissionController --> ReviewerManager : assignBestReviewers(data)
 ReviewerManager --> Database             : fetchReviewers()
Database --> ReviewerManager             : reviewerList
 ReviewerManager [internal]               : eligibility check & assignReview()
 ReviewerManager --> SubmissionController : success

 */
public class ReviewerManager {

    private Database      database;
    private DecisionTable decisionTable;

    // Maxi reviewers 
    private static final int MAX_REVIEWERS = 3;

    /*
     * Pre-loaded reviewer pool (simulates a database table of reviewers).
     *
     * Reviewer workloads:
     *   R001  - workload 0 (but has conflict with author A001)
     *   R002 - workload 0 (always available)
     *   R003 - workload 5 (TOO BUSY )
     *   R004 - workload 0 (always available)
     *   R005 - workload 0 (always available)
     */
    private List<Reviewer> allReviewers;

    public ReviewerManager(Database database, DecisionTable decisionTable) {
        this.database      = database;
        this.decisionTable = decisionTable;
        this.allReviewers  = new ArrayList<>();
        loadReviewers();
    }

    private void loadReviewers() {
        allReviewers.add(new Reviewer("R001", "Dr. Pillay",   "Artificial Intelligence"));
        allReviewers.add(new Reviewer("R002", "Prof. Singh",  "Software Engineering"));
        allReviewers.add(new Reviewer("R003", "Dr. Marshall",   "Data Science"));
        allReviewers.add(new Reviewer("R004", "Prof. Makura", "Systems Design"));
        allReviewers.add(new Reviewer("R005", "Dr. Bosman",      "Artificial Intelligence"));

        // Dr. Marshall has 5 active reviews, over limit of 4
        allReviewers.get(2).setWorkload(5);
    }

    /**
    assignBestReviewers(data)
    replaces getAvailableReviewers() + external assignReview() loop.

      1. Fetches reviewers from DB (one call, same as baseline)
      2. Checks eligibility in a SINGLE PASS using DecisionTable
           (baseline used TWO passes: filterConflicts then checkWorkload)
      3. Assigns internally — the controller no longer runs the loop
  
     Sequence diagram
     SC --> RM : assignBestReviewers(data)
     RM --> DB : fetchReviewers()
      DB --> RM : reviewerList
      RM [internal eligibility check & assignment]
     RM --> SC : success
     */
    public List<Reviewer> assignBestReviewers(Submission submission) {
        log("[ReviewerManager] assignBestReviewers(data) called");
        log("  Sequence: SubmissionController --> ReviewerManager : assignBestReviewers(data)");
        blank();

        // Fetch all reviewers from the database
        // Sequence: ReviewerManager --> Database : fetchReviewers()
        log("  Fetching reviewer list from database...");
        log("  Sequence: ReviewerManager --> Database : fetchReviewers()");
        List<Reviewer> reviewerList = new ArrayList<>(allReviewers);
        log("  Database --> ReviewerManager : reviewerList (" + reviewerList.size() + " reviewers)");
        blank();

        //  Single-pass eligibility check + assignment
        // This replaces the TWO separate loops from the baseline:
        //   filterConflicts() [loop 1] + checkWorkload() [loop 2]
         // Now done in ONE loop 
        log("  [Internal eligibility check & assignment — single pass]");
        log("  DecisionTable checks each reviewer against Table 2 rules:");
        log("  (Baseline ran two separate loops here — this is one loop)");
        blank();

        List<Reviewer> assigned = new ArrayList<>();

        for (Reviewer r : reviewerList) {
            if (assigned.size() >= MAX_REVIEWERS) break;

            // DecisionTable evaluates BOTH conflict AND workload in one call
            boolean eligible = decisionTable.isReviewerEligible(r, submission.getAuthorId());

            if (eligible) {
                // Assign directly here ,no need for controller to call assignReview()
                r.incrementWorkload();
                assigned.add(r);
                log("  " + r.getName() + " [" + r.getExpertise() + "]");
                log("    -> ELIGIBLE   -- assigned (workload now " + r.getWorkload() + ")");
            } else {
                boolean conflict   = r.getId().equals("R001")
                                  && submission.getAuthorId().equals("A001");
                boolean overloaded = r.getWorkload() >= DecisionTable.MAX_WORKLOAD;
                String  reason     = conflict   ? "conflict of interest with author "
                                                 + submission.getAuthorId()
                                 : overloaded  ? "workload " + r.getWorkload()
                                                 + " >= limit of " + DecisionTable.MAX_WORKLOAD
                                 :               "not eligible";
                log("  " + r.getName() + " [" + r.getExpertise() + "]");
                log("    -> EXCLUDED   -- " + reason);
            }
        }

        blank();
        log("  " + assigned.size() + " reviewer(s) assigned.");
        log("  ReviewerManager --> SubmissionController : success");
        blank();
        return assigned;
    }


   
     //Returns all reviewers 
     //Sequence: ReviewerManager --> Database : fetchReviewers()
     
    public List<Reviewer> fetchReviewers() {
        return new ArrayList<>(allReviewers);
    }

    private void log(String msg)  { System.out.println("  " + msg); }
    private void blank()          { System.out.println(); }
}
