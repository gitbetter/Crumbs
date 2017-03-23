import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.animation.FillTransition;
import javafx.util.Duration;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;

import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import java.util.*;
import java.util.stream.Collectors;

public class CrumbsClient extends Application {
	private final String[] menuOptions = {"New", "Open", "Save", "Save as", "Settings"};
	private boolean isMenuCollapsed = false;

	private LinkedHashMap<FileHandle, AnchorPane> openFiles = new LinkedHashMap<>();
	private FileHandle currentFile;

	private AnchorPane pane;
	private Stage theStage;
	private TextArea editor;
	private Pane menuContainer;
	private Pane mainContent;
	private HBox topTools;
	private HBox bottomTools;
	private HBox tabs;
	private Button tabsButton;

	@Override
	public void start(Stage stg) {
		theStage = stg;

		theStage.setTitle("Crumbs");
		theStage.show();
		theStage.centerOnScreen();

		makeCrumbs();
		newFile();
	}

	@Override
	public void stop() {
		Platform.exit();
	}



	/* SETUP SETUP SETUP SETUP SETUP SETUP SETUP SETUP SETUP SETUP SETUP SETUP SETUP SETUP SETUP SETUP
		////////////////////////////////////////////////////////////////////////////////////////////////
	 SETUP SETUP SETUP SETUP SETUP SETUP SETUP SETUP SETUP SETUP SETUP SETUP SETUP SETUP SETUP SETUP */



	public void makeCrumbs() {
		pane = new AnchorPane();

		setupBottomToolbar();

		AnchorPane.setLeftAnchor(bottomTools, 0.0);
		AnchorPane.setRightAnchor(bottomTools, 0.0);
		AnchorPane.setBottomAnchor(bottomTools, 0.0);

		setupContentArea();

		AnchorPane.setTopAnchor(mainContent, 0.0);
		AnchorPane.setRightAnchor(mainContent, 0.0);
		AnchorPane.setBottomAnchor(mainContent, 0.0);
		AnchorPane.setLeftAnchor(mainContent, 100.0);

		setupCrumbsMenu();

		AnchorPane.setLeftAnchor(menuContainer, 0.0);
		AnchorPane.setTopAnchor(menuContainer, 0.0);
		AnchorPane.setBottomAnchor(menuContainer, 30.0);

		Scene scene = new Scene(pane, 750, 900);
		scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

		theStage.setScene(scene);
		theStage.sizeToScene();
		theStage.setResizable(true);
	}

	private void setupCrumbsMenu() {
    menuContainer = new Pane();
		menuContainer.setPrefWidth(100);

		VBox crumbsMenu = new VBox();
		crumbsMenu.setId("cr-menu");
		crumbsMenu.setFillWidth(true);
		crumbsMenu.setAlignment(Pos.TOP_CENTER);
		crumbsMenu.prefWidthProperty().bind(menuContainer.widthProperty());
		crumbsMenu.prefHeightProperty().bind(menuContainer.heightProperty());

		Rectangle menuToggle = new Rectangle();
		menuToggle.setId("cr-menu-toggle");
		menuToggle.setWidth(3);
		menuToggle.heightProperty().bind(menuContainer.heightProperty());
		menuToggle.setLayoutX(100.0);

		for(String option : menuOptions) {
			String identifier = "cr-menu-" + option.toLowerCase();

			Button menuOption = new Button(option);
			menuOption.getStyleClass().add("cr-menu-option");
			menuOption.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

			switch(option.toLowerCase()) {
				case "new":
					menuOption.setOnMouseClicked(ev -> {
						if(editor.isDisable()) editor.setDisable(false);
						newFile();
					});
					break;
				case "save":
					menuOption.setOnMouseClicked(ev -> {
						save();
					});
					break;
				case "save as":
					menuOption.setOnMouseClicked(ev -> {
						saveAs();
					});
					break;
				case "open":
					menuOption.setOnMouseClicked(ev -> {
						FileChooser fc = new FileChooser();
						fc.setTitle("Open file");
						File existing = fc.showOpenDialog(theStage);

						if(existing != null) {
							loadFile(existing);
						}
					});
					break;
				case "edit":
					break;
				case "settings":
					break;
				default:
					break;
			}

			crumbsMenu.getChildren().add(menuOption);
		}

		menuContainer.getChildren().addAll(crumbsMenu, menuToggle);

		pane.getChildren().add(menuContainer);

		menuToggle.setOnMouseEntered(ev -> {
			FillTransition ft = new FillTransition(Duration.millis(300), menuToggle, Color.web("#333030"), Color.web("#55bded"));
			ft.play();
		});

		menuToggle.setOnMouseExited(ev -> {
			FillTransition ft = new FillTransition(Duration.millis(300), menuToggle, Color.web("#55bded"), Color.web("#333030"));
			ft.play();
		});

		menuToggle.setOnMouseClicked(ev -> {
			if(!isMenuCollapsed) {
				AnchorPane.setLeftAnchor(menuContainer, -100.0);
				AnchorPane.setLeftAnchor(mainContent, 0.0);
				menuToggle.setWidth(6.0);
			} else {
				AnchorPane.setLeftAnchor(menuContainer, 0.0);
				AnchorPane.setLeftAnchor(mainContent, 100.0);
				menuToggle.setWidth(3.0);
			}

			isMenuCollapsed = !isMenuCollapsed;
		});
	}

