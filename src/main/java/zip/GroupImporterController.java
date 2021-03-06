package zip;

import gui.JavaCodeArea;
import gui.Tools;
import gui.feedback.FeedbackListView;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import model.Feedback;
import model.FeedbackListener;
import model.FeedbackManager;
import model.IO;
import model.ManagerListener;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.ViewActions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Created by Richard Sundqvist on 17/04/2017. */
public class GroupImporterController implements FeedbackListener {
  private final JavaCodeArea codeArea;
  private FeedbackManager tmpManager = new FeedbackManager(), realManager;
  @FXML private TextField rootDirectoryField, currentGroupsTextField, manualGroupsTextField;
  @FXML private ListView filesListView;
  @FXML private TreeView treeView;
  @FXML private BorderPane previewContainer;
  @FXML private TextArea filePatternsTextArea;
  @FXML private Label filePreviewLabel;
  @FXML private Button replaceAllButton, importButton;
  @FXML private CheckBox erpaCheckBox; // Enable "Replace All" checkbox
  @FXML private VBox hintVBox, feedbackListContainer;
  @FXML private GridPane rootGrid;
  @FXML private CheckBox toggleOpenArchivesCheckBox;
  private List<String> fileEndingList;
  private ManagerListener listener;
  private boolean newCrawl = true;
  private boolean openArchives;

  private FeedbackListView groupListView;
  private Feedback feedback; // Selected item

  public GroupImporterController() {
    codeArea = new JavaCodeArea();
    codeArea.setEditable(false);
    codeArea.setShowCaret(ViewActions.CaretVisibility.AUTO);
  }

  public static void removeItem(FeedbackTreeItem treeItem, Feedback feedback) {
    File file = treeItem.getFile();

    if (file.isDirectory())
      treeItem
          .getChildren()
          .forEach(childItem -> removeItem((FeedbackTreeItem) childItem, feedback));
    else feedback.removeFile(file.getName());
  }

  public static void addItem(FeedbackTreeItem treeItem, Feedback feedback) {
    File file = treeItem.getFile();

    if (file.isDirectory())
      treeItem.getChildren().forEach(childItem -> addItem((FeedbackTreeItem) childItem, feedback));
    else {
      String content = IO.getFileAsString(file);
      feedback.addFile(file.getName(), content);
    }
  }

  private static void setNodeIcon(TreeItem node, ImageView iw) {
    iw.setFitWidth(18);
    iw.setFitHeight(18);
    iw.setOpacity(0.7);
    node.setGraphic(iw);
  }

  public static void showNoFeedbackAlert() {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setHeaderText("No associated group");
    alert.setContentText("Items must be located in a child folder to add.");

    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
    alert.showAndWait();
  }

  public void onManual() {
    List<String> groups = Tools.extractTokens(manualGroupsTextField.getText());
    manualGroupsTextField.clear();
    tmpManager.generateFeedback(groups);
    onGroupSelectionChanged();
    groupListView.update(tmpManager.getFeedbackList());
    tmpManager.updateFeedback();
  }

  public void onReplaceAll() {
    realManager.clear(); // Replace all content.
    realManager.importFeedback(tmpManager.getFeedbackList(), true);
    close(true);
  }

  public void onCancel() {
    close(false);
  }

  public void onImport() {
    tmpManager.removeFeedbackByGroup(realManager.getGroups()); // Don't overwrite existing content.
    realManager.importFeedback(tmpManager.getFeedbackList(), true);
    close(true);
  }

  public void onGroupSelectionChanged() {
    feedback = groupListView.getSelectionModel().getSelectedItem();
    updateFilesViews();
  }

  public void updateFilesViews() {
    if (feedback != null) filesListView.getItems().setAll(feedback.getFiles().keySet());
    else filesListView.getItems().clear();
    if (treeView.getRoot() != null) crawlNodeStatus((FeedbackTreeItem) treeView.getRoot(), 0, null);
  }

