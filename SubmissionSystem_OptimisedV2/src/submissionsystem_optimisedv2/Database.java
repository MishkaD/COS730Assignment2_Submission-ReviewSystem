package submissionsystem_optimisedv2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*

 changed from baseline , addition of saveScores(batch)
 Baseline
 EvaluationManager called saveScore(score) ONCE per rviewer inside a loop
 Optimised solution
 EvaluationManager collects ALL scores first, then calls saveScores(batch)

 Sequence diagram
 EvaluationManager --> Database : saveScores(batch)   [single call]

 */
public class Database {

    // Stores all submitted papers
    private Map<String, Submission>   submissions = new HashMap<>();

    // Stores reviewer scores per submission
    // Key = submission ID, Value = full list of scores from all reviewers
    private Map<String, List<Double>> scores      = new HashMap<>();


    public String saveSubmission(Submission submission) {
        submissions.put(submission.getId(), submission);
        return "SAVED_OK_" + submission.getId();
    }

    /*
    
     Replaces the per-reviewer saveScore() loop.
     Receives the complete list of all reviewer scores in one call
  
    Sequence diagram: EvaluationManager --> Database : saveScores(batch)
    basically:
     Baseline had:  saveScore(7.5)  saveScore(8.5)  saveScore(3.0)  
     Optimised has: saveScores([7.5, 8.5, 3.0])                   
     */
    public void saveScores(String submissionId, List<Double> scoreList) {
        scores.put(submissionId, new ArrayList<>(scoreList));
    }

    public List<Double> getScores(String submissionId) {
        return scores.getOrDefault(submissionId, new ArrayList<>());
    }

    public Submission getSubmission(String id) {
        return submissions.get(id);
    }
}
