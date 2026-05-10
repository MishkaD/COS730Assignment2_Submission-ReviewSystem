package submissionsystem_baselinev2;
import java.util.List;

/*
 -The central coordinator of the entire submission process
 -After the UI forwards a submission here, this class drives every step
 - The steps it performs
  1. Call Validator to check file format
  2. invalid: return error to UI and stop
  3.  valid: save the submission to the Database
  4. Ask ReviewerManager for the available reviewers
  5. Loop: assign each reviewer to the submission
  6. Start the evaluation process

 - Sequence diagram :
  UI --> SubmissionController          : submit(data)
   SubmissionController --> Validator   : validateFormat(data)
   [alt invalid] --> UI                 : return error
   [alt valid]:
      SubmissionController --> Database  : saveSubmission(data)
      SubmissionController --> ReviewerManager : getAvailableReviewers()
      [loop assign reviewers]:
        SubmissionController --> ReviewerManager : assignReview()
        SubmissionController --> EvaluationManager : startEvaluation()

 */
public class SubmissionController {

    private Validator         validator;
    private Database          database;
    private ReviewerManager   reviewerManager;
    private EvaluationManager evaluationManager;

    // Counter for  method calls are made 
    //ben chmark
    private int methodCallCount = 0;

    public SubmissionController(Validator validator, Database database,
                                ReviewerManager reviewerManager,
                                EvaluationManager evaluationManager) {
        this.validator         = validator;
        this.database          = database;
        this.reviewerManager   = reviewerManager;
        this.evaluationManager = evaluationManager;
    }

    /*
    Receives the submission from the UI and processes it
    Sequence diagram: UI --> SubmissionController : submit(data)
     */
    public void submit(Submission submission) {
        methodCallCount++;

        // STEP 1: Validate format 
        // Sequence diagram: SubmissionController --> Validator : validateFormat(data)
        //                   Validator --> SubmissionController : valid/invalid
     
        methodCallCount++;
        boolean formatIsValid = validator.validateFormat(submission);

        //  ALT invalid 
        // If the format check failed, stop here and return an error
        // Sequence diagram: alt [invalid] --> return error
 
        if (!formatIsValid) 
        {
            methodCallCount++;          
            return;
        }

        //ALT [valid] 
        // STEP 2: Save the submission 
        // Sequence diagram: SubmissionController --> Database : saveSubmission(data)
        //                   Database --> SubmissionController : confirmation
        methodCallCount++;
        String confirmation = database.saveSubmission(submission);

        // STEP 3: Get available reviewers 
        // Internally: fetchReviewers -> filterConflicts -> checkWorkload
        // Sequence diagram: SubmissionController --> ReviewerManager : getAvailableReviewers()
        //                   ReviewerManager --> SubmissionController : filteredReviewers
        methodCallCount++;
        List<Reviewer> filteredReviewers = reviewerManager.getAvailableReviewers(submission);

         //  max of 3 reviewers per sub
        int toAssign = Math.min(3, filteredReviewers.size());
        List<Reviewer> assignedReviewers = filteredReviewers.subList(0, toAssign);

        // STEP 4: LOOP — assign reviewers 
        // The sequence diagram shows a loop labelled [assign reviewers]
        // SubmissionController calls assignReview() once per reviewer

        for (Reviewer reviewer : assignedReviewers)
        {
            methodCallCount++;
            reviewerManager.assignReview(reviewer, submission);
        }

        // STEP 5: Start evaluation 
        // Sequence diagram: SubmissionController --> EvaluationManager : startEvaluation()
        methodCallCount++;
        evaluationManager.startEvaluation(submission, assignedReviewers);
    }

    public int getMethodCallCount() { return methodCallCount; }
    public void resetCallCount()    { methodCallCount = 0; }
}
