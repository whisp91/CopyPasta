package model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/** @author Richard Sundqvist */
public class Pasta implements Comparable<Pasta>, Cloneable, Content {

  // region Constant
  // ================================================================================= //
  // Constant
  // ================================================================================= //
  public static final transient int CONTENT_SNIPPET_LENGTH = 45;
  public static final transient String NO_CONTENT = "<No content>";

  // endregion

  // region Field
  // ================================================================================= //
  // Field
  // ================================================================================= //
  private final Date creationDate;
  private final List<String> contentTags;
  private final List<String> assignmentTags;
  private Date lastModificationDate;
  private String title;
  private String content;
  // endregion

  // region Constructor
  // ================================================================================= //
  // Constructor
  // ================================================================================= //

  /** Constructs a new Pasta. */
  public Pasta() {
    creationDate = Calendar.getInstance().getTime();
    lastModificationDate = Calendar.getInstance().getTime();
    contentTags = new UniqueArrayList<>();
    assignmentTags = new UniqueArrayList<>();

    title = "";
    content = "";
  }

  /**
   * Cloning constructor. Creates a new Pasta from an existing object.
   *
   * @param orig The Pasta to copy.
   */
  public Pasta(Pasta orig) {
    creationDate = orig.creationDate;
    lastModificationDate = orig.lastModificationDate;
    contentTags = new UniqueArrayList<>(orig.contentTags);
    assignmentTags = new UniqueArrayList<>(orig.assignmentTags);
    title = orig.title;
    content = orig.content;
  }

  /**
   * Copy a {@link Collection} of pasta.
   *
   * @param c The original collection.
   * @return A new collection containing copies of the original Pasta.
   */
  public static List<Pasta> copy(Collection<Pasta> c) {
    ArrayList<Pasta> copy = new ArrayList<>(c.size());

    for (Pasta pasta : c) copy.add(pasta.copy());

    return copy;
  }

  // endregion

  // region Getters and setters
  // ================================================================================= //
  // Getters and setters
  // ================================================================================= //

  /** Returns a copy of this pasta. */
  public Pasta copy() {
    return new Pasta(this);
  }

  /**
   * Returns the creation date.
   *
   * @return The creation date.
   */
  public Date getCreationDate() {
    return creationDate;
  }

  /**
   * Returns most recent modification date.
   *
   * @return The modification date.
   */
  public Date getLastModificationDate() {
    return lastModificationDate;
  }

  /**
   * Set the most recent modification date.
   *
   * @param lastModificationDate The new modification date.
   */
  public void setLastModificationDate(Date lastModificationDate) {
    this.lastModificationDate = lastModificationDate;
  }

  /** Set the last modification date to right now. */
  public void setLastModificationDate() {
    lastModificationDate = Calendar.getInstance().getTime();
  }

  /**
   * Returns the content tags.
   *
   * @return The content tags.
   */
  public List<String> getContentTags() {
    return contentTags;
  }

  /**
   * Returns the assignment tags.
   *
   * @return The assignment tags.
   */
  public List<String> getAssignmentTags() {
    return assignmentTags;
  }

  /**
   * Returns the title string, or part of the content if there is no title. Returns {@link
   * #NO_CONTENT} if there's nothing to show.
   *
   * @return The title string.
   */
  public String getTitle() {
    if (!isAutomaticTitle()) return title;

    if (hasContent()) {
      String content = this.content;
      content = content.replaceAll("\n|\r", "").replaceAll("\\s+", " ").trim();
      return content.substring(0, Math.min(CONTENT_SNIPPET_LENGTH, Math.max(0, content.length())));
    } else {
      return NO_CONTENT;
    }
  }

  /**
   * Sets the title string.
   *
   * @param title The title string.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Returns true if there's content, false otherwise.
   *
   * @return {@code content != null && !content.trim().isEmpty()}
   */
  public boolean hasContent() {
    return content != null && !content.trim().isEmpty();
  }

  /**
   * Returns the automatic title setting of this Pasta. Automatic content is enabled when the tittle
   * is null or empty.
   *
   * @return The automatic title setting.
   */
  public boolean isAutomaticTitle() {
    return title == null || title.isEmpty();
  }

  /**
   * Returns the title as-is.
   *
   * @return The title.
   */
  public String getRawTitle() {
    return title;
  }

  /**
   * Returns the content of this model.Pasta.
   *
   * @return The content of this model.Pasta.
   */
  public String getContent() {
    return content;
  }
  // endregion

  /**
   * Sets the content of this model.Pasta.
   *
   * @param content The content of this model.Pasta.
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * Calls {@link #getTitle()}.
   *
   * @return A String representation of this object.
   */
  public String toString() {
    return getTitle();
  }

  /**
   * Compares: {@link #content}, {@link #contentTags}, {@link #assignmentTags}, {@link #title}
   *
   * @param other The object to compare to.
   * @return {@code true} if the given object represents a {@code Pasta} equivalent to this pasta,
   *     {@code false} otherwise
   */
  public boolean equals(Object other) {
    if (this == other) return true;

    // Derived types may equal
    if (other instanceof Pasta) { // "null instanceof class" evaluates to false
      Pasta rhs = (Pasta) other;

      boolean contentEqual =
          (content == rhs.content) || (content != null && content.equals(rhs.content));
      boolean titlesEqual = getTitle().equals(rhs.getTitle());
      boolean listsEqual = // Equals for a list depends on order -- not what we want.
          contentTags.containsAll(rhs.contentTags)
              && assignmentTags.containsAll(rhs.assignmentTags);
      return contentEqual && titlesEqual && listsEqual;
    }

    return false;
  }

  /** Compares titles only. */
  @Override
  public int compareTo(Pasta o) {
    return getTitle().compareTo(o.getTitle());
  }

  @Override
  public Pasta clone() {
    return new Pasta(this);
  }
}
