package submissionsystem_baselinev2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*

 Simulates a database that stores submissions and reviewer scores
 Sequence diagram :
 SubmissionController --> Database : saveSubmission(data)
 Database --> SubmissionController : confirmation
 EvaluationManager --> Database : saveScore(score)   [called once per reviewer]

 */
public class Database {

    // Stores all submitted papers, keyed by submission ID
    private Map<String, Submission> submissions = new HashMap<>();

    // Stores reviewer scores for each submission
    // Key = submission ID, Value = list of scores from all reviewers
    private Map<String, List<Double>> scores = new HashMap<>();

    /*

      Stores the submission in the database and returns a confirmation string
      Sequence diagram: SubmissionController --> Database : saveSubmission(data)
      Database --> SubmissionController : confirmation
     */
    public String saveSubmission(Submission submission) {
        submissions.put(submission.getId(), submission);
        return "SAVED_OK_" + submission.getId();
    }

    

      //Saves a single reviewer's score for a submission.
      //Sequence diagram: EvaluationManager --> Database : saveScore(score)

    
    public void saveScore(String submissionId, double score) {
        // If this is the first score for this submission, create the list first
        if (!scores.containsKey(submissionId)) 
        {
            scores.put(submissionId, new ArrayList<>());
        }
        scores.get(submissionId).add(score);
    }

     // Returns all scores stored for a given submission
    public List<Double> getScores(String submissionId) {
        return scores.getOrDefault(submissionId, new ArrayList<>());
    }

    public Submission getSubmission(String id) {
        return submissions.get(id);
    }
}
