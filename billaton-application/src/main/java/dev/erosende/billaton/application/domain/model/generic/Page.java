package dev.erosende.billaton.application.domain.model.generic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Page<T> {

  private List<T> content;
  private int pageNumber;
  private int pageSize;
  private long totalElements;
  private int totalPages;
  private boolean first;
  private boolean last;
  private boolean empty;

  public static <T> Page<T> of(List<T> content, int pageNumber, int pageSize, long totalElements) {
    int totalPages = (int) Math.ceil((double) totalElements / pageSize);
    return Page.<T>builder()
        .content(content)
        .pageNumber(pageNumber)
        .pageSize(pageSize)
        .totalElements(totalElements)
        .totalPages(totalPages)
        .first(pageNumber == 0)
        .last(pageNumber >= totalPages - 1)
        .empty(content.isEmpty())
        .build();
  }

  public <U> Page<U> map(Function<? super T, ? extends U> converter) {
    List<U> convertedContent = this.content.stream()
        .map(converter)
        .collect(Collectors.toList());

    return new Page<>(
        convertedContent,
        this.pageNumber,
        this.pageSize,
        this.totalElements,
        this.totalPages,
        this.first,
        this.last,
        convertedContent.isEmpty()
    );
  }

}