package com.robert.buildergenerator.test;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.Type;

public class TestGenerics {

	@Test
	public void shouldParseGenericsType() throws Exception {
		final JavaDocBuilder docBuilder = new JavaDocBuilder();
		docBuilder.addSource(new File("src/test/java/com/robert/buildergenerator/test/TestDto.java"));

		assertEquals(1, docBuilder.getClasses().length);

		final JavaClass jc = docBuilder.getClasses()[0];

		assertEquals("TestDto", jc.getName());

		final JavaMethod[] methods = jc.getMethods();
		assertEquals(1, methods.length);

		final JavaMethod method = jc.getMethods()[0];
		assertEquals("getStrings", method.getName());

		final Type returnType = method.getReturnType();
		assertEquals("java.util.List", returnType.getValue());

		final Type[] actualTypeArguments = returnType.getActualTypeArguments();
		assertEquals("java.lang.String", actualTypeArguments[0].getValue());
	}
}
