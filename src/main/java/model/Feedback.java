package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Richard Sundqvist
 */
public class Feedback implements Comparable<Feedback> {

    //region Constant
    // ================================================================================= //
    // Constant
    // ================================================================================= //
    public transient static final String HEADER = "%HEADER%";
    public transient static final String TEACHER = "%TEACHER%";
    public transient static final String GROUP = "%GROUP%";
    /**
     * Tag indicating that the pasta is incomplete and should be modified by the teacher.
     */
    public transient static final String MANUAL = "%MANUAL%";
    //endregion

    //region Field
    // ================================================================================= //
    // Field
    // ================================================================================= //
    private String content, header, teacher, group, assignment;
    private boolean done;
    //endregion


    //region Constructor
    // ================================================================================= //
    // Constructor
    // ================================================================================= //
    public Feedback () {
        content = "";
        header = "";
        teacher = "";
        group = "";
        assignment = "";
        done = false;
    }

    /**
     * Cloning constructor. Creates a new Pasta from an existing object.
     *
     * @param orig The Pasta to copy.
     */
    public Feedback (Feedback orig) {
        content = orig.content;
        header = orig.header;
        teacher = orig.teacher;
        group = orig.group;
        done = orig.done;
    }

    /**
     * Copy a {@link Collection} of pasta.
     *
     * @param c The original collection.
     * @return A new collection containing copies of the original Pasta.
     */
    public static List<Feedback> copy (Collection<Feedback> c) {
        ArrayList<Feedback> copy = new ArrayList<>(c.size());

        for (Feedback feedback : c)
            copy.add(feedback.copy());

        return copy;
    }

    /**
     * Check whether there are any %MANUAL% tags present.
     *
     * @param feedbackList A list of Feedback.
     * @return A list of feedback contain the %MANUAL% tag.
     */
    public static List<Feedback> checkManual (List<Feedback> feedbackList) {
        List<Feedback> containsTag = new ArrayList<>(feedbackList.size());

        for (Feedback feedback : feedbackList)
            if (checkManual(feedback.content))
                containsTag.add(feedback);

        return containsTag;
    }

    /**
     * Check a string for the {@link #MANUAL} tag.
     *
     * @param s a string
     * @return {@code true} if the string contains the manual tag. {@code false} if it doesn't, or if {@code s} is
     * {@code null}.
     */
    public static boolean checkManual (String s) {
        return s == null ? false : s.contains(MANUAL);
    }

    //endregion


    //region Getters and setters
    // ================================================================================= //
    // Getters and setters
    // ================================================================================= //

    /**
     * Returns a copy of this pasta.
     */
    public Feedback copy () {
        return new Feedback(this);
    }

    /**
     * Calls {@link #getStylizedContent()}.
     *
     * @return A String representation of this Feedback.
     */
    public String toString () {
        return getStylizedContent();
    }

    /**
     * Returns a String representation of this Feedback, with wildcards replaced by actual values.
     *
     * @return A String representation of this Feedback.
     */
    public String getStylizedContent () {
        return getStylizedContent(true);
    }

    /**
     * Returns a String representation of this Feedback, with wildcards replaced by actual values.
     *
     * @param changeHeader If {@code true}, the header will be changed as well.
     * @return A String representation of this Feedback.
     */
    public String getStylizedContent (boolean changeHeader) {
        String s = content;

        if (changeHeader)
            s = s.replace(HEADER, header); //Should be done first!

        s = s.replace(TEACHER, teacher);
        s = s.replace(GROUP, group);

        return s;
    }

    /**
     * Gets the Content for this Feedback.
     *
     * @return The content of this Feedback.
     */
    public String getContent () {
        return content;
    }

    /**
     * Set the content for this Feedback.
     *
     * @param content The new content for this Feedback.
     */
    public void setContent (String content) {
        this.content = content;
    }

    /**
     * Sets the header value, which will replace the {@link #HEADER} wildcard.
     *
     * @return The header.
     */
    public String getHeader () {
        return header;
    }

    /**
     * Sets the header value, which will replace the {@link #HEADER} wildcard.
     *
     * @param header The new header.
     */
    public void setHeader (String header) {
        this.header = header;
    }

    /**
     * Gets the teacher name value, which will replace the {@link #TEACHER} wildcard.
     *
     * @return new teacher name.
     */
    public String getTeacher () {
        return teacher;
    }

    /**
     * Sets the teacher name value, which will replace the {@link #TEACHER} wildcard.
     *
     * @param teacher The new teacher name.
     */
    public void setTeacher (String teacher) {
        this.teacher = teacher;
    }

    /**
     * Gets the group number value, which will replace the {@link #GROUP} wildcard.
     *
     * @return The group number.
     */
    public String getGroup () {
        return group;
    }

    /**
     * Sets the group number value, which will replace the {@link #GROUP} wildcard.
     *
     * @param group The new group number.
     */
    public void setGroup (String group) {
        this.group = group;
    }

    /**
     * Returns the done status of this Feedback.
     *
     * @return The done status of this Feedback.
     */
    public boolean isDone () {
        return done;
    }

    /**
     * Set the done status of this Feedback.
     *
     * @param done The new done status of this Feedback.
     */
    public void setDone (boolean done) {
        this.done = done;
    }

    @Override
    public int compareTo (Feedback other) {
        return group.compareTo(other.group);
    }

    /**
     * Returns the assignment with which this Feedback is associated.
     *
     * @return The associated assignment assignment.
     */
    public String getAssignment () {
        return assignment;
    }

    /**
     * Set the assignment with which this Feedback is associated.
     *
     * @param assignment The new associated assignment assignment.
     */
    public void setAssignment (String assignment) {
        this.assignment = assignment;
    }
    //endregion
}
