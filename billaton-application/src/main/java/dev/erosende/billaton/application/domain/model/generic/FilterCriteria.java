package dev.erosende.billaton.application.domain.model.generic;

import dev.erosende.billaton.application.domain.enums.FilterOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterCriteria {

    private String field;
    private FilterOperation operation;
    private Object value;
    private Object valueTo;

}