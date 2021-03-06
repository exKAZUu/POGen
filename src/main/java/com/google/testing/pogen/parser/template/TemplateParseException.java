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

package com.google.testing.pogen.parser.template;

/**
 * This class encapsulates a template-parsing-related error condition that occurred while parsing a
 * template.
 * 
 * @author Kazunori Sakamoto
 */
public class TemplateParseException extends Exception {

  private static final long serialVersionUID = 215938637302984908L;

  /**
   * Construct a template-parsing exception with the specified detail message.
   * 
   * @param message the string to describe the detail message
   */
  public TemplateParseException(String message) {
    super(message);
  }

  /**
   * Construct a template-parsing exception chaining the supplied {@link Exception}.
   * 
   * @param exception the supplied {@link Exception}
   */
  public TemplateParseException(Exception exception) {
    super(exception);
  }
}
