package com.medibook.notification.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long appointmentId;
    private Long patientId;
    private String doctorName;
    private String message;
}