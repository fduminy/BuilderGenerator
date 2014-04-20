package com.robert.buildergenerator;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.thoughtworks.qdox.JavaDocBuilder;

public abstract class AbstractCodeGeneratorMojo extends AbstractMojo {

	/**
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 * @since 1.0
	 */
	MavenProject project;

	/**
	 * Sources
	 * 
	 * @parameter
	 * @required
	 */
	List<String> sources;

	/**
	 * @parameter default-value="target/generated-sources/buildergenerator"
	 * @required
	 */
	File outputDirectory;

	STGroup templates;
	JavaDocBuilder docBuilder;

	@Override
	public void execute() {
		try {
			this.templates = new STGroupFile("template.stg");

			this.docBuilder = new JavaDocBuilder();
			for (final String r : sources) {
				docBuilder.addSourceTree(new File(r));
			}

			generate();

			project.addCompileSourceRoot(outputDirectory.getAbsolutePath());

		} catch (final Exception e) {
			getLog().error("General error", e);
		}
	}

	protected abstract void generate() throws Exception;

}
