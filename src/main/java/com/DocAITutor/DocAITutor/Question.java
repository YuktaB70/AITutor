package com.DocAITutor.DocAITutor;

import java.util.List;

import org.apache.pdfbox.pdmodel.PDPage;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class Question {

	
	private String question;
	private List<String> options;
	private String answer; 
	
	

	
}
