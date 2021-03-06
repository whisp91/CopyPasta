package gui.pasta;

import gui.Tools;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import model.IO;
import model.ManagerListener;
import model.Pasta;
import model.PastaManager;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PastaViewController implements ManagerListener {
  // region Field
  // ================================================================================= //
  // Field
  // ================================================================================= //
  private PastaControllerListener listener;
  @FXML private ListView listView;
  @FXML private TextArea previewTextArea;
  @FXML private FlowPane filterFlowPane;
  @FXML private TextField searchField;

  private PastaManager pastaManager = new PastaManager();;
  // endregion

  private static void exportPastaTXT(List<Pasta> pastaList) {
    String gatheredContent = PastaManager.gatherContent(pastaList);
    File file = IO.showTXTSaveDialog(null, "pasta");
    IO.printStringToFile(gatheredContent, file);
  }

  public void initialize() {
    listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    searchField.textProperty().addListener(event -> searchPasta());
  }

  private void searchPasta() {
    String searchString = searchField.getText();
    List<String> searchTerms = Arrays.asList(searchString.split(","));
    pastaManager.search(searchTerms);
    updateFilteredList();
  }

  public void initialize(PastaControllerListener listener) {
    initialize(listener, true);
  }

  public void initialize(PastaControllerListener listener, boolean importPasta) {
    this.listener = listener;

    if (importPasta) {
      List<Pasta> importedPastaList;
      importedPastaList = pastaManager.importSavedPasta();

      if (importedPastaList != null) {
        listView.getItems().addAll(importedPastaList);
        updateFilters();
      }
    }
  }

  public void quickInsert() {
    Pasta pasta = (Pasta) listView.getSelectionModel().getSelectedItem();
    listener.quickInsert(pasta);
    listView.getSelectionModel().clearSelection();
  }

  public void onMouseClicked(MouseEvent event) {
    Pasta pasta = (Pasta) listView.getSelectionModel().getSelectedItem();
    if (pasta != null) {
      previewTextArea.setText(pasta.getContent());

      if (event.getClickCount() > 1) preview();
    }
    if (listener != null) listener.select(pasta);
  }

  public void copyItem() {
    Pasta pasta = (Pasta) listView.getSelectionModel().getSelectedItem();
    if (pasta != null) PastaManager.copyPastaContentsToClipboard(pasta);
  }

  public void exportAllPasta() {
    pastaManager.exportPasta();
  }

  public void exportPastaJSON() {
    List<Pasta> pastaList = listView.getSelectionModel().getSelectedItems();
    PastaManager.exportPasta(pastaList);
  }

  public void exportPastaTXT() {
    List<Pasta> pastaList = listView.getSelectionModel().getSelectedItems();
    exportPastaTXT(pastaList);
  }

  public void importPasta() {
    List<Pasta> importedPastaList = IO.importPasta();
    importPasta(importedPastaList);
  }

  public void importPasta(List<Pasta> importedPastaList) {
    importedPastaList = pastaManager.importPasta(importedPastaList);
    if (importedPastaList != null) {
      updateFilters();
      listView.getItems().addAll(importedPastaList);
    }
  }

  public void save() {
    pastaManager.exportSavedPasta();
  }

  public void update() {
    listView.getItems().setAll(pastaManager.getPastaList());
    updateFilters();
  }

  public void clearAllPasta() {
    int numItems = pastaManager.getPastaList().size();

    if (Tools.confirmDelete(numItems)) clearAllNoWarning();
  }

  public void clearAllNoWarning() {
    pastaManager.clear();
    filterFlowPane.getChildren().clear();
    listView.getItems().clear();
  }

  public void clearTagFilters() {
    pastaManager.clearTagFilters();
    updateFilters();
    updateFilteredList();
  }

  public void updateFilters() {
    filterFlowPane.getChildren().clear();

    for (String tag : pastaManager.getTagList()) {
      CheckBox cb = new CheckBox(tag);
      cb.setPadding(new Insets(0, 0, 0, 2));
      cb.setOnAction(event -> onTagChanged(cb));
      filterFlowPane.getChildren().add(cb);
    }
  }

  public void matchAnyTag() {
    pastaManager.setAnyTag(true);
    updateFilteredList();
  }

  public void matchAllTag() {
    pastaManager.setAnyTag(false);
    updateFilteredList();
  }

  private void onTagChanged(CheckBox cb) {

    boolean changed;
    if (cb.isSelected()) changed = pastaManager.addFilterTag(cb.getText());
    else changed = pastaManager.removeFilterTag(cb.getText());

    if (changed) updateFilteredList();
  }

  public void refreshListView() {
    listView.refresh();
  }

  private void updateFilteredList() {
    listView.getItems().clear();
    listView.getItems().addAll(pastaManager.getFilteredPastaList());
  }

  public void delete() {
    List<Pasta> selectedItems = listView.getSelectionModel().getSelectedItems();
    int numberOfItems = selectedItems.size();
    if (numberOfItems > 1) {
      String contentText =
          "Really delete all selected Pasta? There are currently "
              + numberOfItems
              + " items selected.";
      Alert alert =
          new Alert(Alert.AlertType.CONFIRMATION, contentText, ButtonType.OK, ButtonType.CANCEL);
      alert.setHeaderText("Really delete all selected Pasta?");

      alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
      alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
      Optional<ButtonType> result = alert.showAndWait();
      if (!result.isPresent() || result.get() != ButtonType.OK) return;
    }

    pastaManager.removePasta(selectedItems);
    listView.getItems().removeAll(selectedItems);
    listener.select(null);
  }

  public void preview() {
    Pasta pasta = (Pasta) listView.getSelectionModel().getSelectedItem();
    if (pasta != null) PastaManager.preview(pasta);
  }

  public void toggleNotTag(Event event) {
    CheckBox cb = (CheckBox) event.getSource();
    pastaManager.setNegate(cb.isSelected());
    updateFilteredList();
  }

  public PastaManager getPastaManager() {
    return pastaManager;
  }

  public void setPastaManager(PastaManager pastaManager) {
    this.pastaManager = pastaManager;
  }

  public void toggleCurrentAssignmentOnly(Event event) {
    ToggleButton toggleButton = (ToggleButton) event.getSource();

    if (toggleButton.isSelected()) {
      String assignment = listener.getAssignment();
      pastaManager.setAssignment(assignment);
    } else {
      pastaManager.setAssignment(null);
    }
    updateFilteredList();
  }

  public Pasta createNew() {
    Pasta newPasta = pastaManager.createNew();
    listView.getItems().remove(newPasta);
    listView.getItems().add(0, newPasta);
    listView.getSelectionModel().clearAndSelect(0);
    previewTextArea.clear();
    return newPasta;
  }

  @Override
  public void close(boolean managerChanged) {
    if (managerChanged) update();
  }

  // ====================================================
  // Interface declaration
  // ====================================================

  /** FeedbackListListener interface for the controller. */
  public interface PastaControllerListener {
    /**
     * Called when an item is selected. May be null.
     *
     * @param pasta The selected item.
     */
    void select(Pasta pasta);

    /**
     * Called to quick insert an item.
     *
     * @param pasta The selected item
     */
    void quickInsert(Pasta pasta);

    /**
     * Called {@link #toggleCurrentAssignmentOnly} invocation.
     *
     * @return The assignment to filter for.
     */
    String getAssignment();
  }
}
