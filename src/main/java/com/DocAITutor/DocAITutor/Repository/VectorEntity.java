package com.DocAITutor.DocAITutor.Repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VectorEntity {
 private String id;
 private String text;
 private String type;
 private float[] embedding;
 private Double similarity;
 private Boolean isActive;
 
 // Additional metadata fields as needed
}