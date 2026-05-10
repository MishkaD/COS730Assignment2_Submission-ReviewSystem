COS 730 — Assignment 2
Student Name: Mishka Dukhanti
Student Number: u22617541

Overview
+--------------------------------------------------+
This repository contains the source code for the empirical evaluation of an Intelligent Submission and Review System Assignment.
Key Objectives:
•	Implement a system based on a provided sequence diagram. 
•	Analyse design inefficiencies, including high coupling, redundant interactions, and God Class behaviour. 
•	Refactor the system using a Decision Engine and encapsulated logic. 
•	Empirically compare both systems using interaction counts and execution time metrics. 
+--------------------------------------------------+

Repository Structure
The project is organized into the following directories:
•	/SubmissionSystem_BaselineV2: The original implementation. It follows the baseline sequence diagram exactly.
•	/SubmissionSystem_OptimisedV2: The refactored implementation. It utilizes a DecisionTable for centralized rules, batch database writes, and decoupled notification logic. 
•	/Outputs from main: Contains the console logs for each scenario to verify results in my reports. 
+---------------------------------------------------+
How to Run ?
1.	Clone the Repository:
    git clone https://github.com/[Your-Username]/COS730Assignment2_Submission-ReviewSystem.git
2.	Open in IDE: Open the project in NetBeans.
3.	Run Baseline: Execute Main.java in submissionsystem_baselinev2 project in src folder to see results.
4.	Run Optimised: Execute Main.java in submissionsystem_optimisedv2 project in src folder to see results.

