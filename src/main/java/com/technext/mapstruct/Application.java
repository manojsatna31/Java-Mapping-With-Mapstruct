package com.technext.mapstruct;

import com.technext.mapstruct.dto.DepartmentDto;
import com.technext.mapstruct.dto.EmployeeDto;
import com.technext.mapstruct.mapper.EntityToDtoMapper;
import com.technext.mapstruct.model.Department;
import com.technext.mapstruct.model.Employee;
import com.technext.mapstruct.model.Organization;
import java.util.Set;

public class Application {
  public static void main(String[] args) {
    Organization organization = Organization.builder().name("Electrical").build();
    Department department = Department.builder().name("Electrical").organization(organization).build();
    Employee employee =
        Employee.builder()
            .firstName("Manoj")
            .lastName("Mishra")
            .age(10)
            .salary(10000)
            .position("Assistant")
            .departmentName(department.getName())
            .organization(organization)
            .build();
    System.out.println(employee);
    EmployeeDto employeeDto = EntityToDtoMapper.MAPPER.toDto(employee);
    System.out.println(employeeDto);

    Set<Employee> employees =Set.of(employee);
    department.setEmployees(employees);
    System.out.println(department);

    DepartmentDto departmentDto = EntityToDtoMapper.MAPPER.toDto(department);

    System.out.println(departmentDto);
  }
}
