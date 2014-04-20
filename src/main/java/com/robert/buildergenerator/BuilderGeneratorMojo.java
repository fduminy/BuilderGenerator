package com.robert.buildergenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.stringtemplate.v4.ST;

import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;

/**
 * @goal generate-sources
 * @phase generate-sources
 */
public class BuilderGeneratorMojo extends AbstractCodeGeneratorMojo {

	@Override
	public void generate() throws Exception {
		for (final JavaClass jc : docBuilder.getClasses()) {
			final String builderSource = generateBuilderFor(jc);
			if (builderSource != null) {
				writeBuilder(jc, builderSource);
			}
		}
	}

	private void writeBuilder(final JavaClass jc, final String builder) throws IOException {
		final File builderFile = new File(outputDirectory, jc.getPackageName().replace(".", "/") + "/"
				+ getBuilderName(jc) + ".java");
		FileUtils.writeStringToFile(builderFile, builder);
	}

	public String generateBuilderFor(final JavaClass jc) throws IOException {
		final DocletTag tag = jc.getTagByName("generatebuilder");
		if (tag != null && classHasEmptyConstructor(jc)) {
			final String builderName = getBuilderName(jc);

			final ST st = templates.getInstanceOf("builder");
			st.add("packageName", jc.getPackageName());
			st.add("builderName", builderName);
			st.add("resultClass", jc.getName());
			st.add("builderNameCapitalized", capitalize(builderName));
			st.add("attributes", getAttributes(jc));

			return st.render();
		}
		return null;
	}

	private List<Attribute> getAttributes(final JavaClass jc) {
		final ArrayList<Attribute> attrs = new ArrayList<>();

		for (final JavaMethod method : jc.getMethods()) {
			if (method.getName().startsWith("set") && method.getParameters().length == 1) {
				final JavaParameter parameter = method.getParameters()[0];
				final String name = method.getName().substring(3);

				final Attribute attr = new Attribute();
				attr.setName(uncapitalize(name));
				attr.setNameCapitalized(name);
				attr.setType(parameter.getType().getFullyQualifiedName());
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
