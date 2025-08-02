package dev.erosende.billaton.application.domain.model.generic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagingParams {

  @Builder.Default
  private int page = 0;

  @Builder.Default
  private int size = 20;

  private List<SortCriteria> sort;
  private List<FilterCriteria> filters;

  public int getOffset() {
    return page * size;
  }

  public void addFilter(FilterCriteria filter) {
    if (filters == null) {
      filters = new ArrayList<>();
    }
    filters.add(filter);
  }

}