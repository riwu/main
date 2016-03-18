package tableUi;

import defaultPart.Logic;
import defaultPart.Storage;
import defaultPart.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class Controller implements Initializable {
	@FXML
	public TableView<TaskModel> floatingTaskTable;
	public TableView<TaskModel> eventsTable;
	public TableColumn<TaskModel, Number> floatingTaskId;
	public TableColumn<TaskModel, String> floatingTaskDescription;
	public TableColumn<TaskModel, Boolean> floatingTaskCheckbox;
	public TableColumn<TaskModel, Number> eventsId;
	public TableColumn<TaskModel, String> eventsDescription;
	public TableColumn<TaskModel, String> eventsDate;
	public TableColumn<TaskModel, String> eventsRecur;
	public TableColumn<TaskModel, Boolean> eventsCheckbox;
	public TextField inputBox;
	public Label userPrompt;
	public Button addFloatingTask;
	public Button addEvent;
	public Button deleteFloatingTask;
	public Button deleteEvent;
	public Button showAllEvents;
	public Button showIncompleteEvents;
	public Button showOverdueEvents;
	public Button showCompletedEvents;

	public static final boolean DEVELOPER_MODE = true;
	public static final String EDIT_COMMAND = "e %d %s";
	public static final String DELETE_COMMAND = "d %d";
	public static final String TOGGLE_COMMAND = "t %d";
	public static final String INVALID_DATE_PROMPT = "\"%s\" is not a valid date format, use dd/MM/yy";
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");
	public static final String INVALID_EDIT_DATE_PROMPT = "edit date action could not be done on id %d";
	public static final String INVALID_EDIT_DESCRIPTION_PROMPT = "edit date action could not be done on id %d";

	private static final Logger log = Logger.getLogger(Logic.class.getName());

	private List<Task> taskList;

	private ObservableList<TaskModel> floatingTaskList;
	private ObservableList<TaskModel> eventList;
	private ArrayList<TaskModel> taskModels;

	private int lastId;

	private Logic logic;
	private Storage storage;
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		floatingTaskList = FXCollections.observableArrayList();
		eventList = FXCollections.observableArrayList();
		taskModels = new ArrayList<>();
		lastId = 0;

		floatingTaskTable.setItems(floatingTaskList);
		eventsTable.setItems(eventList);

		floatingTaskId.setCellValueFactory(cellData -> cellData.getValue().taskId());
		eventsId.setCellValueFactory(cellData -> cellData.getValue().taskId());
		eventsRecur.setCellValueFactory(cellData -> {
			if(cellData.getValue().getIsRecur()){
				return cellData.getValue().recur();
			}else{
				return null;
			}
		});

		floatingTaskCheckbox.setCellValueFactory(cellData -> cellData.getValue().isComplete());
		floatingTaskCheckbox.setCellFactory(e -> new CheckBoxTableCell());

		eventsCheckbox.setCellValueFactory(cellData -> cellData.getValue().isComplete());
		eventsCheckbox.setCellFactory(e -> new CheckBoxTableCell());

		eventsDate.setCellValueFactory(cellData -> cellData.getValue().dateTime());
		eventsDate.setCellFactory(TextFieldTableCell.forTableColumn());
		eventsDate.setOnEditCommit(e ->{
			TaskModel taskModel = e.getTableView().getItems().get(e.getTablePosition().getRow());
			int id = taskModel.getTaskId();
			try{
				Calendar newDate = logic.getDateFromString(e.getNewValue());
				String dateString = DATE_FORMAT.format(newDate.getTime());
				sendToLogicAndUpdatePrompt(String.format(EDIT_COMMAND, id, dateString));
			}catch(Exception exception){
				// if the date format is invalid
				setUserPrompt(String.format(INVALID_DATE_PROMPT, e.getNewValue()));
				e.consume();
				eventsDate.setVisible(false);
				eventsDate.setVisible(true);
			}
		});


		floatingTaskDescription.setCellValueFactory(cellData -> cellData.getValue().taskDescription());
		floatingTaskDescription.setCellFactory(TextFieldTableCell.forTableColumn());
		floatingTaskDescription.setOnEditCommit(e -> {
			int id = e.getTableView().getItems().get(e.getTablePosition().getRow()).getTaskId();
			sendToLogicAndUpdatePrompt(String.format(EDIT_COMMAND, id, e.getNewValue()));
		});

		eventsDescription.setCellValueFactory(cellData -> cellData.getValue().taskDescription());
		eventsDescription.setCellFactory(TextFieldTableCell.forTableColumn());
		eventsDescription.setOnEditCommit(e -> {
			int id = e.getTableView().getItems().get(e.getTablePosition().getRow()).getTaskId();
			sendToLogicAndUpdatePrompt(String.format(EDIT_COMMAND, id, e.getNewValue()));
		});


		storage = new Storage();
		logic = new Logic(storage);
		inputBox.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			if(e.getCode().equals(KeyCode.ENTER)){
				sendToLogicAndUpdatePrompt(inputBox.getText());
				inputBox.clear();
			}
		});

		inputBox.requestFocus();
	}

	public void debug(){
		editEventDescriptionById(3);
	}

	public void setUserPrompt(String prompt){
		// the length of feedback should not be longer than 100 characters
		if(prompt.length() > 100){
			prompt = prompt.substring(0,97) + "...";
		}
		if(DEVELOPER_MODE)
			System.out.println("Sent back to user: " + prompt);
		userPrompt.setText(prompt);
	}

	private int getRowFromModel(TaskModel task){
		if(task.getIsEvent()){
			return eventsTable.getItems().indexOf(task);
		}else{
			return floatingTaskTable.getItems().indexOf(task);
		}
	}

	public void editEventDateById(int id){
		try {
			eventsTable.edit(getRowFromModel(getTaskModelFromId(id)), eventsDate);
		}catch (Exception e){
			setUserPrompt(String.format(INVALID_EDIT_DATE_PROMPT, id));
		}
	}

	public void editEventDescriptionById(int id){
		try {
			eventsTable.edit(getRowFromModel(getTaskModelFromId(id)), eventsDescription);
		}catch (Exception e){
			setUserPrompt(String.format(INVALID_EDIT_DESCRIPTION_PROMPT, id));
		}
	}

	public void editFloatingTaskDescriptionById(int id){
		try {
			floatingTaskTable.edit(getRowFromModel(getTaskModelFromId(id)), floatingTaskDescription);
		}catch (Exception e){
			setUserPrompt(String.format(INVALID_EDIT_DESCRIPTION_PROMPT, id));
		}
	}

	private TaskModel getTaskModelFromId(int id){
		return taskModels.get(id - 1);
	}

	public void deleteFloatingTask(){
		int selectedIndex = floatingTaskTable.getSelectionModel().getSelectedIndex();
		if (selectedIndex >= 0) {
			int id = floatingTaskTable.getItems().get(selectedIndex).getTaskId();
			sendToLogicAndUpdatePrompt(String.format(DELETE_COMMAND, id));
		} else {
			// Nothing selected
			setUserPrompt("No floating task is selected");
		}
		inputBox.requestFocus();
	}

	public void deleteEvent(){
		int selectedIndex = eventsTable.getSelectionModel().getSelectedIndex();
		if (selectedIndex >= 0) {
			int id = eventsTable.getItems().get(selectedIndex).getTaskId();
			sendToLogicAndUpdatePrompt(String.format(DELETE_COMMAND, id));
		} else {
			// Nothing selected
			setUserPrompt("No event is selected");
		}
		inputBox.requestFocus();
	}

	public void showAllTasks(){
		retrieveTaskFromStorage();
		inputBox.requestFocus();
	}

	private void retrieveTaskFromStorage(){
		lastId = 0;
		eventList.clear();
		floatingTaskList.clear();
		taskModels.clear();
		taskList = storage.getTaskList();

		for(int i = 0; i < taskList.size(); i++){
			Task task = taskList.get(i);
			TaskModel newModel = new TaskModel(task, i+1, this);
			if(task.getDate() == null){
				floatingTaskList.add(newModel);
			}else{
				eventList.add(newModel);
			}
			taskModels.add(newModel);
			lastId++;
		}
	}

	public void sendToLogicAndUpdatePrompt(String command){
		logic.executeCommand(command);
		if(DEVELOPER_MODE){
			System.out.println("Send to logic: " + command);
		}
		setUserPrompt(logic.getFeedback());
		showAllTasks();
	}

	public void showIncompleteEvents(){
		retrieveTaskFromStorage();
		eventList.removeIf(e->e.getIsComplete());
		floatingTaskList.removeIf(e->e.getIsComplete());
	}

	public void showOverdueEvents(){
		Calendar today = new GregorianCalendar();
		retrieveTaskFromStorage();
		eventList.removeIf(e -> e.getTask().getEndTime().compareTo(today) == 1);
		floatingTaskList.removeIf(e -> e.getTask().getEndTime().compareTo(today) == 1);
	}

	public void setShowCompletedEvents(){
		retrieveTaskFromStorage();
		eventList.removeIf(e->!e.getIsComplete());
		floatingTaskList.removeIf(e->!e.getIsComplete());
	}

}
