package com.redislabs.riot.cli;

import java.util.Map;

import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "db", description = "SQL database")
public class DatabaseWriterCommand extends AbstractWriterCommand {

	@ArgGroup(exclusive = false, heading = "Database connection%n")
	private DatabaseConnectionOptions connection = new DatabaseConnectionOptions();

	@Option(names = "--sql", required = true, description = "Insert query, e.g. \"INSERT INTO people (id, name) VALUES (:id, :name)\"", paramLabel = "<query>")
	private String sql;

	@Override
	protected JdbcBatchItemWriter<Map<String, Object>> writer() {
		JdbcBatchItemWriterBuilder<Map<String, Object>> builder = new JdbcBatchItemWriterBuilder<Map<String, Object>>();
		builder.itemSqlParameterSourceProvider(MapSqlParameterSource::new);
		builder.dataSource(connection.dataSource());
		builder.sql(sql);
		JdbcBatchItemWriter<Map<String, Object>> writer = builder.build();
		writer.afterPropertiesSet();
		return writer;
	}

	@Override
	protected String getTargetDescription() {
		return "database";
	}

}