package submissionsystem_baselinev2;

/**

-Responsible for sending the final outcome notification to the researcher

- last step in the process. After EvaluationManager has made
 the decision (ACCEPTED / REJECTED / REVISION), it calls one of the three methods below. 
 Each method internally calls sendNotification() to deliver the message to the researcher.

-Sequence diagram traces:
 [alt accepted]  EvaluationManager --> NotificationService : notifyAcceptance()
 [alt rejected]  EvaluationManager --> NotificationService : notifyRejection()
 [alt revision]  EvaluationManager --> NotificationService : notifyRevision()
 NotificationService --> Researcher : sendNotification()

 */
public class NotificationService {

    /*
      notifyAcceptance()
      Sequence diagram alt branch: [accepted]
     */
    public void notifyAcceptance(Submission submission) {
        sendNotification(submission,
            "ACCEPTED",
            "Congratulations! Your paper has been accepted for publication.");
    }

    /*
      notifyRejection()
     Sequence diagram alt branch: [rejected]
     */
    public void notifyRejection(Submission submission) {
        sendNotification(submission,
            "REJECTED",
            "Unfortunately, your paper does not meet the required standard. " +
            "Please review the feedback and consider resubmitting.");
    }

    /**
     notifyRevision()
    Sequence diagram alt branch: [revision]
     */
    public void notifyRevision(Submission submission) {
        sendNotification(submission,
            "REVISION REQUIRED",
            "Your paper has potential but requires revisions before it can " +
            "be accepted. Please address the reviewer feedback and resubmit.");
    }

    /*
  
      The actual message delivery to the researcher
      Sequence diagram: NotificationService --> Researcher : sendNotification()
    Public convenience method used by Main to display notification output
     */
    public void sendNotification(String outcome, Submission submission) {
        switch (outcome) 
        {
            case "ACCEPTED": notifyAcceptance(submission); break;
            case "REJECTED": notifyRejection(submission);  break;
            default:         notifyRevision(submission);   break;
        }
    }

    /*
     
    The actual message delivery to the researcher
     Sequence diagram: NotificationService --> Researcher : sendNotification()
     */
    private void sendNotification(Submission submission, String outcome, String message) {
        System.out.println("    Outcome     : " + outcome);
        System.out.println("    Paper       : " + submission.getTitle());
        System.out.println("    Researcher  : " + submission.getAuthorId());
        System.out.println("    Message     : " + message);
    }

}
