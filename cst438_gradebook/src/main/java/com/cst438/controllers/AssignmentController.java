package com.cst438.controllers;

import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.AssignmentListDTO.AssignmentDTO;
import com.cst438.services.RegistrationService;

@RestController
@CrossOrigin(origins = {"http://localhost:3000","http://localhost:3001"})
public class AssignmentController {
	
	@Autowired
	AssignmentRepository assignmentRepository;
	
	@Autowired
	AssignmentGradeRepository assignmentGradeRepository;
	
	@Autowired
	CourseRepository courseRepository;
	
	@Autowired
	RegistrationService registrationService;
	
	//Add a new assignment
	@PostMapping("/assignment/new/{courseId}")
	@Transactional
	public AssignmentDTO addAssignment(@RequestBody AssignmentDTO ad, @PathVariable("courseId") Integer course_id) {
		//Taking in assignmentName, dueDate, the id after 'new' is the course id which is used to pull that info
		//{"assignmentName": ,"dueDate":}
		String email = "dwisneski@csumb.edu";
		//Check if the assignment is valid or not
		Assignment a = newAssignmentCheck(email, ad, course_id);
		assignmentRepository.save(a);
		return ad;
	}

	//Change the name of an assignment
	@PutMapping("/assignment/edit/{assignmentId}")
	@Transactional
	public int editAssignment(@RequestBody AssignmentDTO assignment, @PathVariable("assignmentId") Integer assignment_id) {
		//Read in the changed assignment from the body
		String email = "dwisneski@csumb.edu";
		//Check if the assignment is valid or not
		Assignment a = checkAssignment(assignment_id,email);
		
		//Displays the body data read in from the PUT request
		System.out.printf("%s\n", assignment.toString());
		a.setName(assignment.assignmentName);
		System.out.printf("%s\n", a.toString());
		
		assignmentRepository.save(a);
		
		return a.getId();
	}
	
	//Delete an assignment
	@DeleteMapping("/assignment/delete/{assignmentId}")
	@Transactional
	public void deleteAssignment(@PathVariable("assignmentId") Integer assignment_id){
		
		String email = "dwisneski@csumb.edu";
		Assignment assignment = checkAssignment(assignment_id, email);
		System.out.println(assignment_id);
		
		//verify that assignment has not been graded
		if(assignment.getNeedsGrading() == 1) {
			//delete the foreign keys first
			assignmentGradeRepository.deleteByAssignmentId(assignment_id);
			//Then the assignment can be deleted.
			assignmentRepository.deleteById(assignment_id);
		} else {
			throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Graded Assignments cannot be deleted.");
		}
	}
	
	private Assignment newAssignmentCheck(String email, AssignmentDTO ad, Integer course_id) {
		//Check to make sure the user is authorized.
		Course course = courseRepository.findById(course_id).orElse(null);
		if(!course.getInstructor().equals(email)){
			throw new ResponseStatusException( HttpStatus.UNAUTHORIZED, "Not Authorized. " );
		//Checks the data to make sure there are no errors
		}else if(ad.assignmentName == null || ad.dueDate == null) {
			throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Assignment name and/or Assignment due date is invalid.");
		} 
		Assignment newAssignment = new Assignment();
		newAssignment.setName(ad.assignmentName);
		newAssignment.setDueDate(Date.valueOf(ad.dueDate));
		newAssignment.setCourse(course);
		newAssignment.setNeedsGrading(1);
		
		return newAssignment;
	}
	
	private Assignment checkAssignment(int assignmentId, String email) {
		// get assignment 
		Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
		if (assignment == null) {
			throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Assignment not found. "+assignmentId );
		}
		// check that user is the course instructor
		if (!assignment.getCourse().getInstructor().equals(email)) {
			throw new ResponseStatusException( HttpStatus.UNAUTHORIZED, "Not Authorized. " );
		}
		
		return assignment;
	}
	
	
}
