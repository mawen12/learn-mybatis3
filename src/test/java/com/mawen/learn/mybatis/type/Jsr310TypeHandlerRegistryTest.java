package com.mawen.learn.mybatis.type;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.chrono.JapaneseDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/9/25
 */
public class Jsr310TypeHandlerRegistryTest {

	private TypeHandlerRegistry registry;

	@BeforeEach
	void setUp() {
		registry = new TypeHandlerRegistry();
	}

	@Test
	void shouldRegisterJsr310TypeHandlers() {
		assertThat(registry.getTypeHandler(Instant.class)).isInstanceOf(InstantTypeHandler.class);
		assertThat(registry.getTypeHandler(LocalDateTime.class)).isInstanceOf(LocalDateTimeTypeHandler.class);
		assertThat(registry.getTypeHandler(LocalDate.class)).isInstanceOf(LocalDateTypeHandler.class);
		assertThat(registry.getTypeHandler(LocalTime.class)).isInstanceOf(LocalTimeTypeHandler.class);
		assertThat(registry.getTypeHandler(OffsetDateTime.class)).isInstanceOf(OffsetDateTimeTypeHandler.class);
		assertThat(registry.getTypeHandler(OffsetTime.class)).isInstanceOf(OffsetTimeTypeHandler.class);
		assertThat(registry.getTypeHandler(ZonedDateTime.class)).isInstanceOf(ZonedDateTimeTypeHandler.class);
		assertThat(registry.getTypeHandler(Month.class)).isInstanceOf(MonthTypeHandler.class);
		assertThat(registry.getTypeHandler(Year.class)).isInstanceOf(YearTypeHandler.class);
		assertThat(registry.getTypeHandler(YearMonth.class)).isInstanceOf(YearMonthTypeHandler.class);
		assertThat(registry.getTypeHandler(JapaneseDate.class)).isInstanceOf(JapaneseDateTypeHandler.class);
	}
}
