package com.excelfore.aws.awstask.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String error;
    private String errorMessage;

    // Constructor for error response
    public ApiResponse(String error, String errorMessage) {
        this.error = error;
        this.errorMessage = errorMessage;
    }
}
