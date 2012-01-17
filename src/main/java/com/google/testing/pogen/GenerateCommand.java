// Copyright 2011 The PageObjectGenerator Authors.
// Copyright 2011 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS-IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.testing.pogen;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.testing.pogen.generator.template.TemplateUpdater;
import com.google.testing.pogen.generator.test.PageObjectUpdateException;
import com.google.testing.pogen.generator.test.java.NameConverter;
import com.google.testing.pogen.generator.test.java.TestCodeGenerator;
import com.google.testing.pogen.parser.template.TemplateInfo;
import com.google.testing.pogen.parser.template.TemplateParseException;
import com.google.testing.pogen.parser.template.TemplateParser;
import com.google.testing.pogen.parser.template.soy.SoyParser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * A class which represents the generate command to generate modified templates
 * and skeleton test code.
 *
 * @author Kazunori Sakamoto
 */
public class GenerateCommand extends Command {

  /**
   * A path string pointing the {@code AbstractPage} class.
   */
  private static final String ABSTRACT_PAGE_PATH =
      "com/google/testing/pogen/generator/test/java/page/AbstractPage.java";
  /**
   * A package name of the {@code AbstractPage} class.
   */
  private static final String ABSTRACT_PAGE_PACKAGE =
      "com.google.testing.pogen.generator.test.java.page";

  /**
   * Template paths to be parsed.
   */
  private final String[] templatePaths;
  /**
   * A output directory path of test codes.
   */
  private final String testOutDirPath;
  /**
   * A package name to generate skeleton test codes.
   */
  private final String packageName;
  /**
   * A boolean whether prints processed files verbosely.
   */
  private final boolean verbose;

  /**
   * Constructs an instance with the specified output-directory path, the
   * specified package name and the specified template paths.
   *
   * @param templatePaths the template paths to be parsed
   * @param testOutDirPath the output directory path of test codes
   * @param packageName the package name to generate skeleton test codes
   * @param verbose the boolean whether prints processed files verbosely
   */
  public GenerateCommand(String[] templatePaths, String testOutDirPath, String packageName,
      boolean verbose) {
    this.templatePaths = Arrays.copyOf(templatePaths, templatePaths.length);
    this.testOutDirPath = testOutDirPath;
    this.packageName = packageName;
    this.verbose = verbose;
  }

  @Override
  public String getHelpMessage() {
    return "java PageObjectGenerator generate -o <test_out_dir> -p <package_name>"
        + " [OPTIONS] <template_file1> <template_file2> ...";
  }

  @Override
  public void execute() throws IOException {
    File testOutDir = createFileFromDirectoryPath(testOutDirPath, false, true);
    // Generate the AbstractPage class
    File newAbstractPageFile = new File(testOutDir.getPath(), "AbstractPage.java");
    if (!newAbstractPageFile.exists()) {
      URL abstractPageUrl = Resources.getResource(ABSTRACT_PAGE_PATH);
      String abstractPage = Resources.toString(abstractPageUrl, Charset.defaultCharset());
      abstractPage = abstractPage.replaceAll(ABSTRACT_PAGE_PACKAGE, packageName);
      Files.write(abstractPage, newAbstractPageFile, Charset.defaultCharset());
    } else if (verbose) {
      System.err.println("Already exists: " + newAbstractPageFile.getAbsolutePath() + ".");
    }
    TemplateParser templateParser = new SoyParser();

    for (String templatePath : templatePaths) {
      File file = createFileFromFilePath(templatePath, true, true);
      try {
        parseAndGenerate(file, testOutDir, templateParser);
      } catch (TemplateParseException e) {
        throw new FileProcessException("Errors occur in parsing the specified files", file, e);
      } catch (PageObjectUpdateException e) {
        throw new FileProcessException("Errors occur in updating the specified files", file, e);
      }
    }
  }

  /**
   * Parses the specified template file and generates a modified template file
   * and skeleton test code.
   *
   * @param templateFile the template file to be modified
   * @param codeOutDir the output directory of skeleton test code
   * @param parser the parser to parse template files
   * @throws IOException if errors occur in reading and writing files
   * @throws TemplateParseException if the specified template is in bad format
   * @throws PageObjectUpdateException if the existing test code doesn't have
   *         generated code
   */
  private void parseAndGenerate(File templateFile, File codeOutDir, TemplateParser parser)
      throws IOException, TemplateParseException, PageObjectUpdateException {
    Preconditions.checkNotNull(templateFile);
    Preconditions.checkNotNull(codeOutDir);
    Preconditions.checkArgument(!Strings.isNullOrEmpty(packageName));
    Preconditions.checkNotNull(parser);

    if (verbose) {
      System.out.println(templateFile.getAbsolutePath() + " ... ");
    }
    File orgTemplateFile = backupFile(templateFile);
    String pageName = NameConverter.getJavaClassName(getFileNameWithoutExtension(templateFile));
    // Read template file
    String template = Files.toString(orgTemplateFile, Charset.defaultCharset());
    // Parse template extracting template variables
    TemplateInfo templateInfo = parser.parse(template);
    if (verbose) {
      System.out.print(".");
    }
    // Generate modified template
    String modifiedTemplate = new TemplateUpdater().generate(templateInfo);
    if (verbose) {
      System.out.print(".");
    }
    // Generate skeleton test code
    TestCodeGenerator pogen = new TestCodeGenerator();
    File codeFile = new File(codeOutDir.getPath(), pageName + "Page.java");
    if (codeFile.exists() && !codeFile.canWrite()) {
      throw new FileProcessException("No permission for writing the specified file", codeFile);
    }

    // @formatter:off
    String testCode = codeFile.exists()
        ? pogen.update(templateInfo, Files.toString(codeFile, Charset.defaultCharset()))
        : pogen.generate(templateInfo, packageName, pageName);
    // @formatter:on
    if (verbose) {
      System.out.print(".");
    }
    // Write generated template and skeleton test code
    Files.write(modifiedTemplate, templateFile, Charset.defaultCharset());
    if (verbose) {
      System.out.print(".");
    }
    Files.write(testCode, codeFile, Charset.defaultCharset());
    if (verbose) {
      System.out.println("\n" + templateFile.getAbsolutePath() + " processed successfully");
    }
  }

  /**
   * Backups the specified file. If the backup file has existed, does nothing.
   *
   * @param file the file to be backuped
   * @return newly backuped file whose name is xxxxx.org if the backup file
   *         doesn't exist, otherwise existing backup file
   * @throws IOException if errors occur in backuping files
   */
  private File backupFile(File file) throws IOException {
    Preconditions.checkNotNull(file);

    File orgHtmlFile = new File(file.getPath() + ".org");
    if (!orgHtmlFile.exists()) {
      Files.copy(file, orgHtmlFile);
    }
    return orgHtmlFile;
  }

  /**
   * Gets the file name without the extension of the specified file.
   *
   * @param file file to get the name
   * @return the file name without the extension
   */
  private String getFileNameWithoutExtension(File file) {
    Preconditions.checkNotNull(file);

    String fileName = file.getName();
    int endIndex = fileName.lastIndexOf('.');
    if (endIndex > 0) {
      return fileName.substring(0, endIndex);
    }
    return fileName;
  }
}