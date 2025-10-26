package com.DocAITutor.DocAITutor.PDFController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.Map.Entry;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.DocAITutor.DocAITutor.Page;
import com.DocAITutor.DocAITutor.PDFService.PDFService;

@CrossOrigin(origins = {"http://localhost:5173"})
@RestController
@RequestMapping("/pdf")
public class PDFController {
	
    private final PDFService pdfService;  // instance field

    // Constructor injection
    public PDFController(PDFService pdfService) {
        this.pdfService = pdfService;
    }

	
	
	@PostMapping("/uploadpdf")
	public ResponseEntity<Map<String, String>> uploadPDF(@RequestParam("file") MultipartFile file) throws IOException {
		pdfService.pdfDocTracker = new HashMap<>();
		pdfService.encodedPages = new TreeMap<>();
		pdfService.pdfTracker = new HashMap<>();
		pdfService.pdfMeta =  new HashMap<>();
		pdfService.head = null;
		pdfService.prev = null;
//		pdfService.conversation = new ArrayList<>();
		System.out.print("PDF Received in service");

		try {
		String filename = file.getOriginalFilename() + "_" + System.currentTimeMillis();
		
		String id = pdfService.setPDF(file.getBytes());
		pdfService.addPrompt(id);
		
		Map<String, String> response = new HashMap<>();
		response.put("fileName", filename);
		response.put("fileId", id);
		response.put("message", "File uploaded successfully");
		return ResponseEntity.ok(response);
		}
		catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("Error", "Upload Failed"));
			
			
		}
	}
	
	
	@GetMapping("/{id}")
	public ResponseEntity<String> getPDF(@PathVariable String id ) throws IOException {
		try {
			String page = pdfService.returnHeadPointer(id);
			if (page == null) {
				return ResponseEntity.notFound().build();
			}
			
			return ResponseEntity.ok()
					.header("Content-Type", "application/json")
					.body(page);
			

		} catch (Exception e) {
			e.printStackTrace();
	        return ResponseEntity.internalServerError().build();

		}
		
				
				
		
	}
	@GetMapping("/{id}/metadata")
	public ResponseEntity<Integer> getMeta(@PathVariable String id ) throws IOException {
		try {
			int pageNum = pdfService.returnMeta(id);
					
			return ResponseEntity.ok()
					.header("Content-Type", "application/json")
					.body(pageNum);
			

		} catch (Exception e) {
			e.printStackTrace();
	        return ResponseEntity.internalServerError().build();

		}
		
				
				
		
	}
	
	@GetMapping("/{id}/Next")
	public ResponseEntity<String> getNextPage(@PathVariable String id) throws IOException {
		try {
			String page = pdfService.handlePageChange(id, "NEXT");
			if (page == null) {
				return ResponseEntity.notFound().build();
			}
			
			return ResponseEntity.ok()
					.header("Content-Type", "application/json")
					.body(page);
			


			
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
			
		}
	}
	
	@GetMapping("/{id}/Prev")
	public ResponseEntity<String> getPrevPage(@PathVariable String id) throws IOException {
		try {
			String page = pdfService.handlePageChange(id, "PREV");
			if (page == null) {
				return ResponseEntity.notFound().build();
			}
			
			return ResponseEntity.ok()
					.header("Content-Type", "application/json")
					.body(page);
			


			
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
			
		}
	}
	
	@PostMapping("/{id}/QAs")
	public ResponseEntity<String> startChat(@PathVariable String id, @RequestBody  String input) {
		String response = pdfService.askQuestions(id, input);
		
   
	return ResponseEntity.ok(response);



		

	}


}