  public void onFileSelectionChanged() {
    Feedback feedback = groupListView.getSelectionModel().getSelectedItem();
    if (feedback == null) return;

    String fileName = (String) filesListView.getSelectionModel().getSelectedItem();
    if (fileName == null) return;

    String content = feedback.getFiles().get(fileName);
    codeArea.setText(content);
    filePreviewLabel.setText(fileName + " - \"" + feedback.getGroup() + "\"");
  }

  public void initialize() {
    previewContainer.setCenter(new VirtualizedScrollPane(codeArea));
    filesListView
        .getSelectionModel()
        .selectedIndexProperty()
        .addListener(event -> onFileSelectionChanged());

    filesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    String s = IO.getFileAsString(Tools.GROUP_IMPORT_FILE_PATTERNS);
    if (s != null && s.length() > 0) {
      filePatternsTextArea.setText(s);
    }

    replaceAllButton.disableProperty().bind(erpaCheckBox.selectedProperty().not());

    treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    treeView.setContextMenu(createTreeViewContextMenu());

    // Tooltip for archive types
    String archiveTypes = Arrays.toString(ArchiveHandler.ARCHIVE_TYPES);
    archiveTypes = archiveTypes.substring(1, archiveTypes.length() - 1);
    Tooltip tooltip = new Tooltip("Archive types: " + archiveTypes);
    tooltip.setFont(new Font(15));
    ImageView iw = NodeColor.GREEN.getImageView();
    iw.setFitWidth(24);
    iw.setFitHeight(24);
    tooltip.setGraphic(iw);
    toggleOpenArchivesCheckBox.setTooltip(tooltip);
  }

  public void onChangeRootDirectory() {
    File dir = IO.showDirectoryChooser(null);

    if (dir != null) {
      hintVBox.setMouseTransparent(true);
      hintVBox.setVisible(false);
      rootGrid.getChildren().remove(hintVBox);
      importButton.setDefaultButton(true);
      newCrawl = true;
      groupListView.getItems().clear();
      filesListView.getItems().clear();
      tmpManager.clear();
      updateFileEndingList();
      feedback = null;
      openArchives = toggleOpenArchivesCheckBox.isSelected();

      try {
        rootDirectoryField.setText(dir.getCanonicalPath());
        FeedbackTreeItem root = crawl(dir, 0, null);
        crawlNodeStatus(root, 0, null);
        newCrawl = false;
        treeView.setRoot(root);
        tmpManager.updateFeedback();
        updateFilesViews();
      } catch (Exception e) {
        IO.showExceptionAlert(e);
        e.printStackTrace();
      }
    } else {
      rootDirectoryField.setText(null);
    }
  }

  public void saveFilePatterns() {
    updateFileEndingList();
    StringBuilder sb = new StringBuilder();
    for (String string : fileEndingList) sb.append(string + "\n");
    IO.printStringToFile(sb.toString(), Tools.GROUP_IMPORT_FILE_PATTERNS);
  }

  private ContextMenu createTreeViewContextMenu() {
    ContextMenu contextMenu = new ContextMenu();

    MenuItem add = new MenuItem("Add files");
    add.setOnAction(event -> addItems());
    MenuItem remove = new MenuItem("Remove files");
    remove.setOnAction(event -> removeItems());

    contextMenu.getItems().addAll(add, new SeparatorMenuItem(), remove);

    return contextMenu;
  }

  public void removeItems() {
    List<FeedbackTreeItem> selectedItems = treeView.getSelectionModel().getSelectedItems();

    for (FeedbackTreeItem treeItem : selectedItems) {
      Feedback feedback = treeItem.getFeedback();
      if (feedback != null) removeItem(treeItem, feedback);
    }
    updateFilesViews();
  }

  public void addItems() {
    List<FeedbackTreeItem> selectedItems = treeView.getSelectionModel().getSelectedItems();

    boolean alertShown = false;
    for (FeedbackTreeItem treeItem : selectedItems) {
      Feedback feedback = treeItem.getFeedback();

      if (!alertShown && feedback == null) {
        showNoFeedbackAlert();
        alertShown = true;
      }

      if (feedback != null) addItem(treeItem, feedback);
    }
    updateFilesViews();
  }

