package com.technext.mapstruct.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EmployeeDto {
  private String firstName;
  private String lastName;
  private String position;
  private Integer salary;
  private Integer age;
  private String departmentName;
  private String organizationName;
}
