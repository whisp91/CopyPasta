package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author Richard Sundqvist */
public class Feedback implements Comparable<Feedback> {

  // region Constant
  // ================================================================================= //
  // Constant
  // ================================================================================= //
  public static final transient String HEADER = "%HEADER%";
  public static final transient String FOOTER = "%FOOTER%";
  public static final transient String SIGNATURE = "%SIGNATURE%";
  public static final transient String GROUP = "%GROUP%";
  public static final transient String FILE = "%FILE: <file>%";
  public static final transient String FILE_REGEX = "%[ \t]*([Ff]ile|FILE):[ \t]*\\S+[ \t]*%";

  /** Tag indicating that the pasta is incomplete and should be modified by the teacher. */
  public static final transient String MANUAL = "%MANUAL%";
  // endregion
  private final Map<String, String> files;
  // region Field
  // ================================================================================= //
  // Field
  // ================================================================================= //
  private String content, header, footer, signature, group, assignment;
  private boolean done;
  // endregion

  // region Constructor
  // ================================================================================= //
  // Constructor
  // ================================================================================= //
  public Feedback() {
    content = "";
    header = "";
    footer = "";
    signature = "";
    group = "";
    assignment = "";
    files = new HashMap<>();
    done = false;
  }

  /**
   * Cloning constructor. Creates a new Pasta from an existing object.
   *
   * @param orig The Pasta to copy.
   */
  public Feedback(Feedback orig) {
    content = orig.content;
    header = orig.header;
    footer = orig.footer;
    signature = orig.signature;
    group = orig.group;
    files = new HashMap<>(orig.files); // Shallow copy
    done = orig.done;
  }

  /**
   * Copy a {@link Collection} of pasta.
   *
   * @param c The original collection.
   * @return A new collection containing copies of the original Pasta.
   */
  public static List<Feedback> copy(Collection<Feedback> c) {
    ArrayList<Feedback> copy = new ArrayList<>(c.size());

    for (Feedback feedback : c) copy.add(feedback.copy());

    return copy;
  }

  /**
   * Check whether there are any %MANUAL% tags present.
   *
   * @param feedbackList A list of Feedback.
   * @return A list of feedback contain the %MANUAL% tag.
   */
  public static List<Feedback> checkManual(List<Feedback> feedbackList) {
    List<Feedback> containsTag = new ArrayList<>(feedbackList.size());

    for (Feedback feedback : feedbackList)
      if (checkManual(feedback.content)) containsTag.add(feedback);

    return containsTag;
  }

  /**
   * Check a string for the {@link #MANUAL} tag.
   *
   * @param s a string
   * @return {@code true} if the string contains the manual tag. {@code false} if it doesn't, or if
   *     {@code s} is {@code null}.
   */
  public static boolean checkManual(String s) {
    return s != null && s.contains(MANUAL);
  }

  // endregion

  // region Getters and setters
  // ================================================================================= //
  // Getters and setters
  // ================================================================================= //

  /**
   * Returns a {@link #FILE} tag for the argument filename.
   *
   * @param file The file.
   * @return A {@link #FILE} tag for the argument filename.
   */
  public static String getFileTag(String file) {
    return FILE.replace("<file>", file);
  }

  /** Returns a copy of this pasta. */
  public Feedback copy() {
    return new Feedback(this);
  }

  /**
   * Calls {@link #getStylizedContent()}.
   *
   * @return A String representation of this Feedback.
   */
  public String toString() {
    return getStylizedContent();
  }

  /**
   * Returns a String representation of this Feedback, with wildcards replaced by actual values.
   *
   * @return A String representation of this Feedback.
   */
  public String getStylizedContent() {
    return getStylizedContent(true, true);
  }

  /**
   * Returns a String representation of this Feedback, with wildcards replaced by actual values.
   *
   * @param live If {@code true}, the header and footer will be changed as well.
   * @param replaceTabs If {@code true}, tabs will be replaced by 4 spaces.
   * @return A String representation of this Feedback.
   */
  public String getStylizedContent(boolean live, boolean replaceTabs) {
    String s = content;

    if (live) {
      s = s.replace(HEADER, header);
      s = s.replace(FOOTER, footer);
    }

    s = s.replace(SIGNATURE, signature);
    s = s.replace(GROUP, group);
    if (replaceTabs) s = s.replace("\t", "    ");
    s = s.replaceAll(FILE_REGEX, ""); // Need replaceAll - replace doesnt take regex

    return s;
  }

