package com.redislabs.riot.gen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.batch.item.faker.FakerItemReader;
import org.springframework.batch.item.redisearch.support.RediSearchConnectionBuilder;

import com.redislabs.lettusearch.RediSearchCommands;
import com.redislabs.lettusearch.RediSearchUtils;
import com.redislabs.lettusearch.StatefulRediSearchConnection;
import com.redislabs.lettusearch.index.IndexInfo;
import com.redislabs.lettusearch.index.field.Field;
import com.redislabs.lettusearch.index.field.GeoField;
import com.redislabs.lettusearch.index.field.TagField;
import com.redislabs.lettusearch.index.field.TextField;
import com.redislabs.riot.AbstractImportCommand;
import com.redislabs.riot.Transfer;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(name = "import", aliases = { "i" }, description = "Import generated data")
public class GenerateCommand extends AbstractImportCommand<Map<String, Object>> {

	@CommandLine.Parameters(description = "SpEL expressions", paramLabel = "SPEL")
	private Map<String, String> fakerFields = new LinkedHashMap<>();
	@CommandLine.Option(names = "--faker-index", description = "Use given search index to introspect Faker fields", paramLabel = "<index>")
	private String fakerIndex;
	@CommandLine.Option(names = "--locale", description = "Faker locale (default: ${DEFAULT-VALUE})", paramLabel = "<tag>")
	private Locale locale = Locale.ENGLISH;
	@CommandLine.Option(names = "--metadata", description = "Include metadata (index, partition)")
	private boolean includeMetadata;

	@Override
	@SuppressWarnings("unchecked")
	protected List<Transfer<Map<String, Object>, Object>> transfers() {
		FakerItemReader reader = FakerItemReader.builder().locale(locale).includeMetadata(includeMetadata)
				.fields(fakerFields()).build();
		return transfers("Importing Faker data", reader, objectMapProcessor(), writer());
	}

	private String expression(Field<String> field) {
		if (field instanceof TextField) {
			return "lorem.paragraph";
		}
		if (field instanceof TagField) {
			return "number.digits(10)";
		}
		if (field instanceof GeoField) {
			return "address.longitude.concat(',').concat(address.latitude)";
		}
		return "number.randomDouble(3,-1000,1000)";
	}

	private String quotes(String field, String expression) {
		return "\"" + field + "=" + expression + "\"";
	}

	private List<String> fakerArgs(Map<String, String> fakerFields) {
		List<String> args = new ArrayList<>();
		fakerFields.forEach((k, v) -> args.add(quotes(k, v)));
		return args;
	}

	private Map<String, String> fakerFields() {
		Map<String, String> fields = new LinkedHashMap<>(fakerFields);
		if (fakerIndex == null) {
			return fields;
		}
		RediSearchConnectionBuilder<?> connectionBuilder = new RediSearchConnectionBuilder<>();
		configure(connectionBuilder);
		StatefulRediSearchConnection<String, String> connection = connectionBuilder.connection();
		RediSearchCommands<String, String> commands = connection.sync();
		IndexInfo<String> info = RediSearchUtils.getInfo(commands.ftInfo(fakerIndex));
		for (Field<String> field : info.getFields()) {
			fields.put(field.getName(), expression(field));
		}
		log.info("Introspected fields: {}", String.join(" ", fakerArgs(fields)));
		return fields;
	}

}