	public void setupContentArea() {
		mainContent = new Pane();
		topTools = new HBox();

		topTools.setId("cr-editor-tools");
		topTools.setFillHeight(true);
		topTools.prefWidthProperty().bind(mainContent.widthProperty());
		topTools.setPrefHeight(35.0);

		tabsButton = new Button();
		tabsButton.setId("cr-tabs-button");
		tabsButton.setGraphic(new ImageView(new Image("icons/pages.png", 30, 30, true, true)));

		topTools.getChildren().add(tabsButton);

		tabs = new HBox();
		tabs.setId("cr-tabs-area");
		tabs.setPrefHeight(0.0);
		tabs.prefWidthProperty().bind(mainContent.widthProperty());
		tabs.layoutYProperty().bind(topTools.prefHeightProperty());

		editor = new TextArea();
		editor.setWrapText(true);
		editor.setId("cr-editor");
		editor.prefWidthProperty().bind(mainContent.widthProperty());
		editor.prefHeightProperty().bind(mainContent.heightProperty().subtract(topTools.getPrefHeight() + bottomTools.getPrefHeight() + 3.0));
		editor.layoutYProperty().bind(topTools.prefHeightProperty().add(3.0));

		mainContent.getChildren().addAll(topTools, tabs, editor);

		pane.getChildren().add(mainContent);

		tabsButton.setOnMouseEntered(ev -> {
			tabs.setPrefHeight(80.0);
			editor.layoutYProperty().bind(topTools.prefHeightProperty().add(tabs.getPrefHeight()));
			editor.prefHeightProperty().bind(mainContent.heightProperty().subtract(topTools.getPrefHeight() + bottomTools.getPrefHeight() + tabs.getPrefHeight()));
		});

		pane.addEventFilter(MouseEvent.MOUSE_MOVED, ev -> {
			if(!Utils.withinNode(tabsButton, ev.getSceneX(), ev.getSceneY()) && !Utils.withinRegion(tabs, ev.getSceneX(), ev.getSceneY())) {
					tabs.setPrefHeight(0.0);
					editor.layoutYProperty().bind(topTools.prefHeightProperty().add(tabs.getPrefHeight() + 3.0));
					editor.prefHeightProperty().bind(mainContent.heightProperty().subtract(topTools.getPrefHeight() + bottomTools.getPrefHeight() + 3.0));
			}
		});

		editor.setOnKeyTyped(kEv -> {
			if(kEv.isMetaDown() && kEv.getCharacter().equals("s"))
				save();
		});
	}

	public void setupBottomToolbar() {
		bottomTools = new HBox();
		bottomTools.setId("cr-bottom-tools");
		bottomTools.setFillHeight(true);
		bottomTools.setPrefHeight(30.0);

		Button numbersIcon = new Button();
		numbersIcon.setId("cr-numbers-button");
		numbersIcon.setGraphic(new ImageView(new Image("icons/line_numbers.png", 15, 15, true, true)));

		bottomTools.getChildren().add(numbersIcon);

		pane.getChildren().add(bottomTools);
	}



	/* COMPONENTS COMPONENTS COMPONENTS COMPONENTS COMPONENTS COMPONENTS COMPONENTS COMPONENTS COMPONENTS
		///////////////////////////////////////////////////////////////////////////////////////////////////
	 	COMPONENTS COMPONENTS COMPONENTS COMPONENTS COMPONENTS COMPONENTS COMPONENTS COMPONENTS COMPONENTS */



