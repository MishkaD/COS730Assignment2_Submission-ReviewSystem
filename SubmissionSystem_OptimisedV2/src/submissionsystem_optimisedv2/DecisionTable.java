package submissionsystem_optimisedv2;

/*

 It replaces THREE scattered decision points from the baseline
 1. Validator.validateFormat()       -> now: isFormatValid(data)
 2. ReviewerManager.filterConflicts() + ReviewerManager.checkWorkload() -> now: isReviewerEligible()
  3. EvaluationManager.applyRules()   -> now: determineOutcome(avg, consensus)



 Sequence diagram
 SubmissionController --> DecisionTable : isFormatValid(data)
 DecisionTable --> SubmissionController : valid/invalid
 ReviewerManager uses DecisionTable internally for eligibility checks
 EvaluationManager --> DecisionTable : determineOutcome(avg, consensus)
 DecisionTable --> EvaluationManager : outcome

 DECISION TABLE 1 Format Validation
 DECISION TABLE 2 Reviewer Eligibility
 DECISION TABLE 3 — Outcome Determination
 */
public class DecisionTable {


    // Format validation 

    private static final String[] ACCEPTED_FORMATS = { "PDF", "DOC", "DOCX" };

    // Reviewer eligibility max
    public static final int MAX_WORKLOAD = 4;
    public static final double ACCEPT_THRESHOLD = 7.0;
    public static final double REJECT_THRESHOLD = 4.0;
    public static final double CONSENSUS_MARGIN = 2.0;

 
    // METHOD 1: isFormatValid(data)
    // Sequence: SubmissionController --> DecisionTable : isFormatValid(data)
    public boolean isFormatValid(Submission submission) {
        if (submission.getFormat() == null) return false;

        for (String accepted : ACCEPTED_FORMATS) {
            if (submission.getFormat().equalsIgnoreCase(accepted)) {
                return true;
            }
        }
        return false;
    }


    // METHOD 2: isReviewerEligible(reviewer, authorId)
    // Used internally by ReviewerManager.assignBestReviewers()

    public boolean isReviewerEligible(Reviewer reviewer, String authorId) {
        boolean hasConflict = reviewer.getId().equals("R001")
                           && authorId.equals("A001");
        boolean overloaded = reviewer.getWorkload() >= MAX_WORKLOAD;
        return !hasConflict && !overloaded;
    }


    // METHOD 3: determineOutcome(avg, consensus)
     // Sequence: EvaluationManager --> DecisionTable : determineOutcome(avg, consensus)
     //           DecisionTable --> EvaluationManager : outcome


    public String determineOutcome(double average, boolean consensus) {
        //  ACCEPTED
        if (average >= ACCEPT_THRESHOLD && consensus) 
        {
            return "ACCEPTED";
        }

        //  REVISION
        if (average >= ACCEPT_THRESHOLD && !consensus) 
        {
            return "REVISION";
        }

        // REJECTED
        if (average < REJECT_THRESHOLD) 
        {
            return "REJECTED";
        }

        //  REVISION 
        return "REVISION";
    }
}
