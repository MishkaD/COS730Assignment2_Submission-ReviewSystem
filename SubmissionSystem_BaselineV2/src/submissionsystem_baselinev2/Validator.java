package submissionsystem_baselinev2;

/**

 -Responsible for checking whether a submitted file is in an acceptable format

 -Sequence diagram trace:
SubmissionController --> Validator : validateFormat(data)
 Validator --> SubmissionController : valid/invalid


 */
public class Validator {


     //validateFormat(data)

    
     /*
      ACCEPTED FORMATS: PDF, DOC, DOCX
      REJECTED FORMATS: Anything else (e.g. TXT, ZIP, PNG)
     
     TRUE  -> format is acceptable, continue to save the submission
      FALSE -> format is not acceptable, return error to UI immediately
     
     */
    public boolean validateFormat(Submission submission) {
        String format = submission.getFormat();

        // Check if the format matches any of our accepted types
        boolean isValid = format != null &&
                (format.equalsIgnoreCase("PDF")  ||
                 format.equalsIgnoreCase("DOC")  ||
                 format.equalsIgnoreCase("DOCX"));

        return isValid;
    }
}
