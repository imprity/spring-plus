package org.example.expert.domain.todo.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TodoSearchCondition {
    private int page = 1;
    private int size = 10;

    // null 이 아니라면 문자열의 크기가 1 이상일 것
    @Size(min = 1)
    private String weatherPattern;

    private LocalDateTime minModifiedAt;
    private LocalDateTime maxModifiedAt;
}
