package com.cst438.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentDTO;
import com.cst438.domain.EnrollmentRepository;

@RestController
public class EnrollmentController {

	@Autowired
	CourseRepository courseRepository;

	@Autowired
	EnrollmentRepository enrollmentRepository;

	/*
	 * endpoint used by registration service to add an enrollment to an existing
	 * course.
	 */
	@PostMapping("/enrollment")
	@Transactional
	public EnrollmentDTO addEnrollment(@RequestBody EnrollmentDTO enrollmentDTO) {
		
		Enrollment er = new Enrollment();
		er.setStudentEmail(enrollmentDTO.studentEmail);
		er.setStudentName(enrollmentDTO.studentName);
		//Finds the course to relate to the enrollment
		Course c = courseRepository.findById(enrollmentDTO.course_id).orElse(null);
		if(c == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course is not found.");
		}
		er.setCourse(c);
		er = enrollmentRepository.save(er);
		enrollmentDTO.id = er.getId();
		return enrollmentDTO;
		
	}

}
