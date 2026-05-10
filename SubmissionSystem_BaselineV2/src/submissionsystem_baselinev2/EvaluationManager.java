package submissionsystem_baselinev2;

import java.util.List;

public class EvaluationManager {

    private Database database;
    private NotificationService notificationService;
    private static final double ACCEPT_THRESHOLD = 7.0;
    private static final double REJECT_THRESHOLD = 4.0; 
    private static final double CONSENSUS_MARGIN = 2.0;

    public EvaluationManager(Database database, NotificationService notificationService) {
        this.database = database;
        this.notificationService = notificationService;
    }

    public void startEvaluation(Submission submission, List<Reviewer> reviewers) {
        // Loop [each reviewer]: Reviewer -> EvaluationManager : submitScore(score) [cite: 59, 61]
        for (Reviewer reviewer : reviewers) {
            double score = reviewer.submitScore(getSimulatedScore(reviewer, submission.getId()));
            // EvaluationManager -> Database : saveScore(score) [cite: 62]
            saveScore(submission.getId(), score);
        }

        // Internal Decision Logic [cite: 65, 66, 67]
        double average = calculateAverage(submission.getId());
        boolean consensus = checkConsensus(submission.getId());
        String outcome = applyRules(average, consensus);

        submission.setStatus(outcome);

        System.out.println("  [DECISION] Avg: " + String.format("%.2f", average) + 
                           " | Consensus Agreed: " + consensus + " -> " + outcome);

        // Alt branches: Notify via NotificationService 
        if (outcome.equals("ACCEPTED")) {
            notificationService.notifyAcceptance(submission);
        } else if (outcome.equals("REJECTED")) {
            notificationService.notifyRejection(submission);
        } else {
            notificationService.notifyRevision(submission);
        }
    }

    public void saveScore(String submissionId, double score) {
        database.saveScore(submissionId, score);
    }

    public double calculateAverage(String submissionId) {
        List<Double> scores = database.getScores(submissionId);
        if (scores.isEmpty()) return 0.0;
        double total = 0;
        for (double s : scores) total += s;
        return total / scores.size();
    }

    public boolean checkConsensus(String submissionId) {
        List<Double> scores = database.getScores(submissionId);
        if (scores.size() < 2) return true;

        double sum = 0;
        for (double s : scores) sum += s;
        double mean = sum / scores.size();
        
        double varianceSum = 0;
        for (double s : scores) varianceSum += Math.pow(s - mean, 2);
        
        double stdDev = Math.sqrt(varianceSum / scores.size());
        return stdDev <= CONSENSUS_MARGIN;
    }

    public String applyRules(double average, boolean consensus) {
        if (average >= ACCEPT_THRESHOLD && consensus) return "ACCEPTED";
        if (average < REJECT_THRESHOLD) return "REJECTED";
        return "REVISION";
    }

    private double getSimulatedScore(Reviewer reviewer, String subId) {
        // Special case for Scenario 4 (Rejected)
        if (subId.equals("SUB004")) return 2.0;

        // Default Scores based on Reviewer ID
        switch (reviewer.getId()) {
            case "R001": return 8.5; // High
            case "R002": return 8.0; // High
            case "R004": return 7.5; // High
            case "R005": return 3.0; // Low (Outlier)
            default: return 6.0;
        }
    }
}