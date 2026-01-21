package com.DocAITutor.DocAITutor;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.ByteArrayOutputStream;
import java.nio.file.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.DocAITutor.DocAITutor.PDFService.PDFService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class DocAiTutorApplicationTests {


	@Autowired
    private MockMvc mockMvc;
	
	
	/**
	 * 
	 * Testing file upload apis 
	 */
	
	//Testing file upload api
	@Test
	public void Test01() throws Exception {
        ClassPathResource resource = new ClassPathResource("Test.pdf");
        byte[] pdfBytes = resource.getInputStream().readAllBytes();


	    MockMultipartFile mockfile = new MockMultipartFile(
	        "file",
	        "test.pdf",
	        "application/pdf",
	        pdfBytes
	    );

	    mockMvc.perform(multipart("/pdf/uploadpdf").file(mockfile))
	            .andExpect(status().isOk())
	            .andExpect(content().string(containsString("File uploaded successfully")));

	}
	
	
	//Testing file upload api, assert id exists
	@Test
	public void Test02() throws Exception {
        ClassPathResource resource = new ClassPathResource("Test.pdf");
        byte[] pdfBytes = resource.getInputStream().readAllBytes();
        ObjectMapper mapper = new ObjectMapper();


	    MockMultipartFile mockfile = new MockMultipartFile(
	        "file",
	        "test.pdf",
	        "application/pdf",
	        pdfBytes
	    );

	    mockMvc.perform(multipart("/pdf/uploadpdf").file(mockfile))
	    .andExpect(status().isOk())
	    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
	    .andExpect(jsonPath("$.message").value("File uploaded successfully"))
	    .andExpect(jsonPath("$.fileId").exists())
	    .andExpect(jsonPath("$.fileId").isNotEmpty());

	    

	}
	
	
	//Testing retrieving file from api endpoint and checking if pdf is returned 
	@Test
	public void Test03() throws Exception {
        ClassPathResource resource = new ClassPathResource("Test.pdf");
        byte[] pdfBytes = resource.getInputStream().readAllBytes();
        ObjectMapper mapper = new ObjectMapper();


	    MockMultipartFile mockfile = new MockMultipartFile(
	        "file",
	        "test.pdf",
	        "application/pdf",
	        pdfBytes
	    );

	    MvcResult uploadResults = mockMvc.perform(multipart("/pdf/uploadpdf").file(mockfile))
	    .andExpect(status().isOk())
	    .andReturn();
	    
	    String responseJson = uploadResults.getResponse().getContentAsString();
	    String fileId = mapper.readTree(responseJson).get("fileId").asText();
	    
	    MvcResult response = mockMvc.perform(get("/pdf/{id}", fileId))
	    .andExpect(status().isOk())
	    .andReturn();
	    
	    String base64 = response.getResponse().getContentAsString();
	    assertNotNull(base64);
	    assertFalse(base64.isBlank());
	    
	    byte[] pdf = Base64.getDecoder().decode(base64);
	    PDDocument doc = Loader.loadPDF(pdf);				

	    assertTrue(doc.getNumberOfPages() == 1);
	    
	    doc.close();
	}
	
	
	//Testing false id 
	@Test
	public void Test04() throws Exception {
        ClassPathResource resource = new ClassPathResource("Test.pdf");
        byte[] pdfBytes = resource.getInputStream().readAllBytes();
        ObjectMapper mapper = new ObjectMapper();


	    MockMultipartFile mockfile = new MockMultipartFile(
	        "file",
	        "test.pdf",
	        "application/pdf",
	        pdfBytes
	    );

	    MvcResult uploadResults = mockMvc.perform(multipart("/pdf/uploadpdf").file(mockfile))
	    .andExpect(status().isOk())
	    .andReturn();
	    
	    String responseJson = uploadResults.getResponse().getContentAsString();
	    String fileId = mapper.readTree(responseJson).get("fileId").asText();
	    fileId += "1";
	    
	    
	    mockMvc.perform(get("/pdf/{id}", fileId))
	    .andExpect(status().isNotFound());
	}
	
	
	@Test
	public void Test05() throws Exception {
        ClassPathResource resource = new ClassPathResource("Test.pdf");
        byte[] pdfBytes = resource.getInputStream().readAllBytes();
        ObjectMapper mapper = new ObjectMapper();


	    MockMultipartFile mockfile = new MockMultipartFile(
	        "file",
	        "test.pdf",
	        "application/pdf",
	        pdfBytes
	    );

	    MvcResult uploadResults = mockMvc.perform(multipart("/pdf/uploadpdf").file(mockfile))
	    .andExpect(status().isOk())
	    .andReturn();
	    String responseJson = uploadResults.getResponse().getContentAsString();
	    String fileId = mapper.readTree(responseJson).get("fileId").asText();
	    
	    mockMvc.perform(get("/pdf/{id}/metadata", fileId))
	    .andExpect(status().isOk())
	    .andExpect(jsonPath("$.PageNumber").value(1));

	}
	
	//Testing corrupted file
	@Test
	public void Test06() throws Exception {
        ClassPathResource resource = new ClassPathResource("Test02.pdf");
        byte[] pdfBytes = resource.getInputStream().readAllBytes();
        ObjectMapper mapper = new ObjectMapper();


	    MockMultipartFile mockfile = new MockMultipartFile(
	        "file",
	        "test.pdf",
	        "application/pdf",
	        pdfBytes
	    );

	    mockMvc.perform(multipart("/pdf/uploadpdf").file(mockfile))
	    .andExpect(status().isBadRequest())
	    .andExpect(jsonPath("$.Error").exists())
	    .andExpect(jsonPath("$.Error").value("PDF could not be set"));
	   

	}
	
	
	
	@AfterEach
	void resetState() {
	    PDFService.pdfDocTracker.clear();
	    PDFService.encodedPages.clear();
	    PDFService.pdfMeta.clear();
	    PDFService.head = null;
	    PDFService.prev = null;
	}

	
	
}
