package com.DocAITutor.DocAITutor.PDFService;

import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import com.DocAITutor.DocAITutor.Page;
import com.DocAITutor.DocAITutor.PageLinkedList;
import com.DocAITutor.DocAITutor.Question;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;


@Service
public class PDFService {
	

	
	public static Map<String, PageLinkedList> pdfDocTracker = new HashMap<>();
	public static HashMap<String, PDDocument> pdfTracker = new HashMap<>();
	public static TreeMap<String, TreeMap<Integer, String>> encodedPages = new TreeMap<>();
	
	public static Page head = null;
	public static Page prev = null;
 	public static Map<String, Integer> pdfMeta = new HashMap<>();
    private final ChatModel chatModel; // Autowired by Spring Boot
    public final List<Map<String, String>> conversation = new ArrayList<>();
    public static String doc; 
    public static List<Question> questions = new ArrayList<>();
    public String response = "";
    @Autowired
    public PDFService(ChatModel chatModel) {
        this.chatModel = chatModel;
        String prompt = """
        		You are an exceptional AI tutor. 
        		Your goal is to help students understand complex topics in a simple, structured, and encouraging way.
        		
        		Always:
        		- Explain from first principles.
        		- Use examples where possible.
        		- Summarize key ideas at the end.
        		- Keep tone friendly and supportive.
        		Keep the conversation within the context of the document that are provided by the user.
        		If user attempts to take conversation into another direction that is not related to the document, direct the conversation
        		back to the context of the document. 
        		If needed to, ask the user to clarify what they mean. 
        		Please use plain text only — no markdown tables or special formatting. 
        		Use short paragraphs, numbered or bulleted lists when needed.
        		This will be displayed as a text message. 
        		"""; 
        
        conversation.add(Map.of(
        		  "role", "system",
        		  "content", prompt
        		));
    }

	public static String setPDF(byte[] pdf) throws IOException{
		System.out.print("PDF Received in service");
		String id = UUID.randomUUID().toString(); //Generate an specific id
		PDDocument doc = Loader.loadPDF(pdf);
        int numberOfPages = doc.getNumberOfPages();
        TreeMap<Integer, String> base64Pages = new TreeMap<>();
        
    		
    	for (int i = 0; i < numberOfPages; ++i) {
    		int pageNum = i + 1; 
    		PDPage page = doc.getPage(i);

    		PDDocument singlePageDoc = new PDDocument();
    		singlePageDoc.addPage(page);

    		ByteArrayOutputStream OS = new ByteArrayOutputStream();
    		singlePageDoc.save(OS);
    		singlePageDoc.close();

    		byte[] pageBytes = OS.toByteArray();
    		String base64 = Base64.getEncoder().encodeToString(pageBytes);
    		base64Pages.put(pageNum, base64);

    		Page pageNode = new Page(pageNum, page, base64, null, prev);

    		if (prev != null) {
    		   prev.setNext(pageNode);
    		} 
    		else {
    		   head = pageNode; 
    		}

    		    prev = pageNode; 

    	}
    	
        PageLinkedList pageList = new PageLinkedList(head, prev);

    	pdfDocTracker.put(id, pageList);///Use id as key
    	encodedPages.put(id, base64Pages);
    	pdfMeta.put(id,numberOfPages);
		

		return id; 
	}
	
	

	public static String returnHeadPointer(String id) {
	    PageLinkedList list = pdfDocTracker.get(id);
	    if (list == null || list.getHead() == null) {
	    	throw new IllegalArgumentException("Head points to null");
	    }
		return list.getHead().getBase64Data();
	}
	
	public static int returnMeta(String id) {
		return pdfMeta.get(id).intValue();
	}
	
	public static String handlePageChange(String id, String direction) {
		
		if (direction.toUpperCase().equals("PREV") && pdfDocTracker.get(id).traverseBackward() != null) {
			return pdfDocTracker.get(id).getHead().getBase64Data();
			
		}
		else if (direction.toUpperCase().equals("NEXT") && pdfDocTracker.get(id).traverseForward() != null) {
			return pdfDocTracker.get(id).getHead().getBase64Data();
			
		}
		return null; 
	}
	
	
	public void addPrompt(String id) {
		 try {
			 TreeMap<Integer, String> pages = encodedPages.get(id);
		        if (pages == null || pages.isEmpty()) {
		        }

		        StringBuilder fullText = new StringBuilder();
		        for (String base64 : pages.values()) {
		            byte[] pdfBytes = Base64.getDecoder().decode(base64);
		            PDDocument doc = Loader.loadPDF(pdfBytes);
		            PDFTextStripper stripper = new PDFTextStripper();
		            fullText.append(stripper.getText(doc)).append("\n");
		            doc.close();
		        }
		        doc = fullText.toString(); 
		        String prompt = """
		        		Here are the documents provided by the user:  
		        		""" + fullText.toString();
		        
		        
		        conversation.add(Map.of(
		        		  "role", "system",
		        		  "content", prompt
		        		));
		 }
		 catch (Exception e) {
			 e.printStackTrace(); 
		 }
	        
	}
	public String askQuestions(String id, String input) {
		System.out.println(input);
    try {
       
  
    	   String prompt = """
	        		Here is the question/statement provided by user
	        		""" + input + "\n" +
	        		"""
	        		Here is the conversation history: 
	        				""" + conversation;
	        
	        
	        conversation.add(Map.of(
	        		  "role", "user",
	        		  "content", input
	        		));


        // Call the model
        String chatResponse = chatModel.call(prompt);
        conversation.add(Map.of(
      		  "role", "ai",
      		  "content", chatResponse
      		));


        // Print for debugging
        System.out.println("AI Response: " + chatResponse);
        return chatResponse;

    } catch (Exception e) {
        e.printStackTrace();
        return "Error summarizing PDF: " + e.getMessage();
    }
}
	
	
	public List<Question> genQuestions(String id) {
		ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
		
		String randomSeed = UUID.randomUUID().toString();

		System.out.println("printing questions");
		String prompt = """
				You are an AI tutor.
				Please ensure that they differ from the previous set of question: """ + response + """
				Based on the following document text, generate **10 random multiple-choice questions** to test understanding.
				There can only be one right answer. 
				Each time this prompt is called, generate a *different random selection of questions*, varying their wording and difficulty.
				Format your response strictly as valid JSON:
				[
				   {
				            "question": "Question text here",
				            "options": ["Option A", "Option B", "Option C", "Option D"],
				            "answer": "Correct answer text here"
					}
				]
				
				Do NOT include explanations or any text outside of JSON.
				All questions must be related to the context of the document. Questions should range in easy to hard difficulty.
				
				Document text: 
				""" + doc;
		
		
		
		String res = chatModel.call(prompt);
	
		if (res.trim().startsWith("<")) {
			System.err.println("Received HTML instead of JSON");
			throw new RuntimeException("OpenRouter returned HTML instead of JSON");
		}
		
		String res_cleaned = res    
				.replaceAll("```json", "")
			    .replaceAll("```", "")
			    .trim();
		
		
		Pattern JSONPattern = Pattern.compile("(\\[.*\\]|\\{.*\\})", Pattern.DOTALL);
		Matcher matcher = JSONPattern.matcher(res_cleaned);
		if (matcher.find()) {
			res_cleaned = matcher.group(0);
		}

		response = res_cleaned; 
		
				

		try {

	        mapper.readTree(res_cleaned);
	        questions = mapper.readValue(res_cleaned, new TypeReference<List<Question>>() {});

		}
		catch (JacksonException e) {
			e.printStackTrace();
		}
		
		return questions;
	}
	
	


	
	


}