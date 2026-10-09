package com.nexus.rinde.shared.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

  private final CorrelationIdFilter filter = new CorrelationIdFilter();

  @Test
  void propagatesValidIncomingHeaderAndPutsItInMdc() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(CorrelationIdFilter.HEADER, "req-001");
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicReference<String> seenInChain = new AtomicReference<>();

    filter.doFilter(
        request,
        response,
        new MockFilterChain() {
          @Override
          public void doFilter(
              jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
            seenInChain.set(MDC.get(CorrelationIdFilter.MDC_KEY));
          }
        });

    assertThat(seenInChain.get()).isEqualTo("req-001");
    assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo("req-001");
    assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
  }

  @Test
  void generatesIdWhenHeaderIsMissing() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(new MockHttpServletRequest(), response, new MockFilterChain());

    assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isNotBlank();
  }

  @Test
  void replacesHeaderWithUnsafeCharacters() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(CorrelationIdFilter.HEADER, "bad value\r\nX-Injected: 1");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, new MockFilterChain());

    assertThat(response.getHeader(CorrelationIdFilter.HEADER)).doesNotContain(" ", "\n");
  }
}
