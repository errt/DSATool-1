/*
 * Copyright 2017 DSATool team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dsatool.gui;

import java.io.File;
import java.io.FileInputStream;
import java.util.function.Supplier;

import org.controlsfx.control.StatusBar;

import dsatool.control.MainWindowController;
import dsatool.plugins.PluginLoader;
import dsatool.resources.GroupFileManager;
import dsatool.resources.Settings;
import dsatool.settings.BooleanSetting;
import dsatool.ui.DetachableNode;
import dsatool.ui.MenuGroup;
import dsatool.update.Update;
import dsatool.util.ErrorLogger;
import dsatool.util.Util;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {
	/**
	 * The applications main menu
	 */
	public static dsatool.ui.Menu mainMenu;

	public static StatusBar statusBar;

	/**
	 * The menu tools can be selected from
	 */
	public static dsatool.ui.Menu toolsMenu;

	private static MainWindowController window;

	public static Main app;

	public static void addDetachableToolComposite(final String groupName, final String name, final int width, final int height,
			final Supplier<Node> constructor) {
		final MenuGroup group = Main.toolsMenu.addGroup(groupName);
		final dsatool.ui.MenuItem item = group.addItem(name);

		final DetachableNode composite = new DetachableNode(constructor, item, name, window.getToolArea(), width, height);

		window.resizeToolSelector();

		item.setAction(event -> {
			composite.bringToTop();
			event.consume();
		});
	}

	public static void main(final String[] args) {
		Settings.addSetting(new BooleanSetting("Auto-Update", true, "Allgemein", "Auto-Update"));
		if (new File(Update.updateListPath).exists()) {
			Update.execute();
		}
		if (Settings.getSettingBoolOrDefault(true, "Allgemein", "Auto-Update")) {
			new Thread(() -> new Update().searchUpdates(false)).start();
		}
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) {
		app = this;
		try {
			final FXMLLoader fxmlLoader = new FXMLLoader();
			final BorderPane root = fxmlLoader.load(getClass().getResource("MainWindow.fxml").openStream());
			window = fxmlLoader.getController();
			window.setStage(primaryStage);

			Font.loadFont(new FileInputStream(new File(Util.getAppDir() + "/resources/fonts/MaterialIcons-Regular.ttf")), 15);

			final Scene scene = new Scene(root, 1100, 880);

			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			primaryStage.setTitle("DSA Tool");
			primaryStage.setScene(scene);

			window.initializeMenus();
			statusBar = window.getStatusBar();
			statusBar.setText("");

			PluginLoader.loadPlugins();

			primaryStage.show();

			window.resizeToolSelector();

			GroupFileManager.openCurrentGroup();
		} catch (final Exception e) {
			ErrorLogger.logError(e);
		}
	}
}
