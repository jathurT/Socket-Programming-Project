package com.uor.eng.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
  private Long id;
  private String fullName;
  private String email;
  private String username;
  private String password;
}