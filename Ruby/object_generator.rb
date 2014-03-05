require 'fileutils'
require 'json'
require 'test/unit'
include Test::Unit::Assertions

class Session
	def html()
		<<-HTML
		<html>
		<head>
			<!--POGEN,__pogen_0,title,{$title|escapeUri}--><title class="__pogen_0">{$title|escapeUri}</title>
		</head>
		<body>
			<!--POGEN,__pogen_1,value,{$value}--><p id="test" class="__pogen_1">{$value}</p>
			<!--POGEN,__pogen_2,value2,{$value2}--><p class="__pogen_2 test2">{$value2}</p>
			<!--POGEN,__pogen_3,value,{$value}--><p id="test" class="__pogen_3">value: {$value}</p>
			<!--POGEN,__pogen_4,value3,{$value3}--><p lang="test3" class="__pogen_4">{$value3}</p>
			{foreach}<!--POGEN,__pogen_5,value4,{$value4}--><p class="__pogen_5">{$value4}</p>{/foreach}
			test
		</body>
		</html>
		HTML
	end
end

class OracleGenerator
	def initialize()
		@last_name = nil
		@index = 0
		@expected_value_store = {}
		@actual_value_store = {}
		@oracle_path_for_checking = nil
		@oracle_path_for_saving = nil
		@checking_oracles = false
		@saving_oracles = false
		@setted_at_exit = false
	end

	def enable_checking(oracle_path)
		if @oracle_path_for_checking == nil || File.expand_path(@oracle_path_for_checking) != File.expand_path(oracle_path)
			@expected_value_store = open(oracle_path, "r") { |f| JSON.load(f) }
			@oracle_path_for_checking = oracle_path
		end
		@checking_oracles = true
	end

	def disable_checking()
		@checking_oracles = false
	end

	def enable_saving(oracle_path)
		if @oracle_path_for_saving == nil || File.expand_path(@oracle_path_for_saving) != File.expand_path(oracle_path)
			save() if @setted_at_exit
			@oracle_path_for_saving = oracle_path
			@last_name = nil
			@index = 0
		end
		@saving_oracles = true
		if !@setted_at_exit
			at_exit { save() }
			@setted_at_exit = true
		end
	end

	def disable_saving()
		@saving_oracles = false
	end

	def save()
		parent_path = File.expand_path("..", @oracle_path_for_saving)
		FileUtils.mkdir_p parent_path if parent_path != nil
		expected_json_str = JSON.generate(@expected_value_store)
		actual_json_str = JSON.generate(@actual_value_store)
		if expected_json_str != actual_json_str
			open(@oracle_path_for_saving, "w") { |f| f.write actual_json_str }
		end
	end

	def verify_and_save(session, klass, test_case_name)
		name = klass.name + "/" + test_case_name
		if name != @last_name
			@last_name = name
			@index = 0
		else
			@index += 1
		end

		return if !@checking_oracles && !@saving_oracles

		oracles = extract_oracles(session)

		if @checking_oracles
			assert_equal(true, @expected_value_store.key?(@last_name))
			expected_value_sets = @expected_value_store[@last_name]
			assert_equal(true, @index < @expected_value_store.size)
			expected_values = expected_value_sets[@index]
			assert_equal(expected_values.size, oracles.size)
			for kv in expected_values do
				assert_equal(kv[1], oracles[kv[0]])
			end
		end

		if @saving_oracles
			actual_value_sets = @actual_value_store[@last_name]
			if actual_value_sets == nil
				actual_value_sets = []
				@actual_value_store[@last_name] = actual_value_sets
			end
			actual_value_sets << oracles
		end
	end

	def extract_oracles(session)
		map = {}
		regex = /<!--POGEN,([^,]*),([^,]*),(.*?)-->/m
		for arr in session.html.scan(regex) do
			map[arr[1]] = arr[2]
		end
		map
	end
end

gen = OracleGenerator.new()
gen.enable_saving("test.json")
gen.verify_and_save(Session.new(), Object, "test")
gen.save()
gen.enable_checking("test.json")
gen.enable_saving("test2.json")
gen.verify_and_save(Session.new(), Object, "test")
