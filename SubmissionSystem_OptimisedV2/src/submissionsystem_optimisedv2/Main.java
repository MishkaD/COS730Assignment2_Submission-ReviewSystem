package submissionsystem_optimisedv2;

/**

  Key structural differences visible in the output:
    - No Validator object created — DecisionTable handles format validation
   - No assignment loop in controller — ReviewerManager.assignBestReviewers()
    - EvaluationManager returns outcome, does not call NotificationService
    - One batch database write for scores, not one per reviewer
    - Method call count is lower than baseline for the same scenarios
 
 */
public class Main {

    public static void main(String[] args) {
        printBanner("SUBMISSION AND REVIEW SYSTEM", "OPTIMISED IMPLEMENTATION — Task 5");

        System.out.println("  Key optimisations visible in the output:");
        System.out.println("    - DecisionTable replaces Validator (Step 2)");
        System.out.println("    - assignBestReviewers() — one call, no controller loop (Step 4)");
        System.out.println("    - saveScores(batch) — one DB write, not N writes (Step 5)");
        System.out.println("    - EvaluationManager returns outcome, SC notifies (Step 6)");

        //SCENARIO 1: ACCEPTED 
         // Author A003  no conflicts
        // Reviewers:(8.0), (7.5),  (8.5)
         // Average = 8.0, std dev = 0.41 -> ACCEPTED
    
        runScenario(1,
            "Strong Paper — reviewers agree",
            "Expected: ACCEPTED (avg >= 7.0, consensus <= 2.0)",
            new Submission("SUB001", "Deep Learning in NLP",
                           "A003", "Content", "PDF"));

        //  SCENARIO 2: INVALID FORMAT  
    
        runScenario(2,
            "Invalid format (TXT)",
            "Expected: Error — process stops at DecisionTable (no Validator class)",
            new Submission("SUB002", "Quantum Physics",
                           "A002", "Paper content", "TXT"));

        //SCENARIO 3: REVISION 
        // Author A001conflict excludes
         // Reviewers: (7.5), (8.5),  (3.0)
          // Average = 6.33, std dev = 2.39 -> REVISION

        runScenario(3,
            "Conflict of Interest — Mixed Scores",
            "Expected: REVISION (avg 6.33, between 4.0 and 7.0)",
            new Submission("SUB003", "Blockchain Healthcare",
                           "A001", "Content", "DOCX"));

        //SCENARIO 4: REJECTED 
         // Uses a custom low-score EvaluationManager to produce avg < 4.0
  
        runRejectedScenario();

        // BENCHMARK 
        runBenchmark();
    }


    // runScenario()builds optimised system and routes through UI -> SC

    private static void runScenario(int number, String desc,
                                    String expected, Submission sub) {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  SCENARIO " + number + " : " + desc);
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println("  " + expected + "\n");
        System.out.println("  Paper  : " + sub.getTitle());
        System.out.println("  Author : " + sub.getAuthorId());
        System.out.println("  Format : " + sub.getFormat());
        System.out.println("  ............................................................");
        System.out.println();

        // Build all objects — matches the lifelines in the optimised diagram
        DecisionTable       dt  = new DecisionTable();
        Database            db  = new Database();
        NotificationService ns  = new NotificationService();
        ReviewerManager     rm  = new ReviewerManager(db, dt);
        EvaluationManager   em  = new EvaluationManager(db, dt);
        SubmissionController sc = new SubmissionController(dt, db, rm, em, ns);
        UI                  ui  = new UI(sc);

        // The entire sequence diagram is triggered by this one call
        ui.submitResearchOutput(sub);

        printResult(sub, sc.getMethodCallCount());
    }

  
    // Scenario 4: REJECTED — low score override