  /**
   * Looks for the {@link #FILE} tag, ant returns the position <it>after</it> the final '%'. For
   * example, if this function were called with arg {@code file = "main.cpp"}, the it would return
   * the index after the string "{@code %FILE: main.cpp%}". Whitespace is not permitted.
   *
   * @param file The file to look for.
   * @return The position after the sought tag, or -1 if it could not be found.
   */
  public int getFilePosition(String file) {
    String file_regex = "%([Ff]ile|FILE):[ \t]*" + file + "[ \t]*%";
    Pattern pattern = Pattern.compile(file_regex);
    Matcher matcher = pattern.matcher(content);

    if (matcher.find()) return matcher.end(); // First match only
    else return -1;
  }

  /**
   * Gets the Content for this Feedback.
   *
   * @return The content of this Feedback.
   */
  public String getContent() {
    return content;
  }

  /**
   * Set the content for this Feedback.
   *
   * @param content The new content for this Feedback.
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * Sets the header value, which will replace the {@link #HEADER} wildcard.
   *
   * @return The header.
   */
  public String getHeader() {
    return header;
  }

  /**
   * Sets the header value, which will replace the {@link #HEADER} wildcard.
   *
   * @param header The new header.
   */
  public void setHeader(String header) {
    this.header = header;
  }

  /**
   * Sets the header value, which will replace the {@link #FOOTER} wildcard.
   *
   * @return The footer.
   */
  public String getFooter() {
    return footer;
  }

  /**
   * Sets the header value, which will replace the {@link #FOOTER} wildcard.
   *
   * @param footer The new footer.
   */
  public void setFooter(String footer) {
    this.footer = footer;
  }

  /**
   * Gets the signature name value, which will replace the {@link #SIGNATURE} wildcard.
   *
   * @return new signature name.
   */
  public String getSignature() {
    return signature;
  }

  /**
   * Sets the signature name value, which will replace the {@link #SIGNATURE} wildcard.
   *
   * @param signature The new signature name.
   */
  public void setSignature(String signature) {
    this.signature = signature;
  }

  /**
   * Gets the group number value, which will replace the {@link #GROUP} wildcard.
   *
   * @return The group number.
   */
  public String getGroup() {
    return group;
  }

  /**
   * Sets the group number value, which will replace the {@link #GROUP} wildcard.
   *
   * @param group The new group number.
   */
  public void setGroup(String group) {
    this.group = group;
  }

  /**
   * Returns the done status of this Feedback.
   *
   * @return The done status of this Feedback.
   */
  public boolean isDone() {
    return done;
  }

  /**
   * Set the done status of this Feedback.
   *
   * @param done The new done status of this Feedback.
   */
  public void setDone(boolean done) {
    this.done = done;
  }

  @Override
  public int compareTo(Feedback other) {
    return group.compareTo(other.group);
  }

  /**
   * Returns the assignment with which this Feedback is associated.
   *
   * @return The associated assignment assignment.
   */
  public String getAssignment() {
    return assignment;
  }

  /**
   * Set the assignment with which this Feedback is associated.
   *
   * @param assignment The new associated assignment assignment.
   */
  public void setAssignment(String assignment) {
    this.assignment = assignment;
  }

  /**
   * Add a file to the file-map of this Feedback.
   *
   * @param fileName The filename, used as key.
   * @param content The content, used as value.
   */
  public void addFile(String fileName, String content) {
    files.put(fileName, content);
  }

  /**
   * Remove a file from the Feedback, if it exists.
   *
   * @param fileName The file to remove.
   */
  public void removeFile(String fileName) {
    files.remove(fileName);
  }

  /**
   * Return the map of file for this Feedback, with file names as keys and content as values.
   *
   * @return A map of files.
   */
  public Map<String, String> getFiles() {
    return files;
  }
  // endregion
}