	private AnchorPane makeTabThumb(FileHandle fh) {
		AnchorPane tab = new AnchorPane();
		tab.getStyleClass().add("cr-tab");
		tab.setPrefWidth(120.0);
		tab.prefHeightProperty().bind(tabs.prefHeightProperty());

		Label fileName = new Label(fh.getFile().getName());
		fileName.getStyleClass().add("cr-tab-tag");
		fileName.setPrefHeight(12.0);

		AnchorPane.setTopAnchor(fileName, 0.0);
		AnchorPane.setRightAnchor(fileName, 0.0);
		AnchorPane.setLeftAnchor(fileName, 0.0);

		TextArea filePreview = new TextArea();
		filePreview.getStyleClass().add("cr-tab-preview");
		filePreview.setWrapText(true);
		filePreview.setEditable(false);
		filePreview.setText(fh.getFileContents());
		filePreview.setPrefHeight(53.0);

		AnchorPane.setBottomAnchor(filePreview, 0.0);
		AnchorPane.setRightAnchor(filePreview, 0.0);
		AnchorPane.setLeftAnchor(filePreview, 0.0);

		Button xOut = new Button("X");
		xOut.getStyleClass().add("cr-tab-remove-btn");
		xOut.setPrefSize(3, 3);
		xOut.setLayoutY(fileName.getPrefHeight() + 10);

		AnchorPane.setRightAnchor(xOut, 0.0);

		tab.getChildren().addAll(fileName, filePreview, xOut);

		ScrollBar fpScroll = (ScrollBar)filePreview.lookup(".scroll-bar:vertical");
		if(fpScroll != null) fpScroll.setDisable(true);

		xOut.setOnMouseClicked(evC1 -> {
			int idx = tabs.getChildren().indexOf(tab);
			tabs.getChildren().remove(tab);
			this.openFiles.remove(fh);

			if(tabs.getChildren().size() > 0) {
				if(idx > 0) idx -= 1;

				for(Map.Entry<FileHandle, AnchorPane> fileEntry : openFiles.entrySet()) {
					if(fileEntry.getValue().equals(tabs.getChildren().get(idx)))
						this.currentFile = fileEntry.getKey();
				}

				updateTabs();
				populateEditor();
			} else {
				emptyStateEditor();
			}
		});

		double deltaX[] = {0};
		filePreview.addEventFilter(MouseEvent.MOUSE_PRESSED, pEv -> {
			deltaX[0] = pEv.getX();
		});

		filePreview.addEventFilter(ScrollEvent.SCROLL, evS -> {
			if(evS.getDeltaY() != 0)
				evS.consume();
		});

		int newTabIdx[] = {tabs.getChildren().indexOf(tab)};
		filePreview.addEventFilter(MouseEvent.MOUSE_DRAGGED, dEv -> {
			for(Node tabPane : tabs.getChildren()) {
				tabPane.lookup(".cr-tab-preview").setStyle("");
			}

			if(deltaX[0] != dEv.getX()) {
				if(dEv.getX() < 0) {
					newTabIdx[0] = tabs.getChildren().indexOf(tab) - Math.round((float)(dEv.getX() / filePreview.getWidth())) * -1;
					if(newTabIdx[0] < 0) newTabIdx[0] = 0;

					AnchorPane tabPane = (AnchorPane) tabs.getChildren().get(newTabIdx[0]);
					tabPane.lookup(".cr-tab-preview").setStyle("-fx-border-width: 0 0 0 3px; -fx-border-style: solid; -fx-border-color: #55bded");
				} else {
					int tabDelta = Math.round((float)(dEv.getX() / filePreview.getWidth()));
					if(tabDelta == 0) tabDelta = 1;
					newTabIdx[0] = tabs.getChildren().indexOf(tab) + tabDelta - 1;
					if(newTabIdx[0] >= tabs.getChildren().size()) newTabIdx[0] = tabs.getChildren().size() - 1;

					AnchorPane tabPane = (AnchorPane) tabs.getChildren().get(newTabIdx[0]);
					tabPane.lookup(".cr-tab-preview").setStyle("-fx-border-width: 0 3px 0 0; -fx-border-style: solid; -fx-border-color: #55bded");
				}
			}
			dEv.consume();
		});

		filePreview.addEventFilter(MouseEvent.MOUSE_RELEASED, rEv -> {
			for(Node tabPane : tabs.getChildren()) {
				tabPane.lookup(".cr-tab-preview").setStyle("");
			}

			if(!Utils.withinNode(filePreview, rEv.getSceneX(), rEv.getSceneY())) {
				tabs.getChildren().remove(tab);
				tabs.getChildren().add(newTabIdx[0], tab);

				reorderTabs();
			}
		});

		filePreview.addEventFilter(MouseEvent.MOUSE_CLICKED, evC2 -> {
			this.currentFile.setFileContents(editor.getText());

			this.currentFile = fh;
			updateTabs();
			populateEditor();

			editor.requestFocus();
		});

		return tab;
	}



