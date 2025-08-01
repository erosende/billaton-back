package dev.erosende.billaton.application.domain.util;

import dev.erosende.billaton.application.domain.enums.FilterOperation;
import dev.erosende.billaton.application.domain.enums.SortDirection;
import dev.erosende.billaton.application.domain.model.generic.FilterCriteria;
import dev.erosende.billaton.application.domain.model.generic.SortCriteria;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PagingUtils {

  public static List<SortCriteria> parseSortCriteria(List<String> sort) {
    if (sort == null || sort.isEmpty()) {
      return null;
    }

    List<SortCriteria> sortCriteria = new ArrayList<>();
    for (String sortParam : sort) {
      String[] parts = sortParam.split("@");
      if (parts.length >= 1) {
        SortDirection direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
            ? SortDirection.DESC
            : SortDirection.ASC;
        sortCriteria.add(SortCriteria.builder()
            .field(parts[0])
            .direction(direction)
            .build());
      }
    }

    return sortCriteria;
  }

  public static List<FilterCriteria> buildDocumentFilters(Integer documentTypeId,
                                                          LocalDate dateFrom,
                                                          LocalDate dateTo,
                                                          Integer recipientId) {
    List<FilterCriteria> filters = new ArrayList<>();

    if (documentTypeId != null) {
      filters.add(FilterCriteria.builder()
          .field("documentTypeId")
          .operation(FilterOperation.EQUALS)
          .value(documentTypeId)
          .build());
    }

    if (dateFrom != null && dateTo != null) {
      filters.add(FilterCriteria.builder()
          .field("documentDate")
          .operation(FilterOperation.BETWEEN)
          .value(dateFrom)
          .valueTo(dateTo)
          .build());
    } else if (dateFrom != null) {
      filters.add(FilterCriteria.builder()
          .field("documentDate")
          .operation(FilterOperation.GREATER_THAN_OR_EQUALS)
          .value(dateFrom)
          .build());
    } else if (dateTo != null) {
      filters.add(FilterCriteria.builder()
          .field("documentDate")
          .operation(FilterOperation.LESS_THAN_OR_EQUALS)
          .value(dateTo)
          .build());
    }

    if (recipientId != null) {
      filters.add(FilterCriteria.builder()
          .field("recipientId")
          .operation(FilterOperation.EQUALS)
          .value(recipientId)
          .build());
    }

    return filters.isEmpty() ? null : filters;
  }

}
