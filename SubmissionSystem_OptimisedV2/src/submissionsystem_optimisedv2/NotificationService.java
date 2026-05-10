package submissionsystem_optimisedv2;

/*

  Changed from baseline. 

  Baseline:
   EvaluationManager called notifyAcceptance() / notifyRejection() / notifyRevision() directly
   was three separate methods, called from the wrong class

  Optimised:
   SubmissionController calls sendOutcome(outcome, submission) one method
   controller passes the outcome string and the submission object
   NotificationService decides what message to send based on the outcome

 Sequence Diagram:
  SubmissionController --> NotificationService : sendOutcome(outcome, data)
  NotificationService --> Researcher           : sendNotification()

 */
public class NotificationService {

    /*
     Single entry point for all notifications
     notifyAcceptance/Rejection/Revision() methods from the baseline.

    Sequence diagram:
    SubmissionController --> NotificationService : sendOutcome(outcome, data)
     NotificationService --> Researcher           : sendNotification()
     */
    public void sendOutcome(String outcome, Submission submission) {
        switch (outcome) {
            case "ACCEPTED":
                sendNotification(submission, "ACCEPTED",
                    "Congratulations! Your paper has been accepted for publication.");
                break;
            case "REJECTED":
                sendNotification(submission, "REJECTED",
                    "Unfortunately, your paper did not meet the required standard. " +
                    "Please review feedback and consider resubmitting.");
                break;
            default: // REVISION
                sendNotification(submission, "REVISION REQUIRED",
                    "Your paper shows promise but requires revisions before " +
                    "it can be accepted. Please address reviewer feedback and resubmit.");
                break;
        }
    }

    /*
     Delivers the message to the researcher
      Sequence: NotificationService --> Researcher : sendNotification()
     */
    private void sendNotification(Submission submission, String outcome, String message) {
        System.out.println("    Outcome     : " + outcome);
        System.out.println("    Paper       : " + submission.getTitle());
        System.out.println("    Researcher  : " + submission.getAuthorId());
        System.out.println("    Message     : " + message);
    }
}
