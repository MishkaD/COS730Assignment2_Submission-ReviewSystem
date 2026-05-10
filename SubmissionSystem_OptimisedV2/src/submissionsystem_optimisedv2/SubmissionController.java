package submissionsystem_optimisedv2;

import java.util.List;

/**
 This class is no longer a "God class"
BASELINE had the controller:
   - Calling Validator directly
   - Running the reviewer assignment loop itself
   - Passing control to EvaluationManager which then called NotificationService
   - Knowing about 4 other classes directly
 
 * OPTIMISED controller:
   - Asks DecisionTable to validate format (no separate Validator class)
   - Asks ReviewerManager to assign reviewers (no loop here)
   - Asks EvaluationManager to evaluate (receives outcome string back)
   - Calls NotificationService.sendOutcome() with the result
 
 Sequence diagram:
    UI --> SubmissionController                        : submit(data)
    SubmissionController --> DecisionTable             : isFormatValid(data)
    DecisionTable --> SubmissionController             : valid/invalid
    [alt invalid] SC --> UI                            : return error
    [alt valid]:
      SC --> Database                                  : saveSubmission(data)
      SC --> ReviewerManager                           : assignBestReviewers(data)
      ReviewerManager --> SC                           : success
      SC --> EvaluationManager                         : evaluate(submission)
      EvaluationManager --> SC                         : outcome
      SC --> NotificationService                       : sendOutcome(outcome, data)
      NotificationService --> Researcher               : sendNotification()

 */
public class SubmissionController {

    private DecisionTable       decisionTable;
    private Database            database;
    private ReviewerManager     reviewerManager;
    private EvaluationManager   evaluationManager;
    private NotificationService notificationService;

    // Counts method calls
    private int methodCallCount = 0;

    public SubmissionController(DecisionTable decisionTable,
                                Database database,
                                ReviewerManager reviewerManager,
                                EvaluationManager evaluationManager,
                                NotificationService notificationService) {
        this.decisionTable       = decisionTable;
        this.database            = database;
        this.reviewerManager     = reviewerManager;
        this.evaluationManager   = evaluationManager;
        this.notificationService = notificationService;
    }

  

     // delegates every step to the responsible class
     // Sequence: UI --> SubmissionController : submit(data)
   
    public void submit(Submission submission) {
        methodCallCount++; // 1. submit()

        // STEP 1: Researcher submits via UI
        log("[STEP 1] Researcher submits paper via the UI");
        log("         Sequence: Researcher --> UI --> SubmissionController : submit(data)");
        blank();

        // STEP 2: Format validation via DecisionTable 
        // Sequence: SubmissionController --> DecisionTable : isFormatValid(data)
        //           DecisionTable --> SubmissionController : valid/invalid

        log("[STEP 2] SubmissionController calls DecisionTable to validate format");
        log("         Sequence: SubmissionController --> DecisionTable : isFormatValid(data)");
        log("         (No separate Validator class — DecisionTable owns this rule)");
        blank();

        methodCallCount++; 
        boolean formatIsValid = decisionTable.isFormatValid(submission);

        // ALT [invalid]
        if (!formatIsValid) 
        {
            methodCallCount++; // 3. return error
            log("  [ALT: INVALID FORMAT]");
            log("  +----------------------------------------------------------+");
            log("  | Format '" + submission.getFormat()
                    + "' is NOT accepted.                          |");
            log("  | DecisionTable returned: invalid                          |");
            log("  | Sequence: SC --> UI : return error                       |");
            log("  | Process stops here.                                      |");
            log("  +----------------------------------------------------------+");
            return;
        }

        // ALT [valid]
        log("  DecisionTable returned: valid  [format = " + submission.getFormat() + "]");
        blank();

        //STEP 3: Save submission 
        // Sequence: SubmissionController --> Database : saveSubmission(data)
        //           Database --> SubmissionController : confirmation
  
        log("[STEP 3] Saving submission to database");
        log("         Sequence: SubmissionController --> Database : saveSubmission(data)");
        methodCallCount++; // 3. saveSubmission()
        String confirmation = database.saveSubmission(submission);
        log("         Confirmation: " + confirmation);
        blank();

        //STEP 4: Assign reviewers via ReviewerManager
        // ONE call — ReviewerManager handles fetch, eligibility, and assignment internally
        // Sequence: SC --> ReviewerManager : assignBestReviewers(data)
        //           ReviewerManager --> SC : success

        log("[STEP 4] Assigning reviewers");
        log("         Sequence: SC --> ReviewerManager : assignBestReviewers(data)");
        log("         (Fetch + eligibility check + assignment all done inside ReviewerManager)");
        log("         (Controller no longer runs an assignment loop — that is now encapsulated)");
        blank();

        methodCallCount++; // 4. assignBestReviewers()
        List<Reviewer> assignedReviewers = reviewerManager.assignBestReviewers(submission);

        // STEP 5: Evaluate 
        // EvaluationManager collects scores, batch-saves, calls DecisionTable, RETURNS the outcome. NO call NotificationService
         // Sequence: SC --> EvaluationManager : evaluate(submission)
        //           EvaluationManager --> SC : outcome

        log("[STEP 5] Starting evaluation");
        log("         Sequence: SC --> EvaluationManager : evaluate(submission)");
        log("         (EvaluationManager will return an outcome string — not call NS directly)");
        blank();

        methodCallCount++; // 5. evaluate()
        String outcome = evaluationManager.evaluate(submission, assignedReviewers);

        // STEP 6: Notify researcher — controller's responsibility 
        // Decoupled notification: EvaluationManager has NO knowledge of NS
         // The controller receives the outcome and decides who to notify
       // Sequence: SC --> NotificationService : sendOutcome(outcome, data)
        //           NotificationService --> Researcher : sendNotification()

        log("[STEP 6] Decoupled notification — SubmissionController sends outcome");
        log("         Sequence: SC --> NotificationService : sendOutcome(outcome, data)");
        log("         (EvaluationManager has no knowledge of NotificationService)");
        blank();
        log("  +----------------------------------------------------------+");
        log("  |  NOTIFICATION TO RESEARCHER                              |");
        methodCallCount++; // 6. sendOutcome()
        notificationService.sendOutcome(outcome, submission);
        log("  +----------------------------------------------------------+");
        blank();
        log("         NotificationService --> Researcher : sendNotification()  [delivered]");
        blank();
    }

    private void log(String msg) { System.out.println("  " + msg); }
    private void blank()         { System.out.println(); }

    public int  getMethodCallCount() { return methodCallCount; }
    public void resetCallCount()     { methodCallCount = 0; }
}
