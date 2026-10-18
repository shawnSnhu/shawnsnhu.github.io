package com.shawnbethune.contactservice.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateContactRequest(
        @NotBlank(message = "Contact ID is required")
        @Size(max = 10, message = "Contact ID must not exceed 10 characters")
        @Pattern(
                regexp = "[A-Za-z0-9_-]+",
                message = "Contact ID may contain only letters, numbers, underscores, and hyphens")
        String contactId,

        @NotBlank(message = "First name is required")
        @Size(max = 10, message = "First name must not exceed 10 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 10, message = "Last name must not exceed 10 characters")
        String lastName,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "[0-9]{10}", message = "Phone must contain exactly 10 digits")
        String phone,

        @NotBlank(message = "Address is required")
        @Size(max = 30, message = "Address must not exceed 30 characters")
        String address) {
}
