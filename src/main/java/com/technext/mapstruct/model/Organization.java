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
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
//@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Organization {
	//private Integer id;
	private String name;
	private Set<Department> departments;
	private Set<Employee> employees;

	@Override
	public String toString() {
		return "Organization{" +
				"name='" + name + '\'' +
				", departments=" + departments +
				", employees=" + employees +
				'}';
	}
}