	/* HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS
	  ////////////////////////////////////////////////////////////////////////////////////////////////
	 HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS HELPERS */



	private void toggleTabs() {
 		if(tabs.getPrefHeight() == 80.0) {
 			tabs.setPrefHeight(0.0);
 		} else {
 			tabs.setPrefHeight(80.0);
 		}
 		editor.layoutYProperty().bind(topTools.prefHeightProperty().add(tabs.getPrefHeight()));
 	}

	public void populateEditor() {
		this.editor.setText(currentFile.getFileContents());
	}

	public void emptyStateEditor() {
		editor.setText("");
		editor.setDisable(true);
	}

	public void updateTabs() {
		tabs.getChildren().clear();
		for(Map.Entry<FileHandle, AnchorPane> tab : openFiles.entrySet()) {
			TextArea tabPreview = (TextArea) tab.getValue().lookup(".cr-tab-preview");
			tabPreview.setText(tab.getKey().getFileContents());
			tabs.getChildren().add(tab.getValue());

			tabPreview.getStyleClass().add("cr-tab-unselected");
			if(tab.getKey().equals(this.currentFile)) {
				tabPreview.getStyleClass().removeAll("cr-tab-unselected");
			}
		}
	}

	/**
	 *	This method ensures tabs retain their
	 *	ordering on subsequent tab updates. The tab ordering
	 *  is also maintained across Crumbs sessions when the
	 *  open files are serialized and stored.
	 */
	public void reorderTabs() {
		LinkedHashMap<FileHandle, AnchorPane> newOpenFiles = new LinkedHashMap<>();
		LinkedHashMap<AnchorPane, FileHandle> revOpenFiles = new LinkedHashMap<>();

		for(Map.Entry<FileHandle, AnchorPane> tab : openFiles.entrySet())
				revOpenFiles.put(tab.getValue(), tab.getKey());

		for(Node tab : tabs.getChildren()) {
			newOpenFiles.put(revOpenFiles.get((AnchorPane)tab), (AnchorPane)tab);
		}

		this.openFiles = newOpenFiles;
	}

	/**
	 *  Create a new file
	 *
	 */
	 public void newFile() {
		 // Save the previous file's contents
		 if(this.currentFile != null)
		 	this.currentFile.setFileContents(editor.getText());

		 FileHandle newFile = new FileHandle(new File(Utils.getUserHomePath() + "untitled"));
		 int copyIndex = 1;
		 while(this.openFiles.get(newFile) != null) {
			 newFile.setFile(new File(Utils.getUserHomePath() + "untitled(" + copyIndex++ + ")"));
		 }
		 this.currentFile = newFile;
		 this.openFiles.put(newFile, makeTabThumb(newFile));

		 populateEditor();
		 updateTabs();

		 editor.requestFocus();
	 }


	/**
	 *  Save the current file to disk
	 *  @param f		The file whose contents are to be loaded
	 */
	public void save() {
		if(currentFile.isNew()) {
			saveAs();
		} else {
			currentFile.setFileContents(editor.getText());
			currentFile.saveToFile();

			updateTabs();
		}
	}

	/**
	 *  Saves the current file to disk as
   *  a new file specified by the user
	 *	@param f		The file to be saved
	 */
	public void saveAs() {
		FileChooser fc = new FileChooser();
		fc.setTitle("Save File As");
		File newFile = fc.showSaveDialog(theStage);

		if(newFile != null) {
			this.currentFile.setFile(newFile);
			this.currentFile.setFileContents(editor.getText());
			this.currentFile.saveToFile();

			openFiles.replace(this.currentFile, makeTabThumb(this.currentFile));

			updateTabs();
		}
	}

	/**
	 *  Load the contents of an existing file
	 *  onto the editor
	 *  @param f		The file whose contents are to be loaded
	 */
	public void loadFile(File f) {
		FileHandle newFile = new FileHandle(f);
		String contents = newFile.fileContents();

		if(openFiles.get(newFile) == null)
			openFiles.put(newFile, makeTabThumb(newFile));

		this.currentFile = newFile;

		populateEditor();
		updateTabs();

		editor.requestFocus();
	}
}
