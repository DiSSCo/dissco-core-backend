package eu.dissco.backend.configuration;

import static eu.dissco.backend.controller.BaseController.DATE_STRING;

import com.fasterxml.jackson.annotation.JsonSetter.Value;
import com.fasterxml.jackson.annotation.Nulls;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class ApplicationConfiguration {

	@Bean
	public JsonMapper jsonMapper() {
		return JsonMapper.builder()
			.findAndAddModules()
			.defaultDateFormat(new SimpleDateFormat(DATE_STRING))
			.defaultTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC))
			.withConfigOverride(List.class, cfg -> cfg.setNullHandling(Value.forValueNulls(Nulls.AS_EMPTY)))
			.withConfigOverride(Map.class, cfg -> cfg.setNullHandling(Value.forValueNulls(Nulls.AS_EMPTY)))
			.withConfigOverride(Set.class, cfg -> cfg.setNullHandling(Value.forValueNulls(Nulls.AS_EMPTY)))
			.build();
	}

	@Bean
	public DateTimeFormatter formatter() {
		return DateTimeFormatter.ofPattern(DATE_STRING).withZone(ZoneOffset.UTC);
	}

}
