package submissionsystem_optimisedv2;

import java.util.ArrayList;
import java.util.List;

/*

 changed from baseline

 CHANGE 1 : Batch score saving 
 Baseline: saveScore() called once per reviewer inside the loop
            -> 3 reviewers = 3 database writes
  Optimised: all scores collected first, then saveScores(batch) called ONCE
           -> 3 reviewers = 1 database write

 CHANGE 2 : Decision delegated to DecisionTable
    Baseline: applyRules() lived inside EvaluationManager 
    Optimised: determineOutcome(avg, consensus) called on DecisionTable
             
 CHANGE 3 : evaluate() RETURNS outcome instead of calling NotificationService
   Baseline: EvaluationManager called notifyAcceptance/Rejection/Revision()
            directly 
  Optimised: evaluate() returns the outcome STRING to SubmissionController
            -> controller decides who to notify (correct responsibility)

 Sequence diagram 
    SubmissionController --> EvaluationManager : evaluate(submission)
    loop [each reviewer]:
    Reviewer --> EvaluationManager : submitScore(score)   
    EvaluationManager --> Database : saveScores(batch)      
    EvaluationManager --> DecisionTable : determineOutcome(avg, consensus)
    DecisionTable --> EvaluationManager : outcome
    EvaluationManager --> SubmissionController : outcome
 */
public class EvaluationManager {

    private Database      database;
    private DecisionTable decisionTable;

    public EvaluationManager(Database database, DecisionTable decisionTable) {
        this.database      = database;
        this.decisionTable = decisionTable;
    }

 
    // Called by SubmissionController.

    
      //Sequence diagram: SubmissionController --> EvaluationManager : evaluate(submission)
      //    EvaluationManager --> SubmissionController : outcome  [return]
     
    public String evaluate(Submission submission, List<Reviewer> reviewers) {
        log("[EvaluationManager] evaluate(submission) called");
        log("  Sequence: SubmissionController --> EvaluationManager : evaluate(submission)");
        blank();


        // Collect ALL scores into a local list first
         // Sequence: loop [each reviewer] -> Reviewer : submitScore(score)
    
        log("  LOOP [each reviewer submits score]:");
        log("  Sequence: loop [each reviewer] --> Reviewer : submitScore(score)");
        log("  NOTE: scores are COLLECTED here, NOT saved one by one (N+1 fix)");
        blank();

        List<Double> collectedScores = new ArrayList<>();

        for (Reviewer reviewer : reviewers) {
            double score = reviewer.submitScore(getSimulatedScore(reviewer));
            collectedScores.add(score); // collect, don't save yet
            log("  " + reviewer.getName() + " submits score: " + score + " / 10");
        }
        blank();

        // BATCH SAVE once in database write for all scores
             // Sequence: EvaluationManager --> Database : saveScores(batch)

        log("  Batch update — solves N+1 problem:");
        log("  Sequence: EvaluationManager --> Database : saveScores(batch)");
        log("  Saving all " + collectedScores.size() + " scores in ONE database call");
        log("  (Baseline made " + collectedScores.size() + " separate calls — now just 1)");
        database.saveScores(submission.getId(), collectedScores);
        blank();

        //Calculate average
        double average = calculateAverage(collectedScores);
        log("  Average score : " + String.format("%.2f", average) + " / 10");

          //Check consensus
        boolean consensus = checkConsensus(collectedScores);
        double  stdDev    = getStdDev(collectedScores);
        log("  Std deviation : " + String.format("%.2f", stdDev)
                + "  (threshold: " + DecisionTable.CONSENSUS_MARGIN + ")");
        log("  Reviewers agreed: " + (consensus ? "YES" : "NO"));
        blank();

        //  Delegate outcome decision to DecisionTable
         // Sequence: EvaluationManager --> DecisionTable : determineOutcome(avg, consensus)
          //           DecisionTable --> EvaluationManager : outcome
       
        log("  Business rules centralised in DecisionTable:");
        log("  Sequence: EvaluationManager --> DecisionTable"
                + " : determineOutcome(avg, consensus)");
        log("  THRESHOLDS (all defined in DecisionTable — one source of truth):");
        log("    Accept threshold : avg >= " + DecisionTable.ACCEPT_THRESHOLD);
        log("    Reject threshold : avg <  " + DecisionTable.REJECT_THRESHOLD);
        log("    Consensus margin : std dev <= " + DecisionTable.CONSENSUS_MARGIN);
        blank();

        String outcome = decisionTable.determineOutcome(average, consensus);
        logDecisionReason(average, consensus, outcome);
        blank();

        log("  DecisionTable --> EvaluationManager : outcome = " + outcome);
        blank();

        // Update submission status
        submission.setStatus(outcome);

        // Return outcome to SubmissionController 
        log("  EvaluationManager --> SubmissionController : outcome");
        log("  (NotificationService is NOT called here — that is the controller's job)");
        blank();

        return outcome;
    }

    // Calculation helpers functiosn

    private double calculateAverage(List<Double> scores) {
        if (scores.isEmpty()) return 0.0;
        double total = 0;
        for (double s : scores) total += s;
        return total / scores.size();
    }

    private boolean checkConsensus(List<Double> scores) {
        return getStdDev(scores) <= DecisionTable.CONSENSUS_MARGIN;
    }

    private double getStdDev(List<Double> scores) {
        if (scores.size() < 2) return 0.0;
        double sum = 0;
        for (double s : scores) sum += s;
        double mean = sum / scores.size();
        double varianceSum = 0;
        for (double s : scores) varianceSum += Math.pow(s - mean, 2);
        return Math.sqrt(varianceSum / scores.size());
    }

    // reason log

    private void logDecisionReason(double avg, boolean consensus, String outcome) {
        String a = String.format("%.2f", avg);
        if      (outcome.equals("ACCEPTED") )              log("  avg " + a + " >= "
                + DecisionTable.ACCEPT_THRESHOLD + "  AND  reviewers agreed  -->  ACCEPTED");
        else if (avg >= DecisionTable.ACCEPT_THRESHOLD)    log("  avg " + a + " >= "
                + DecisionTable.ACCEPT_THRESHOLD + "  BUT  reviewers disagreed  -->  REVISION");
        else if (avg < DecisionTable.REJECT_THRESHOLD)     log("  avg " + a + " < "
                + DecisionTable.REJECT_THRESHOLD + "  -->  REJECTED");
        else                                               log("  avg " + a + " between "
                + DecisionTable.REJECT_THRESHOLD + " and "
                + DecisionTable.ACCEPT_THRESHOLD + "  -->  REVISION");
    }

    private double getSimulatedScore(Reviewer reviewer) {
        switch (reviewer.getId()) {
            case "R001": return 8.0;
            case "R002": return 7.5;
            case "R003": return 5.0;
            case "R004": return 8.5;
            case "R005": return 3.0;
            default:     return 6.0;
        }
    }

    private void log(String msg) { System.out.println("  " + msg); }
    private void blank()         { System.out.println(); }
}
