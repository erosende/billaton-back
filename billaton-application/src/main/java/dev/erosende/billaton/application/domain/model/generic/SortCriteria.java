package dev.erosende.billaton.application.domain.model.generic;

import dev.erosende.billaton.application.domain.enums.SortDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SortCriteria {

    private String field;

    @Builder.Default
    private SortDirection direction = SortDirection.ASC;

}