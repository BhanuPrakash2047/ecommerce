package com.snackecommerce.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FAQRequest {
    @NotBlank(message = "Question is required")
    @Size(min = 5, max = 500, message = "Question must be 5-500 characters")
    private String question;

    @NotBlank(message = "Answer is required")
    @Size(min = 10, max = 2000, message = "Answer must be 10-2000 characters")
    private String answer;

    private Integer displayOrder;
}
