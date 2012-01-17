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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A class which represents a command to be executed in the main class.
 *
 * @author Kazunori Sakamoto
 */
public abstract class Command {

  /**
   * Returns a message to print as help.
   *
   * @return the message to print as help
   */
  public abstract String getHelpMessage();

  /**
   * Executes this command.
   *
   * @throws IOException if errors occur in processing file
   */
  public abstract void execute() throws IOException;

  /**
   * Creates a {@link File} instance for the file specified by the path.
   *
   * @param filePath the path pointing to the existing file whose {@link File}
   *        instance we want to create
   * @param checkCanRead {@code true} if checks the read permission
   * @param checkCanWrite {@code true} if checks the write permission
   * @return the {@link File} instance for the file
   * @throws FileNotFoundException if the file does not exist
   * @throws FileProcessException if errors occur in processing the file
   */
  public File createFileFromFilePath(String filePath, boolean checkCanRead, boolean checkCanWrite)
      throws FileNotFoundException, FileProcessException {
    File file = new File(filePath);
    checkExistenceAndPermission(file, checkCanRead, checkCanWrite);
    if (!file.isFile()) {
      throw new FileProcessException("The specified path is not a file", file);
    }
    return file;
  }

  /**
   * Creates a {@link File} instance for the directory specified by the path.
   *
   * @param directoryPath the path pointing to the existing directory whose
   *        {@link File} instance we want to create
   * @param checkCanRead {@code true} if checks the read permission
   * @param checkCanWrite {@code true} if checks the write permission
   * @return the {@link File} instance for the directory
   * @throws FileNotFoundException if the file does not exist
   * @throws FileProcessException if errors occur in processing the directory
   */
  public File createFileFromDirectoryPath(String directoryPath, boolean checkCanRead,
      boolean checkCanWrite) throws FileNotFoundException, FileProcessException {
    File dir = new File(directoryPath);
    checkExistenceAndPermission(dir, checkCanRead, checkCanWrite);
    if (!dir.isDirectory()) {
      throw new FileProcessException("The specified path is not a directory", dir);
    }
    return dir;
  }

  /**
   * Checks an existence and permission of the specified {@link File} instance.
   *
   * @param file the file to be checked
   * @param checkCanRead {@code true} if checks the read permission
   * @param checkCanWrite {@code true} if checks the write permission
   * @throws FileNotFoundException if the file does not exist
   * @throws FileProcessException if errors occur in processing the directory
   */
  private void checkExistenceAndPermission(File file, boolean checkCanRead, boolean checkCanWrite)
      throws FileNotFoundException, FileProcessException {
    if (!file.exists()) {
      // @formatter:off
      throw new FileNotFoundException("The specified path does not exist:\n"
          + file.getPath() + ".");
      // @formatter:on
    }
    if (checkCanRead && !file.canRead()) {
      throw new FileProcessException("No permission for reading the specified "
          + (file.isFile() ? "file" : "directory"), file);
    }
    if (checkCanWrite && !file.canWrite()) {
      throw new FileProcessException("No permission for writing the specified "
          + (file.isFile() ? "file" : "directory"), file);
    }
  }
}