BuilderGenerator
================

Automatically generate Builder classes that allow chaining instead of calling every single setter by itself, all through the magic of Maven.

MyDto dto = new MyDto();
dto.setA("a");
dto.setB(2);

MyOtherDto dto2 = new MyOtherDto();
dto2.setSomething(new Object());

dto.setDto(dto2);

list.add(dto);

becomes

list.add(
  MyDtoBuilder.aMyDtoBuilder()
    .withA("a")
    .withB(2)
    .withDto(
      MyOtherDtoBuilder.aMyOtherDtoBuilder()
      .withSomething(new Object())
      .build()
    )
    .build();
);
