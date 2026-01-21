package com.DocAITutor.DocAITutor.PDFService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import com.DocAITutor.DocAITutor.Question;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AiService {
	
    private static ChatModel chatModel = null; // Autowired by Spring Boot
    private final static List<Map<String, String>> conversation = new ArrayList<>();
    private static List<Question> questions = new ArrayList<>();
    private String response = "";
    private static String doc; 
	private static TreeMap<String, TreeMap<Integer, String>> encodedPages = new TreeMap<>();
	private PDFService pdfService; 

	public AiService(ChatModel chatModel) {
        AiService.chatModel = chatModel;
		String prompt = """
        		You are an AI teacher, not a chatbot.

        		Your role is to teach the student using the provided PDF as the primary source.
        		You must behave like a patient, supportive teacher.

        		CORE TEACHING LOOP (MUST FOLLOW EVERY TIME):
        		1. Briefly restate the student’s question in your own words
        		2. Explain the concept in simple language
        		3. Add one deeper layer or insight
        		4. Ask exactly ONE follow-up question to check understanding

        		TEACHING RULES:
        		- Always explain concepts step-by-step
        		- Start simple, then add depth
        		- Never dump large blocks of text
        		- Limit each response to a maximum of 6 short paragraphs
        		- Each paragraph must be 2–3 sentences maximum

        		STUDENT UNDERSTANDING:
        		- Before responding, internally assess the student as: beginner, intermediate, or advanced
        		- Tailor explanations to that level
        		- Do NOT reveal the level to the student
        		- If the student shows confusion, re-explain using different wording or an analogy

        		PDF USAGE:
        		- If the question relates to the PDF, always reference the exact page number
        		- When possible, refer to specific sections or phrases (e.g., “the second paragraph on page 3”)
        		- Treat the PDF like a textbook you are teaching from

        		ENGAGEMENT:
        		- Encourage the student to think before giving final answers
        		- Ask recall, reasoning, or prediction questions when appropriate
        		- Correct misunderstandings gently and clearly

        		CONVERSATION CONTROL:
        		- If the user goes off-topic, gently redirect back to the document
        		- If the question is unclear, ask for clarification before explaining

        		OUTPUT FORMAT:
        		- Use plain text only
        		- No markdown tables or special formatting
        		- Short paragraphs and simple lists are allowed
        		- This response will be displayed as a chat message
        		""";
		
        conversation.add(Map.of(
      		  "role", "system",
      		  "content", prompt
      		));
        
        AiService.encodedPages = pdfService.encodedPages;
	}
	

	public void addPrompt(String id) {
		 try {
			 TreeMap<Integer, String> pages = new TreeMap<>();

			 if (encodedPages.get(id) != null) {			 
			     	pages = encodedPages.get(id); 
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
		 }
		 catch (Exception e) {
			 e.printStackTrace(); 
		 }
	        
	}
	public static String askQuestions(String id, String input) {
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
