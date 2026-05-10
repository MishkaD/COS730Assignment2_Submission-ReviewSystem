package submissionsystem_baselinev2;


public class Main {

    public static void main(String[] args) {
        printBanner("SUBMISSION AND REVIEW SYSTEM", "BASELINE IMPLEMENTATION");
        
        System.out.println("  Key baseline characteristics visible in the output:");
        System.out.println("    - Separate Validator class used for format check (Step 2)");
        System.out.println("    - TWO separate filter loops in ReviewerManager (Step 4)");
        System.out.println("    - Controller runs the reviewer assignment loop itself (Step 4)");
        System.out.println("    - saveScore() called once PER REVIEWER — N+1 writes (Step 5)");
        System.out.println("    - EvaluationManager calls NotificationService directly (Step 5/6)");

        // SCENARIO 1: ACCEPTED 
        // Author A003 
        // Reviewers: R001(8.5), R002(8.0), R004(7.5) -> avg 8.0 -> ACCEPTED

        runScenario(1,
            "Strong Paper — reviewers agree",
            "Expected: ACCEPTED (avg >= 7.0, consensus reached)",
            new Submission("SUB001", "Deep Learning in NLP",
                           "A003", "Content", "PDF"));

        // SCENARIO 2: INVALID FORMAT

        runScenario(2,
            "Invalid format (TXT)",
            "Expected: Error — process stops at Validator",
            new Submission("SUB002", "Quantum Physics",
                           "A002", "Paper content", "TXT"));

        // SCENARIO 3: REVISION
        // Author A001 conflict excludes R001
        // Assigned: R002(8.0), R004(7.5), R005(3.0) -> avg 6.17, high variance
     
        runScenario(3,
            "Conflict of Interest — Mixed Scores",
            "Expected: REVISION (mid-range avg or high variance)",
            new Submission("SUB003", "Blockchain Healthcare",
                           "A001", "Content", "DOCX"));

        // SCENARIO 4: REJECTED 
      
        runScenario(4,
            "Weak Paper — Low scores",
            "Expected: REJECTED (avg < 4.0)",
            new Submission("SUB004", "Flat Earth Survey",
                           "A004", "Content", "PDF"));

        // BENCHMAR
        runBenchmark();
    }



    // Builds the baseline system, prints each step explicitly, then routes
     // the submission through UI -> SC for the actual execution and call count

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

     

        Database             db  = new Database();
        Validator             v  = new Validator();
        NotificationService  ns  = new NotificationService();
        ReviewerManager      rm  = new ReviewerManager(db);
        EvaluationManager    em  = new EvaluationManager(db, ns);
        SubmissionController sc  = new SubmissionController(v, db, rm, em);
        UI                   ui  = new UI(sc);

        //STEP 1: Researcher submits via UI 
        System.out.println("  [STEP 1] Researcher submits paper via the UI");
        System.out.println("           Sequence: Researcher --> UI --> SubmissionController : submit(data)");
        System.out.println();

        //STEP 2: Validator checks format
        System.out.println("  [STEP 2] SubmissionController calls Validator to check format");
        System.out.println("           Sequence: SubmissionController --> Validator : validateFormat(data)");
        System.out.println("           (BASELINE: a separate Validator class is instantiated here)");
        System.out.println();

        boolean formatIsValid = v.validateFormat(sub);

        if (!formatIsValid) {
            System.out.println("  [ALT: INVALID FORMAT]");
            System.out.println("  +----------------------------------------------------------+");
            System.out.println("  | Format '" + sub.getFormat()
                    + "' is NOT accepted.                          |");
            System.out.println("  | Accepted formats: PDF, DOC, DOCX                        |");
            System.out.println("  | Sequence: Validator --> SC : invalid                    |");
            System.out.println("  |           SC --> UI : return error                      |");
            System.out.println("  | Process stops here. Nothing is saved.                   |");
            System.out.println("  +----------------------------------------------------------+");
            // Still route through sc so the method counter registers
            ui.submitResearchOutput(sub);
            printResult(sub, sc.getMethodCallCount());
            return;
        }

        System.out.println("  Format '" + sub.getFormat() + "' is VALID. Continuing...");
        System.out.println();

        // TEP 3: Save submission
        System.out.println("  [STEP 3] Saving submission to database");
        System.out.println("           Sequence: SubmissionController --> Database : saveSubmission(data)");
        String confirmation = db.saveSubmission(sub);
        System.out.println("           Confirmation: " + confirmation);
        System.out.println();

        // STEP 4: Get reviewers
        System.out.println("  [STEP 4] Finding and assigning reviewers");
        System.out.println("           Sequence: SC --> ReviewerManager : getAvailableReviewers()");
        System.out.println("                     [loop] SC --> ReviewerManager : assignReview()");
        System.out.println("           (BASELINE: three separate operations below)");
        System.out.println();

        // a)fetchReviewers
        System.out.println("    a) fetchReviewers()");
        System.out.println("       Sequence: ReviewerManager --> Database : fetchReviewers()");
        java.util.List<Reviewer> allReviewers = rm.fetchReviewers();
        System.out.println("       Database --> ReviewerManager : reviewerList ("
                + allReviewers.size() + " reviewers)");
        System.out.println();

