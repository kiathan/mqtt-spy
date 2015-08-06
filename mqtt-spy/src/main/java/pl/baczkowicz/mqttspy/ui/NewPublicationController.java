/***********************************************************************************
 * 
 * Copyright (c) 2014 Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 *    
 * The Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation
 *    
 */
package pl.baczkowicz.mqttspy.ui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.common.generated.ConversionMethod;
import pl.baczkowicz.mqttspy.common.generated.SimpleMqttMessage;
import pl.baczkowicz.mqttspy.connectivity.MqttAsyncConnection;
import pl.baczkowicz.mqttspy.exceptions.ConversionException;
import pl.baczkowicz.mqttspy.messages.BaseMqttMessage;
import pl.baczkowicz.mqttspy.scripts.InteractiveScriptManager;
import pl.baczkowicz.mqttspy.scripts.Script;
import pl.baczkowicz.mqttspy.scripts.ScriptManager;
import pl.baczkowicz.mqttspy.scripts.ScriptTypeEnum;
import pl.baczkowicz.mqttspy.ui.events.EventManager;
import pl.baczkowicz.mqttspy.ui.events.observers.ScriptListChangeObserver;
import pl.baczkowicz.mqttspy.ui.keyboard.TimeBasedKeyEventFilter;
import pl.baczkowicz.mqttspy.ui.panes.PaneVisibilityStatus;
import pl.baczkowicz.mqttspy.ui.panes.TitledPaneController;
import pl.baczkowicz.mqttspy.ui.properties.PublicationScriptProperties;
import pl.baczkowicz.mqttspy.ui.utils.DialogUtils;
import pl.baczkowicz.mqttspy.ui.utils.UiUtils;
import pl.baczkowicz.mqttspy.utils.ConversionUtils;
import pl.baczkowicz.mqttspy.utils.FileUtils;
import pl.baczkowicz.mqttspy.utils.MqttUtils;
import pl.baczkowicz.mqttspy.utils.TimeUtils;

/**
 * Controller for creating new publications.
 */
