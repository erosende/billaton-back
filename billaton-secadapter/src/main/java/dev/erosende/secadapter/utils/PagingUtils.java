package dev.erosende.secadapter.utils;

import dev.erosende.billaton.application.domain.enums.FilterOperation;
import dev.erosende.billaton.application.domain.model.generic.FilterCriteria;
import dev.erosende.billaton.application.domain.model.generic.PagingParams;
import dev.erosende.billaton.application.domain.model.generic.SortCriteria;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PagingUtils {

  public static String buildWhereClause(List<FilterCriteria> filters, Map<String, String> fieldMappings, MapSqlParameterSource params) {
    if (filters == null || filters.isEmpty()) {
      return "";
    }

    return IntStream.range(0, filters.size())
        .mapToObj(i -> {
          FilterCriteria filter = filters.get(i);
          String columnName = fieldMappings.getOrDefault(filter.getField(), filter.getField());
          String paramName = filter.getField() + "_" + i;

          switch (filter.getOperation()) {
            case EQUALS -> {
              params.addValue(paramName, filter.getValue());
              return columnName + " = :" + paramName;
            }
            case NOT_EQUALS -> {
              params.addValue(paramName, filter.getValue());
              return columnName + " != :" + paramName;
            }
            case GREATER_THAN -> {
              params.addValue(paramName, filter.getValue());
              return columnName + " > :" + paramName;
            }
            case GREATER_THAN_OR_EQUALS -> {
              params.addValue(paramName, filter.getValue());
              return columnName + " >= :" + paramName;
            }
            case LESS_THAN -> {
              params.addValue(paramName, filter.getValue());
              return columnName + " < :" + paramName;
            }
            case LESS_THAN_OR_EQUALS -> {
              params.addValue(paramName, filter.getValue());
              return columnName + " <= :" + paramName;
            }
            case LIKE -> {
              params.addValue(paramName, "%" + filter.getValue() + "%");
              return columnName + " LIKE :" + paramName;
            }
            case IN -> {
              if (filter.getValue() instanceof List) {
                params.addValue(paramName, filter.getValue());
                return columnName + " IN (:" + paramName + ")";
              }
              throw new IllegalArgumentException("IN operation requires a List value");
            }
            case BETWEEN -> {
              params.addValue(paramName + "_from", filter.getValue());
              params.addValue(paramName + "_to", filter.getValueTo());
              return columnName + " BETWEEN :" + paramName + "_from AND :" + paramName + "_to";
            }
            case IS_NULL -> {
              return columnName + " IS NULL";
            }
            case IS_NOT_NULL -> {
              return columnName + " IS NOT NULL";
            }
            default -> throw new IllegalArgumentException("Unsupported filter operation: " + filter.getOperation());
          }
        })
        .collect(Collectors.joining(" AND "));
  }

  public static void addFilterToPagingParams(PagingParams params, String fieldName, Object value) {
    params.addFilter(
        FilterCriteria.builder()
            .field(fieldName)
            .value(value)
            .operation(FilterOperation.EQUALS)
            .build()
    );
  }

  public static String buildOrderByClause(List<SortCriteria> sortCriteria, Map<String, String> fieldMappings) {
    if (sortCriteria == null || sortCriteria.isEmpty()) {
      return "";
    }

    return sortCriteria.stream()
        .map(sort -> {
          String columnName = fieldMappings.getOrDefault(sort.getField(), sort.getField());
          return columnName + " " + sort.getDirection().name();
        })
        .collect(Collectors.joining(", "));
  }

}
