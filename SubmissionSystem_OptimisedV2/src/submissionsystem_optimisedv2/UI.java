package submissionsystem_optimisedv2;

/**

 Unchanged from baseline. 
Sequence diagram
  Researcher --> UI                   : submitResearchOutput(data)
  UI --> SubmissionController         : submit(data)
 */
public class UI {

    private SubmissionController submissionController;

    public UI(SubmissionController submissionController) {
        this.submissionController = submissionController;
    }

    public void submitResearchOutput(Submission submission) {
        submissionController.submit(submission);
    }
}