  public void update() {
    update(false);
  }

  public void update(boolean clearDeleted) {
    crawlNodeStatus((FeedbackTreeItem) treeView.getRoot(), 0, null);
    if (clearDeleted) clearDeletedNodes();
    onGroupSelectionChanged();
    groupListView.update(tmpManager.getFeedbackList());
  }

  private void clearDeletedNodes() {
    ArrayList<FeedbackTreeItem> removed = new ArrayList<>();

    List<Feedback> existingFeedback = tmpManager.getFeedbackList();
    for (Object o : treeView.getRoot().getChildren()) {
      FeedbackTreeItem treeItem = (FeedbackTreeItem) o;
      if (!existingFeedback.contains(treeItem.getFeedback())) removed.add(treeItem);
    }
    treeView.getRoot().getChildren().removeAll(removed);
  }

  public void updateFileEndingList() {
    String text = filePatternsTextArea.getText();
    fileEndingList = Tools.extractTokens(text);
  }

  public FeedbackTreeItem crawl(File file, int level, Feedback feedback) throws Exception {
    boolean isRootNode = level == 1;
    if (isRootNode && file.isDirectory()) {
      String group = file.getName();

      feedback = tmpManager.getByGroup(group);

      if (feedback == null) {
        feedback = new Feedback();
        feedback.setGroup(group);
        groupListView.getItems().add(feedback);
        tmpManager.importFeedback(feedback);
      }
    }
    FeedbackTreeItem root = new FeedbackTreeItem(file, feedback, isRootNode);

    for (File dirFile : file.listFiles()) {
      // Directory
      if (dirFile.isDirectory()) {
        FeedbackTreeItem childDir = crawl(dirFile, level + 1, feedback);
        root.getChildren().add(childDir);
      } else {
        // Archive
        if (openArchives && ArchiveHandler.isArchive(dirFile)) {
          String str = "CopyPasta/temp/" + file.getName().toString().replaceAll("\\.", "");
          File extractionFolder = Tools.create(str, null, false);
          boolean extract = ArchiveHandler.extractArchive(dirFile, extractionFolder, true);
          if (!extract) {
            Exception e =
                new Exception(
                    "\nExtraction of \""
                        + dirFile.getName()
                        + "\" didn't go as expected. CopyPasta tried its best, but the result might be a little iffy. "
                        + "Stack Trace has been printed to the standard output stream.");
            IO.showExceptionAlert(e);
          }

          FeedbackTreeItem childDir = crawl(extractionFolder, level + 1, feedback);
          root.getChildren().add(childDir);
          // Regular file
        } else {
          FeedbackTreeItem child = new FeedbackTreeItem(dirFile, feedback);
          root.getChildren().add(child);

          String[] s = dirFile.getName().split("\\.");
          if (feedback != null && s.length > 1 && fileEndingList.contains(s[s.length - 1])) {
            String content = IO.getFileAsString(dirFile);
            feedback.addFile(dirFile.getName(), content);
          }
        }
      }
    }
    return root;
  }

  private NodeColor crawlNodeStatus(FeedbackTreeItem root, int level, Feedback feedback) {
    NodeColor nodeColor;
    // =========================== //
    // level 1 => group root
    // =========================== //
    if (level == 1) {
      feedback = root.getFeedback();
      if (feedback != null) root.setValue(feedback.getGroup());
    }

    if (root.isLeaf()) { // leaf => file or empty folder
      if (feedback != null && feedback.getFiles().keySet().contains(root.getValue()))
        nodeColor = NodeColor.GREEN;
      else nodeColor = NodeColor.RED;

      // =========================== //
      // Child node
      // =========================== //
    } else {
      List<TreeItem<String>> children = root.getChildren();
      NodeColor[] childNodeColors = new NodeColor[children.size()];

      for (int i = 0; i < children.size(); i++) {
        FeedbackTreeItem treeItem = (FeedbackTreeItem) children.get(i);
        childNodeColors[i] = crawlNodeStatus(treeItem, level + 1, feedback);
      }
      nodeColor = NodeColor.getCombinedStatus(childNodeColors);
      setNodeIcon(root, nodeColor.getImageView());
    }
    setNodeIcon(root, nodeColor.getImageView());
    if (newCrawl && nodeColor != NodeColor.RED) root.setExpanded(true);
    return nodeColor;
  }

