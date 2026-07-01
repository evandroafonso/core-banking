package com.tuum.corebanking.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UUIDTypeHandlerTest {

    private final UUIDTypeHandler handler = new UUIDTypeHandler();

    @Mock
    private PreparedStatement ps;

    @Mock
    private ResultSet rs;

    @Mock
    private CallableStatement cs;

    @Test
    void setNonNullParameterShouldSetObjectOnPreparedStatement() throws Exception {
        UUID uuid = UUID.randomUUID();
        handler.setNonNullParameter(ps, 1, uuid, null);
        verify(ps).setObject(1, uuid);
    }

    @Test
    void getNullableResultByColumnNameShouldReturnUuid() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(rs.getObject("test_col")).thenReturn(uuid);
        assertThat(handler.getNullableResult(rs, "test_col")).isEqualTo(uuid);
    }

    @Test
    void getNullableResultByColumnNameShouldReturnNull() throws Exception {
        when(rs.getObject("test_col")).thenReturn(null);
        assertThat(handler.getNullableResult(rs, "test_col")).isNull();
    }

    @Test
    void getNullableResultByColumnIndexShouldReturnUuid() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(rs.getObject(1)).thenReturn(uuid);
        assertThat(handler.getNullableResult(rs, 1)).isEqualTo(uuid);
    }

    @Test
    void getNullableResultByColumnIndexShouldReturnNull() throws Exception {
        when(rs.getObject(1)).thenReturn(null);
        assertThat(handler.getNullableResult(rs, 1)).isNull();
    }

    @Test
    void getNullableResultFromCallableStatementShouldReturnUuid() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(cs.getObject(1)).thenReturn(uuid);
        assertThat(handler.getNullableResult(cs, 1)).isEqualTo(uuid);
    }

    @Test
    void getNullableResultFromCallableStatementShouldReturnNull() throws Exception {
        when(cs.getObject(1)).thenReturn(null);
        assertThat(handler.getNullableResult(cs, 1)).isNull();
    }
}