@SuppressWarnings("deprecation")
public class NewPublicationController implements Initializable, ScriptListChangeObserver, TitledPaneController
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(NewPublicationController.class);

	/** How many recent messages to store. */
	private final static int MAX_RECENT_MESSAGES = 10;

	private MenuButton settingsButton;
	
	@FXML
	private SplitMenuButton publishButton;
	
	@FXML
	private ToggleGroup publishScript;

	@FXML
	private ComboBox<String> publicationTopicText;

	@FXML
	private ChoiceBox<String> publicationQosChoice;

	@FXML
	private StyleClassedTextArea publicationData;
		
	@FXML
	private ToggleGroup formatGroup;
	
	@FXML
	private CheckBox retainedBox;
	
	@FXML
	private Label retainedLabel;
	
	@FXML
	private Label dataLabel;
	
	@FXML
	private Label publicationQosLabel;
	
	@FXML
	private Label lengthLabel;
	
	@FXML
	private MenuButton formatMenu;
	
	@FXML
	private Menu publishWithScriptsMenu;
	
	@FXML
	private Menu recentMessagesMenu;
	
	@FXML
	private Menu saveRecentMessagesMenu;
		
	private ObservableList<String> publicationTopics = FXCollections.observableArrayList();

	private MqttAsyncConnection connection;

	private ConversionMethod formatSelected = ConversionMethod.PLAIN;

	private boolean connected;

	private boolean detailedView;
	
	private InteractiveScriptManager scriptManager;

	private EventManager eventManager;
	
	private List<BaseMqttMessage> recentMessages = new ArrayList<>();

	private TimeBasedKeyEventFilter timeBasedFilter;

	private TitledPane pane;

	private AnchorPane paneTitle;

	protected ConnectionController connectionController;
	
	public void initialize(URL location, ResourceBundle resources)
	{
		timeBasedFilter = new TimeBasedKeyEventFilter(15);
		
		publicationTopicText.setItems(publicationTopics);
		formatGroup.getToggles().get(0).setUserData(ConversionMethod.PLAIN);
		formatGroup.getToggles().get(1).setUserData(ConversionMethod.HEX_DECODE);
		formatGroup.getToggles().get(2).setUserData(ConversionMethod.BASE_64_DECODE);
		formatGroup.selectToggle(formatGroup.getToggles().get(0));
		
		formatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue)
			{
				// If plain has been selected
				if (newValue != null)
				{
					switch ((ConversionMethod) formatGroup.getSelectedToggle().getUserData())
					{
						case BASE_64_DECODE:
							showAsBase64();
							break;
						case HEX_DECODE:
							showAsHex();
							break;
						case PLAIN:
							showAsPlain();
							break;
						default:
							break;					
					}					
				}
			}
		});
		
		publicationTopicText.addEventFilter(KeyEvent.KEY_PRESSED, 
				new EventHandler<KeyEvent>() 
		{
	        @Override
	        public void handle(KeyEvent keyEvent) 
	        {
	        	switch (keyEvent.getCode())
	        	{
		        	case ENTER:
		        	{
		        		if (connected && timeBasedFilter.processEvent(keyEvent))
		        		{
		        			publish();
		        			keyEvent.consume();
		        		}
		        		break;
		        	}		      
		        	case DIGIT0:
		        	{
		        		restoreFromKeypress(keyEvent, 0);
		        		break;
		        	}
		        	case DIGIT1:
		        	{
		        		restoreFromKeypress(keyEvent, 1);
		        		break;
		        	}
		        	case DIGIT2:
		        	{
		        		restoreFromKeypress(keyEvent, 2);
		        		break;
		        	}
		        	case DIGIT3:
		        	{
		        		restoreFromKeypress(keyEvent, 3);
		        		break;
		        	}
		        	case DIGIT4:
		        	{
		        		restoreFromKeypress(keyEvent, 4);
		        		break;
		        	}
		        	case DIGIT5:
		        	{
		        		restoreFromKeypress(keyEvent, 5);
		        		break;
		        	}
		        	case DIGIT6:
		        	{
		        		restoreFromKeypress(keyEvent, 6);
		        		break;
		        	}
		        	case DIGIT7:
		        	{
		        		restoreFromKeypress(keyEvent, 7);
		        		break;
		        	}
		        	case DIGIT8:
		        	{
		        		restoreFromKeypress(keyEvent, 8);
		        		break;
		        	}
		        	case DIGIT9:
		        	{
		        		restoreFromKeypress(keyEvent, 9);
		        		break;
		        	}
		        	default:
		        		break;
	        	}
	        }
	    });
			
		publicationData.setWrapText(true);
		publicationData.setOnKeyReleased(new EventHandler<Event>()
		{
			@Override
			public void handle(Event event)
			{					
				final BaseMqttMessage values = readMessage(false, true);
				
				String payload = "";
				if (values != null)
				{
					payload = values.getPayload();
				}
				
				MessageController.populatePayloadLength(lengthLabel, null, payload.length());
				
				lengthLabel.getStyleClass().removeAll("newLinesPresent", "noNewLines");
				if (payload.contains(ConversionUtils.LINE_SEPARATOR_LINUX) 
						|| payload.contains(ConversionUtils.LINE_SEPARATOR_WIN) 
						|| payload.contains(ConversionUtils.LINE_SEPARATOR_MAC))
				{					
					lengthLabel.getStyleClass().add("newLinesPresent");
				}
				else
				{
					lengthLabel.getStyleClass().add("noNewLines");
				}
			}
		});
		
		publishScript.getToggles().get(0).setUserData(null);		
		publishScript.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue)
			{
				if (newValue.getUserData() == null)
				{
					publishButton.setText("Publish");					
				}
				else
				{
					publishButton.setText("Publish" + System.lineSeparator() + "with" + System.lineSeparator() + "script");
				}
			}			
		});
	}		
	
	public static ChangeListener<Number> createPaneTitleWidthListener(final TitledPane pane, final AnchorPane paneTitle)
	{
		return new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{				 										
				Platform.runLater(new Runnable()
				{										
					@Override
					public void run()
					{
						SubscriptionController.updateTitleWidth(pane, paneTitle, 40);
					}
				});
			}
		};
	}
	
	public static MenuButton createTitleButton(final String title, final String iconLocation, final double offset, 
			final ConnectionController connectionController, final TitledPane pane)
	{
		final MenuButton button = new MenuButton();
		button.setId("pane-settings-button");
		button.setTooltip(new Tooltip(title));
		// button.setPadding(new Insets(0, 0, 0, 0));
		button.setPadding(Insets.EMPTY);
		button.setLineSpacing(0);
		button.setBorder(null);
		button.setGraphicTextGap(0);
		button.setFocusTraversable(false);
		// button.setMaxHeight(16);
		
		button.setGraphic(UiUtils.createImage(iconLocation, 14));
		
		// TODO: actions
		final MenuItem detach = new MenuItem("Detach to a separate window", UiUtils.createImage("images/tab-detach.png", 14, "pane-settings-menu-graphic"));
		detach.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{				
				connectionController.setPaneVisiblity(
						connectionController.getPaneToStatusMapping().get(pane), 
						PaneVisibilityStatus.DETACHED);				
			}
		});
		final MenuItem hide = new MenuItem("Hide this pane", UiUtils.createImage("images/tab-close.png", 14, "pane-settings-menu-graphic"));
		hide.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{				
				connectionController.setPaneVisiblity(
						connectionController.getPaneToStatusMapping().get(pane), 
						PaneVisibilityStatus.NOT_VISIBLE);				
			}
		});
		button.getItems().add(detach);
		button.getItems().add(hide);
		
		for (MenuItem item : button.getItems())
		{
			item.getStyleClass().add("pane-settings-menu-item");
		}
		
		button.setTextAlignment(TextAlignment.RIGHT);
		button.setAlignment(Pos.CENTER_RIGHT);
		AnchorPane.setRightAnchor(button, offset);
		
		return button;
	}
	
	public static MenuButton createTitleButtons(final TitledPane pane, final AnchorPane paneTitle, final ConnectionController connectionController)	
	{
		final MenuButton settingsButton = createTitleButton("Pane settings", "images/settings.png", -5, connectionController, pane);
			      
		HBox titleBox = new HBox();
		titleBox.setPadding(new Insets(0, 0, 0, 0));	
		logger.info(pane + ", " + paneTitle + ", " + connectionController);
		titleBox.getChildren().addAll(new Label(pane.getText()));
		titleBox.prefWidth(Double.MAX_VALUE);		
		
		paneTitle.setPadding(new Insets(0, 0, 0, 0));
		paneTitle.getChildren().addAll(titleBox, settingsButton);
		paneTitle.setMaxWidth(Double.MAX_VALUE);
		
		pane.setText(null);
		pane.setGraphic(paneTitle);
		pane.widthProperty().addListener(createPaneTitleWidthListener(pane, paneTitle));
		
		return settingsButton;
	}

	public void init()
	{
		eventManager.registerScriptListChangeObserver(this, connection);		
		
		paneTitle = new AnchorPane();
		settingsButton = createTitleButtons(pane, paneTitle, connectionController);
	}

	@Override
	public void onScriptListChange()
	{
		final List<PublicationScriptProperties> scripts = scriptManager.getObservableScriptList();
		
		final List<Script> pubScripts = new ArrayList<>();
		
		for (final PublicationScriptProperties properties : scripts)
		{
			if (ScriptTypeEnum.PUBLICATION.equals(properties.typeProperty().getValue()))
			{
				pubScripts.add(properties.getScript());
			}
		}
		
		updateScriptList(pubScripts, publishWithScriptsMenu, publishScript, "Publish with '%s' script", null);
	}
	
	public static void updateScriptList(final List<Script> scripts, final Menu scriptsMenu, final ToggleGroup toggleGroup, 
			final String format, final EventHandler<ActionEvent> eventHandler)
	{
		while (scriptsMenu.getItems().size() > 0)
		{
			scriptsMenu.getItems().remove(0);
		}
		
		if (scripts.size() > 0)
		{
			for (final Script script : scripts)
			{
				final RadioMenuItem item = new RadioMenuItem(String.format(format, script.getName()));
				item.setOnAction(eventHandler);
				item.setToggleGroup(toggleGroup);
				item.setUserData(script);
				
				scriptsMenu.getItems().add(item);
			}
		}
	}
	
	public void setConnectionController(final ConnectionController connectionController)
	{
		this.connectionController = connectionController;
	}

	public void recordPublicationTopic(final String publicationTopic)
	{
		MqttUtils.recordTopic(publicationTopic, publicationTopics);
	}
	
	public void setConnected(final boolean connected)
	{
		this.connected = connected;
		this.publishButton.setDisable(!connected);
		this.publicationTopicText.setDisable(!connected);
	}
	
	private void decodeToPlain()
	{
		if (formatSelected.equals(ConversionMethod.HEX_DECODE))
		{
			try
			{
				final String convertedText = ConversionUtils.hexToString(publicationData.getText());
				logger.info("Converted {} to {}", publicationData.getText(), convertedText);
				
				publicationData.clear();				
				publicationData.appendText(convertedText);
				
				formatMenu.setText("Input format: Plain");
			}
			catch (ConversionException e)
			{
				showAndLogHexError();
				
				formatGroup.selectToggle(formatGroup.getToggles().get(1));
				formatMenu.setText("Input format: Hex");
			}
		}
		else if (formatSelected.equals(ConversionMethod.BASE_64_DECODE))
		{
			final String convertedText = ConversionUtils.base64ToString(publicationData.getText());
			logger.info("Converted {} to {}", publicationData.getText(), convertedText);
			
			publicationData.clear();				
			publicationData.appendText(convertedText);
			
			formatMenu.setText("Input format: Plain");
		}
	}
	
	@FXML
	public void showAsPlain()
	{
		if (!ConversionMethod.PLAIN.equals(formatSelected))
		{
			decodeToPlain();
			formatSelected = ConversionMethod.PLAIN;
		}		
	}
	
	@FXML
	public void showAsHex()
	{		
		if (!ConversionMethod.HEX_DECODE.equals(formatSelected))
		{
			final BaseMqttMessage message = readMessage(false, false);
			
			// Use the raw format to ensure correct transformation between binary formats
			final String convertedText = ConversionUtils.arrayToHex(message.getRawMessage().getPayload());
			logger.info("Converted {} to {}", publicationData.getText(), convertedText);
			
			publicationData.clear();
			publicationData.appendText(convertedText);
			
			formatMenu.setText("Input format: Hex");
			formatSelected = ConversionMethod.HEX_DECODE;
		}
	}
	
	@FXML
	public void showAsBase64()
	{		
		if (!ConversionMethod.BASE_64_DECODE.equals(formatSelected))
		{
			final BaseMqttMessage message = readMessage(false, false);
			
			// Use the raw format to ensure correct transformation between binary formats
			final String convertedText = ConversionUtils.arrayToBase64(message.getRawMessage().getPayload());
			logger.info("Converted {} to {}", publicationData.getText(), convertedText);
			
			publicationData.clear();
			publicationData.appendText(convertedText);
			
			formatMenu.setText("Input format: Base64");
			formatSelected = ConversionMethod.BASE_64_DECODE;
		}
	}
	
	private void updateVisibility()
	{
		if (detailedView)
		{
			AnchorPane.setRightAnchor(publicationTopicText, 327.0);
			AnchorPane.setRightAnchor(publicationData, 326.0);
			AnchorPane.setTopAnchor(dataLabel, 31.0);
		}
		else
		{
			AnchorPane.setRightAnchor(publicationTopicText, 128.0);
			AnchorPane.setRightAnchor(publicationData, 127.0);
			AnchorPane.setTopAnchor(dataLabel, 37.0);
		}
		
		formatMenu.setVisible(detailedView);
		publicationQosChoice.setVisible(detailedView);
		publicationQosLabel.setVisible(detailedView);
		retainedBox.setVisible(detailedView);
		retainedLabel.setVisible(detailedView);
		lengthLabel.setVisible(detailedView);
	}
	
	public void setDetailedViewVisibility(final boolean visible)
	{
		detailedView = visible;
		updateVisibility();
	}
	
	public void toggleDetailedViewVisibility()
	{
		detailedView = !detailedView;
		updateVisibility();
	}
	
	/**
	 * Displays the given message.
	 * 
	 * @param message The message to display
	 */
	private void displayMessage(final BaseMqttMessage message)
	{
		displayMessage(new SimpleMqttMessage(message.getPayload(), message.getTopic(), message.getQoS(), message.isRetained()));	
	}
	
	/**
	 * Displays the given message.
	 * 
	 * @param message The message to display
	 */
	public void displayMessage(final SimpleMqttMessage message)
	{
		if (message == null)
		{
			publicationTopicText.setValue("");
			publicationTopicText.setPromptText("(cannot be empty)");
			publicationQosChoice.getSelectionModel().select(0);
			publicationData.clear();
			retainedBox.setSelected(false);
		}
		else
		{
			publicationTopicText.setValue(message.getTopic());
			publicationQosChoice.getSelectionModel().select(message.getQos());
			publicationData.clear();
			publicationData.appendText(message.getValue());
			retainedBox.setSelected(message.isRetained());
		}
	}
	
	public BaseMqttMessage readMessage(final boolean verify, final boolean ignoreErrors)
	{
		// Note: here using the editor, as the value stored directly in the ComboBox might
		// not be committed yet, whereas the editor (TextField) has got the current text in it
		final String topic = publicationTopicText.getEditor().getText();
		
		if (verify && (topic == null || topic.isEmpty()))
		{
			logger.error("Cannot publish to an empty topic");
			
			DialogUtils.showError("Invalid topic", "Cannot publish to an empty topic.");
			return null;
		}
		
		final BaseMqttMessage message = new BaseMqttMessage(0, topic, new MqttMessage());
		try
		{
			if (formatSelected.equals(ConversionMethod.PLAIN))
			{
				final byte[] data = ConversionUtils.stringToArray(publicationData.getText());
				message.getRawMessage().setPayload(data);
			}
			else if (formatSelected.equals(ConversionMethod.HEX_DECODE))
			{
				final byte[] data = ConversionUtils.hexToArray(publicationData.getText());
				message.getRawMessage().setPayload(data);
			}								
			else if (formatSelected.equals(ConversionMethod.BASE_64_DECODE))
			{
				final byte[] data = ConversionUtils.base64ToArray(publicationData.getText());
				message.getRawMessage().setPayload(data);
			}
			
			message.getRawMessage().setQos(publicationQosChoice.getSelectionModel().getSelectedIndex());
			message.getRawMessage().setRetained(retainedBox.isSelected());
			
			return message;
		}
		catch (ConversionException e)
		{
			if (!ignoreErrors)
			{
				showAndLogHexError();
			}
			return null;
		}		
	}
	
	@FXML
	public void publish()
	{						
		final BaseMqttMessage message = readMessage(true, false);
		
		if (message != null)
		{
			recordMessage(message);
			
			final Script script = (Script) publishScript.getSelectedToggle().getUserData();
					
			if (script == null)
			{			
				logger.debug("Publishing with no script");
				// This requires a proper byte[] to be passed, to be sure the encoding/format is not broken
				connection.publish(message.getTopic(), message.getRawMessage().getPayload(), message.getQoS(), message.isRetained());
			
				recordPublicationTopic(message.getTopic());
			}
			else
			{
				logger.debug("Publishing with '{}' script", script.getName());
				
				// Publish with script
				scriptManager.runScriptFileWithMessage(script, message);
			}
		}
	}
	
	/**
	 * Records the given message on the list of 'recent' messages.
	 * 
	 * @param message The message to record
	 */
	private void recordMessage(final BaseMqttMessage message)
	{
		// If the message is the same as previous one, remove the old one
		if (recentMessages.size() > 0 
				&& message.getTopic().equals(recentMessages.get(0).getTopic()) 
				&& message.getPayload().equals(recentMessages.get(0).getPayload()))
		{
			recentMessages.remove(0);
		}
		
		recentMessages.add(0, message);
		
		while (recentMessages.size() > MAX_RECENT_MESSAGES)
		{
			recentMessages.remove(MAX_RECENT_MESSAGES);
		}
		
		refreshRecentMessages();
	}
	
	/**
	 * Refreshes the list of recent messages shown in the publish button's context menu.
	 */
	private void refreshRecentMessages()
	{
		// Remove all elements
		while (recentMessagesMenu.getItems().size() > 0)
		{
			recentMessagesMenu.getItems().remove(0);
		}
		while (saveRecentMessagesMenu.getItems().size() > 0)
		{
			saveRecentMessagesMenu.getItems().remove(0);
		}
		
		// Add all elements
		for (final BaseMqttMessage message : recentMessages)
		{
			final String topic = message.getTopic();
			final String payload = message.getPayload().length() > 10 ? message.getPayload().substring(0, 10) + "..." : message.getPayload();
			final String time = TimeUtils.DATE_WITH_SECONDS_SDF.format(message.getDate());

			final String messageText = "Topic = '" + topic + "', payload = '" + payload + "', published at " + time;
			final MenuItem recentMessageItem = new MenuItem(messageText);
			recentMessageItem.setOnAction(new EventHandler<ActionEvent>()
			{	
				@Override
				public void handle(ActionEvent event)
				{
					displayMessage(message);
				}
			});
			recentMessagesMenu.getItems().add(recentMessageItem);
			
			final MenuItem saveMessageItem = new MenuItem(messageText);
			saveMessageItem.setOnAction(new EventHandler<ActionEvent>()
			{	
				@Override
				public void handle(ActionEvent event)
				{
					saveAsScript(message);
				}
			});
			saveRecentMessagesMenu.getItems().add(saveMessageItem);
		}
		
		recentMessagesMenu.setDisable(recentMessagesMenu.getItems().size() == 0);
		saveRecentMessagesMenu.setDisable(saveRecentMessagesMenu.getItems().size() == 0);
	}	
	
	@FXML
	private void saveCurrentAsScript()
	{
		final BaseMqttMessage message = readMessage(true, false);
		
		if (message != null)
		{
			saveAsScript(message);
		}
	}
	
	private void saveAsScript(final BaseMqttMessage message)
	{
		boolean valid = false;
		
		while (!valid)
		{
			final Optional<String> response = DialogUtils.askForScriptName(pane);
			
			logger.info("Script name response = " + response);
			if (response.isPresent())
			{
				final String scriptName = response.get();
				
				final String configuredDirectory = connection.getProperties().getConfiguredProperties().getPublicationScripts();
				final String directory = InteractiveScriptManager.getScriptDirectoryForConnection(configuredDirectory);
				final File scriptFile = new File(directory + scriptName + ScriptManager.SCRIPT_EXTENSION);
				
				final Script script = scriptManager.getScriptObjectFromName(
						Script.getScriptIdFromFile(scriptFile));
				
				if (script != null)
				{
					Action duplicateNameResponse = DialogUtils.showQuestion("Script name already exists", 
							"Script with name \"" + scriptName 
							+ "\" already exists in your script folder (" 
							+ directory + "). Do you want to override it?");
					if (duplicateNameResponse == Dialog.ACTION_NO)
					{
						continue;
					}
					else if (duplicateNameResponse == Dialog.ACTION_CANCEL)
					{
						break;
					}
				}
				
				createScriptFromMessage(scriptFile , configuredDirectory, message);
				break;
			}
			else
			{
				break;
			}
		}
	}
	
	private void createScriptFromMessage(final File scriptFile, 
			final String configuredDirectory, final BaseMqttMessage message)
	{
		final StringBuffer scriptText = new StringBuffer();
		scriptText.append("mqttspy.publish(\"");
		scriptText.append(message.getTopic());
		scriptText.append("\", \"");
		// TODO: any conversions needed here if this is not plain text?
		scriptText.append(message.getPayload());
		scriptText.append("\", ");
		scriptText.append(message.getQoS());
		scriptText.append(", ");
		scriptText.append(message.isRetained());
		scriptText.append(");");
		
		try
		{
			final File orig = new File(NewPublicationController.class.getResource("/samples/template-script.js").toURI());
			final String template = org.apache.commons.io.FileUtils.readFileToString(orig);
			final String script = template.replace(
					"mqttspy.publish(\"topic\", \"payload\");", 
					scriptText.toString());
			
			logger.info("Writing file to " + scriptFile.getAbsolutePath());
			FileUtils.writeToFile(scriptFile, script);
			scriptManager.addScripts(configuredDirectory, ScriptTypeEnum.PUBLICATION);
			// TODO: move this to script manager?
			eventManager.notifyScriptListChange(connection);
		}
		catch (URISyntaxException | IOException e)
		{
			logger.error("Cannot create the script file at " + scriptFile.getAbsolutePath(), e);
		} 			
	}

	/**
	 * Restores message from the key event.
	 * 
	 * @param keyEvent The generated key event
	 * @param keyNumber The key number
	 */
	private void restoreFromKeypress(final KeyEvent keyEvent, final int keyNumber)
	{
		if (keyEvent.isAltDown())
		{
			// 1 means first message (most recent); 2 is second, etc.; 0 is the 10th (the oldest)
			final int arrayIndex = (keyNumber > 0 ? keyNumber : MAX_RECENT_MESSAGES) - 1;
			
			if (arrayIndex < recentMessages.size())
			{
				displayMessage(recentMessages.get(arrayIndex));
			}
        	keyEvent.consume();
		}
	}
	
	private void showAndLogHexError()
	{
		logger.error("Cannot convert " + publicationData.getText() + " to plain text");
		
		DialogUtils.showError("Invalid hex format", "Provided text is not a valid hex string.");
	}

	public void setConnection(MqttAsyncConnection connection)
	{
		this.connection = connection;
	}

	public void clearTopics()
	{
		publicationTopics.clear();		
	}	

	public ComboBox<String> getPublicationTopicText()
	{
		return publicationTopicText;
	}

	public ChoiceBox<String> getPublicationQosChoice()
	{
		return publicationQosChoice;
	}

	public StyleClassedTextArea getPublicationData()
	{
		return publicationData;
	}

	public CheckBox getRetainedBox()
	{
		return retainedBox;
	}
	
	public void setScriptManager(final InteractiveScriptManager scriptManager)
	{
		this.scriptManager = scriptManager;
	}
	
	public void setEventManager(final EventManager eventManager)
	{
		this.eventManager = eventManager;
	}

	public void hidePublishButton()
	{
		this.publishButton.setVisible(false);	
	}

	@Override
	public TitledPane getTitledPane()
	{
		return pane;
	}

	@Override
	public void setTitledPane(TitledPane pane)
	{
		this.pane = pane;
	}

	@Override
	public void updatePane(PaneVisibilityStatus status)
	{
		if (PaneVisibilityStatus.ATTACHED.equals(status))
		{
			settingsButton.setVisible(true);
		}		
		else
		{
			settingsButton.setVisible(false);
		}
	}
}
