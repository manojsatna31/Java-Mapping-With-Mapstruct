package com.technext.mapstruct.dto;

import com.technext.mapstruct.model.Employee;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DepartmentDto {
  private String name;
  private Set<Employee> employeeName;
  private String organizationName;
}
