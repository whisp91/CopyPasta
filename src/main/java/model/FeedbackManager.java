package model;

import gui.Tools;
import gui.feedback.FeedbackText;
import gui.settings.Settings;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/** Created by Richard Sundqvist on 22/02/2017. */
public class FeedbackManager {

  // region Field
  // ================================================================================= //
  // Field
  // ================================================================================= //
  private final UniqueArrayList<Feedback> feedbackList = new UniqueArrayList<>();
  private final UniqueArrayList<Feedback> doneFeedbackList = new UniqueArrayList<>();
  private final UniqueArrayList<Feedback> notDoneFeedbackList = new UniqueArrayList<>();
  private Feedback template = null;
  // endregion

  /**
   * Produce a preview of a Feedback item, displayed in an Alert dialog.
   *
   * @param feedback The Feedback to preview.
   */
  public static void preview(Feedback feedback) {
    if (Settings.USE_NATIVE_TXT_EDITOR && Tools.isDesktopSupported())
      previewFeedbackNative(feedback);
    else previewFeedbackJavaFX(feedback);
  }

  private static void previewFeedbackJavaFX(Feedback feedback) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.getButtonTypes().clear();
    alert.getButtonTypes().add(ButtonType.CLOSE);
    String assignment =
        feedback.getAssignment() == null ? "" : ": \"" + feedback.getAssignment() + "\"";
    alert.setTitle("Feedback Preview" + assignment);
    alert.setHeaderText("Feedback preview for group \"" + feedback.getGroup() + "\"");
    alert.setContentText("Output when exporting as a .txt-file:");

    FeedbackText feedbackText = new FeedbackText();
    feedbackText.setText(feedback.toString());
    feedbackText.setEditable(false);
    feedbackText.setWrapText(true);

    feedbackText.setPrefSize(800, 500);
    feedbackText.setMaxWidth(Double.MAX_VALUE);
    feedbackText.setMaxHeight(Double.MAX_VALUE);
    feedbackText.moveTo(0);
    feedbackText.requestFollowCaret();

