package com.ExamPort.ExamPort.Repository;

import com.ExamPort.ExamPort.Entity.Course;
import com.ExamPort.ExamPort.Entity.CourseVisibility;
import com.ExamPort.ExamPort.Entity.CoursePricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    /**
     * Find courses by instructor ID
     * @param instructorId The instructor's user ID
     * @return List of courses for the instructor
     */
    List<Course> findByInstructor_Id(Long instructorId);
    
    /**
     * Find courses by instructor ID (alternative method name)
     * @param instructorId The instructor's user ID
     * @return List of courses for the instructor
     */
    List<Course> findByInstructorId(Long instructorId);
    
    /**
     * Find course by name
     * @param name The course name
     * @return Course with the specified name
     */
    Course findByName(String name);
    
    /**
     * Find courses by visibility
     * @param visibility The course visibility (PRIVATE or PUBLIC)
     * @return List of courses with the specified visibility
     */
    List<Course> findByVisibility(CourseVisibility visibility);
    
    /**
     * Find courses by visibility and pricing
     * @param visibility The course visibility
     * @param pricing The course pricing
     * @return List of courses matching the criteria
     */
    List<Course> findByVisibilityAndPricing(CourseVisibility visibility, CoursePricing pricing);
    
    /**
     * Find public free courses
     * @return List of public free courses
     */
    @Query("SELECT c FROM Course c WHERE c.visibility = 'PUBLIC' AND c.pricing = 'FREE'")
    List<Course> findPublicFreeCourses();
    
    /**
     * Find public paid courses
     * @return List of public paid courses
     */
    @Query("SELECT c FROM Course c WHERE c.visibility = 'PUBLIC' AND c.pricing = 'PAID'")
    List<Course> findPublicPaidCourses();
    
    /**
     * Find courses by instructor and visibility
     * @param instructorId The instructor's user ID
     * @param visibility The course visibility
     * @return List of courses for the instructor with specified visibility
     */
    List<Course> findByInstructor_IdAndVisibility(Long instructorId, CourseVisibility visibility);
    
    /**
     * Count courses by instructor
     * @param instructorId The instructor's user ID
     * @return Number of courses for the instructor
     */
    @Query("SELECT COUNT(c) FROM Course c WHERE c.instructor.id = :instructorId")
    Long countByInstructorId(@Param("instructorId") Long instructorId);
    
    /**
     * Count public courses
     * @return Number of public courses
     */
    @Query("SELECT COUNT(c) FROM Course c WHERE c.visibility = 'PUBLIC'")
    Long countPublicCourses();
    
    /**
     * Check if course exists by name and instructor ID
     * @param name The course name
     * @param instructorId The instructor's user ID
     * @return true if course exists, false otherwise
     */
    boolean existsByNameAndInstructorId(String name, Long instructorId);
    
    /**
     * Find courses by instructor username
     * @param username The instructor's username
     * @return List of courses for the instructor
     */
    List<Course> findByInstructorUsername(String username);
}
