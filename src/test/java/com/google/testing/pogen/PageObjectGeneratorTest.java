// Copyright 2011 The PageObjectGenerator Authors.
// Copyright 2011 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS-IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.testing.pogen;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.testing.pogen.parser.template.TemplateParseException;

/**
 * Tests for {@link PageObjectGenerator}.
 * 
 * @author Kazunori Sakamoto
 */
@RunWith(JUnit4.class)
public class PageObjectGeneratorTest {
  @Test
  public void test() throws TemplateParseException {
    File rootDir = new File("PageObjectGeneratorTest/src/main/resources/");
    File testDir = new File("PageObjectGeneratorTest/src/test/java/com/google/testing/pogen/pages");

    // @formatter:off
    String[] args = new String[] {
        "generate",
        "-a",
        "class",
        "-o",
        testDir.getAbsolutePath(),
        "-p",
        "com.google.testing.pogen.pages",
        "-i",
        rootDir.getAbsolutePath(),
        "-e",
        "(.*)\\.html",
        "-v",
    };
    // @formatter:on
    PageObjectGenerator.main(args);
  }
}
