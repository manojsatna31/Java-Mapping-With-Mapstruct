package com.technext.mapstruct.model;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

//@Data
@Setter
@Getter
//@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
//@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Department {
	//private Integer id;
	private String name;
	private Set<Employee> employees;
	private Organization organization;

	@Override
	public String toString() {
		return "Department{" +
				"name='" + name + '\'' +
				", employees=" + employees +
				", organization=" + organization +
				'}';
	}
}


