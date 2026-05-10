package submissionsystem_baselinev2;
/*
 - Represents the user interface that the Researcher interacts with.
 - When the researcher clicks "Submit", this class receives the submission
   and forwards it to the SubmissionController for processing

 -Sequence diagram :
    Researcher --> UI                    : submitResearchOutput(data)
    UI --> SubmissionController          : submit(data)

 */
public class UI {

    private SubmissionController submissionController;

    public UI(SubmissionController submissionController) {
        this.submissionController = submissionController;
    }

    /*

     - Called by the Researcher when they submit their paper
     - passes the submission on to the SubmissionController
     */
    public void submitResearchOutput(Submission submission) {
        submissionController.submit(submission);
    }
}
