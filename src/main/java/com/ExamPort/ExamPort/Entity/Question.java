package com.ExamPort.ExamPort.Entity;

import java.util.List;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

@Entity
public class Question {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long Que_id;
	
	
	@Column(nullable = false)
	private String question;
	
	
	@Column(nullable=false)
@OneToMany(cascade = CascadeType.ALL)
@JoinColumn(name="QId")
private List<ExamOption> options;

	@Column(nullable = false)
	private String type; // "mcq" or "multiple"
	
	@Column
	private Integer marks = 1; // Default marks per question

	// For MCQ: only one element; for Multiple: can be many
	@jakarta.persistence.ElementCollection
	@jakarta.persistence.CollectionTable(
		name = "question_correct_options",
		joinColumns = @jakarta.persistence.JoinColumn(name = "question_que_id")
	)
	@jakarta.persistence.Column(name = "correct_option_index")
	private List<Integer> correct_options;

	public Question() {
		// Default constructor
	}

	public Question(long que_id, String question, List<ExamOption> options, String type, List<Integer> correct_options) {
		super();
		Que_id = que_id;
		this.question = question;
		this.options = options;
		this.type = type;
		this.correct_options = correct_options;
		this.marks = 1; // Default marks
	}

	public long getQue_id() {
		return Que_id;
	}

	public void setQue_id(long que_id) {
		Que_id = que_id;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public List<ExamOption> getOptions() {
		return options;
	}

	public void setOptions(List<ExamOption> options) {
		this.options = options;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Integer> getCorrect_options() {
		return correct_options;
	}

	public void setCorrect_options(List<Integer> correct_options) {
		this.correct_options = correct_options;
	}
	
	public Integer getMarks() {
		return marks;
	}

	public void setMarks(Integer marks) {
		this.marks = marks;
	}

	@Override
	public String toString() {
		return "Question [Que_id=" + Que_id + ", question=" + question + ", type=" + type + ", marks=" + marks + ", correct_options=" + correct_options + "]";
	}
}
