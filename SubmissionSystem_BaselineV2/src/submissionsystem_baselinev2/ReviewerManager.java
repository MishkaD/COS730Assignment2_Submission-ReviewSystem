package submissionsystem_baselinev2;
import java.util.ArrayList;
import java.util.List;

/**

 -Responsible for finding suitable reviewers for a submission

 -The sequence diagram shows this class doing three things in sequence:
  1. fetchReviewers()            -> get the full list from the database
  2. filterConflicts(list)       -> remove reviewers who have a conflict of
                                     interest with the author
   3. checkWorkload(list)         -> remove reviewers who are too busy

 -returns the filtered list (filteredReviewers) to SubmissionController then SubmissionController calls assignReview() in a loop for each
 reviewer that was selected
 


 -Sequence diagram traces:
  SubmissionController --> ReviewerManager : getAvailableReviewers()
 ReviewerManager --> Database             : fetchReviewers()
 Database --> ReviewerManager             : reviewerList
 ReviewerManager --> ReviewerManager      : filterConflicts(reviewerList)
ReviewerManager --> ReviewerManager      : checkWorkload(reviewerList)
 ReviewerManager --> SubmissionController : filteredReviewers
 [loop] SubmissionController --> ReviewerManager : assignReview()
 

 */
public class ReviewerManager {

    private Database database;

    /*
     
     My mock data 
     Reviewer workloads:
    R001 Dr. Pillay    - workload 0 (available)
     R002 Prof. Singh   - workload 0 (available)
    R003 Dr. Marshall   - workload 5 (TOO BUSY - will be filtered out)
    R004 Prof. Makura  - workload 0 (available)
    R005 Dr. Bosman       - workload 0 (available)
     */
    private List<Reviewer> allReviewers;

   
    // If a reviewer already has 4 or more, they are too busy
    private static final int MAX_WORKLOAD = 4; //max

    public ReviewerManager(Database database) {
        this.database     = database;
        this.allReviewers = new ArrayList<>();
        loadReviewers();
    }

    private void loadReviewers() {
        allReviewers.add(new Reviewer("R001", "Dr. Pillay",   "Artificial Intelligence"));
        allReviewers.add(new Reviewer("R002", "Prof. Singh",  "Software Engineering"));
        allReviewers.add(new Reviewer("R003", "Dr. Marshall",   "Data Science"));
        allReviewers.add(new Reviewer("R004", "Prof. Makura", "Systems Design"));
        allReviewers.add(new Reviewer("R005", "Dr. Bosman",      "Artificial Intelligence"));

        // Dr. Marshall  has 5 active reviews ,over the limit of 4
        allReviewers.get(2).setWorkload(5);
    }

    /*
     Called by SubmissionController to get a list of suitable reviewers
     Internally calls fetchReviewers(), filterConflicts(), checkWorkload()
     

    Sequence diagram: SubmissionController --> ReviewerManager : getAvailableReviewers()
     */
    public List<Reviewer> getAvailableReviewers(Submission submission) {
        // Get all reviewers 
        List<Reviewer> reviewerList = fetchReviewers();
        // Remove anyone who has a conflict of interest with this author
        reviewerList = filterConflicts(reviewerList, submission);
        //  Remove anyone who is already too busy
        reviewerList = checkWorkload(reviewerList);
        return reviewerList;
    }

    /**

     Retrieves the full list of all reviewers 
     Sequence diagram: ReviewerManager --> Database : fetchReviewers()
                 Database --> ReviewerManager : reviewerList
     */
    public List<Reviewer> fetchReviewers() {
        // Return a copy so we can safely filter it without altering the original list
        return new ArrayList<>(allReviewers);
    }

    /*

     Removes reviewers where reviewer and the author know each other 

      In my simulation: Reviewer R001 (Dr. Pillay) has a conflict with Author A001. 
      So if A001 submitted the paper, Dr. Pillay is excluded
     
    Sequence diagram: ReviewerManager --> ReviewerManager : filterConflicts(reviewerList)
     (this is a self-call — ReviewerManager calls itself)
     */
    public List<Reviewer> filterConflicts(List<Reviewer> reviewers, Submission submission) {
        List<Reviewer> noConflicts = new ArrayList<>();

        for (Reviewer r : reviewers) {
            
            boolean hasConflict = r.getId().equals("R001")
                               && submission.getAuthorId().equals("A001");

            if (!hasConflict) 
            {
                noConflicts.add(r); // No conflict 
            }
            // If there IS a conflict,  don't add them to the list
        }

        return noConflicts;
    }

    /*

      Removes reviewers who already have too many active reviews, MAX_WORKLOAD (4)
     Sequence diagram: ReviewerManager --> ReviewerManager : checkWorkload(reviewerList)
     (this is also a self-call)
   
     */
    public List<Reviewer> checkWorkload(List<Reviewer> reviewers) {
        List<Reviewer> available = new ArrayList<>();

        for (Reviewer r : reviewers) {
            if (r.getWorkload() < MAX_WORKLOAD) {
                available.add(r); // Within workload limit
            }
            // If workload is at or above the limit, exclude this reviewer
        }

        return available;
    }

    /**
     actually assigns a reviewer to a submission and their workload count increases by 1 to reflect the new assignment
     Sequence diagram: [loop] SubmissionController --> ReviewerManager : assignReview()
     */
    public void assignReview(Reviewer reviewer, Submission submission) {
        reviewer.incrementWorkload();
    }
}