         // b)filterConflicts — LOOP 1
        System.out.println("    b) filterConflicts(reviewerList) — LOOP 1 over the list");
        System.out.println("       Sequence: ReviewerManager --> ReviewerManager : filterConflicts()");
        System.out.println("       (Self-call — ReviewerManager calls itself)");
        for (Reviewer r : allReviewers) {
            boolean conflict = r.getId().equals("R001")
                            && sub.getAuthorId().equals("A001");
            String verdict = conflict
                    ? "EXCLUDED  (conflict of interest with author " + sub.getAuthorId() + ")"
                    : "kept";
            if (conflict) System.out.println("       " + r.getName() + " -> " + verdict);
        }
        java.util.List<Reviewer> afterConflict = rm.filterConflicts(allReviewers, sub);
        System.out.println("       " + afterConflict.size()
                + " reviewer(s) remain after conflict filter.");
        System.out.println();

        // c) checkWorkload — LOOP 2 over the SAME list
        System.out.println("    c) checkWorkload(reviewerList) — LOOP 2 over the same list");
        System.out.println("       Sequence: ReviewerManager --> ReviewerManager : checkWorkload()");
        System.out.println("       (Another self-call — same list, second full pass)");
        System.out.println("       (BASELINE: two loops where one would do — redundant)");
        for (Reviewer r : afterConflict) {
            boolean overloaded = r.getWorkload() >= 4;
            System.out.println("       " + r.getName() + " -> "
                    + (overloaded
                        ? "EXCLUDED  (workload " + r.getWorkload() + " >= limit of 4)"
                        : "AVAILABLE (workload " + r.getWorkload() + ")"));
        }
        java.util.List<Reviewer> available = rm.checkWorkload(afterConflict);
        System.out.println("       ReviewerManager --> SC : filteredReviewers ("
                + available.size() + " available)");
        System.out.println();

        // d) assignReview loop — controller runs this
        int toAssign = Math.min(3, available.size());
        java.util.List<Reviewer> assigned = available.subList(0, toAssign);
        System.out.println("    d) [loop assign reviewers] — controller drives this loop");
        System.out.println("       Sequence: [loop] SC --> ReviewerManager : assignReview()");
        System.out.println("       (BASELINE: controller owns this loop)");
        for (Reviewer r : assigned) {
            rm.assignReview(r, sub);
            System.out.println("       Assigned: " + r.getName());
        }
        System.out.println();

        //STEP 5: Evaluation via EvaluationManager
         // EvaluationManager owns all score logic 
        System.out.println("  [STEP 5] Starting evaluation");
        System.out.println("           Sequence: SC --> EvaluationManager : startEvaluation()");
        System.out.println();
        System.out.println("    LOOP [each reviewer submits score]:");
        System.out.println("    Sequence: Reviewer --> EvaluationManager : submitScore(score)");
        System.out.println("              EvaluationManager --> Database  : saveScore(score)  [one per reviewer]");
        System.out.println("    (BASELINE: database written to once PER reviewer)");
        System.out.println();

        // Run EvaluationManager it handles scoring, DB writes, decision, notification
         // Score logic, thresholds, and decision all live in EvaluationManager 
        em.startEvaluation(sub, assigned);

         // Read scores from DB 
        java.util.List<Double> savedScores = db.getScores(sub.getId());
        if (!savedScores.isEmpty()) {
            System.out.println();
            System.out.println("    Scores saved to DB (one write per reviewer — "
                    + savedScores.size() + " total writes):");
            for (int i = 0; i < assigned.size() && i < savedScores.size(); i++) {
                System.out.println("      " + assigned.get(i).getName()
                        + " : " + savedScores.get(i) + " / 10"
                        + "  <-- individual DB write");
            }
            System.out.println();
            System.out.println("    Self-call: calculateAverage()");
            System.out.println("    Sequence: EvaluationManager --> EvaluationManager : calculateAverage()");
            System.out.println("    Self-call: checkConsensus()");
            System.out.println("    Sequence: EvaluationManager --> EvaluationManager : checkConsensus()");
            System.out.println("    Self-call: applyRules()");
            System.out.println("    Sequence: EvaluationManager --> EvaluationManager : applyRules()");
            System.out.println("    (BASELINE: decision logic buried inside EvaluationManager)");
        }
        System.out.println();

        //STEP 6: Notification
        System.out.println("  [STEP 6] Notification sent by EvaluationManager directly");
        System.out.println("           (BASELINE: EvaluationManager calls NotificationService)");

        System.out.println("           Sequence: EvaluationManager --> NotificationService : notify"
                + sub.getStatus().charAt(0)
                + sub.getStatus().substring(1).toLowerCase() + "()");
        System.out.println("                     NotificationService --> Researcher : sendNotification()");
        System.out.println();

        // Route through sc for method count
        ui.submitResearchOutput(sub);
        printResult(sub, sc.getMethodCallCount());
    }

 
    // Benchmark 

    private static void runBenchmark() {
        System.out.println("\n  BENCHMARK — 1000 iterations (for Task 6 ");
       

        int  iterations = 1000;
        long start      = System.nanoTime();
        long totalCalls = 0;

        java.io.PrintStream original = System.out;

        for (int i = 0; i < iterations; i++) {
            Database            db  = new Database();
            Validator            v  = new Validator();
            NotificationService ns  = new NotificationService();
            ReviewerManager     rm  = new ReviewerManager(db);
            EvaluationManager   em  = new EvaluationManager(db, ns);
            SubmissionController sc = new SubmissionController(v, db, rm, em);
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
        System.out.println("  ║  " + l2 + "                         ║");
        System.out.println("  ╚════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
}