  public void initialize(FeedbackManager feedbackManager) {
    realManager = feedbackManager;
    List<String> existingGroups = realManager.getGroups();
    existingGroups.sort(String::compareToIgnoreCase);
    String groups = existingGroups.toString();
    currentGroupsTextField.setText(groups.substring(1, groups.length() - 1));
    tmpManager.setTemplate(realManager.getTemplate().copy());
    groupListView = new FeedbackListView(tmpManager.getFeedbackList(), tmpManager, this);
    groupListView
        .getSelectionModel()
        .selectedIndexProperty()
        .addListener(event -> onGroupSelectionChanged());
    feedbackListContainer.getChildren().add(groupListView);
  }

  public void setListener(ManagerListener listener) {
    this.listener = listener;
  }

  private void close(boolean managerChanged) {
    saveFilePatterns();
    managerChanged = managerChanged && !tmpManager.getFeedbackList().isEmpty();
    listener.close(managerChanged);
  }

  @Override
  public void changeGroup(List<Feedback> feedbackList) {
    if (feedbackList.isEmpty()) return;

    feedbackList.forEach(
        feedback -> {
          tmpManager.changeFeedbackGroup(feedback);
        });
    update();
  }

  public void onChangeGroup() {
    List<Feedback> feedbackList = groupListView.getSelectionModel().getSelectedItems();
    changeGroup(feedbackList);
  }

  public void onDelete() {
    List<Feedback> feedbackList = groupListView.getSelectionModel().getSelectedItems();
    if (!feedbackList.isEmpty()) {
      FeedbackManager.deleteFeedbackSafe(feedbackList, tmpManager);
      listChanged();
    }
  }

  @Override
  public void toggleDone(List<Feedback> feedbackList) {
    Tools.notSupportedInView();
  }

  @Override
  public void preview(List<Feedback> feedbackList) {
    tmpManager.updateFeedback();
    System.out.println("tmpManager = " + tmpManager.getFeedbackList().size());
    System.out.println("realManager.getTemplate() = " + realManager.getTemplate());
    feedbackList.forEach(FeedbackManager::preview);
  }

  @Override
  public boolean exportFeedback(List<Feedback> feedbackList, boolean asTxt, boolean asJson) {
    Tools.notSupportedInView();
    return false;
  }

  @Override
  public void listChanged() {
    update(true);
  }

  public enum NodeColor {
    RED,
    GREEN,
    BLUE;

    public static NodeColor getCombinedStatus(NodeColor... status) {
      NodeColor status0 = status[0];

      if (status0 == RED || status0 == GREEN)
        for (int i = 1; i < status.length; i++) if (status[i] != status0) return BLUE;

      return status0;
    }

    public ImageView getImageView() {
      String s = null;

      switch (this) {
        case RED:
          s = "/img/red_circle.png";
          break;
        case GREEN:
          s = "/img/green_circle.png";
          break;
        case BLUE:
          s = "/img/blue_circle.png";
          break;
      }
      return new ImageView(new Image(getClass().getResourceAsStream(s)));
    }
  }

  public static class FeedbackTreeItem extends TreeItem<String> {
    private final Feedback feedback;
    private final File file;

    public FeedbackTreeItem(File file, Feedback feedback, boolean isRootNode) {
      super(isRootNode ? feedback.getGroup() : file.getName());
      this.feedback = feedback;
      this.file = file;
    }

    public FeedbackTreeItem(File file, Feedback feedback) {
      super(file.getName());
      this.feedback = feedback;
      this.file = file;
    }

    public Feedback getFeedback() {
      return feedback;
    }

    public File getFile() {
      return file;
    }
  }
}
