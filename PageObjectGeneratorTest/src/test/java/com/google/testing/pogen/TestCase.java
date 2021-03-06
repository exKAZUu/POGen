package com.google.testing.pogen;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.google.testing.pogen.pages.IndexPage;

public class TestCase {
  private FirefoxDriver driver;

  @Before
  public void before() {
    driver = new FirefoxDriver();
  }

  @After
  public void after() {
    driver.close();
  }

  @Test
  public void test() {
    File file = new File("src/main/resources/index.html");
    driver.get("file:///" + file.getAbsolutePath().replace('\\', '/'));
    IndexPage page = new IndexPage(driver);
    assertEquals("{$title|escapeUri}", page.getTextOfTitle());
    assertEquals("{$value}", page.getTextOfValue());
    assertEquals("{$value2}", page.getTextOfValue2());
    assertEquals("{$value3}", page.getTextOfValue3());
    assertEquals(1, page.getElementsOfValue4().size());
    assertEquals("{$value4}", page.getTextsOfValue4().get(0));
  }
}