    // Set expandable Exception into the dialog pane.
    alert.getDialogPane().setExpandableContent(new VirtualizedScrollPane<>(feedbackText));
    alert.getDialogPane().setExpanded(true);

    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
    alert.showAndWait();
  }

  private static void previewFeedbackNative(Feedback feedback) {
    File file = Tools.PREVIEW_FILE;
    IO.printStringToFile(feedback.getStylizedContent(), file);
    try {
      java.awt.Desktop.getDesktop().open(file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // region Control
  // ================================================================================= //
  // Control
  // ================================================================================= //

  /**
   * Parse an assignment string. This string is almost never visible to the user but rather as a
   * member of {@link Pasta#assignmentTags}. Will return all whitespace and convert to lower case.
   *
   * @param assignment The string to parse.
   * @return A parsed assignment string.
   */
  public static String parseAssignmentString(String assignment) {
    if (assignment == null) return null;

    assignment = assignment.replaceAll("\\s+", "");
    assignment = assignment.toLowerCase();

    return assignment;
  }

  /**
   * Returns a list of all the groups contained in the list. The list may be altered at will.
   *
   * @param feedbackList A group of feedback.
   * @return A list of the groups contained in the list.
   */
  public static List<String> getGroups(List<Feedback> feedbackList) {
    List<String> groups = new UniqueArrayList<>();

    for (Feedback feedback : feedbackList) groups.add(feedback.getGroup());

    return groups;
  }

  public static boolean deleteFeedbackSafe(
      List<Feedback> selectedItems, FeedbackManager feedbackManager) {
    int numberOfItems = selectedItems.size();
    if (numberOfItems > 1 && !Tools.confirmDelete(numberOfItems)) return false;

    if (numberOfItems == 1) {
      Feedback feedback = selectedItems.get(0);
      if (feedbackManager.isContentModified(feedback)) {

        String contentText = "The content of this feedback seems to have been modified.";
        Alert alert =
            new Alert(Alert.AlertType.CONFIRMATION, contentText, ButtonType.OK, ButtonType.CANCEL);

        alert.setHeaderText("Really delete feedback for group \"" + feedback.getGroup() + "\"?");

        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        if (!result.isPresent() || result.get() != ButtonType.OK) return false;
      }
    }

    feedbackManager.deleteFeedback(selectedItems);
    return true;
  }

  /**
   * Returns {@code true} if the group was changed.
   *
   * @param feedback The feedback to change group for.
   * @return {@code true} if the group was changed.
   */
  public boolean changeFeedbackGroup(Feedback feedback) {
    String oldGroup = feedback.getGroup();

    String digitsOnly = oldGroup.replaceAll("[^0-9]", "");
    ButtonType digitsOnlyBt = new ButtonType("Digits only");

    boolean hasDigits = !digitsOnly.isEmpty();
    if (!hasDigits) {
      digitsOnly = "<Empty string not permitted>";
    }

    Alert alert =
        new Alert(
            Alert.AlertType.CONFIRMATION,
            "Will not permit duplicates, empty strings, or strings with only whitespace.",
            ButtonType.CANCEL,
            ButtonType.OK,
            digitsOnlyBt);
    alert.setTitle("Change group");
    alert.setHeaderText("Change group: \"" + oldGroup + "\"");

    javafx.scene.control.Label label1 = new javafx.scene.control.Label("New group: ");
    javafx.scene.control.Label label2 = new javafx.scene.control.Label("Digits only: ");
    label1.setMaxHeight(Double.MAX_VALUE);
    label2.setMaxHeight(Double.MAX_VALUE);

    javafx.scene.control.TextField textField1 = new javafx.scene.control.TextField(oldGroup);
    GridPane.setHgrow(textField1, Priority.ALWAYS);

    javafx.scene.control.TextField textField2 = new TextField(digitsOnly);
    textField2.setEditable(false);
    GridPane.setHgrow(textField1, Priority.ALWAYS);

    GridPane gridPane = new GridPane();
    gridPane.addRow(0, label1, textField1);
    gridPane.addRow(1, label2, textField2);
    alert.getDialogPane().setExpandableContent(gridPane);
    alert.getDialogPane().setExpanded(true);

    Platform.runLater(() -> textField1.requestFocus());
    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
    Optional<ButtonType> result = alert.showAndWait();
    if (!result.isPresent() || result.get() == ButtonType.CANCEL) return false;

    String newGroup = oldGroup;

    if (result.get() == ButtonType.OK) newGroup = textField1.getText().trim();
    else if (hasDigits && result.get() == digitsOnlyBt) newGroup = textField2.getText();

    ArrayList<String> groups = (ArrayList) getGroups();
    if (!groups.contains(newGroup) && !newGroup.isEmpty()) feedback.setGroup(newGroup);

    return oldGroup.equals(newGroup);
  }

  /** Clear all feedback from the manager. Template is not changed. */
  public void clear() {
    feedbackList.clear();
    notDoneFeedbackList.clear();
    doneFeedbackList.clear();
  }

  /**
   * Generate new feedback from the current template. See {@link #setTemplate(Feedback)}.
   *
   * @param groupList The list of groups to create feedback for.
   * @return The list of Feedback which was created.
   * @throws IllegalStateException If template has not been set.
   */
  public List<Feedback> generateFeedback(List<String> groupList) {
    if (template == null) throw new IllegalStateException("Template not set.");
    List<Feedback> newFeedbackList = new ArrayList<>(groupList.size());
    List<String> existingGroups = getGroups();

    for (String group : groupList) {
      if (existingGroups.contains(group)) continue; // Skip duplicate.

      Feedback feedback = new Feedback(template);
      feedback.setGroup(group);
      newFeedbackList.add(feedback);
    }

    feedbackList.addAll(newFeedbackList);
    notDoneFeedbackList.addAll(newFeedbackList);
    return newFeedbackList;
  }

  /**
   * Check whether there are any %MANUAL% tags present.
   *
   * @return A list of feedback contain the %MANUAL% tag.
   */
  public List<Feedback> checkManual() {
    return Feedback.checkManual(feedbackList);
  }

  /**
   * Remove feedback from manager.
   *
   * @param feedback The feedback to remove.
   */
  public void deleteFeedback(Feedback feedback) {
    feedbackList.remove(feedback);
    doneFeedbackList.remove(feedback);
    notDoneFeedbackList.remove(feedback);
  }

  /**
   * Remove all feedback in the list from the manager.
   *
   * @param feedbackList The list of feedback to remove.
   */
  public void deleteFeedback(List<Feedback> feedbackList) {
    feedbackList = new ArrayList<>(feedbackList);
    this.feedbackList.removeAll(feedbackList);
    doneFeedbackList.removeAll(feedbackList);
    notDoneFeedbackList.removeAll(feedbackList);
  }

  /**
   * Removes all feedback with group designations found in {@code groupList}
   *
   * @param groupList A list of groups whose feedback is to be removed.
   * @return Removed feedback items.
   */
  public List<Feedback> removeFeedbackByGroup(List<String> groupList) {
    List<Feedback> removedFeedbackList = new ArrayList<>();

    for (Feedback feedback : feedbackList)
      if (groupList.contains(feedback.getGroup())) removedFeedbackList.add(feedback);

    deleteFeedback(removedFeedbackList);
    return removedFeedbackList;
  }

  /**
   * This method must be called to ensure {@link #doneFeedbackList} and {@link #notDoneFeedbackList}
   * are accurate after changing status of Feedback without using the {@link
   * #setDoneStatus(Feedback, boolean)} method.
   */
  public void updateDoneUndoneLists() {
    doneFeedbackList.clear();
    notDoneFeedbackList.clear();

    for (Feedback feedback : feedbackList)
      if (feedback.isDone()) doneFeedbackList.add(feedback);
      else notDoneFeedbackList.add(feedback);
  }

  /**
   * Change the done status of an item, update {@link #doneFeedbackList} and {@link
   * #notDoneFeedbackList}.
   *
   * @param feedback The feedback items to change.
   * @param done The new done status.
   * @throws IllegalArgumentException If the supplied feedback isn't managed by this
   *     FeedbackManager.
   */
  public void setDoneStatus(Feedback feedback, boolean done) {
    feedback.setDone(done);
    updateDoneUndoneLists(feedback, done);
  }

  private void updateDoneUndoneLists(Feedback feedback, boolean done) {
    if (done) {
      notDoneFeedbackList.remove(feedback);
      doneFeedbackList.add(feedback);
    } else {
      doneFeedbackList.remove(feedback);
      notDoneFeedbackList.add(feedback);
    }
  }

  /**
   * Import the saved template using {@link Tools#importSavedTemplate()} ()}
   *
   * @return The saved template, or {@code null} if nothing was imported.
   */
  public Feedback importSavedTemplate() {
    Feedback template = Tools.importSavedTemplate();
    setTemplate(template);
    return template;
  }

  /**
   * Import saved feedback using {@link Tools#importSavedFeedback()} ()} ()}
   *
   * @return The saved list of feedback, or {@code null} if nothing was imported.
   */
  public List<Feedback> importSavedFeedback() {
    List<Feedback> feedbackList = Tools.importSavedFeedback();
    if (feedbackList != null) {
      importFeedback(feedbackList);
    }
    return feedbackList;
  }

  /**
   * Add feedback to the manager.
   *
   * @param feedback The feedback to add.
   */
  public void importFeedback(Feedback feedback) {
    if (feedbackList.add(feedback)) updateDoneUndoneLists(feedback, feedback.isDone());
  }
  // endregion

  // region Getters and Setters
  // ================================================================================= //
  // Getters and Setters
  // ================================================================================= //

  /**
   * Opens a file chooser dialog and returns a list of Feedback. Will return {@code null} if the
   * import failed.
   *
   * @return A list of Feedback, or {@code null} if nothing was imported.
   */
  public List<Feedback> importFeedback() {
    List<Feedback> feedbackList = IO.importFeedback();
    if (feedbackList != null) for (Feedback feedback : feedbackList) importFeedback(feedback);

    return feedbackList;
  }

  /**
   * Returns {@code true} if the content of this Feedback differs from the template ( {@link
   * #getTemplate()} ).
   *
   * @param feedback The feedback to test.
   * @return {@code true} if the content of this Feedback differs from the template.
   */
  public boolean isContentModified(Feedback feedback) {
    return !feedback.getContent().equals(template.getContent());
  }

  /**
   * Test a list of Feedback for differences from the template ( {@link #getTemplate()} ).
   *
   * @param feedbackList The Feedback to check.
   * @return The modified items. Will return an empty list if no items are modified.
   */
  public List<Feedback> isContentModified(List<Feedback> feedbackList) {
    List<Feedback> modified = new ArrayList<>(feedbackList.size());

    for (Feedback feedback : feedbackList) if (isContentModified(feedback)) modified.add(feedback);

    return modified;
  }

  /**
   * Get Feedback by group designation.
   *
   * @param groups A list of groups (will not be changed).
   * @return A list of Feedback whose group were present in {@code groups}.
   */
  public List<Feedback> getByGroup(List<String> groups) {
    groups = new LinkedList<>(groups);
    List<Feedback> feedbackList = new LinkedList<>(this.feedbackList);
    List<Feedback> found = new ArrayList<>(groups.size());

    int i = 0;
    while (!groups.isEmpty() && i < feedbackList.size()) {
      Feedback feedback = feedbackList.get(i);

      String group = feedback.getGroup();
      if (groups.contains(group)) {
        feedbackList.remove(i);
        groups.remove(group);
        found.add(feedback);
        i = 0;
      } else {
        i++;
      }
    }

    return found;
  }

  /**
   * Get feedback by group designation.
   *
   * @param group A string identifier for the Feedback.
   * @return A Feedback item, if found. {@code null} otherwise.
   */
  public Feedback getByGroup(String group) {
    for (Feedback feedback : feedbackList) {
      if (feedback.getGroup().equals(group)) return feedback;
    }

    return null;
  }

  /**
   * Returns {@code true} if all feedback is marked as done.
   *
   * @return {@code true} if all feedback is marked as done.
   */
  public boolean isAllFeedbackDone() {
    return feedbackList.size() == doneFeedbackList.size();
  }

  public List<Feedback> getFeedbackList() {
    return Collections.unmodifiableList(feedbackList);
  }

  /**
   * Add a list of feedback to the manager. Will not accept feedback whose group collide with an
   * existing member.
   *
   * @param feedbackList The feedback added.
   * @param setTemplateContent If {@code true}, overwrite current content with template content.
   * @return The feedback that was imported.
   */
  public List<Feedback> importFeedback(List<Feedback> feedbackList, boolean setTemplateContent) {
    List<Feedback> newFeedbackList = new ArrayList<>(feedbackList.size());

    List<String> groups = getGroups();
    List<String> newGroups = new ArrayList<>();
    for (Feedback feedback : feedbackList) {
      if (!groups.contains(feedback.getGroup())) {
        newFeedbackList.add(feedback);
        newGroups.add(feedback.getGroup());
        if (setTemplateContent) feedback.setContent(template.getContent());
      }
    }

    this.feedbackList.addAll(newFeedbackList);
    updateDoneUndoneLists();
    return newFeedbackList;
  }

  /**
   * Add a list of feedback to the manager. Will not accept feedback whose group collide with an
   * existing member. Equivalent to calling {@link #importFeedback(List feedbackList, boolean
   * setTemplateContent)} with {@code setTemplateContent = false}.
   *
   * @param feedbackList The feedback added.
   */
  public void importFeedback(List<Feedback> feedbackList) {
    importFeedback(feedbackList, false);
  }

  /**
   * Returns a list of all the groups ({@link Feedback#getGroup()}) of this manager. The list may be
   * altered at will.
   *
   * @return A list of the groups contained in this manager.
   */
  public List<String> getGroups() {
    return getGroups(feedbackList);
  }

  public List<Feedback> getDoneFeedback() {
    return Collections.unmodifiableList(doneFeedbackList);
  }

  public List<Feedback> getNotDoneFeedback() {
    return Collections.unmodifiableList(notDoneFeedbackList);
  }

  public Feedback getTemplate() {
    return template;
  }

  public void setTemplate(Feedback template) {
    this.template = template;
  }

  /**
   * Update all feedback based on current template. Will override assignment, teacher, header, and
   * footer.
   */
  public void updateFeedback() {
    for (Feedback feedback : feedbackList) updateFeedback(feedback);
  }

  public void updateFeedback(Feedback feedback) {
    feedback.setAssignment(template.getAssignment());
    feedback.setHeader(template.getHeader());
    feedback.setFooter(template.getFooter());
    feedback.setSignature(template.getSignature());
  }

  // endregion
}
