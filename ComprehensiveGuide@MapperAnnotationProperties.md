# **A Comprehensive Guide to MapStruct's @Mapper Annotation Properties**

Welcome to the comprehensive guide on the `@Mapper` annotation. In this document, you will find detailed explanations
and examples for all the properties available to configure MapStruct mappers.

**Aims of this Documentation**
- **Comprehensive Breakdown:** Offer an in-depth explanation of every `@Mapper` property along with practical, illustrative examples.
- **Ease of Navigation:** Provide an interactive experience with expandable sections that allow users to drill down into topics only when needed.
- **High-Performance Mapping:** Explain how MapStruct, through compile-time code generation, achieves type safety and performance efficiency compared to dynamic mapping frameworks.
- **Centralizing Mapping Contracts:** Emphasize the role of `@Mapper` as the central point in defining contracts for object mappings within Java applications.



# Table of Contents
1. [Introduction](#introduction)
2. [Core Functionality of the `@Mapper` Annotation](#core-functionality-of-the-mapper-annotation)
3. [Detailed Explanation of `@Mapper` Properties with Examples](#detailed-explanation-of-mapper-properties-with-examples)
4. [Interaction with Other MapStruct Annotations](#interaction-with-other-mapstruct-annotations)
5. [Using `@Mapper` with `@MapperConfig`](#using-mapper-with-mapperconfig)
6. [Advanced Scenarios and Best Practices](#advanced-scenarios-and-best-practices)
7. [Conclusion](#conclusion)


## Introduction

MapStruct is a powerful code generator designed to significantly simplify the implementation of mappings between java
bean types.It operates based on a convention-over-configuration approach, automatically generating mapping code during
compile time.This generated code utilizes plain method invocations, resulting in high performance, type safety, and ease
of understanding.Compared to dynamic mapping frameworks, MapStruct offers notable advantages, including faster execution
due to the absence of reflection and compile-time type safety, which prevents accidental mappings between incompatible
types.

At the heart of MapStruct's functionality lies the @org.mapstruct.Mapper annotation. This annotation serves to mark a
java interface or an abstract class as a mapper, thereby instructing the MapStruct code generator to create an
implementation of that type. Essentially, the @Mapper annotation acts as the central point for defining the contracts
for object mappings within an application.


## Core Functionality of the @Mapper Annotation

To initiate the mapping process with MapStruct, a developer simply needs to annotate a java interface or an abstract
class with @org.mapstruct.Mapper. For instance, a basic mapper interface for converting between Car and CarDto objects
could be defined as: @Mapper public interface CarMapper {... }.

During the project's build process, the MapStruct annotation processor comes into play. This processor analyzes the
interfaces or abstract classes annotated with @Mapper and generates concrete implementation classes.These generated
implementations contain the actual logic for transferring data between source and target objects, based on the mapping
methods defined in the mapper interface and any associated configurations. As highlighted in the research material,
developers are relieved from the burden of manually writing these often verbose and error-prone implementation
classes. MapStruct employs a convention-based approach for mapping fields. When a property in the source object has the
same name as a property in the target object, and their types are compatible, MapStruct will automatically map them
implicitly.However, when the names of the properties differ between the source and target entities, the
@org.mapstruct.Mapping annotation can be used to explicitly specify the correspondence between them.

The fundamental operation of MapStruct centers around automating the creation of mapping logic, freeing developers to
concentrate on defining the mapping contract through the @Mapper annotation and its associated methods. The annotation
processor's role in generating the implementation underscores MapStruct's dedication to minimizing boilerplate code.

---
My aims is to provide a detailed explanation of all the properties available within the @Mapper annotation, accompanied
by illustrative examples, to facilitate a comprehensive understanding of its capabilities.
## Detailed Explanation of `@Mapper` Properties with Examples
<details>
<summary><strong>General Properties</strong></summary>

| **Property** | **Description**                                                                | **Default Value**        |
|--------------|--------------------------------------------------------------------------------|--------------------------|
| `uses`       | Other mapper types used by this mapper.                                        | `{}`                     |
| `imports`    | Additional types for import statements in the generated mapper.                | `{}`                     |
| `config`     | A class annotated with `@MapperConfig` to be used as a configuration template. | `void.class`             |
| `builder`    | Provides information for builder mappings.                                     | `@org.mapstruct.Builder` |
---
<details id="gp-uses">
<summary>uses</summary>

This property, of type Class<?>, allows specification of other mapper types that the current mapper will utilize. Its default value is an empty array, {}. The primary purpose of uses is to enable the mapping of nested or complex types by referencing other mapper interfaces or hand-written classes containing custom mapping logic.Consider a scenario where a Car object has a java.util.Date representing its manufacturing date, and the corresponding CarDto should represent this date as a String in "yyyy-MM-dd" format. This requires custom conversion logic, which can be implemented in a hand-written class like DateMapper:

```java
public class DateMapper {
    public String asString(Date date) {
        return date != null ? new SimpleDateFormat("yyyy-MM-dd").format(date) : null;
    }

    public Date asDate(String date) {
        try {
            return date != null ? new SimpleDateFormat("yyyy-MM-dd").parse(date) : null;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
```

To instruct MapStruct to use this custom mapper, the CarMapper interface would be annotated as follows:

```java

@Mapper(uses = DateMapper.class)
public interface CarMapper {
    CarDto carToCarDto(Car car);
}
```

In this case, when MapStruct generates the implementation for carToCarDto(), it will detect the need for conversion between Date and String and will utilize the asString(Date date) method from the DateMapper class.

Another common use case is reusing existing MapStruct-generated mappers. Suppose there's a Person class with a corresponding PersonDto and a PersonMapper already defined. If the Car class has a driver property of type Person, and CarDto has a driver property of type PersonDto, the PersonMapper can be reused within CarMapper:

```java  

@Mapper(uses = PersonMapper.class)
public interface CarMapper {
    CarDto carToCarDto(Car car);
}

@Mapper
public interface PersonMapper {
    PersonDto personToPersonDto(Person person);
}
```

When mapping the driver property, MapStruct will invoke the personToPersonDto(Person person) method from the
PersonMapper.

Furthermore, utility classes with specific transformation logic can also be referenced. For example, if a Person class
has firstName and lastName fields, and the PersonDto requires a combined fullName field, a utility class NameUtils
could
provide this functionality:

```java
public class NameUtils {
    public static String combineNames(String firstName, String lastName) {
        return firstName + " " + lastName;
    }
}
```

The PersonMapper can then use this utility class in an @Mapping annotation:

```java

@Mapper(uses = NameUtils.class)
public interface PersonMapper {
    @Mapping(target = "fullName",
            expression = "java( NameUtils.combineNames(person.getFirstName(), person.getLastName()) )")
    PersonDto personToPersonDto(Person person);
}
```

Even though combineNames doesn't directly map between the types of firstName, lastName, and fullName, its invocation
within the expression is enabled because NameUtils is listed in the uses property. The uses property facilitates a
modular approach to mapping, allowing the decomposition of complex transformations into reusable components. If a
specific type mapping is not found within the current mapper, MapStruct will consult the classes specified in uses,
promoting cleaner and more focused mapper interfaces.
---
</details>
<details id="gp-imports">
<summary>imports</summary>
This property, also of type Class<?>, allows specifying additional types for which import statements should be added
to the generated mapper implementation class. The default value is an empty array, {}. The primary purpose is to
enable
the use of simple, unqualified class names within expressions defined in the @Mapping annotation, improving
readability.

Consider a scenario where a mapping needs to use a class named TimeAndFormat from the package org.sample within an
expression. Without importing it, the @Mapper might look like this:

```java

@Mapper
public interface SourceTargetMapper {
    @Mapping(target = "timeAndFormat",
            expression = "java( new org.sample.TimeAndFormat( s.getTime(), s.getFormat() ) )")
    Target sourceToTarget(Source s);
}
```

As evident, the fully qualified name org.sample.TimeAndFormat is used in the expression. To simplify this, the imports
property can be utilized:

```java
import org.sample.TimeAndFormat;

@Mapper(imports = TimeAndFormat.class)
public interface SourceTargetMapper {
    @Mapping(target = "timeAndFormat",
            expression = "java( new TimeAndFormat( s.getTime(), s.getFormat() ) )")
    Target sourceToTarget(Source s);
}
```

Now, within the expression, TimeAndFormat can be used without its package prefix.

Similarly, entire packages can be imported if multiple classes from the same package are used in expressions. For
instance, if java.util.UUID is used in a default expression:

```java
import java.util.UUID;

@Mapper
public interface SourceTargetMapper {
    @Mapping(target = "id", source = "sourceId", defaultExpression = "java( UUID.randomUUID().toString() )")
    Target sourceToTarget(Source s);
}
```

Here, java.util.UUID is used fully qualified (implicitly with the standard import). By using the imports property:

```java

@Mapper(imports = java.util.class)
public interface SourceTargetMapper {
    @Mapping(target = "id", source = "sourceId", defaultExpression = "java( UUID.randomUUID().toString() )")
    Target sourceToTarget(Source s);
}
```

UUID can now be used directly in the defaultExpression. While standard java import statements in the mapper interface
handle the types used in the interface definition, the @Mapper's imports property specifically directs the MapStruct
code generator to include import statements in the generated implementation for types referenced within the
string-based expressions of annotations like @Mapping. This ensures the generated code compiles correctly and enhances
the clarity of mapper definitions.
---
</details>
<details id="gp-config">
<summary>config</summary>
This property, of type Class<?>, allows specifying a class annotated with @org.mapstruct.MapperConfig to be used as a
configuration template for the current mapper. The default value is void.class.The @MapperConfig annotation enables
the definition of shared configurations, such as which custom mappers to use or
the policy for handling unmapped target properties, in a central location. These configurations can then be applied to
multiple mapper interfaces via the config property, promoting reusability and consistency.

Consider defining a central configuration interface:

```java

@MapperConfig(
        uses = CustomMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface CentralConfig {
}
```

This CentralConfig specifies that CustomMapper should be used and that unmapped target properties should result in an
error. A mapper can then reference this configuration:

```java

@Mapper(config = CentralConfig.class)
public interface SourceTargetMapper {
    Target sourceToTarget(Source s);
}
```

The SourceTargetMapper will now inherit the configurations from CentralConfig, effectively using CustomMapper and
enforcing the ERROR policy for unmapped target properties.Configurations can also be overridden at the @Mapper level.
For example:

```java

@Mapper(config = CentralConfig.class, unmappedTargetPolicy = ReportingPolicy.WARN)
public interface AnotherSourceTargetMapper {
    AnotherTarget sourceToAnotherTarget(AnotherSource as);
}
```

Here, AnotherSourceTargetMapper inherits the uses setting from CentralConfig but overrides the unmappedTargetPolicy to
WARN.

Furthermore, @MapperConfig can contain prototype methods with method-level mapping annotations, which can be inherited
based on the mappingInheritanceStrategy. The config property provides a powerful mechanism for managing MapStruct
configurations centrally, enhancing reusability and consistency across multiple mappers. Changes to the @MapperConfig
interface can affect all referencing mappers, streamlining configuration updates.
</details>
<details id="gp-builder">
<summary>builder</summary>
This property, of type org.mapstruct.Builder (which is an annotation itself), provides configuration for builder
mappings, particularly useful when mapping to immutable objects created via a builder pattern. The @Builder annotation
has a property disableBuilder of type boolean, with a default value of false.

By default (disableBuilder = false), MapStruct will attempt to detect and use a builder for the target type if one is
available (typically identified by a static builder() method and a build() method in the builder class). To disable
this behavior for a specific mapper:

```java

@Mapper(builder = @Builder(disableBuilder = true))
public interface PersonMapper {
    Person map(PersonDto dto);
}

// MapStruct will not use a builder for Person, even if one exists
```

In this case, MapStruct will try to map PersonDto to Person using its constructor or by directly accessing fields.
Conversely, with the default setting, if a Product class has a builder:

```java

@Mapper
public interface ProductMapper {
    Product map(ProductDto dto);
}

// MapStruct will likely use the Product's builder to create an instance
```

The builder property allows fine-grained control over whether MapStruct should utilize the builder pattern for
creating target objects within a specific mapper, which is crucial for supporting immutable objects.
---
</details>
</details>
<details>
<summary><strong>Mapping Strategies</strong></summary>

| **Property**                         | **Description**                                       | **Default Value**                                       |
|--------------------------------------|-------------------------------------------------------|---------------------------------------------------------|
| `unmappedSourcePolicy`               | Defines how unmapped source properties are handled.   | `org.mapstruct.ReportingPolicy.IGNORE`                  |
| `unmappedTargetPolicy`               | Controls the handling of unmapped target properties.  | `org.mapstruct.ReportingPolicy.WARN`                    |
| `typeConversionPolicy`               | Specifies behavior for lossy type conversions.        | `org.mapstruct.ReportingPolicy.IGNORE`                  |
| `collectionMappingStrategy`          | Controls how collections are mapped.                  | `org.mapstruct.CollectionMappingStrategy.ACCESSOR_ONLY` |
| `mappingInheritanceStrategy`         | Defines inheritance behavior for mappings.            | `org.mapstruct.MappingInheritanceStrategy.EXPLICIT`     |
| `disableSubMappingMethodsGeneration` | Disables automatic generation of sub-mapping methods. | `FALSE`                                                 |

---
<details id="ms-unmappedSourcePolicy">
<summary>unmappedSourcePolicy</summary>
This property, of type org.mapstruct.ReportingPolicy, dictates how MapStruct should handle unmapped properties in the
source object during a mapping. The default value is org.mapstruct.ReportingPolicy.IGNORE. The possible values are
IGNORE, WARN, and ERROR.

Setting unmappedSourcePolicy to org.mapstruct.ReportingPolicy.ERROR will cause a compilation error if any property in
the source object does not have a corresponding target property or an explicit mapping defined. For example:

```java

@Mapper(unmappedSourcePolicy = ReportingPolicy.ERROR)
public interface SourceTargetMapper {
    Target sourceToTarget(Source source);
}

public class Source {
    private String sourceProperty1;
    private String sourceProperty2;
// Getters and setters
}

public class Target {
    private String targetProperty1;
// Getters and setters
}
```

In this case, because sourceProperty2 in Source is not mapped to any property in Target, MapStruct will generate a
compilation error. This policy is vital for ensuring mapping completeness and preventing unintentional data loss.

If unmappedSourcePolicy is set to org.mapstruct.ReportingPolicy.WARN, MapStruct will generate a warning during
compilation for each unmapped source property:

```java

@Mapper(unmappedSourcePolicy = ReportingPolicy.WARN)
public interface SourceTargetMapper {
    Target sourceToTarget(Source source);
}
```

Here, a warning will be issued for sourceProperty2. This provides a less strict approach for identifying potential
oversights in the mapping configuration.

Finally, the default value, org.mapstruct.ReportingPolicy.IGNORE, instructs MapStruct to silently ignore any source
properties that do not have a corresponding target property:

```java

@Mapper(unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface SourceTargetMapper {
    Target sourceToTarget(Source source);
}
```

In this scenario, sourceProperty2 will be ignored without any error or warning. Choosing the appropriate policy
depends on the level of strictness required for the mapping process. Setting the policy to ERROR can prevent runtime
issues by ensuring all source data is considered, although it might lead to build failures if partial mapping is
intentional.
---
</details>
<details id="ms-unmappedTargetPolicy">
<summary>unmappedTargetPolicy</summary>
unmappedTargetPolicy: This property, also of type org.mapstruct.ReportingPolicy, defines how MapStruct should handle
unmapped properties in the target object. The default value is org.mapstruct.ReportingPolicy.WARN. The possible values
are IGNORE, WARN, and ERROR.

Setting unmappedTargetPolicy to org.mapstruct.ReportingPolicy.ERROR will result in a compilation error if any property
in the target object is not mapped from the source:

```java

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CarMapper {
    CarDto carToCarDto(Car car);
}

public class Car {
//...
}

public class CarDto {
    private String make;
    private String color; // Unmapped target property
// Getters and setters
}
```

Here, if the Car class does not have a color property or an explicit mapping to CarDto.color is missing, a compilation
error will occur. This policy helps ensure that the target object is fully populated as expected, and is particularly
useful when all target fields are mandatory.

If unmappedTargetPolicy is set to org.mapstruct.ReportingPolicy.WARN, MapStruct will generate a warning for each
unmapped target property:

```java  

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN)
public interface CarMapper {
    CarDto carToCarDto(Car car);
}
```

A warning will be issued for the unmapped color property in CarDto. The default value of WARN strikes a balance
between
strictness and allowing for cases where some target properties might be intentionally left unmapped or have default
values.

Setting unmappedTargetPolicy to org.mapstruct.ReportingPolicy.IGNORE will cause MapStruct to silently skip any
unmapped
target properties:

```java

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CarMapper {
    CarDto carToCarDto(Car car);
}
```

In this case, the color property in CarDto will remain at its default value (likely null) without any notification.
The choice of this policy impacts the data integrity of the target objects, with ERROR being suitable for scenarios
where all target fields are mandatory.
---
</details>
<details id="ms-typeConversionPolicy">
<summary>typeConversionPolicy</summary>
This property, of type org.mapstruct.ReportingPolicy, controls how MapStruct handles potential loss of precision or
data during type conversions, such as from a long to an int. The default value is
org.mapstruct.ReportingPolicy.IGNORE. The possible values are IGNORE, WARN, and ERROR.

If typeConversionPolicy is set to org.mapstruct.ReportingPolicy.WARN, MapStruct will issue a warning during
compilation whenever a conversion from a larger data type to a smaller one occurs:

```java

@Mapper(typeConversionPolicy = ReportingPolicy.WARN)
public interface NumberMapper {
    long intToLong(int source); // No warning

    int longToInt(long source); // Warning will be issued
}
```

Here, the conversion from long to int will trigger a warning. This helps developers identify potential data loss
issues.

Setting typeConversionPolicy to org.mapstruct.ReportingPolicy.ERROR will cause the build to fail if a potentially
lossy
conversion is detected:

```java

@Mapper(typeConversionPolicy = ReportingPolicy.ERROR)
public interface AnotherNumberMapper {
    short intToShort(int source); // Error will be issued

    int shortToInt(short source); // No error
}
```

The conversion from int to short will result in a compilation error. This policy is crucial for applications where
data
precision is critical.

The default policy, org.mapstruct.ReportingPolicy.IGNORE, means that MapStruct will perform the type conversion
without
any warnings or errors, even if there is a potential loss of data:

```java

@Mapper
public interface DefaultNumberMapper {
    byte intToByte(int source); // No warning or error
}
```

In this case, the conversion from int to byte will be performed silently. This property is essential for maintaining
data integrity, especially with numerical types, and setting it to WARN or ERROR can help prevent silent data
truncation. The existence of this policy underscores the importance of explicit type handling in data mapping.
</details>
<details id="ms-collectionMappingStrategy">
<summary>collectionMappingStrategy</summary>
This property, of type org.mapstruct.CollectionMappingStrategy, controls how MapStruct handles the mapping of
collection-typed properties. The default value is org.mapstruct.CollectionMappingStrategy.ACCESSOR_ONLY. Other
possible values are SETTER_PREFERRED, ADDER_PREFERRED, and TARGET_IMMUTABLE.The ACCESSOR_ONLY strategy relies
primarily on the presence of a setter method for the target collection property. If
a setter is available, MapStruct will create a new instance of the target collection, copy elements from the source,
and use the setter to assign it. For example:

```java

@Mapper
public interface CarMapper {
    @Mapping(target = "parts", source = "componentList")
    CarDto carToCarDto(Car car);
}
```

If CarDto has a setParts() method, it will be used.

The SETTER_PREFERRED strategy prioritizes the use of a setter. If a setter is present, it behaves like ACCESSOR_ONLY.
If only an adder method (add<PropertyName>(ElementType element)) is available, MapStruct will use the adder to add
each mapped element individually:

```java

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.SETTER_PREFERRED)
public interface EngineMapper {
    @Mapping(target = "cylinders", source = "cylinderSet")
    EngineDto engineToEngineDto(Engine engine);
}
```

If EngineDto has setCylinders(), it's used; otherwise, if addCylinder() exists, it's used for each element.The
ADDER_PREFERRED strategy prioritizes the use of an adder method. If an adder is available, it's used to add elements
individually. If only a setter is present, it behaves like ACCESSOR_ONLY:

```java

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface OrderMapper {
    @Mapping(target = "items", source = "productList")
    OrderDto orderToOrderDto(Order order);
}
```

If OrderDto has addItem(), it's used; otherwise, setItems() is used if available.The TARGET_IMMUTABLE strategy is
designed for mapping to immutable target collections (those without setters or adders). MapStruct will create a new
instance of the target collection and assign it to the target property, often via a constructor parameter:

```java

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE)
public interface ImmutableListMapper {
    @Mapping(target = "values", source = "inputList")
    ImmutableListDto mapList(List<Integer> inputList);
}
```

This strategy is useful for mapping to immutable data transfer objects. This property allows precise control over
collection mapping, accommodating different object models and handling updates and immutable collections
appropriately. The choice of strategy can affect performance and how target collections are modified.
---
</details>
<details id="ms-mappingInheritanceStrategy">
<summary>mappingInheritanceStrategy</summary>
This property, of type org.mapstruct.MappingInheritanceStrategy, defines the strategy for applying method-level
configuration annotations (like @Mapping) from prototype methods in a @MapperConfig interface to methods in the
current mapper. The default value is org.mapstruct.MappingInheritanceStrategy.EXPLICIT. Other possible values are
AUTO_INHERIT_FROM_CONFIG, AUTO_INHERIT_REVERSE_FROM_CONFIG, and AUTO_INHERIT_ALL_FROM_CONFIG.

With the EXPLICIT strategy, inheritance only occurs if the target mapping method is explicitly annotated with
@InheritConfiguration:

```java

@Mapper(config = CentralConfig.class)
public interface SourceTargetMapper {
    Car toCar(CarDto car); // No inheritance

    @InheritConfiguration
    Car toCarWithPrimaryKey(CarDto car); // Inherits configurations from CentralConfig
}
```

The AUTO_INHERIT_FROM_CONFIG strategy automatically inherits configurations if the source and target types are
assignable:

```java

@Mapper(config = CentralConfig.class, mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG)
public interface SourceTargetMapper {
    Car toCar(CarDto car); // Inherits configurations from CentralConfig if types match
}
```

AUTO_INHERIT_REVERSE_FROM_CONFIG automatically inherits the inverse mapping configuration from prototype methods:

```java

@Mapper(config = CentralConfig.class, mappingInheritanceStrategy =
        MappingInheritanceStrategy.AUTO_INHERIT_REVERSE_FROM_CONFIG)
public interface SourceTargetMapper {
    Car toCar(CarDto car); // Inherits the inverse mapping from CentralConfig if types match
}
```

AUTO_INHERIT_ALL_FROM_CONFIG automatically inherits both forward and reverse mapping configurations:

```java

@Mapper(config = CentralConfig.class, mappingInheritanceStrategy =
        MappingInheritanceStrategy.AUTO_INHERIT_ALL_FROM_CONFIG)
public interface SourceTargetMapper {
    Car toCar(CarDto car); // Inherits forward mapping

    CarDto toCarDto(Car car); // Inherits reverse mapping
}
```

This property offers powerful mechanisms for reusing mapping configurations centrally, reducing redundancy and improving
maintainability. The different strategies provide varying levels of automation for inheritance, supporting the DRY
principle in mapping configurations.
---
</details>
<details id="ms-disableSubMappingMethodsGeneration">
<summary>disableSubMappingMethodsGeneration</summary>
This property, of type boolean, when set to true, prevents MapStruct from automatically generating new mapping methods
for nested bean properties if an exact mapping method with matching source and target types is not found. The default
value is false.

By default, if MapStruct encounters a nested property and no explicit mapping method exists, it will try to generate
one. However, if disableSubMappingMethodsGeneration = true:

```java

@Mapper(disableSubMappingMethodsGeneration = true)
public interface UserMapper {
    @Mapping(source = "homeAddress", target = "address")
    UserDTO userToUserDTO(User user);
}
// If no explicit mapping from Address to AddressDTO exists, a compilation error will occur
```

In this case, if there isn't a method in UserMapper that maps Address to AddressDTO, MapStruct will throw a compilation
error instead of trying to generate a sub-mapping method. To resolve this, an explicit mapping method needs to be
provided:

```java

@Mapper(disableSubMappingMethodsGeneration = true)
public interface UserMapper {
    @Mapping(source = "homeAddress", target = "address")
    UserDTO userToUserDTO(User user);

    AddressDTO addressToAddressDTO(Address address);

}
```

Setting this property to true enforces explicit definition of all mappings, including nested objects, improving code
clarity and preventing unexpected implicit mappings. While it might require more manual definition of mapping methods,
it can lead to safer and more transparent mappings.
</details>
</details>

<details>
<summary><strong>Null Handling Policies</strong></summary>

| **Property**                             | **Description**                                                             | **Default Value** |
|------------------------------------------|-----------------------------------------------------------------------------|-------------------|
| `nullValueMappingStrategy`               | Defines how `null` values should be mapped.                                 | `org.mapstruct.NullValueMappingStrategy.RETURN_NULL` |
| `nullValueIterableMappingStrategy`       | Controls `null` handling for iterable collections.                          | `org.mapstruct.NullValueMappingStrategy.RETURN_NULL` |
| `nullValueMapMappingStrategy`            | Defines behavior for `null` values in maps.                                 | `org.mapstruct.NullValueMappingStrategy.RETURN_NULL` |
| `nullValuePropertyMappingStrategy`       | Specifies handling for `null` property values.                              | `org.mapstruct.NullValuePropertyMappingStrategy.SET_TO_DEFAULT` |
| `nullValueCheckStrategy`                 | Determines when to include a `null` check on the source property value.     | `org.mapstruct.NullValueCheckStrategy.ON_IMPLICIT_CONVERSION` |

<details id="nhp-nullValueMappingStrategy">
<summary>nullValueMappingStrategy</summary>
This property, of type org.mapstruct.NullValueMappingStrategy, controls the behavior when the source argument of a
mapping method is null. The default value is org.mapstruct.NullValueMappingStrategy.RETURN_NULL. The other possible
value is org.mapstruct.NullValueMappingStrategy.RETURN_DEFAULT.

The default strategy, RETURN_NULL, means that if the source argument to a mapping method is null, the method will
return null:

```java
@Mapper
public interface CarMapper {
CarDto carToCarDto(Car car);
}
```

// Generated implementation will return null if car is null
If nullValueMappingStrategy is set to RETURN_DEFAULT, and the source argument is null, the mapping method will return
an empty default value. For bean mappings, this means a new instance of the target class with default field values.
For
iterables or arrays, an empty iterable or array will be returned. For maps, an empty map will be returned. For
example:

```java

@Mapper(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface CarMapper {
CarDto carToCarDto(Car car); // Returns an empty CarDto if car is null

List<CarDto> carsToCarDtos(List<Car> cars); // Returns an empty ArrayList if cars is null

Map<String, CarDto> carMapToCarDtoMap(Map<String, Car> carMap); // Returns an empty LinkedHashMap if carMap is null
}
```

This property allows controlling how null source objects are handled, either by returning null or a default empty
value, which can help prevent NullPointerExceptions and ensure consistent behavior. The option to return default
values
aligns with practices for graceful null handling.
</details>
<details id="nhp-nullValueIterableMappingStrategy">
<summary>nullValueIterableMappingStrategy</summary>
This property, of type org.mapstruct.NullValueMappingStrategy, specifically controls how iterable mapping methods (
those annotated with @IterableMapping) handle a null source argument. The default value is
org.mapstruct.NullValueMappingStrategy.RETURN_NULL. The other possible value is
org.mapstruct.NullValueMappingStrategy.RETURN_DEFAULT.

With the default RETURN_NULL strategy, if the source iterable is null, the mapping method will return null:

```java
@Mapper
public interface SourceTargetMapper {
List<String> integersToStrings(List<Integer> integers);
}
// Generated implementation will return null if integers is null
```
If nullValueIterableMappingStrategy is set to RETURN_DEFAULT, a null source iterable will result in the mapping method
returning an empty iterable. The specific type of empty collection depends on the return type (e.g., an empty
ArrayList for List, an empty LinkedHashSet for Set):

```java
@Mapper(nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface SourceTargetMapper {
List<String> integersToStrings(List<Integer> integers); // Returns an empty ArrayList if integers is null
Set<Integer> stringsToIntegers(Set<String> strings); // Returns an empty LinkedHashSet if strings is null
}
```

This property offers more specific control over null source iterables, separate from the general
nullValueMappingStrategy, which is useful when different null handling is required for collections.
</details>
<details id="nhp-nullValueMapMappingStrategy">
<summary>nullValueMapMappingStrategy</summary>
</details>
<details id="nhp-nullValuePropertyMappingStrategy">
<summary>nullValuePropertyMappingStrategy</summary>
This property, of type org.mapstruct.NullValuePropertyMappingStrategy, controls how null source properties are handled
during the mapping of bean properties in update mapping methods (those annotated with @MappingTarget). The default
value is org.mapstruct.NullValuePropertyMappingStrategy.SET_TO_DEFAULT. The other possible value is
org.mapstruct.NullValuePropertyMappingStrategy.IGNORE.

With the default SET_TO_DEFAULT strategy, when the source property is null, the corresponding target property in an
update mapping will be set to its default value (e.g., null for objects, 0 for primitives, false for booleans, "" for
String, empty collections, etc.):

```java
@Mapper
public interface CarMapper {
void updateCarFromDto(CarDto carDto, @MappingTarget Car car);
}
// If carDto.getColor() is null, car.setColor("") will be called
```

If nullValuePropertyMappingStrategy is set to IGNORE, a null source property will leave the target property's original
value unchanged in an update mapping:

```java
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CarMapper {
void updateCarFromDto(CarDto carDto, @MappingTarget Car car);
}
// If carDto.getColor() is null, car.setColor() will not be called, and car.color will retain its original value
```

This property is particularly important for update mappings, allowing control over whether null source values should
reset target properties or leave them as they are. The choice between these strategies directly impacts how existing
target objects are updated with potentially null source values.
</details>
<details id="nhp-nullValueCheckStrategy">
<summary>nullValueCheckStrategy</summary>
This property, of type org.mapstruct.NullValueCheckStrategy, determines when to include a null check on the source
property value of a bean mapping. The default value is org.mapstruct.NullValueCheckStrategy.ON_IMPLICIT_CONVERSION. The
other possible value is org.mapstruct.NullValueCheckStrategy.ALWAYS.

The default ON_IMPLICIT_CONVERSION strategy generates a null check when directly setting a source value to a target
primitive type, or when applying a type conversion followed by setting the target value, or when calling another mapping
method followed by setting the target value. For example:

```java
@Mapper
public interface SourceTargetMapper {
Target map(Source source);
}

public class Source {
private Integer intWrapper;
//...
}
public class Target {
private int intPrimitive;
//...
}
// A null check will be generated for source.getIntWrapper()
```

Setting nullValueCheckStrategy to ALWAYS will include a null check for all non-primitive source properties, unless a
source presence checker method is defined:

```java
@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface SourceTargetMapper {
Target map(Source source);
}

public class Source {
private String stringValue;
private Integer intWrapper;
//...
}

public class Target {
private String stringValue;
private Integer intWrapper;
//...
}
// Null checks will be generated for both source.getStringValue() and source.getIntWrapper()
```


This property allows fine-tuning the null-checking behavior of generated mappers, balancing performance and the need to
handle nullable properties correctly. The ALWAYS option provides a more defensive approach by ensuring null checks even
without explicit type conversion at the property level.
</details>
</details>

<details>
<summary><strong>Dependency Injection</strong></summary>

| **Property**          | **Description**                                                   | **Default Value** |
|----------------------|-----------------------------------------------------------------|-------------------|
| `componentModel`     | Determines the instantiation method for mappers (`spring`, `cdi`, `default`, etc.). | `"default"` |
| `injectionStrategy`  | Determines whether dependencies are injected via fields or constructors. | `org.mapstruct.InjectionStrategy.FIELD` |

<details id="dp-componentModel">
<summary>componentModel</summary>
This property, of type String, specifies the component model that the generated mapper should adhere to, enabling
integration with dependency injection frameworks. The default value is "default". Supported values include "
default", "cdi", "spring", "jsr330", "jakarta", and "jakarta-cdi".
When componentModel is set to "default", the mapper uses no specific component model, and instances are typically
retrieved using the static method Mappers.getMapper(Class):

```java
@Mapper(componentModel = "default")
public interface CarMapper {
    CarMapper INSTANCE = Mappers.getMapper(CarMapper.class);
    CarDto carToCarDto(Car car);
}
```

In this scenario, an instance of CarMapper is obtained via CarMapper.INSTANCE.

Setting componentModel to "cdi" generates a mapper that is an application-scoped CDI (Contexts and Dependency
Injection)
bean, annotated with @javax.enterprise.context.ApplicationScoped or @jakarta.enterprise.context.ApplicationScoped,
and
can be retrieved via @Inject:

```java
@Mapper(componentModel = "cdi")
public interface CarMapper {
    CarDto carToCarDto(Car car);
}
```

The CarMapper can then be injected into other CDI-managed beans using @Inject.

Using "spring" as the componentModel generates a mapper that is a singleton-scoped Spring bean, annotated with
@org.springframework.stereotype.Component and @org.springframework.context.annotation.Scope("singleton"), and can
be
autowired using @Autowired:

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Mapper(componentModel = "spring")
public interface CarMapper {
    CarDto carToCarDto(Car car);
}

@Service
public class CarService {
    @Autowired
    private CarMapper carMapper;
    //...
}
```

Here, CarMapper is autowired into CarService.

The value "jsr330" results in a mapper annotated with @javax.inject.Named or @jakarta.inject.Named and can be
retrieved
via @Inject. This is often used with DI frameworks like Spring that also support JSR 330 annotations:

```java
import javax.inject.Inject;
import javax.inject.Named;

@Mapper(componentModel = "jsr330")
public interface CarMapper {
    CarDto carToCarDto(Car car);
}

public class CarService {
    @Inject
    @Named("carMapper")
    private CarMapper carMapper;
    //...
}
```

An instance of CarMapper can be injected using @Inject and @Named.

Similarly, "jakarta" specifically uses the @jakarta.inject.Named and @jakarta.inject.Inject annotations. "
jakarta-cdi"
generates an application-scoped Jakarta CDI bean, annotated with @jakarta.enterprise.context.ApplicationScoped and
retrievable via @jakarta.inject.Inject. The componentModel property is crucial for seamless integration of
MapStruct
into applications utilizing dependency injection, promoting loose coupling and testability. The support for
various
component models highlights MapStruct's broad applicability across different java and Jakarta EE environments.
</details>
<details id="dp-injectionStrategy">
<summary>injectionStrategy</summary>
This property, of type org.mapstruct.InjectionStrategy, determines whether to use field or constructor injection for
  annotated-based component models like CDI, Spring, and JSR 330. The default value is
  org.mapstruct.InjectionStrategy.FIELD. Other possible values are CONSTRUCTOR and SETTER.

The default FIELD strategy will generate fields annotated with the appropriate injection annotation (e.g., @Autowired
for Spring, @Inject for CDI/JSR 330):

  ```java
  @Mapper(componentModel = "spring", uses = EngineMapper.class, injectionStrategy = InjectionStrategy.FIELD)
  public interface CarMapper {
    CarDto carToCarDto(Car car);
  }
  
  // Generated CarMapperImpl will have:
  // @Autowired
  // private EngineMapper engineMapper;
  
  ```
The CONSTRUCTOR strategy will generate a constructor with the dependencies as parameters:

The SETTER strategy will generate setter methods for the dependencies:

  ```java
  @Mapper(componentModel = "jsr330", uses = EngineMapper.class, injectionStrategy = InjectionStrategy.SETTER)
  public interface CarMapper {
      CarDto carToCarDto(Car car);
  }
  
  // Generated CarMapperImpl will have:
  // @Inject
  // public void setEngineMapper(EngineMapper engineMapper) {... }
  ```

This property allows aligning the dependency injection strategy of the generated mapper with the conventions of the
chosen DI framework. Constructor injection is generally preferred for testability and immutability. The support for
different injection strategies reflects the diverse practices in dependency management within the java ecosystem.
</details>
</details>

<details>
<summary><strong>Miscellaneous Properties</strong></summary>

| **Property**                         | **Description**                                                                | **Default Value** |
|--------------------------------------|------------------------------------------------------------------------------|-------------------|
| `implementationName`                 | Customizes the generated implementation’s name.                              | `<CLASS_NAME>Impl` |
| `implementationPackage`              | Defines the package for the generated mapper implementation.                  | `<PACKAGE_NAME>` |
| `subclassExhaustiveStrategy`         | Defines how to handle missing implementations for superclass mappings.         | `org.mapstruct.SubclassExhaustiveStrategy.RUNTIME_EXCEPTION` |
| `mappingControl`                     | Allows for detailed control over the mapping process.                         | `org.mapstruct.control.MappingControl.class` |
| `unexpectedValueMappingException`    | Specifies the exception thrown if no mapping matches for enums.               | `java.lang.IllegalArgumentException.class` |
| `suppressTimestampInGenerated`       | Indicates whether to suppress timestamps in the `@Generated` annotation.      | `FALSE` |
<details id="mp-implementationName">
<summary>implementationName</summary>
This property, of type String, allows customization of the name of the generated mapper implementation class. The
default value is <CLASS_NAME>Impl, where <CLASS_NAME> is replaced by the name of the mapper interface.

For example, if there is a mapper interface named CarMapper, the default implementation class name would be
CarMapperImpl. However, using the implementationName property, this can be changed:

```java
import org.mapstruct.Mapper;

@Mapper(implementationName = "CarMapperGenerated")
public interface CarMapper {
  // Mapping methods here
}
```

In this case, the generated implementation class will be named CarMapperGenerated instead of CarMapperImpl. This
customization can be useful for adhering to specific project naming conventions or for avoiding potential naming
conflicts. The implementationName property works seamlessly with different component models, and it primarily affects
the naming of the generated class without altering the mapper's functionality.

* **implementationPackage: Defines the package for the generated mapper implementation**
  This property, of type String, specifies the target package for the generated implementation class. The default value
  is <PACKAGE_NAME>, which represents the same package as the mapper interface.

For instance, if the CarMapper interface resides in the package com.example.mappers, by default, the CarMapperImpl
class will also be generated in the same package. However, the implementationPackage property can be used to direct
the
generated class to a different package:

```java
import org.mapstruct.Mapper;

@Mapper(implementationPackage = "com.example.generated")
public interface CarMapper {
  // Mapping methods here
}
```

In this scenario, the CarMapperImpl class will be generated in the com.example.generated package. This feature allows
for better organization of generated code, especially in larger projects where separating generated code into a
distinct package might be desirable for clarity or to align with project structure guidelines.

</details>
<details id="ms-implementationPackage">
<summary>implementationPackage</summary>
This property, of type String, specifies the target package for the generated implementation class. The default value
is <PACKAGE_NAME>, which represents the same package as the mapper interface.

For instance, if the CarMapper interface resides in the package com.example.mappers, by default, the CarMapperImpl
class will also be generated in the same package. However, the implementationPackage property can be used to direct
the
generated class to a different package:

```java
import org.mapstruct.Mapper;

@Mapper(implementationPackage = "com.example.generated")
public interface CarMapper {
    // Mapping methods here
}
```

In this scenario, the CarMapperImpl class will be generated in the com.example.generated package. This feature allows
for better organization of generated code, especially in larger projects where separating generated code into a
distinct package might be desirable for clarity or to align with project structure guidelines.


</details>
<details id="mp-subclassExhaustiveStrategy">
<summary>subclassExhaustiveStrategy</summary>
This property, of type org.mapstruct.SubclassExhaustiveStrategy, determines how to handle missing implementations for
superclasses when using @SubclassMapping. The default value is
org.mapstruct.SubclassExhaustiveStrategy.RUNTIME_EXCEPTION.

When using @SubclassMapping to map between inheritance hierarchies, if a source object's actual type is a subclass for
which no specific @SubclassMapping is defined on the mapper, the subclassExhaustiveStrategy dictates the behavior. With
the default RUNTIME_EXCEPTION, MapStruct will throw an IllegalArgumentException at runtime in such cases. This enforces
that mappings for all relevant subclasses are explicitly defined:

```java
@Mapper(subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION)
public interface FruitMapper {
@SubclassMapping(source = AppleDto.class, target = Apple.class)
@SubclassMapping(source = BananaDto.class, target = Banana.class)
Fruit map(FruitDto source);
}

// If source is a GrapeDto (and no @SubclassMapping for it exists), an IllegalArgumentException 
// will be thrown at runtime
```


This property is crucial for ensuring that all relevant subclasses in an inheritance hierarchy are explicitly mapped,
preventing unexpected behavior by catching missing subclass mappings at runtime. This feature supports mapping complex
inheritance structures exhaustively.
</details>
<details id="ms-mappingControl">
<summary>mappingControl</summary>
This property, of type Class<? extends Annotation>, allows for detailed control over the mapping process by specifying
a mapping control strategy for all mapping methods within the mapper interface. The default value is
org.mapstruct.control.MappingControl.class.

MappingControl is an annotation that can be created to specify an enum that corresponds to the first four options
MapStruct considers when mapping a source attribute to a target attribute: DIRECT, MAPPING_METHOD,
BUILT_IN_CONVERSION, and COMPLEX_MAPPING. The presence of an enum value in the custom MappingControl annotation
enables the corresponding mapping option, while its absence disables it.

For example, to create a mapper that only allows direct mappings:

```java
import org.mapstruct.MappingControl;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.CLASS)
@MappingControl.Use(MappingControl.Use.DIRECT)
public @interface DirectMappingOnly {}

@Mapper(mappingControl = DirectMappingOnly.class)
public interface SimpleMapper {
Target map(Source source);
}
```

In this SimpleMapper, MapStruct will only attempt to directly copy properties of the same type. Similarly, to allow only
direct mappings and built-in conversions:

```java
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.CLASS)
@MappingControl.Use({MappingControl.Use.DIRECT, MappingControl.Use.BUILT_IN_CONVERSION})
public @interface SimpleTypeMapping {}

@Mapper(mappingControl = SimpleTypeMapping.class)
public interface BasicTypeMapper {
Target map(Source source);
}
```

MapStruct also provides a predefined meta-annotation @DeepClone that only allows direct mappings. The mappingControl
property offers a high degree of customization over the mapping process, allowing enforcement of specific mapping
strategies or optimization by limiting mapping resolutions.
</details>
<details id="mp-unexpectedValueMappingException">
<summary>unexpectedValueMappingException</summary>
unexpectedValueMappingException: This property, of type Class<? extends Exception>, specifies the exception that
should be thrown by the generated code if no mapping matches for enums when using @ValueMapping. The default value is
java.lang.IllegalArgumentException.class.

By default, if a source enum constant is not explicitly mapped using @ValueMapping, MapStruct throws an
IllegalArgumentException. This property allows specifying a custom exception class instead:

```java
public class UnknownOrderTypeException extends RuntimeException {
public UnknownOrderTypeException(String message) {
  super(message);
  }
}

@Mapper(unexpectedValueMappingException = UnknownOrderTypeException.class)
public interface OrderMapper {
@ValueMappings({
@ValueMapping(target = "NORMAL", source = "STANDARD"),
@ValueMapping(target = "VIP", source = "SPECIAL")
})
ExternalOrderType orderTypeToExternalOrderType(OrderType orderType);
}
// If orderType is PRIORITY (and not mapped), an UnknownOrderTypeException will be thrown
```

This allows for more specific error handling for unmapped enum values, enabling the throwing of custom exceptions that
are more meaningful within the application's context. This customization improves the robustness and maintainability of
the application by providing more informative error messages.
</details>
<details id="ms-suppressTimestampInGenerated">
<summary>suppressTimestampInGenerated</summary>
This property, of type boolean, indicates whether the addition of a timestamp in the @Generated annotation of the
generated mapper implementation should be suppressed. The default value is false.

When set to true, MapStruct will not include a timestamp in the @Generated annotation:

```java

@Mapper(suppressTimestampInGenerated = true)
public interface CarMapper {
  CarDto carToCarDto(Car car);
}

// The @Generated annotation in CarMapperImpl will not contain the timestamp
```

Suppressing the timestamp can be useful for version control systems as it reduces unnecessary changes in the generated
code between builds. This property primarily manages the metadata of the generated code and does not affect the mapping
logic itself.
</details>
</details>

---

## Interaction with Other MapStruct Annotations

The properties of the @Mapper annotation significantly influence the behavior of other MapStruct annotations. For
instance, the unmappedTargetPolicy set at the @Mapper level acts as a default for all mapping methods within that
mapper, but this can be overridden for specific mapping methods using the ignoreUnmappedTargetProperties property of
the @org.mapstruct.BeanMapping annotation. Similarly, the componentModel specified in @Mapper determines how MapStruct
resolves and utilizes other mappers referenced in @org.mapstruct.Mapping annotations through attributes like uses,
qualifiedBy, or qualifiedByName. These interactions demonstrate the hierarchical nature of MapStruct's configuration,
where mapper-level settings provide defaults that can be tailored at more granular levels.
---

## Using @Mapper with @MapperConfig

The @org.mapstruct.MapperConfig annotation serves as a central template for defining shared mapping configurations.
Theconfig property of the @Mapper annotation allows a mapper to reference a @MapperConfig interface, inheriting its
settings. Properties defined directly within the @Mapper annotation take precedence over those specified in the
referenced @MapperConfig. For list properties like uses, the values from both the @Mapper annotation and the
@MapperConfig are combined. This mechanism promotes the reuse of common configurations across multiple mappers,
enhancing maintainability and consistency.
---

## Advanced Scenarios and Best Practices

Complex mapping requirements can often be addressed by strategically combining various @Mapper properties. For example,
in a project utilizing Spring for dependency injection and having immutable target objects built with a builder pattern,
a mapper might be annotated with @Mapper(componentModel = "spring", builder = @Builder(disableBuilder = false),
unmappedTargetPolicy = ReportingPolicy.ERROR). This configuration ensures that the generated mapper is a Spring-managed
bean, attempts to use builders for creating target objects, and reports an error if any target property is left
unmapped.

Choosing appropriate values for @Mapper properties should be guided by the specific needs of the project. For instance,
setting unmappedSourcePolicy and unmappedTargetPolicy to ERROR promotes strict mapping and helps prevent data loss or
incomplete target objects, but might require more explicit mapping configurations. The componentModel should be chosen
based on the dependency injection framework in use. Handling of null values via nullValueMappingStrategy and related
properties should align with the application's requirements for null safety and default value handling. Performance
considerations might influence the choice of nullValueCheckStrategy. Additionally, MapStruct provides processor options
that can be used for global configuration, which can be overridden by settings at the @Mapper level.
---

## Conclusion

The @Mapper annotation in MapStruct offers a comprehensive suite of properties that provide granular control over
various aspects of the code generation and mapping behavior. These properties range from basic settings like specifying
other mappers to be used and handling unmapped properties, to more advanced configurations such as integrating with
dependency injection frameworks, customizing the generated code, controlling collection mapping strategies, and managing
null value handling. A thorough understanding of these properties is essential for effectively leveraging MapStruct's
capabilities in diverse application contexts and for tailoring the mapping process to meet specific project
requirements. By utilizing these properties judiciously, developers can create robust, efficient, and maintainable data
mapping solutions.
---