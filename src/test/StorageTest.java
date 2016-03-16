
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.xml.sax.SAXException;

import defaultPart.Storage;
import defaultPart.Task;

public class StorageTest {

	/* Date format used to save/load from XML */
	public static SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy HH:mm:ss");

	/**
	 * Function to create a sample task for testing" 
	 * @param type
	 *            1-Floating 2-Deadline 3-Event
	 * @return new task Created
	 * @throws ParseException
	 */
	public static Task instantiateTestTask(int type) throws ParseException {

		
		Calendar calDate = new GregorianCalendar();
		Calendar calStart = new GregorianCalendar();
		Calendar calEnd = new GregorianCalendar();
		calDate.setTime(formatter.parse("20-6-2016 00:00:00"));
		calDate.setTime(formatter.parse("20-6-2016 10:00:00"));
		calDate.setTime(formatter.parse("20-6-2016 12:00:00"));

		Task newTask = new Task();
		switch(type)
		{
			case 1:
				newTask.setDescription("Floating Test case");
				break;
			case 2:
				newTask.setDescription("Deadline Test case");
				newTask.setDate(calDate);
				newTask.setEndTime(calEnd);
				break;
			case 3:
				newTask.setDescription("Event Test case");
				newTask.setDate(calDate);
				newTask.setStartTime(calStart);
				newTask.setEndTime(calEnd);
				break;
		}
		return newTask;
	}

	/* Testing method to check if two tasks are equal */
	public static boolean taskEquals(Task task1, Task task2) {
		if (task1 == null || task2 == null) {
			return false;
		}
		if (!task1.getDescription().equals(task2.getDescription())) {
			return false;
		}
		if (task1.isCompleted() != task2.isCompleted()) {
			return false;
		}
		if (!task1.getDate().equals(task2.getDate()))
			return false;
		if (!task1.getRecur().equals(task2.getRecur())) {
			return false;
		}
		return true;
	}

	@Test
	public void testStorage() {
		Storage storage = new Storage();
		assert (storage != null);
	}

	@Test
	public void testGetTaskList() {

		// Setting up expected Task List for comparison
		Task newTask = new Task();
		newTask.setDescription("Test case");
		newTask.setDate(new Calendar("12-12-2001"));
		List<Task> expectedTaskList = new LinkedList<Task>();
		expectedTaskList.add(newTask);

		// Setting up test task list
		Storage storage = new Storage();
		storage.addToTaskList(newTask);
		assertEquals(expectedTaskList, storage.getTaskList());

	}

	@Test
	public void testGetTask() throws IOException {

		// Setting up expected Task for comparison
		Task expectedTask = new Task();
		expectedTask.setDescription("Test case");

		// Setting up test task
		Storage storage = new Storage();
		storage.addToTaskList(expectedTask);
		assertEquals(expectedTask, storage.getTask(0));
	}

	@Test
	public void testIsTaskIndexValid() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveTask() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetPreviousListAsCurrent() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetCurrentListAsPrevious() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddToTaskList() {
		fail("Not yet implemented");
	}

	@Test
	public void testSaveTasks() throws SAXException, IOException {
		File inputFile = new File("sampleXML.xml");
		File outputFile = new File("test.xml");
		Storage storage = new Storage();
		storage.loadTasks(inputFile);
		storage.saveTasks(outputFile);

		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
		XMLUnit.setNormalizeWhitespace(true);

		FileReader fr1 = new FileReader(inputFile);
		FileReader fr2 = new FileReader(outputFile);
		// XMLUnit.compareXML(fr1, fr2);
		XMLAssert.assertXMLEqual(fr1, fr2);

	}

	@Test
	public void testLoadTasks() throws SAXException, IOException {
		File file = new File("sampleXML.xml");
		Storage storage = new Storage();
		storage.loadTasks(file);
		List<Task> testList = storage.getTaskList();

		List<Task> expectedList = new LinkedList<Task>();
		Task expTask1 = testList.get(0);

		Task testTask1 = new Task();
		testTask1.setDescription("Find potato");
		testTask1.toggleCompleted();
		expectedList.add(testTask1);

		assertTrue(taskEquals(expTask1, testTask1));

	}

	@Test
	public void testExtractRecurrFromTask() {
		fail("Not yet implemented");
	}

}
