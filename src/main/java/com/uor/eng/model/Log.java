package com.uor.eng.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Log {
  private Long id;
  private Long hostId;
  private LocalDateTime time;
  private String status;
  private Double responseTime;
  private String oldStatus;
}