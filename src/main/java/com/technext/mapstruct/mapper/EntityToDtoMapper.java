package com.technext.mapstruct.mapper;

import com.technext.mapstruct.dto.DepartmentDto;
import com.technext.mapstruct.dto.EmployeeDto;
import com.technext.mapstruct.model.Department;
import com.technext.mapstruct.model.Employee;
import com.technext.mapstruct.model.Organization;
import java.util.Locale;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ap.shaded.freemarker.template.utility.StringUtil;
import org.mapstruct.factory.Mappers;

@Mapper
 //(builder = @Builder(disableBuilder = true))
//(uses = {EmployeeSetToEmployeeNameMapper.class})
public interface EntityToDtoMapper {
  EntityToDtoMapper MAPPER = Mappers.getMapper(EntityToDtoMapper.class);

  @Mapping(source = "departmentName", target = "departmentName")
  @Mapping(source = "organization.name", target = "organizationName")
  EmployeeDto toDto(Employee source);

  @Mapping(source = "employees.", target = "employeeName")
  @Mapping(source = "organization.name", target = "organizationName")
  DepartmentDto toDto(Department source);

  /**
   * Important: when using a builder, the @AfterMapping annotated method must have the builder
   * as @MappingTarget annotated parameter so that the method is able to modify the object going to be build.
   * The build method is called when the @AfterMapping annotated method scope finishes.
   * MapStruct will not call the @AfterMapping annotated method if the real target is used
   * as @MappingTarget annotated parameter.
   */
  @BeforeMapping
  default void enrichOrganizationName(Department source, @MappingTarget DepartmentDto.DepartmentDtoBuilder target) {
    target.organizationName(source.getName().toLowerCase(Locale.ROOT));
    System.out.println("Before Mapping:"+target);
  }

  @AfterMapping
  default void enrichOrganizationNameWithSuffix(Department source, @MappingTarget DepartmentDto.DepartmentDtoBuilder target) {
    target.organizationName(source.getName().toLowerCase(Locale.ROOT)+"_DEPT");
    System.out.println("After Mapping:"+target);
  }
}
