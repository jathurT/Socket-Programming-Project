package com.uor.eng.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Host {
  private Long id;
  private String name;
  private String hostname;
  private HostType type;
  private Long userId;
  private String status;
}