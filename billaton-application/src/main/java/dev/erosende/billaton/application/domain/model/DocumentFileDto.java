package dev.erosende.billaton.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DocumentFileDto {

  private String fileName;
  private byte[] content;

}
