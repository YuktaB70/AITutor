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
import com.DocAITutor.DocAITutor.Question;
import com.DocAITutor.DocAITutor.PDFService.PDFService;
//http://localhost:5173/
//"https://f88c7bfe.aitutorfrontend.pages.dev/"
@CrossOrigin(origins = {"https://2bbc9f36.aitutorfrontend.pages.dev/"})
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
		PDFService.pdfDocTracker = new HashMap<>();
		PDFService.encodedPages = new TreeMap<>();
		PDFService.pdfTracker = new HashMap<>();
		PDFService.pdfMeta =  new HashMap<>();
		PDFService.head = null;
		PDFService.prev = null;
		if (file == null || file.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("Error", "File is empty"));
		}
		try {
			String id = null;
			String filename = "_" + System.currentTimeMillis();		
			if (file.getOriginalFilename() != null) {
				filename = file.getOriginalFilename() + filename; 
			}
			try {
				
				id = PDFService.setPDF(file.getBytes());	
			}
			catch(Exception e) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("Error", "PDF could not be set"));
				
			}
				
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
			String page = PDFService.returnHeadPointer(id);
			if (page == null) {
				return ResponseEntity.notFound().build();
			}
			
			return ResponseEntity.ok()
					.header("Content-Type", "application/json")
					.body(page);
			

		} catch (Exception e) {
			e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

		}
		
				
				
		
	}
	@GetMapping("/{id}/metadata")
	public ResponseEntity<Map<String, Integer>> getMeta(@PathVariable String id ) throws IOException {
		try {
			int pageNum = PDFService.returnMeta(id);
			Map<String, Integer> response = new HashMap<>();
			response.put("PageNumber", pageNum);

			return ResponseEntity.ok()
					.header("Content-Type", "application/json")
					.body(response);
			

		} catch (Exception e) {
			e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

		}
		
				
				
		
	}
	
	@GetMapping("/{id}/Next")
	public ResponseEntity<String> getNextPage(@PathVariable String id) throws IOException {
		try {
			String page = PDFService.handlePageChange(id, "NEXT");
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
			String page = PDFService.handlePageChange(id, "PREV");
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
//	
//	@PostMapping("/{id}/QAs")
//	public ResponseEntity<String> startChat(@PathVariable String id, @RequestBody  String input) {
//		String response = AiService.askQuestions(id, input);
//	
//		
//	
   
//	return ResponseEntity.ok(response);
//
//	}
	
//	@GetMapping("/{id}/GenQA")
//	public ResponseEntity<List<Question>> genQA(@PathVariable String id) {
//		List<Question> response = pdfService.genQuestions(id);
//		return ResponseEntity.ok(response);
//	}





}
