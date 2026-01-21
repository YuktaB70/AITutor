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

import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;


@Service
@Getter
@Setter
public class PDFService {
	

	
	public static Map<String, PageLinkedList> pdfDocTracker = new HashMap<>();
	public static HashMap<String, PDDocument> pdfTracker = new HashMap<>();
	public static TreeMap<String, TreeMap<Integer, String>> encodedPages = new TreeMap<>();
	
	public static Page head = null;
	public static Page prev = null;
 	public static Map<String, Integer> pdfMeta = new HashMap<>();
    


	public static String setPDF(byte[] pdf) throws IOException{
		String id = UUID.randomUUID().toString(); //Generate an specific id
		
		PDDocument doc = Loader.loadPDF(pdf);
		if (doc != null) {
			int numberOfPages = doc.getNumberOfPages();
			TreeMap<Integer, String> base64Pages = new TreeMap<>();
        
	        if (numberOfPages == 0) {
				System.out.print("File not applicable");
	        }
	    		
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
		}
		

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
	
	

	
	


	
	


}