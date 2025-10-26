package com.DocAITutor.DocAITutor;

import org.apache.pdfbox.pdmodel.PDPage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
public class PageLinkedList {
	
	private Page head = null; 
	private Page tail = null; 
	
	public PageLinkedList(Page head, Page tail) {
		this.head = head;
		this.tail = tail;
	}
	
	
	public Page traverseForward() {
		head = head.getNext();
		return head;
	}
	
	public Page traverseBackward() {
		head = head.getPrev();
		return head; 
	}

}
