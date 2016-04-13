package com.robert.buildergenerator;

import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import org.apache.commons.io.FileUtils;
import org.stringtemplate.v4.ST;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @goal generate-sources
 * @phase generate-sources
 */
public class BuilderGeneratorMojo extends AbstractCodeGeneratorMojo {

	public static final String SUB_PACKAGE = ".builder";

	@Override
	public void generate() throws Exception {
		final Map<String, JavaClass> foundClasses = new HashMap<String, JavaClass>();
		final List<String> configuredClasses = new ArrayList<String>();

		final Queue<JavaClass> classesToParse = new LinkedBlockingQueue<JavaClass>();
		classesToParse.addAll(Arrays.asList(docBuilder.getClasses()));
		JavaClass jc;
		while ((jc = classesToParse.poll()) != null) {
			classesToParse.addAll(Arrays.asList(jc.getNestedClasses()));

			if (shouldGenerateBuilder(jc)) {
				generateBuilderFor(jc);
			}
			configuredClasses.addAll(readConfiguredClasses(jc));

			foundClasses.put(jc.asType().getGenericValue(), jc);
		}

		for (final String clazz : configuredClasses) {
			final JavaClass javaClass = foundClasses.get(clazz);
			if (javaClass != null) {
				generateBuilderFor(javaClass);
			} else {
				System.out.println("Couldn't find class " + clazz);
			}
		}
	}

	private List<String> readConfiguredClasses(final JavaClass jc) {
		final DocletTag tag = jc.getTagByName("generatebuildersfor");

		if (tag != null) {
			return Arrays.asList(tag.getParameters());
		}
		return Collections.emptyList();
	}

	private boolean shouldGenerateBuilder(final JavaClass jc) {
		final DocletTag tag = jc.getTagByName("generatebuilder");
		if (/*tag != null && */classHasEmptyConstructor(jc)) {
			System.out.println("shouldGenerateBuilder(" + jc.getFullyQualifiedName() + ") -> " + true);
			return true;
		} else {
			return false;
		}
	}

	private void writeBuilder(final JavaClass jc, final String builder) throws IOException {
		String builderPackage = jc.getPackageName() + SUB_PACKAGE;
		final File builderFile = new File(outputDirectory, builderPackage.replace(".", "/") + "/"
				+ getBuilderName(jc) + ".java");
		FileUtils.writeStringToFile(builderFile, builder);
	}

	public void generateBuilderFor(final JavaClass jc) throws IOException {
		final String builderName = getBuilderName(jc);

		final ST st = templates.getInstanceOf("builder");
		st.add("packageName", jc.getPackageName() + SUB_PACKAGE);
		st.add("builderName", builderName);
		st.add("resultClass", jc.asType().getGenericValue());
		st.add("builderNameCapitalized", capitalize(builderName));
		st.add("attributes", getAttributes(jc));
		st.add("listAttributes", getListAttributes(jc));

		writeBuilder(jc, st.render());
	}

	private List<Attribute> getListAttributes(final JavaClass jc) {
		final ArrayList<Attribute> attrs = new ArrayList<Attribute>();

		for (final JavaMethod method : jc.getMethods()) {
			if (method.getName().startsWith("get") && method.getParameters().length == 0 && returnsGenericList(method)) {
				final String name = method.getName().substring(3);

				final Attribute attr = new Attribute();
				attr.setName(uncapitalize(name));
				attr.setNameCapitalized(name);
				attr.setType(method.getReturnType().getActualTypeArguments()[0].getValue());
				attrs.add(attr);
			}
		}

		return attrs;
	}

	private boolean returnsGenericList(final JavaMethod method) {
		return method.getReturnType().getValue().equals("java.util.List")
				&& method.getReturnType().getActualTypeArguments().length == 1;
	}

	private List<Attribute> getAttributes(final JavaClass jc) {
		final ArrayList<Attribute> attrs = new ArrayList<Attribute>();

		for (final JavaMethod method : jc.getMethods()) {
			if (method.getName().startsWith("set") && method.getParameters().length == 1) {
				final JavaParameter parameter = method.getParameters()[0];
				final String name = method.getName().substring(3);

				final Attribute attr = new Attribute();
				attr.setName(uncapitalize(name));
				attr.setNameCapitalized(name);
				attr.setType(parameter.getType().getGenericValue());
				attrs.add(attr);
			}
		}

		return attrs;
	}

	private String capitalize(final String builderName) {
		return builderName.substring(0, 1).toUpperCase() + builderName.substring(1);
	}

	private String uncapitalize(final String builderName) {
		return builderName.substring(0, 1).toLowerCase() + builderName.substring(1);
	}

	private String getBuilderName(final JavaClass jc) {
		return jc.getName() + "Builder";
	}

	private boolean classHasEmptyConstructor(final JavaClass jc) {
		boolean foundConstructor = false;

		for (final JavaMethod method : jc.getMethods()) {
			if (method.isConstructor()) {
				if (method.getParameters().length == 0) {
					return true;
				}
				foundConstructor = true;
			}
		}
		return !foundConstructor;
	}

	public static class Attribute {
		private String type;
		private String name;
		private String nameCapitalized;

		public Attribute() {}

		public Attribute(final String type, final String name, final String nameCapitalized) {
			this.type = type;
			this.name = name;
			this.nameCapitalized = nameCapitalized;
		}

		public String getName() {
			return name;
		}

		public String getNameCapitalized() {
			return nameCapitalized;
		}

		public String getType() {
			return type;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public void setNameCapitalized(final String nameCapitalized) {
			this.nameCapitalized = nameCapitalized;
		}

		public void setType(final String type) {
			this.type = type;
		}

	}
}
