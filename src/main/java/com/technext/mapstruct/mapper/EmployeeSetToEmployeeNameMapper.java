package com.technext.mapstruct.mapper;

import com.technext.mapstruct.model.Employee;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public class EmployeeSetToEmployeeNameMapper {
  EmployeeSetToEmployeeNameMapper MAPPER = Mappers.getMapper(EmployeeSetToEmployeeNameMapper.class);
  String toEmployeeNameSet(Employee employee){
    return employee.getFirstName() + employee.getLastName();
  }
}
