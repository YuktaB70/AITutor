package com.DocAITutor.DocAITutor;

import org.apache.pdfbox.pdmodel.PDPage;

import lombok.*;


@Data
@Getter
@Setter
public class Page {
    private int pageNumber; 
    private PDPage page; 
    private String base64Data; 
    private Page next;
    private Page prev;

    public Page(int pageNumber, PDPage page, String base64Data, Page next, Page prev) {
        this.pageNumber = pageNumber;
        this.page = page; 
        this.base64Data = base64Data;
        this.next = next;
        this.prev = prev;
    }



    public Page getNext() {
    	return next;
    };
    public Page getPrev() {
    	return prev; 
    }
    public String base64() {
    	return this.base64Data;
    }
}
