package dev.erosende.billaton.application.domain.model;

import lombok.Data;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Data
public class ConceptDto {

  private Integer conceptId;
  private String description;
  private Integer amount;
  private Double pricePerUnit;
  private Integer documentId;

  public String getFormattedPrice() {
    DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.GERMAN));
    return df.format(this.pricePerUnit);
  }

}