package hr.from.josipantolis.seriouscallersonly.app

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = RANDOM_PORT)
class MainTest(@Autowired val rest: TestRestTemplate) {

    @Test
    fun `Should report as UP on actuator health endpoint`() {
        // given, when
        val actualRes = rest.getForEntity("/actuator/health", String::class.java)

        // then
        then(actualRes.statusCode).isEqualTo(HttpStatus.OK)
        then(actualRes.body).isEqualTo("""{"status":"UP"}""")
    }

}