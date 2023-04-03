package com.cst438;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;

@SpringBootTest
public class EndToEndAssignment {
	
	public static final String CHROME_DRIVER_FILE_LOCATION = "C:/chromedriver_win32/chromedriver.exe";

	public static final String URL = "http://localhost:3000";
	public static final String TEST_USER_EMAIL = "test@csumb.edu";
	public static final String TEST_INSTRUCTOR_EMAIL = "dwisneski@csumb.edu";
	public static final int SLEEP_DURATION = 1000; // 1 second.
	public static final String TEST_ASSIGNMENT_NAME = "Test Assignment";
	public static final String TEST_COURSE_TITLE = "Test Course";
	public static final String TEST_STUDENT_NAME = "Test";
	public static final String TEST_DUE_DATE = "2021-09-21";
	
	@Autowired
	EnrollmentRepository enrollmentRepository;

	@Autowired
	CourseRepository courseRepository;

	@Autowired
	AssignmentGradeRepository assignnmentGradeRepository;

	@Autowired
	AssignmentRepository assignmentRepository;

	@Test
	public void createAssignment() throws Exception {
		
//		Database setup:  create course		
		Course c = new Course();
		c.setCourse_id(99999);
		c.setInstructor(TEST_INSTRUCTOR_EMAIL);
		c.setSemester("Fall");
		c.setYear(2021);
		c.setTitle(TEST_COURSE_TITLE);

//	    add an assignment that needs grading for course 99999
		Assignment a = new Assignment();
		a.setCourse(c);
		// set assignment due date to 24 hours ago
		a.setDueDate(new java.sql.Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
		a.setName("dummy assignment");
		a.setNeedsGrading(1);

//	    add a student TEST into course 99999
		Enrollment e = new Enrollment();
		e.setCourse(c);
		e.setStudentEmail(TEST_USER_EMAIL);
		e.setStudentName(TEST_STUDENT_NAME);

		courseRepository.save(c);
		a = assignmentRepository.save(a);
		e = enrollmentRepository.save(e);
		
		Assignment as = null;
		
		System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

		driver.get(URL);
		Thread.sleep(SLEEP_DURATION);
		
		try {
			//Create a course
			
			WebElement we;
			//1). At the home page, click on "Create Assignment". Use "click()"
			driver.findElement(By.xpath("//a[contains(.,'New Assignment')]")).click();
			Thread.sleep(SLEEP_DURATION);
			//2). Find elements for Assignment Name, Due Date, and Course ID. Use "findElement()"
			//3). Use "sendkeys()"
			we = driver.findElement(By.name("assignmentName"));
			we.sendKeys(TEST_ASSIGNMENT_NAME);
			
			we = driver.findElement(By.name("dueDate"));
			we.sendKeys(TEST_DUE_DATE);
			
			we = driver.findElement(By.name("courseId"));
			we.clear();
			we.sendKeys(Integer.toString(99999));
			//4). Submit. 
			driver.findElement(By.xpath("//button[@id='Submit']")).click();
			Thread.sleep(SLEEP_DURATION);
			//5). Check that all 3 text fields have the entry still inside
			assertEquals(TEST_ASSIGNMENT_NAME, driver.findElement(By.name("assignmentName")).getAttribute("value"),"Assignment Name is not correct.");
			assertEquals(TEST_DUE_DATE, driver.findElement(By.name("dueDate")).getAttribute("value"),"Assignment Due Date is not correct.");
			assertEquals("99999", driver.findElement(By.name("courseId")).getAttribute("value"),"Assignment Course ID is not correct.");
			//6). "assertTrue" that the new assignment was found in the database
			as = assignmentRepository.findByName(driver.findElement(By.name("assignmentName")).getAttribute("value"));
			assertEquals(TEST_ASSIGNMENT_NAME, as.getName(), "Not found in the database.");
			
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		} finally {
			as = assignmentRepository.findByName(TEST_ASSIGNMENT_NAME);
			if (as!=null) assignmentRepository.delete(as);
			enrollmentRepository.delete(e);
			assignmentRepository.delete(a);
			courseRepository.delete(c);
			driver.quit();
		}
	}
}
