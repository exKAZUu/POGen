package com.google.testing.pogen.pages

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.HashMap
import java.util.List
import java.util.Map
import org.openqa.selenium.WebDriver

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*
import com.google.common.base.Objects

class MapTypeReference extends TypeReference<Map<String, List<Map<String, String>>>> {
}

class OracleGenerator {
	public static val instance = new OracleGenerator()

	private new() {
	}

	var String lastName
	var int index
	var Map<String, List<Map<String, String>>> expectedValueStore = new HashMap<String, List<Map<String, String>>>()
	var Map<String, List<Map<String, String>>> actualValueStore = new HashMap<String, List<Map<String, String>>>()

	var File oracleFileForChecking
	var File oracleFileForSaving
	var Thread threadForSaving

	var checkingOracles = false
	var savingOracles = false

	def enableCheckingOracles(File oracleFile) {
		if (oracleFileForChecking == null || oracleFileForChecking.absolutePath != oracleFile.absolutePath) {
			val mapper = new ObjectMapper()
			expectedValueStore = mapper.readValue(oracleFile, new MapTypeReference())
			oracleFileForChecking = oracleFile
		}
		checkingOracles = true
	}

	def disableCheckingOracles() {
		checkingOracles = false
	}

	def saveOracles() {
		val parentPath = oracleFileForSaving.parent
		if (parentPath != null) {
			new File(parentPath).mkdirs
		}
		val mapperForSaving = new ObjectMapper()
		if (!Objects.equal(actualValueStore, expectedValueStore)) {
			mapperForSaving.writeValue(oracleFileForSaving, actualValueStore);
		}
	}

	def enableSavingOracles(File oracleFile) {
		if (oracleFileForSaving == null || oracleFileForSaving.absolutePath != oracleFile.absolutePath) {
			if (threadForSaving != null) {
				saveOracles()
			}
			lastName = null
			index = 0
		}
		oracleFileForSaving = oracleFile
		savingOracles = true
		if (threadForSaving == null) {
			threadForSaving = new Thread([|saveOracles()])
			Runtime.runtime.addShutdownHook(threadForSaving)
		}
	}

	def enableSavingOracles() {
		val cal = Calendar.getInstance();
		val sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		enableSavingOracles(new File("oracles" + File.separatorChar + sdf.format(cal.time) + ".json"))
	}

	def disableSavingOracles() {
		savingOracles = false
	}

	def saveAndVerify(WebDriver driver, Class<?> testClass, String testCaseName) {
		val name = testClass.name + "/" + testCaseName
		if (name != lastName) {
			lastName = name
			index = 0
		} else {
			index = index + 1
		}

		if (!checkingOracles && !savingOracles)
			return;

		val currentVariableFragments = VariableAnalyzer.getVariableTexts(driver)

		if (checkingOracles) {
			assertThat(expectedValueStore.keySet, contains(lastName))
			var expectedValueSets = expectedValueStore.get(lastName)
			assertThat(expectedValueSets.size(), greaterThan(index))
			val expectedValues = expectedValueSets.get(index)
			assertThat(currentVariableFragments.size(), is(expectedValues.size()))
			for (kv : expectedValues.entrySet()) {
				assertThat(currentVariableFragments.get(kv.key), is(kv.value))
			}
		}

		if (savingOracles) {
			var actualValueSets = actualValueStore.get(lastName)
			if (actualValueSets == null) {
				actualValueSets = new ArrayList<Map<String, String>>();
				actualValueStore.put(lastName, actualValueSets)
			}
			actualValueSets.add(currentVariableFragments)
		}
	}

	def saveAndVerify(WebDriver driver, Method method) {
		saveAndVerify(driver, method.declaringClass, method.name)
	}
}