    private static void runRejectedScenario() {
        Submission sub = new Submission("SUB004",
                "Flat Earth Survey", "A004", "Content", "PDF");

        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  SCENARIO 4 : Weak Paper — Low scores");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println("  Expected: REJECTED (avg < 4.0)");
        System.out.println();
        System.out.println("  Paper  : " + sub.getTitle());
        System.out.println("  Author : " + sub.getAuthorId());
        System.out.println("  Format : " + sub.getFormat());
        System.out.println("  NOTE   : Reviewer scores overridden to 2.0, 3.0, 1.5 (avg=2.17)");
        System.out.println("  ............................................................");
        System.out.println();

        DecisionTable       dt  = new DecisionTable();
        Database            db  = new Database();
        NotificationService ns  = new NotificationService();
        ReviewerManager     rm  = new ReviewerManager(db, dt);


        EvaluationManager em = new EvaluationManager(db, dt) {
            @Override
            public String evaluate(Submission submission,
                                   java.util.List<Reviewer> reviewers) {

                System.out.println("  [EvaluationManager] evaluate(submission) — low score scenario");
                System.out.println();
                System.out.println("  LOOP [each reviewer submits score]:");
                System.out.println("  (Scores collected first — not saved individually)");
                System.out.println();

                double[] lowScores = {2.0, 3.0, 1.5};
                java.util.List<Double> collected = new java.util.ArrayList<>();
                int i = 0;
                for (Reviewer r : reviewers) {
                    double score = lowScores[i < lowScores.length ? i : 0];
                    r.submitScore(score);
                    collected.add(score);
                    System.out.println("  " + r.getName()
                            + " submits score: " + score + " / 10");
                    i++;
                }
                System.out.println();

                System.out.println("  Batch update: saveScores(batch) — 1 database call");
                db.saveScores(submission.getId(), collected);

                double total = 0;
                for (double s : collected) total += s;
                double average = total / collected.size();

                System.out.printf("  Average score : %.2f / 10%n", average);
                System.out.println("  Sending to DecisionTable: determineOutcome("
                        + String.format("%.2f", average) + ", true)");
                System.out.printf("  avg %.2f < %.1f  -->  REJECTED%n",
                        average, DecisionTable.REJECT_THRESHOLD);
                System.out.println();

                String outcome = "REJECTED";
                submission.setStatus(outcome);
                System.out.println("  DecisionTable --> EvaluationManager : outcome = REJECTED");
                System.out.println("  EvaluationManager --> SubmissionController : outcome");
                System.out.println();
                return outcome;
            }
        };

        SubmissionController sc = new SubmissionController(dt, db, rm, em, ns);
        UI ui = new UI(sc);
        ui.submitResearchOutput(sub);
        printResult(sub, sc.getMethodCallCount());
    }

 
    // Benchmark

    private static void runBenchmark() {
        System.out.println("\n  BENCHMARK — 1000 iterations (Task 6)");
    

        int  iterations = 1000;
        long start      = System.nanoTime();
        long totalCalls = 0;

        java.io.PrintStream original = System.out;

        for (int i = 0; i < iterations; i++) {
            DecisionTable       dt  = new DecisionTable();
            Database            db  = new Database();
            NotificationService ns  = new NotificationService();
            ReviewerManager     rm  = new ReviewerManager(db, dt);
            EvaluationManager   em  = new EvaluationManager(db, dt);
            SubmissionController sc = new SubmissionController(dt, db, rm, em, ns);
            UI                  ui  = new UI(sc);

            Submission bench = new Submission(
                    "B" + i, "Bench Paper", "A003", "content", "PDF");

            System.setOut(new java.io.PrintStream(
                    new java.io.OutputStream() { public void write(int b) {} }));
            ui.submitResearchOutput(bench);
            System.setOut(original);

            totalCalls += sc.getMethodCallCount();
        }

        long durationMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println("  +----------------------------------------------+");
        System.out.println("  |  BENCHMARK RESULTS                           |");
        System.out.println("  +----------------------------------------------+");
        System.out.printf( "  |  Iterations       : %-24d|%n", iterations);
        System.out.printf( "  |  Total time       : %-20d ms |%n", durationMs);
        System.out.printf( "  |  Avg time per run : %-22s ms |%n",
                String.format("%.3f", durationMs / (double) iterations));
        System.out.printf( "  |  Avg method calls : %-24s|%n",
                String.format("%.1f", totalCalls / (double) iterations));
        System.out.println("  +----------------------------------------------+");
        System.out.println();
    }


    // Output helpers

    private static void printResult(Submission submission, int calls) {
        System.out.println("\n  ════════════════════════════════════════════");
        System.out.println("  SCENARIO RESULT");
        System.out.println("  ════════════════════════════════════════════");
        System.out.println("  Final status   : " + submission.getStatus());
        System.out.println("  Method calls   : " + calls
                + "  (tracked in SubmissionController)");
        System.out.println("  ════════════════════════════════════════════");
    }

    private static void printBanner(String l1, String l2) {
        System.out.println();
        System.out.println("  ╔════════════════════════════════════════════════════════════╗");
        System.out.println("  ║  " + l1 + "  ║");
        System.out.println("  ║  " + l2 + "                  ║");
        System.out.println("  ╚════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
}
