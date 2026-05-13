# Intelligent Submission and Review System

**COS 730 — Assignment 2**
- **Student Name:** Mishka Dukhanti
- **Student Number:** u22617541

---

## Overview
This repository contains the source code for the empirical evaluation of an Intelligent Submission and Review System in the assignment.

### Key Objectives:
- Implement a system based on a provided baseline sequence diagram.
- Analyse design inefficiencies, including high coupling and God Class behaviour.
- Refactor the system using a Decision Engine and encapsulated logic.
- Empirically compare both systems using interaction counts.

---

## Repository Structure
The project is organized into the following directories:

- **/SubmissionSystem_BaselineV2**: The original implementation with a God-object controller and N+1 database interactions.
- **/SubmissionSystem_OptimisedV2**: The refactored implementation utilizing a DecisionTable and batch database writes.
- **/Outputs from main**: Contains console logs for each scenario to verify  results.

---

## How to Run
1. **Clone the Repository:**
   `git clone https://github.com/MishkaD/COS730Assignment2_Submission-ReviewSystem.git`
2. **Open in IDE:** Open the project in NetBeans.
3. **Run Baseline:** Execute `Main.java` in the `submissionsystem_baselinev2`in src folder project.
4. **Run Optimised:** Execute `Main.java` in the `submissionsystem_optimisedv2`in src folder project.
