package com.DocAITutor.DocAITutor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.DocAITutor.DocAITutor.PDFService.AiService;
import com.DocAITutor.DocAITutor.PDFService.RedisTestService;

@SpringBootApplication
public class DocAiTutorApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocAiTutorApplication.class, args);
	}

}
